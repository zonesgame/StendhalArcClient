package z.world.blocks.production;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.struct.Array;
import arc.struct.ObjectIntMap;
import arc.struct.ObjectMap;
import arc.util.Strings;
import arc.z.util.ISOUtils;
import arc.z.util.ZonesAnnotate.ZAdd;
import arc.z.util.ZonesAnnotate.ZField;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.content.Items;
import mindustry.entities.Effects.Effect;
import mindustry.entities.type.TileEntity;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.ui.Bar;
import mindustry.ui.Cicon;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.meta.BlockGroup;
import mindustry.world.meta.BlockStat;
import mindustry.world.meta.StatUnit;
import z.entities.ani.SpriteAniControl;
import z.entities.type.WorkerTileEntity;
import z.tools.serialize.XmlSerialize;
import z.utils.FinalCons;
import z.utils.ShapeRenderer;
import z.world.blocks.defense.WorkBlock;

import static arc.z.util.ISOUtils.TILE_HEIGHT;
import static arc.z.util.ISOUtils.TILE_WIDTH;
import static mindustry.Vars.content;
import static mindustry.Vars.tilesize;
import static mindustry.Vars.world;

/**
 *  矿机 块
 * */
public class DrillIso extends WorkBlock {
    /** 硬度钻探倍数*/
    public float hardnessDrillMultiplier = 50f;

    /** 开采矿物物品*/
    protected final ObjectIntMap<Item> oreCount = new ObjectIntMap<>();
    /** 矿物数量*/
    protected final Array<Item> itemArray = new Array<>();

    /** 钻探最大层数.<p/>Maximum tier of blocks this drill can mine. */
    public int tier;
    /** 获取一个单位需要帧数.<p/>Base time to drill one ore, in frames. */
    @ZField
    public float[] drillTime = {300};
    /** 液体加速倍数.<p/>How many times faster the drill will progress when boosted by liquid. */
    public float liquidBoostIntensity = 1.6f;
    /** 钻头加速倍数.<p/>Speed at which the drill speeds up. */
    public float warmupSpeed = 0.02f;

    //return variables for countOre
    /** 获取物品*/
    protected Item returnItem;
    /** 获取物品数量*/
    protected int returnCount;

    /** 是否绘制钻探物品.<p/>Whether to draw the item this drill is mining. */
    public boolean drawMineItem = false;
    /** 获取物品时效果.<p/>Effect played when an item is produced. This is colored. */
    public Effect drillEffect = Fx.mine;
    /** 绘制旋转速度.<p/>Speed the drill bit rotates at. */
    public float rotateSpeed = 2f;
    /** 钻探时随机播放效果.<p/>Effect randomly played while drilling. */
    public Effect updateEffect = null;      // Fx.pulverizeSmall;
    /** 更新效果出现几率.<p/>Chance the update effect will appear. */
    public float updateEffectChance = 0.02f;

    /** 绘制边缘*/
    public boolean drawRim = false;
    /** 过热颜色*/
    public Color heatColor = Color.valueOf("ff5512");
//    /** 边缘纹理*/
//    public TextureRegion rimRegion;
//    /** 旋转纹理*/
//    public TextureRegion rotatorRegion;
//    /** 覆盖层纹理*/
//    public TextureRegion topRegion;

    // zones add begon
    /**放置瓦砾需要产出的物品*/
    @ZField
    public Item tileItem = null;
    /** 开采物品数量*/
    @ZField
    public int[] drillCount = {1};
//    /** 倾泻物品缓存池*/
//    private Array<ItemStack> dumpItems = new Array<>(4);
    /***/
    private int[] offset;
    /** 动画播放控制器*/
    private SpriteAniControl aniControl;
    /** 动画纹理*/
    private TextureRegion[][] aniRegions;
    /** 粘土坑淹没纹理*/
    private TextureRegion regionDisable;
    // zones add end

    public DrillIso(String name){
        super(name);
        update = true;
        solid = true;
        layer = Layer.overlay;
        group = BlockGroup.drills;
        hasLiquids = true;
        liquidCapacity = 5f;
        hasItems = true;
        entityType = DrillIsoEntity::new;

//        idleSound = Sounds.drill;
//        idleSoundVolume = 0.003f;
        // custom begon
        dumpTime = (int) (FinalCons.second * 6);
//        itemCapacity[0] = 3;
        // custom end
    }

//    @Override
//    @ZMethod
//    public void initMindustry() {
//        super.initMindustry();
//
//    }

    // zones add begon
    private String configFile;
    public DrillIso(String name, String xmlFile){
        this(name);
        this.configFile = xmlFile;
        Vars.xmlSerialize.loadBlockConfig(xmlFile, this);
    }

    @Override
    public void load(){
        super.load();
//        rimRegion = Core.atlas.find(name + "-rim");
//        rotatorRegion = Core.atlas.find(name + "-rotator");
//        topRegion = Core.atlas.find(name + "-top");
        // zones add begon
        ObjectMap<String, Object> tempPool = Vars.xmlSerialize.loadBlockAnimation(this.configFile);
        if (tempPool == null)   return;
        aniRegions = (TextureRegion[][]) tempPool.get(XmlSerialize.aniRegions);
        offset = (int[]) tempPool.get(XmlSerialize.offset);
        tempPool.clear();   // no must

        aniControl = new SpriteAniControl().setFrameData(aniRegions[0].length, 10);
        // zones add end
    }

    @Override
    public void setBars(){
        super.setBars();

        bars.add("drillspeed", e -> {
            DrillIsoEntity entity = (DrillIsoEntity)e;

            return new Bar(() -> Core.bundle.format("bar.drillspeed", Strings.fixed(entity.lastDrillSpeed * 60 * entity.timeScale, 2)), () -> Pal.ammo, () -> entity.warmup);
        });
    }

    @Override
    public void drawCracks(Tile tile){}

    @Override
    public void draw(Tile tile){
            ShapeRenderer.drawDiamond(tile.x, tile.y, tile.block().size);

            Vec2 pos = ISOUtils.tileToWorldCoords(offsetTile() + tile.x , offsetTile() + tile.y );
            float dx = pos.x + (region.getWidth() * Draw.getScl() - (size) * TILE_WIDTH) / 2f;
            float dy = pos.y + (region.getHeight() * Draw.getSclH() - (size) * TILE_HEIGHT) / 2f;
            Draw.rectScaleH(region, dx, dy);
            // 动画绘制
            DrillIsoEntity entity = tile.ent();
            if (entity.lastDrillSpeed > 0) {
                Draw.rectScaleH(aniRegions[0][aniControl.getTimeFrame()], dx + offset[0] * Draw.getScl(), dy + offset[1] * Draw.getScl());
            }
    }

    @Override
    public TextureRegion[] generateIcons(){
        return new TextureRegion[]{Core.atlas.find(name), Core.atlas.find(name + "-rotator"), Core.atlas.find(name + "-top")};
    }

    @Override
    public boolean shouldConsume(Tile tile){
        return tile.entity.items.total() < itemCapacity[tile.entity.level()];
    }

    @Override
    public boolean shouldIdleSound(Tile tile){
        return tile.entity.efficiency() > 0.01f;
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid){
        Tile tile = world.tile(x, y);
        if(tile == null) return;

        countOre(tile);

        if(returnItem != null){
            float width = drawPlaceText(Core.bundle.formatFloat("bar.drillspeed", 60f / (drillTime[0] + hardnessDrillMultiplier * returnItem.hardness) * returnCount, 2), x, y, valid);
            float dx = x * tilesize + offset() - width/2f - 4f, dy = y * tilesize + offset() + size * tilesize / 2f + 5;
            Draw.mixcol(Color.darkGray, 1f);
            Draw.rect(returnItem.icon(Cicon.small), dx, dy - 1);
            Draw.reset();
            Draw.rect(returnItem.icon(Cicon.small), dx, dy);
        }else{
            Tile to = tile.getLinkedTilesAs(this, tempTiles).find(t -> t.drop() != null && t.drop().hardness > tier);
            Item item = to == null ? null : to.drop();
            if(item != null){
                drawPlaceText(Core.bundle.get("bar.drilltierreq"), x, y, valid);
            }
        }
    }

    @Override
    public void drawSelect(Tile tile){
        DrillIsoEntity entity = tile.ent();

        if(entity.dominantItem != null){
            float dx = tile.drawx() - size * tilesize/2f, dy = tile.drawy() + size * tilesize/2f;
            Draw.mixcol(Color.darkGray, 1f);
            Draw.rect(entity.dominantItem.icon(Cicon.small), dx, dy - 1);
            Draw.reset();
            Draw.rect(entity.dominantItem.icon(Cicon.small), dx, dy);
        }
    }

    @Override
    public void setStats(){
        super.setStats();

        stats.add(BlockStat.drillTier, table -> {
            Array<Block> list = content.blocks().select(b -> b.isFloor() && b.asFloor().itemDrop != null && b.asFloor().itemDrop.hardness <= tier);

            table.table(l -> {
                l.left();

                for(int i = 0; i < list.size; i++){
                    Block item = list.get(i);

                    l.addImage(item.icon(Cicon.small)).size(8 * 3).padRight(2).padLeft(2).padTop(3).padBottom(3);
                    l.add(item.localizedName).left().padLeft(1).padRight(4);
                    if(i % 5 == 4){
                        l.row();
                    }
                }
            });


        });

        stats.add(BlockStat.drillSpeed, 60f / drillTime[0] * size * size, StatUnit.itemsSecond);
        if(liquidBoostIntensity > 0){
            stats.add(BlockStat.boostEffect, liquidBoostIntensity * liquidBoostIntensity, StatUnit.timesSpeed);
        }
    }

    /** 计算生产矿物和生产矿物数量*/
    void countOre(Tile tile){
        returnItem = null;
        returnCount = 0;

        oreCount.clear();
        itemArray.clear();

        for(Tile other : tile.getLinkedTilesAs(this, tempTiles)){
            if(isValid(other)){
                oreCount.getAndIncrement(getDrop(other), 0, 1);
            }
        }

        for(Item item : oreCount.keys()){
            itemArray.add(item);
        }

        itemArray.sort((item1, item2) -> {
            int type = Boolean.compare(item1 != Items.sand, item2 != Items.sand);
            if(type != 0) return type;
            int amounts = Integer.compare(oreCount.get(item1, 0), oreCount.get(item2, 0));
            if(amounts != 0) return amounts;
            return Integer.compare(item1.id, item2.id);
        });

        if(itemArray.size == 0){
            return;
        }

        returnItem = itemArray.peek();
        returnCount = oreCount.get(itemArray.peek(), 0);
    }

    @Override
    public void update(Tile tile){
        super.update(tile);

        DrillIsoEntity entity = tile.ent();
        if(!entity.setWorkState || !entity.working)   // 无法执行工作 退出
            return;

        if(entity.dominantItem == null){        // 初始化, 生产物品和产量
            countOre(tile);
            if(returnItem == null) return;
            entity.dominantItem = returnItem;
            entity.dominantItems = returnCount;
        }

        if(entity.timer.get(timerDump, dumpTime) && entity.offerWorker()){      // 工人开始搬运物品
            tryDump(tile, getDumpItems(entity));
        }

        entity.drillTime += entity.warmup * entity.delta();

        if(entity.items.total() < itemCapacity[entity.level()] && entity.dominantItems > 0 && entity.cons.valid()){     // 满足条件开始生产

            float speed = 1f;

            if(entity.cons.optionalValid()){
                speed = liquidBoostIntensity;
            }

            speed *= entity.efficiency(); // Drill slower when not at full power

            entity.lastDrillSpeed = (speed * entity.dominantItems * entity.warmup) / (drillTime[entity.level()] + hardnessDrillMultiplier * entity.dominantItem.hardness);
            entity.warmup = Mathf.lerpDelta(entity.warmup, speed, warmupSpeed);
            entity.progress += entity.delta()
                    * entity.dominantItems * speed * entity.warmup;

//            if(Mathf.chance(Time.delta() * updateEffectChance * entity.warmup))
//                Effects.effect(updateEffect, entity.x + Mathf.range(size * 2f), entity.y + Mathf.range(size * 2f));
        }else{  // 仓库已满停止工作
            entity.lastDrillSpeed = 0f;
            entity.warmup = Mathf.lerpDelta(entity.warmup, 0f, warmupSpeed);
            return;
        }

        // 采集完毕生成物品
        if(entity.dominantItems > 0 && entity.progress >= drillTime[entity.level()] + hardnessDrillMultiplier * entity.dominantItem.hardness && tile.entity.items.total() < itemCapacity[entity.level()]){

            offloadNear(tile, entity.dominantItem, drillCount[entity.level()]);

            useContent(tile, entity.dominantItem);

            entity.index++;
            entity.progress = 0f;

//            Effects.effect(drillEffect, entity.dominantItem.color,
//                    entity.x + Mathf.range(size), entity.y + Mathf.range(size));
        }
    }

    @Override
    public boolean canPlaceOn(Tile tile){
        if(isMultiblock()){
            for(Tile other : tile.getLinkedTilesAs(this, tempTiles)){
                if(isValid(other)){
                    return true;
                }
            }
            return false;
        }else{
            return isValid(tile);
        }
    }

    /** 钻探层数*/
    public int tier(){
        return tier;
    }

    /** 倾泻物品*/
    public Item getDrop(Tile tile){
        return tile.drop();
    }

    /** 指定瓦砾是否可工作*/
    public boolean isValid(Tile tile){
        if(tile == null) return false;
        Item drops = tile.drop();
//        return drops != null && drops.hardness <= tier;
        return drops != null && drops.name.equals(tileItem.name);         // zones editor
    }

    @ZAdd
    @Override
    protected Array<ItemStack> getDumpItems(TileEntity entity) {
        Array<ItemStack> dumpItems = super.getDumpItems(entity);

        DrillIsoEntity blockEntity = (DrillIsoEntity) entity;
        dumpItems.add(new ItemStack(blockEntity.dominantItem, Math.min(carryItemDump[entity.level()], entity.items.get(blockEntity.dominantItem))));
        return dumpItems;
    }


    /**
     *  矿机 瓦砾实体
     * */
    public static class DrillIsoEntity extends WorkerTileEntity {
        /** 当前进度*/
        float progress;
        /** 工作生产数量*/
        int index;
        /** 温度*/
        float warmup;
        /** 当前钻探时间*/
        float drillTime;
        /** 最后钻探速度*/
        float lastDrillSpeed;

        /** 支配物品数量*/
        int dominantItems;
        /** 支配物品*/
        Item dominantItem;
    }

}
