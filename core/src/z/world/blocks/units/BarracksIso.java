package z.world.blocks.units;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import arc.Core;
import arc.Events;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Rect;
import arc.struct.EnumSet;
import arc.struct.ObjectMap;
import mindustry.Vars;
import mindustry.annotations.Annotations;
import mindustry.content.Fx;
import mindustry.content.Items;
import mindustry.entities.Effects;
import mindustry.entities.type.BaseUnit;
import mindustry.entities.type.Unit;
import mindustry.game.EventType;
import mindustry.gen.Call;
import mindustry.graphics.Pal;
import mindustry.graphics.Shaders;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.type.UnitType;
import mindustry.ui.Bar;
import mindustry.ui.Cicon;
import mindustry.world.Tile;
import mindustry.world.consumers.ConsumeItems;
import mindustry.world.consumers.ConsumeType;
import mindustry.world.meta.BlockFlag;
import mindustry.world.meta.BlockStat;
import mindustry.world.meta.StatUnit;
import z.ai.components.Squad;
import z.entities.type.WorkerTileEntity;
import z.entities.type.base.BaseGroundUnit;
import z.tools.serialize.XmlSerialize;
import z.utils.ShapeRenderer;
import z.world.blocks.defense.WorkBlock;

import static mindustry.Vars.net;
import static mindustry.Vars.systemStrategy;
import static mindustry.Vars.tilesize;
import static z.debug.ZDebug.disable_packLoad;

/**
 *  兵营
 */
public class BarracksIso extends WorkBlock {
    /** 生产单位类型*/
    public UnitType unitType;
    /** 生产单位时间*/
    public float produceTime = 1000f;
    /** 单位出生速度*/
    public float launchVelocity = 0f;
    /***/
    public TextureRegion topRegion;
    /** 生产单位最大数量*/
    public int maxSpawn = 4;
    /** 生产物品容器*/
    public int[] capacities;

    // zones add begon
    private Rect rect;
    // zones add end

    public BarracksIso(String name){
        super(name);
        update = true;
        hasPower = true;
        hasItems = true;
        solid = false;
        flags = EnumSet.of(BlockFlag.producer);
        entityType = BarracksIsoEntity::new;
    }

    private String configfile;
    public BarracksIso(String name, String configFile){
        this(name);
        this.configfile = configFile;
        Vars.xmlSerialize.loadBlockConfig(this.configfile, this);
//        update = false;         // zones debug add

        testNeedItems();
    }

    @Deprecated
    private void testNeedItems() {
        consumes[0].items(new ItemStack(Items.timber, 2));
    }

    /** 单位创建回调*/
    @Annotations.Remote(called = Annotations.Loc.server)
    public static void onUnitFactorySpawnCaesar(Tile tile, int spawns){
        if(!(tile.entity instanceof BarracksIsoEntity) || !(tile.block() instanceof BarracksIso)) return;

        BarracksIsoEntity entity = tile.ent();
        BarracksIso factory = (BarracksIso) tile.block();

        entity.buildTime = 0f;
        entity.spawned = spawns;

        Effects.shake(2f, 3f, entity);
        Effects.effect(Fx.producesmoke, tile.drawx(), tile.drawy());

        if(!net.client()){
            BaseUnit unit = factory.unitType.create(tile.getTeam());
            ((BaseGroundUnit) unit).startState = ((BaseGroundUnit) unit).squadState;
            unit.setSpawner(tile);      //
//            unit.set(tile.drawx() + Mathf.range(4), tile.drawy() + Mathf.range(4));     // default
            unit.set(tile.getX() + Mathf.range(4 / (float)tilesize), tile.getY() + Mathf.range(4 / (float)tilesize));
            unit.add();
            unit.velocity().y = factory.launchVelocity;
            // new add begon
            entity.squad.addMember(unit);
            // new add end
            Events.fire(new EventType.UnitCreateEvent(unit));
        }
    }

    @Override
    public void init(){
        super.init();

        // 初始化存储物品容器
        capacities = new int[Vars.content.items().size];
        if(consumes[0].has(ConsumeType.item)){
            ConsumeItems cons = consumes[0].get(ConsumeType.item);
            for(ItemStack stack : cons.items){
                capacities[stack.item.id] = stack.amount * 2;
            }
        }
    }

    @Override
    public void load(){
        super.load();
        topRegion = Core.atlas.find(name + "-top");
        // zones add begon
        if (disable_packLoad)   return;
        ObjectMap dataPool = Vars.xmlSerialize.loadBlockAnimation(configfile);
        region = ((TextureRegion[][][]) dataPool.get(XmlSerialize.qqtxRegions))[0][0][0];
        rect = ((Rect[][][]) dataPool.get(XmlSerialize.qqtxRects))[0][0][0];
        dataPool.clear();
        // zones add end
    }

    @Override
    public void removed(Tile tile) {
        super.removed(tile);

//        tile.<BarracksIsoEntity>ent().squad.clearMember();
//        tile.<BarracksIsoEntity>ent().squad.setValid(false);
//        Vars.systemStrategy.squadIDPro.add(tile.<BarracksIsoEntity>ent().squadID);    // 归还队伍id
        systemStrategy.free(tile.<BarracksIsoEntity>ent().squad);
    }

    @Override
    public void setBars(){
        super.setBars();
        bars.add("progress", entity -> new Bar("bar.progress", Pal.ammo, () -> ((BarracksIsoEntity)entity).buildTime / produceTime));
        bars.add("spawned", entity -> new Bar(() -> Core.bundle.format("bar.spawned", ((BarracksIsoEntity)entity).spawned, maxSpawn), () -> Pal.command, () -> (float)((BarracksIsoEntity)entity).spawned / maxSpawn));
    }

    @Override
    public boolean outputsItems(){
        return false;
    }

    @Override
    public void setStats(){
        super.setStats();

        stats.remove(BlockStat.itemCapacity);
        stats.add(BlockStat.productionTime, produceTime / 60f, StatUnit.seconds);
        stats.add(BlockStat.maxUnits, maxSpawn, StatUnit.none);
    }

    @Override
    public void unitRemoved(Tile tile, Unit unit){
        BarracksIsoEntity entity = tile.ent();
        entity.spawned--;
        entity.spawned = Math.max(entity.spawned, 0);
    }

    @Override
    public TextureRegion[] generateIcons(){
        return new TextureRegion[]{Core.atlas.find(name), Core.atlas.find(name + "-top")};
    }

    @Override
    public void draw(Tile tile){
        if (true) {
            ShapeRenderer.drawDiamond(tile.x, tile.y, size);

            Draw.rectGdx(region, tile.drawxIso(), tile.drawyIso(), rect);
            return;
        }
        BarracksIsoEntity entity = tile.ent();
        TextureRegion region = unitType.icon(Cicon.full);

        Draw.rect(name, tile.drawx(), tile.drawy());

        Shaders.build.region = region;
        Shaders.build.progress = entity.buildTime / produceTime;
        Shaders.build.color.set(Pal.accent);
        Shaders.build.color.a = entity.speedScl;
        Shaders.build.time = -entity.time / 20f;

        Draw.shader(Shaders.build);
        Draw.rect(region, tile.drawx(), tile.drawy());
        Draw.shader();

        Draw.color(Pal.accent);
        Draw.alpha(entity.speedScl);

        Lines.lineAngleCenter(
                tile.drawx() + Mathf.sin(entity.time, 20f, Vars.tilesize / 2f * size - 2f),
                tile.drawy(),
                90,
                size * Vars.tilesize - 4f);

        Draw.reset();

        Draw.rect(topRegion, tile.drawx(), tile.drawy());
    }

    @Override
    public void update(Tile tile){
        BarracksIsoEntity entity = tile.ent();

        if(entity.spawned >= maxSpawn){     // 到达最大生产单位
            return;
        }

        if(entity.cons.valid() || tile.isEnemyCheat()){
            entity.time += entity.delta() * entity.speedScl * Vars.state.rules.unitBuildSpeedMultiplier * entity.efficiency();
            entity.buildTime += entity.delta() * entity.efficiency() * Vars.state.rules.unitBuildSpeedMultiplier;
            entity.speedScl = Mathf.lerpDelta(entity.speedScl, 1f, 0.05f);
        }else{
            entity.speedScl = Mathf.lerpDelta(entity.speedScl, 0f, 0.05f);
        }

        if(entity.buildTime >= produceTime){
            entity.buildTime = 0f;

            Call.onUnitFactorySpawnCaesar(tile, entity.spawned + 1);
            useContent(tile, unitType);

            entity.cons.trigger();
        }
    }

//    @Override
//    public void buildConfiguration(Tile tile, Table table) {
//        super.buildConfiguration(tile, table);
//
//        // debug begon
//        tile.<BarracksIsoEntity>ent().squad.selectState(true);
//        // debug end
//    }

    @Override
    public int getMaximumAccepted(Tile tile, Item item){
        return capacities[item.id];
    }

    @Override
    public boolean shouldConsume(Tile tile){
        BarracksIsoEntity entity = tile.ent();
        return entity.spawned < maxSpawn;
    }

    public static class BarracksIsoEntity extends WorkerTileEntity {          // default TileEntity
        /** 建造时间*/
        float buildTime;
        /***/
        float time;
        /** 单位生产速度*/
        float speedScl;
        /** 块拥有单位数量*/
        int spawned;
        // zones add begon
        /** 队伍ID*/
//        public int squadID = -1;
        /** 队伍数据Squad*/
        public Squad squad;
        /** 队伍ai保存数据*/
//        public SquadBlackboard blackboard;
        // zones add end

        private BarracksIsoEntity() {
        }

        @Override
        public void added() {
            super.added();

//            squadID = Vars.systemStrategy.squadIDPro.pop();
//            squad = Vars.systemStrategy.getSquad(getTeam().id, squadID);
//            squad.setValid(true);
            squad = systemStrategy.popSquad();
//            blackboard = new SquadBlackboard();
        }

        @Override
        public void write(DataOutput stream) throws IOException {
            super.write(stream);
            stream.writeFloat(buildTime);
            stream.writeInt(spawned);
        }

        @Override
        public void read(DataInput stream, byte revision) throws IOException{
            super.read(stream, revision);
            buildTime = stream.readFloat();
            spawned = stream.readInt();
        }
    }
}
