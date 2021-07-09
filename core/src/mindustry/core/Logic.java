package mindustry.core;

import java.util.Iterator;

import arc.ApplicationListener;
import arc.Events;
import arc.ai.GdxAI;
import arc.ai.msg.MessageManager;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.annotations.Annotations.Loc;
import mindustry.annotations.Annotations.Remote;
import mindustry.content.Fx;
import mindustry.core.GameState.State;
import mindustry.ctype.UnlockableContent;
import mindustry.entities.Effects;
import mindustry.entities.type.Player;
import mindustry.entities.type.TileEntity;
import mindustry.game.EventType.BlockBuildEndEvent;
import mindustry.game.EventType.BlockDestroyEvent;
import mindustry.game.EventType.GameOverEvent;
import mindustry.game.EventType.LaunchEvent;
import mindustry.game.EventType.LaunchItemEvent;
import mindustry.game.EventType.PlayEvent;
import mindustry.game.EventType.ResetEvent;
import mindustry.game.EventType.Trigger;
import mindustry.game.EventType.WaveEvent;
import mindustry.game.Rules;
import mindustry.game.Stats;
import mindustry.game.Team;
import mindustry.game.Teams;
import mindustry.game.Teams.BrokenBlock;
import mindustry.game.Teams.TeamData;
import mindustry.gen.Call;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.BuildBlock;
import mindustry.world.blocks.BuildBlock.BuildEntity;

import static mindustry.Vars.blockunitGroup;
import static mindustry.Vars.bulletGroup;
import static mindustry.Vars.collisions;
import static mindustry.Vars.content;
import static mindustry.Vars.data;
import static mindustry.Vars.effectGroup;
import static mindustry.Vars.entities;
import static mindustry.Vars.fireGroup;
import static mindustry.Vars.groundEffectGroup;
import static mindustry.Vars.headless;
import static mindustry.Vars.net;
import static mindustry.Vars.netClient;
import static mindustry.Vars.playerGroup;
import static mindustry.Vars.puddleGroup;
import static mindustry.Vars.shieldGroup;
import static mindustry.Vars.spawner;
import static mindustry.Vars.state;
import static mindustry.Vars.systemStrategy;
import static mindustry.Vars.systemTroops;
import static mindustry.Vars.systemWorker;
import static mindustry.Vars.tileGroup;
import static mindustry.Vars.ui;
import static mindustry.Vars.unitGroup;
import static mindustry.Vars.world;
import static z.debug.ZDebug.disable_mindustryEndCheck;
import static z.debug.ZDebug.disable_mindustryPlayer;
import static z.debug.ZDebug.enable_isoInput;

/**
 * Logic module.
 * Handles all logic for entities and waves.
 * Handles game state events.
 * Does not store any game state itself.
 * <p>
 * This class should <i>not</i> call any outside methods to change state of modules, but instead fire events.
 */
public class Logic implements ApplicationListener{

    public Logic(){
        Events.on(WaveEvent.class, event -> {
            for(Player p : playerGroup.all()){
                p.respawns = state.rules.respawns;
            }

            if(world.isZone()){
                world.getZone().updateWave(state.wave);
            }
        });

        Events.on(BlockDestroyEvent.class, event -> {
            //blocks that get broken are appended to the team's broken block queue
            Tile tile = event.tile;
            Block block = tile.block();
            //skip null entities or un-rebuildables, for obvious reasons; also skip client since they can't modify these requests
            if(tile.entity == null || !tile.block().rebuildable || net.client()) return;

            if(block instanceof BuildBlock){

                BuildEntity entity = tile.ent();

                //update block to reflect the fact that something was being constructed
                if(entity.cblock != null && entity.cblock.synthetic()){
                    block = entity.cblock;
                }else{
                    //otherwise this was a deconstruction that was interrupted, don't want to rebuild that
                    return;
                }
            }

            TeamData data = state.teams.get(tile.getTeam());

            //remove existing blocks that have been placed here.
            //painful O(n) iteration + copy
            for(int i = 0; i < data.brokenBlocks.size; i++){
                BrokenBlock b = data.brokenBlocks.get(i);
                if(b.x == tile.x && b.y == tile.y){
                    data.brokenBlocks.removeIndex(i);
                    break;
                }
            }

            data.brokenBlocks.addFirst(new BrokenBlock(tile.x, tile.y, tile.rotation(), block.id, tile.entity.config()));
        });

        Events.on(BlockBuildEndEvent.class, event -> {
            if(!event.breaking){
                TeamData data = state.teams.get(event.team);
                Iterator<BrokenBlock> it = data.brokenBlocks.iterator();
                while(it.hasNext()){
                    BrokenBlock b = it.next();
                    Block block = content.block(b.block);
                    if(event.tile.block().bounds(event.tile.x, event.tile.y, Tmp.r1).overlaps(block.bounds(b.x, b.y, Tmp.r2))){
                        it.remove();
                    }
                }
            }
        });
    }

    /** Handles the event of content being used by either the player or some block. */
    public void handleContent(UnlockableContent content){
        if(!headless){
            data.unlockContent(content);
        }
    }

    public void play(){
        state.set(State.playing);
        state.wavetime = state.rules.waveSpacing * 2; //grace period of 2x wave time before game starts
        Events.fire(new PlayEvent());

        //add starting items
        if(!world.isZone()){
            for(TeamData team : state.teams.getActive()){
                if(team.hasCore()){
                    TileEntity entity = team.core();
                    entity.items.clear();
                    for(ItemStack stack : state.rules.loadout){
                        entity.items.add(stack.item, stack.amount);
                    }
                }
            }
        }
    }

    public void reset(){
        state.wave = 1;
        state.wavetime = state.rules.waveSpacing;
        state.gameOver = state.launched = false;
        state.teams = new Teams();
        state.rules = new Rules();
        state.stats = new Stats();

        entities.clear();
        Time.clear();
        TileEntity.sleepingEntities = 0;

        Events.fire(new ResetEvent());
    }

    /** 执行回合刷兵*/
    public void runWave(){
        spawner.spawnEnemies();
        state.wave++;
        state.wavetime = world.isZone() && world.getZone().isLaunchWave(state.wave) ? state.rules.waveSpacing * state.rules.launchWaveMultiplier : state.rules.waveSpacing;

        Events.fire(new WaveEvent());
    }

    private void checkGameOver(){
        if (disable_mindustryEndCheck) {
            return;
        }
        if(!state.rules.attackMode && state.teams.playerCores().size == 0 && !state.gameOver){
            state.gameOver = true;
            Events.fire(new GameOverEvent(state.rules.waveTeam));
        }else if(state.rules.attackMode){
            Team alive = null;

            for(TeamData team : state.teams.getActive()){
                if(team.hasCore()){
                    if(alive != null){
                        return;
                    }
                    alive = team.team;
                }
            }

            if(alive != null && !state.gameOver){
                if(world.isZone() && alive == state.rules.defaultTeam){
                    //in attack maps, a victorious game over is equivalent to a launch
                    Call.launchZone();
                }else{
                    Events.fire(new GameOverEvent(alive));
                }
                state.gameOver = true;
            }
        }
    }

    @Remote(called = Loc.both)
    public static void launchZone(){
        if(!headless){
            ui.hudfrag.showLaunch();
        }

        for(TileEntity tile : state.teams.playerCores()){
            Effects.effect(Fx.launch, tile);
        }

        if(world.getZone() != null){
            world.getZone().setLaunched();
        }

        Time.runTask(30f, () -> {
            for(TileEntity entity : state.teams.playerCores()){
                for(Item item : content.items()){
                    data.addItem(item, entity.items.get(item));
                    Events.fire(new LaunchItemEvent(item, entity.items.get(item)));
                }
                entity.tile.remove();
            }
            state.launched = true;
            state.gameOver = true;
            Events.fire(new LaunchEvent());
            //manually fire game over event now
            Events.fire(new GameOverEvent(state.rules.defaultTeam));
        });
    }

    @Remote(called = Loc.both)
    public static void onGameOver(Team winner){
        state.stats.wavesLasted = state.wave;
        ui.restart.show(winner);
        netClient.setQuiet();
    }

    @Override
    public void update(){
        Events.fire(Trigger.update);

        if(!state.is(State.menu)){
            if(!net.client()){
                state.enemies = unitGroup.count(b -> b.getTeam() == state.rules.waveTeam && b.countsAsEnemy());
            }

            if(!state.isPaused()){
                Time.update();

                if(state.rules.waves && state.rules.waveTimer && !state.gameOver){
                    if(!state.rules.waitForWaveToEnd || state.enemies == 0){
                        state.wavetime = Math.max(state.wavetime - Time.delta(), 0);
                    }
                }

                if(!net.client() && state.wavetime <= 0 && state.rules.waves){
                    runWave();
                }

                if(!headless){
                    effectGroup.update();
                    groundEffectGroup.update();
                }

                if(!state.isEditor()){
                    unitGroup.update();
                    if (enable_isoInput) {
                            blockunitGroup.update();
                    }   // zones add code
                    puddleGroup.update();
                    shieldGroup.update();
                    bulletGroup.update();
                    tileGroup.update();
                    fireGroup.update();
                }else{
                    unitGroup.updateEvents();
                    collisions.updatePhysics(unitGroup);
                    if (enable_isoInput) {
                        blockunitGroup.updateEvents();
                        collisions.updatePhysics(blockunitGroup);
                    }
                }

                playerGroup.update();

                //effect group only contains item transfers in the headless version, update it!
                if(headless){
                    effectGroup.update();
                }

                if(!state.isEditor()){
                    //bulletGroup
                    collisions.collideGroups(bulletGroup, unitGroup);
                    if ( !disable_mindustryPlayer) {
                        collisions.collideGroups(bulletGroup, playerGroup);
                    }
                    if (enable_isoInput) {
                        collisions.collideGroups(bulletGroup, blockunitGroup);
                    }
                }

                {   // 更新zones扩展系统
                    //  扩展AI模块更新
                    GdxAI.getTimepiece().update(Time.delta());      // 使用
                    MessageManager.getInstance().update();

                    systemWorker.updateSystem();
                    systemTroops.updateSystem();
                    systemStrategy.updateSystem();
                }
            }

            if(!net.client() && !world.isInvalidMap() && !state.isEditor() && state.rules.canGameOver){
                checkGameOver();
            }
        }
    }
}
