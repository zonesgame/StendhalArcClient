package z.world.blocks.distribution;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import arc.Core;
import arc.func.Boolf;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.math.geom.Point2;
import arc.math.geom.Vec2;
import arc.struct.Array;
import arc.struct.ObjectMap;
import arc.util.ArcAnnotate.Nullable;
import arc.util.Eachable;
import arc.util.Pack;
import arc.z.util.ISOUtils;
import arc.z.util.ZonesAnnotate.ZField;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.entities.traits.BuilderTrait.BuildRequest;
import mindustry.entities.type.TileEntity;
import mindustry.entities.type.Unit;
import mindustry.type.Item;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.distribution.Junction;
import mindustry.world.meta.BlockGroup;
import mindustry.world.meta.BlockStat;
import mindustry.world.meta.StatUnit;
import z.tools.serialize.XmlSerialize;
import z.world.blocks.defense.EditorBlock;

import static arc.z.util.ISOUtils.TILE_HEIGHT;
import static arc.z.util.ISOUtils.TILE_WIDTH;
import static mindustry.Vars.content;
import static mindustry.Vars.tilesize;

/**
 *  传送带 块
 * */
public class RoadBlockIso extends EditorBlock {
    /** 物品空间*/
    private static final float itemSpace = 0.4f;
    /** 物品容量*/
    private static final int capacity = 4;

    private final Vec2 tr1 = new Vec2();
    private final Vec2 tr2 = new Vec2();
    private TextureRegion[][] regions = new TextureRegion[7][4];

    /** 速度*/
    public float speed = 0f;
    /** 显示速度*/
    public float displayedSpeed = 0f;

    // zones add begon
    /** 是否自动调整纹理*/
    @ZField
    public boolean isAutotiler = true;
    private TextureRegion[] regions_ = new TextureRegion[16];
    // zones add end

    protected RoadBlockIso(String name){
        super(name);
        rotate = true;
        update = true;      // default true
//        layer = Layer.overlay;      // default   Layer.overlay
        group = BlockGroup.transportation;
        hasItems = false;        // default true
        itemCapacity[0] = 4;
        conveyorPlacement = true;
        entityType = RoadBlockIsoEntity::new;

//        idleSound = Sounds.conveyor;
//        idleSoundVolume = 0.004f;
        unloadable = false;
    }

    private String configFile;
    protected RoadBlockIso(String name, String configfile){
        this(name);
        this.configFile = configfile;

        Vars.xmlSerialize.loadBlockConfig(configFile, this);
    }

    @Override
    public void setStats(){
        super.setStats();
        //have to add a custom calculated speed, since the actual movement speed is apparently not linear
        stats.add(BlockStat.itemsMoved, displayedSpeed, StatUnit.itemsSecond);
    }

    @Override
    public void load(){
        super.load();

        for(int i = 0; i < regions.length; i++){
            for(int j = 0; j < 4; j++){
                regions[i][j] = Core.atlas.find(name + "-" + i + "-" + j);
            }
        }
//        for(int i = 0; i < regions_.length; i++){
//            regions_[i] = Core.atlas.find(name + (i+1));
//        }
        // zones add begon
        ObjectMap<String, Object> tempPool = Vars.xmlSerialize.loadBlockAnimation(this.configFile);
        if (tempPool == null)   return;
        regions_ = ((TextureRegion[][]) tempPool.get(XmlSerialize.aniRegions))[0];
        tempPool.clear();   // no must
        // zones add end
    }

    /** 是否可通行*/
    public boolean isBlock(Tile tile, TileEntity targetEntity) {
        return false;
    }

    @Override
    public void draw(Tile tile){
        RoadBlockIsoEntity entity = tile.ent();
        int curVariant = entity.curVariant;
        TextureRegion region = regions_[curVariant];
//        Vec2 pos = ISOUtils.tileToWorldCoords(tile.x, tile.y);
//        float dx = pos.x + (region.getWidth() * Draw.getScl() - (size) * TILE_WIDTH) / 2f;
//        float dy = pos.y + (region.getHeight() * Draw.getScl() - (size) * TILE_HEIGHT) / 2f;
//        Draw.rectScale(region, dx, dy);
        Vec2 pos = ISOUtils.tileToWorldCoords(offsetTile() + tile.x , offsetTile() + tile.y );
        float dx = pos.x + (region.getWidth() * Draw.getScl() - (size) * TILE_WIDTH) / 2f;
        float dy = pos.y + (region.getHeight() * Draw.getSclH() - (size) * TILE_HEIGHT) / 2f;
        Draw.rectScaleH(region, dx, dy);
//        Draw.rect(regions_[curVariant], tile.drawx(), tile.drawy());
    }

    @Override
    public boolean shouldIdleSound(Tile tile){
//        ConveyorIsoEntity entity = tile.ent();
//        return entity.clogHeat <= 0.5f ;
        return false;
    }

    @Override
    public void onProximityUpdate(Tile tile){
        if ( !isAutotiler)  return;

        super.onProximityUpdate(tile);
    }

    @Override
    public void drawRequestRegion(BuildRequest req, Eachable<BuildRequest> list){
//        int[] bits = getTiling(req, list);
        int[] bits = null;

        if(bits == null) return;

        TextureRegion region = regions[bits[0]][0];
        Draw.rect(region, req.drawx(), req.drawy(), region.getWidth() * bits[1] * Draw.scl * req.animScale, region.getHeight() * bits[2] * Draw.scl * req.animScale, req.rotation * 90);
    }


    @Override
    public TextureRegion[] generateIcons(){
        return new TextureRegion[]{Core.atlas.find(name + "-0-0")};
    }

//    @Override
//    public void unitOn(Tile tile, Unit unit){
//        ConveyorIsoEntity entity = tile.ent();
//
//        if(entity.clogHeat > 0.5f){
//            return;
//        }
//
//        entity.noSleep();
//
//        float speed = this.speed * tilesize / 2.4f;
//        float centerSpeed = 0.1f;
//        float centerDstScl = 3f;
//        float tx = Geometry.d4[tile.rotation()].x, ty = Geometry.d4[tile.rotation()].y;
//
//        float centerx = 0f, centery = 0f;
//
//        if(Math.abs(tx) > Math.abs(ty)){
//            centery = Mathf.clamp((tile.worldy() - unit.y) / centerDstScl, -centerSpeed, centerSpeed);
//            if(Math.abs(tile.worldy() - unit.y) < 1f) centery = 0f;
//        }else{
//            centerx = Mathf.clamp((tile.worldx() - unit.x) / centerDstScl, -centerSpeed, centerSpeed);
//            if(Math.abs(tile.worldx() - unit.x) < 1f) centerx = 0f;
//        }
//
//        if(entity.len * itemSpace < 0.9f){
//            unit.applyImpulse((tx * speed + centerx) * entity.delta(), (ty * speed + centery) * entity.delta());
//        }
//    }

    @Override
    public void update(Tile tile){
        if (true) return;

        RoadBlockIsoEntity e = tile.ent();
        e.minitem = 1f;
        e.mid = 0;

        //skip updates if possible
        if(e.len == 0){
            e.clogHeat = 0f;
            e.sleep();
            return;
        }

        float nextMax = e.aligned ? 1f - Math.max(itemSpace - e.nextc.minitem, 0) : 1f;

        for(int i = e.len - 1; i >= 0; i--){
            float nextpos = (i == e.len - 1 ? 100f : e.ys[i + 1]) - itemSpace;
            float maxmove = Mathf.clamp(nextpos - e.ys[i], 0, speed * e.delta());

            e.ys[i] += maxmove;

            if(e.ys[i] > nextMax) e.ys[i] = nextMax;
            if(e.ys[i] > 0.5 && i > 0) e.mid = i - 1;
            e.xs[i] = Mathf.approachDelta(e.xs[i], 0, speed*2);

            if(e.ys[i] >= 1f && offloadDir(tile, e.ids[i])){
                //align X position if passing forwards
                if(e.aligned){
                    e.nextc.xs[e.nextc.lastInserted] = e.xs[i];
                }
                //remove last item
                e.items.remove(e.ids[i], e.len - i);
                e.len = Math.min(i, e.len);
            }else if(e.ys[i] < e.minitem){
                e.minitem = e.ys[i];
            }
        }

        if(e.minitem < itemSpace + (e.blendbits == 1 ? 0.3f : 0f)){
            e.clogHeat = Mathf.lerpDelta(e.clogHeat, 1f, 0.02f);
        }else{
            e.clogHeat = 0f;
        }

        e.noSleep();
    }

    @Override
    public boolean isAccessible(){
        return true;
    }

    @Override
    public Block getReplacement(BuildRequest req, Array<BuildRequest> requests){
        if (true)
            return this;
        Boolf<Point2> cont = p -> requests.contains(o -> o.x == req.x + p.x && o.y == req.y + p.y && o.rotation == req.rotation && (req.block instanceof ConveyorIso || req.block instanceof Junction));
        return cont.get(Geometry.d4(req.rotation)) &&
                cont.get(Geometry.d4(req.rotation - 2)) &&
                req.tile() != null &&
                req.tile().block() instanceof ConveyorIso &&
                Mathf.mod(req.tile().rotation() - req.rotation, 2) == 1 ? Blocks.junction : this;
    }

    @Override
    public int removeStack(Tile tile, Item item, int amount){
        RoadBlockIsoEntity e = tile.ent();
        e.noSleep();
        int removed = 0;

        for(int j = 0; j < amount; j++){
            for(int i = 0; i < e.len; i++){
                if(e.ids[i] == item){
                    e.remove(i);
                    removed ++;
                    break;
                }
            }
        }

        e.items.remove(item, removed);

        return removed;
    }

    @Override
    public void getStackOffset(Item item, Tile tile, Vec2 trns){
        trns.trns(tile.rotation() * 90 + 180f, tilesize / 2f);
    }

    @Override
    public int acceptStack(Item item, int amount, Tile tile, Unit source){
        RoadBlockIsoEntity entity = tile.ent();
        return Math.min((int)(entity.minitem / itemSpace), amount);
    }

    @Override
    public void handleStack(Item item, int amount, Tile tile, Unit source){
        RoadBlockIsoEntity e = tile.ent();
        amount = Math.min(amount, itemCapacity[e.level()] - e.len);

        for(int i = amount - 1; i >= 0; i--){
            e.add(0);
            e.xs[0] = 0;
            e.ys[0] = i * itemSpace;
            e.ids[0] = item;
            e.items.add(item, 1);
        }

        e.noSleep();
    }

    @Override
    public boolean acceptItem(Item item, Tile tile, Tile source){
        RoadBlockIsoEntity e = tile.ent();
        if(e.len >= capacity) return false;
        int direction = source == null ? 0 : Math.abs(source.relativeTo(tile.x, tile.y) - tile.rotation());
        return (((direction == 0) && e.minitem >= itemSpace) || ((direction % 2 == 1) && e.minitem > 0.7f)) && (source == null || !(source.block().rotate && (source.rotation() + 2) % 4 == tile.rotation()));
    }

    @Override
    public void handleItem(Item item, Tile tile, Tile source){
        RoadBlockIsoEntity e = tile.ent();
        if(e.len >= capacity) return;

        byte r = tile.rotation();
        int ang = ((source.relativeTo(tile.x, tile.y) - r));
        float x = (ang == -1 || ang == 3) ? 1 : (ang == 1 || ang == -3) ? -1 : 0;

        e.noSleep();
        e.items.add(item, 1);

        if(Math.abs(source.relativeTo(tile.x, tile.y) - r) == 0){ //idx = 0
            e.add(0);
            e.xs[0] = x;
            e.ys[0] = 0;
            e.ids[0] = item;
        }else{ //idx = mid
            e.add(e.mid);
            e.xs[e.mid] = x;
            e.ys[e.mid] = 0.5f;
            e.ids[e.mid] = item;
        }
    }


    /**
     *  传送带 瓦砾实体
     * */
    public static class RoadBlockIsoEntity extends TileEntity{
        //并行数组数据. parallel array data
        Item[] ids = new Item[capacity];
        float[] xs = new float[capacity];
        float[] ys = new float[capacity];
        //物品数量. amount of items, always < capacity
        int len = 0;
        //移动到瓦砾实体. next entity
        @Nullable TileEntity next;
        @Nullable RoadBlockIsoEntity nextc;
        //对齐. whether the next conveyor's rotation == tile rotation
        boolean aligned;

        /** 最后插入*/
        int lastInserted, /** 中间*/mid;
        /** 中间物品*/
        float minitem = 1;

        /** 混合 位数据*/
        int blendbits;
        /** 混合 缩放*/
        int blendsclx, blendscly;

        /** 阻塞值*/
        float clogHeat = 0f;

        // zones add
        public RoadBlockIsoEntity() {

        }

        /** 添加*/
        final void add(int o){
            for(int i = Math.max(o + 1, len); i > o; i--){
                ids[i] = ids[i - 1];
                xs[i] = xs[i - 1];
                ys[i] = ys[i - 1];
            }

            len++;
        }

        /** 移除*/
        final void remove(int o){
            for(int i = o; i < len - 1; i++){
                ids[i] = ids[i + 1];
                xs[i] = xs[i + 1];
                ys[i] = ys[i + 1];
            }

            len--;
        }

        @Override
        public void write(DataOutput stream) throws IOException{
            super.write(stream);
            stream.writeInt(len);

            for(int i = 0; i < len; i++){
                stream.writeInt(Pack.intBytes((byte)ids[i].id, (byte)(xs[i] * 127), (byte)(ys[i] * 255 - 128), (byte)0));
            }
        }

        @Override
        public void read(DataInput stream, byte revision) throws IOException{
            super.read(stream, revision);
            int amount = stream.readInt();
            len = Math.min(amount, capacity);

            for(int i = 0; i < amount; i++){
                int val = stream.readInt();
                byte id = (byte)(val >> 24);
                float x = (float)((byte)(val >> 16)) / 127f;
                float y = ((float)((byte)(val >> 8)) + 128f) / 255f;
                if(i < capacity){
                    ids[i] = content.item(id);
                    xs[i] = x;
                    ys[i] = y;
                }
            }
        }

        private int curVariant = 0;
    }
}
