package mindustry.world.blocks.storage;

import arc.Core;
import arc.Events;
import arc.func.Cons;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.math.geom.Point2;
import arc.struct.EnumSet;
import arc.struct.Queue;
import mindustry.content.Mechs;
import mindustry.entities.traits.SpawnerTrait;
import mindustry.entities.type.Player;
import mindustry.entities.type.TileEntity;
import mindustry.entities.type.Unit;
import mindustry.game.EventType.CoreItemDeliverEvent;
import mindustry.game.EventType.Trigger;
import mindustry.gen.Call;
import mindustry.gen.Sounds;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.type.Item;
import mindustry.type.ItemType;
import mindustry.type.Mech;
import mindustry.ui.Bar;
import mindustry.world.Tile;
import mindustry.world.blocks.RespawnBlock;
import mindustry.world.meta.BlockFlag;
import mindustry.world.modules.ItemModule;
import z.entities.traits.UpgradeTrait;
import z.utils.ShapeRenderer;

import static mindustry.Vars.content;
import static mindustry.Vars.net;
import static mindustry.Vars.netServer;
import static mindustry.Vars.player;
import static mindustry.Vars.renderer;
import static mindustry.Vars.state;
import static mindustry.Vars.tilesize;
import static mindustry.Vars.ui;
import static mindustry.Vars.world;
import static z.debug.ZDebug.disable_mindustryPlayer;
import static z.debug.ZDebug.enable_isoInput;

/**
 *  核心 块
 * */
public class CoreBlock extends StorageBlock{
    /** 机甲*/
    public Mech mech = disable_mindustryPlayer ? Mechs.glaive : Mechs.alpha;

    /** 核心块 构建*/
    public CoreBlock(String name){
        super(name);

        solid = true;
        update = true;
        hasItems = true;
        flags = EnumSet.of(BlockFlag.core, BlockFlag.producer);
        activeSound = Sounds.respawning;
        activeSoundVolume = 1f;
        layer = Layer.overlay;
        entityType = CoreEntity::new;
    }

//    /** 单位重生回调*/
//    @Remote(called = Loc.server)
//    public static void onUnitRespawn(Tile tile, Player player){
//        if(player == null || tile.entity == null) return;
//
//        CoreEntity entity = tile.ent();
//        Effects.effect(Fx.spawn, entity);
//        entity.progress = 0;
//        entity.spawnPlayer = player;
//        entity.spawnPlayer.onRespawn(tile);
//        entity.spawnPlayer.applyImpulse(0, 8f);
//        entity.spawnPlayer = null;
//    }

    @Override
    public void setStats(){
        super.setStats();

        bars.add("capacity", e ->
            new Bar(
                () -> Core.bundle.format("bar.capacity", ui.formatAmount(((CoreEntity)e).storageCapacity)),
                () -> Pal.items,
                () -> e.items.total() / (float)(((CoreEntity)e).storageCapacity * content.items().count(i -> i.type == ItemType.material))
            ));
    }

    @Override
    public void drawLight(Tile tile){
        renderer.lights.add(tile.drawx(), tile.drawy(), 30f * size, Pal.accent, 0.5f + Mathf.absin(20f, 0.1f));
    }

    @Override
    public boolean acceptItem(Item item, Tile tile, Tile source){
        return tile.entity.items.get(item) < getMaximumAccepted(tile, item);
    }

    @Override
    public int getMaximumAccepted(Tile tile, Item item){
        CoreEntity entity = tile.ent();
        return item.type == ItemType.material ? entity.storageCapacity : 0;
    }

    @Override
    public void onProximityUpdate(Tile tile){
        CoreEntity entity = tile.ent();

        for(TileEntity other : state.teams.cores(tile.getTeam())){
            if(other.tile != tile){
                entity.items = other.items;
            }
        }
        state.teams.registerCore(entity);

        entity.storageCapacity = itemCapacity[entity.level()] + entity.proximity().sum(e -> isContainer(e) ? e.block().itemCapacity[e.entity.level()] : 0);
        entity.proximity().each(this::isContainer, t -> {
            t.entity.items = entity.items;
            t.<StorageBlockEntity>ent().linkedCore = tile;
        });

        for(TileEntity other : state.teams.cores(tile.getTeam())){
            if(other.tile == tile) continue;
            entity.storageCapacity += other.block.itemCapacity[entity.level()] + other.proximity().sum(e -> isContainer(e) ? e.block().itemCapacity[e.entity.level()] : 0);
        }

        if(!world.isGenerating()){
            for(Item item : content.items()){
                entity.items.set(item, Math.min(entity.items.get(item), entity.storageCapacity));
            }
        }

        for(CoreEntity other : state.teams.cores(tile.getTeam())){
            other.storageCapacity = entity.storageCapacity;
        }
    }

    @Override
    public void drawSelect(Tile tile){
        Lines.stroke(1f, Pal.accent);
        Cons<Tile> outline = t -> {
            for(int i = 0; i < 4; i++){
                Point2 p = Geometry.d8edge[i];
                float offset = -Math.max(t.block().size - 1, 0) / 2f * tilesize;
                Draw.rect("block-select", t.drawx() + offset * p.x, t.drawy() + offset * p.y, i * 90);
            }
        };
        if(tile.entity.proximity().contains(e -> isContainer(e) && e.entity.items == tile.entity.items)){
            outline.get(tile);
        }
        tile.entity.proximity().each(e -> isContainer(e) && e.entity.items == tile.entity.items, outline);
        Draw.reset();
    }


    public boolean isContainer(Tile tile){
        return tile.entity instanceof StorageBlockEntity;
    }

    @Override
    public float handleDamage(Tile tile, float amount){
        if(player != null && tile.getTeam() == player.getTeam()){
            Events.fire(Trigger.teamCoreDamage);
        }
        return amount;
    }

    @Override
    public boolean canBreak(Tile tile){
        return false;
    }

    @Override
    public void removed(Tile tile){
        CoreEntity entity = tile.ent();
        int total = tile.entity.proximity().count(e -> e.entity != null && e.entity.items != null && e.entity.items == tile.entity.items);
        float fract = 1f / total / state.teams.cores(tile.getTeam()).size;

        tile.entity.proximity().each(e -> isContainer(e) && e.entity.items == tile.entity.items, t -> {
            StorageBlockEntity ent = (StorageBlockEntity)t.entity;
            ent.linkedCore = null;
            ent.items = new ItemModule();
            for(Item item : content.items()){
                ent.items.set(item, (int)(fract * tile.entity.items.get(item)));
            }
        });

        state.teams.unregisterCore(entity);

        int max = itemCapacity[tile.entity.level()] * state.teams.cores(tile.getTeam()).size;
        for(Item item : content.items()){
            tile.entity.items.set(item, Math.min(tile.entity.items.get(item), max));
        }

        for(CoreEntity other : state.teams.cores(tile.getTeam())){
            other.block.onProximityUpdate(other.tile);
        }
    }

    @Override
    public void placed(Tile tile){
        super.placed(tile);
        CoreEntity entity = tile.ent();
        state.teams.registerCore(entity);
    }

    @Override
    public void draw(Tile tile) {
        ShapeRenderer.drawDiamond(tile.x, tile.y, tile.block().size);
        super.draw(tile);
    }

    @Override
    public void drawLayer(Tile tile){
        CoreEntity entity = tile.ent();

        if(entity.heat > 0.001f){
            RespawnBlock.drawRespawn(tile, entity.heat, entity.progress, entity.time, entity.spawnPlayer, mech);
        }
    }

    @Override
    public void handleItem(Item item, Tile tile, Tile source){
        if(net.server() || !net.active()){
            super.handleItem(item, tile, source);
            if(state.rules.tutorial){
                Events.fire(new CoreItemDeliverEvent());
            }
        }
    }

    @Override
    public void update(Tile tile){
        CoreEntity entity = tile.ent();

        if(entity.spawnPlayer != null){
            if(!entity.spawnPlayer.isDead() || !entity.spawnPlayer.isAdded()){
                entity.spawnPlayer = null;
                return;
            }

            if (enable_isoInput) {
                entity.spawnPlayer.set(tile.getX(), tile.getY());
            } else {
                entity.spawnPlayer.set(tile.drawx(), tile.drawy());
            }
            entity.heat = Mathf.lerpDelta(entity.heat, 1f, 0.1f);
            entity.time += entity.delta();
            entity.progress += 1f / state.rules.respawnTime * entity.delta();

            if(entity.progress >= 1f){
                Call.onUnitRespawn(tile, entity.spawnPlayer);
            }
        }else{
            entity.heat = Mathf.lerpDelta(entity.heat, 0f, 0.1f);
        }
    }

    @Override
    public boolean shouldActiveSound(Tile tile){
        CoreEntity entity = tile.ent();

        return entity.spawnPlayer != null;
    }

    public class CoreEntity extends TileEntity implements SpawnerTrait, UpgradeTrait {      // zones editor
        /*protected*/ public Player spawnPlayer;
        /*protected*/ public float progress;
        /*protected*/ public float time;
        /*protected*/ public float heat;
        /*protected*/ public int storageCapacity;
        // zones add begon
        /** 执行更新队列*/
        private Queue<UpgradeRequest> upgradeQueue = new Queue<>();
        // zones add end

        @Override
        public boolean hasUnit(Unit unit){
            return unit == spawnPlayer;
        }

        @Override
        public void updateSpawning(Player player){
            if(!netServer.isWaitingForPlayers() && spawnPlayer == null){
                spawnPlayer = player;
                progress = 0f;
                player.mech = mech;
                player.beginRespawning(this);
            }
        }

        // zones add begon

        @Override
        public Queue<UpgradeRequest> buildQueue(){
            return upgradeQueue;
        }

        @Override
        public void update() {
            super.update();
            updateUpgrade();
        }

        @Override
        public float getUpgradePower() {
            return upgradePower;
        }

        public float upgradePower = 1;

        // zones add end
    }
}
