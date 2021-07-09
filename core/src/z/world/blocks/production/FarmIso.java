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
 *  农场 块
 * */
public class FarmIso extends WorkBlock {
    // zones add begon
    private final int[] cropOffsetIndex = {
            1,1, 0,1, -1,-1, -1,0, -1,1,
    };
    private final int[] cropDrawIndex = {
            4, 3, 0, 1, 2,
    };
    // zones add end
    /** 硬度钻探倍数*/
    public final float hardnessDrillMultiplier = 50f * 1;       // zones editor final  default value 50

    /** 矿物物品*/
    protected final ObjectIntMap<Item> oreCount = new ObjectIntMap<>();
    /** 矿物数量*/
    protected final Array<Item> itemArray = new Array<>();

    /** 钻探最大层数.<p/>Maximum tier of blocks this drill can mine. */
    public int tier = 1;
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
    // zones add code
//    protected Item tileItem = Items.farmland, spawnItem;

    /** 是否绘制钻探物品.<p/>Whether to draw the item this drill is mining. */
    public boolean drawMineItem = true;
    /** 获取物品时效果.<p/>Effect played when an item is produced. This is colored. */
    public Effect drillEffect = Fx.mine;
    /** 绘制旋转速度.<p/>Speed the drill bit rotates at. */
    public float rotateSpeed = 2f;
    /** 钻探时随机播放效果.<p/>Effect randomly played while drilling. */
    public Effect updateEffect = Fx.pulverizeSmall;
    /** 更新效果出现几率.<p/>Chance the update effect will appear. */
    public float updateEffectChance = 0.02f;

    /** 绘制边缘*/
    public boolean drawRim = false;
    /** 过热颜色*/
    public Color heatColor = Color.valueOf("ff5512");
    /** 边缘纹理*/
//    public TextureRegion rimRegion;
//    /** 旋转纹理*/
//    public TextureRegion rotatorRegion;
//    /** 覆盖层纹理*/
//    public TextureRegion topRegion;

    // zones add begon
    /**放置瓦砾需要产出的物品*/
    private final Item tileItem = Items.farmland;
    /** 块生产的物品*/
    @ZField
    public Item spawnItem;
    /** 开采物品数量*/
    @ZField
    public int[] drillCount = {1};

    /** 农场纹理*/
//    public TextureRegion baseRegion;
    /** 农作物纹理*/
    public TextureRegion[] cropRegions;
    // zones add end

    public FarmIso(String name){
        super(name);
        update = true;
        solid = true;
        layer = Layer.overlay;
        group = BlockGroup.drills;
        hasLiquids = true;
        liquidCapacity = 5f;
        hasItems = true;
        entityType = FarmIsoEntity::new;

//        idleSound = Sounds.drill;
//        idleSoundVolume = 0.003f;
        // custom begon
        dumpTime = (int) (FinalCons.second * 3);
//        itemCapacity = 3;
//        drillTime = FinalCons.second * size * size * 100;
        // custom end
    }

    private String configFile;
    public FarmIso(String name, String xmlFile){
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
        cropRegions = ((TextureRegion[][]) tempPool.get(XmlSerialize.aniRegions))[0];
        tempPool.clear();   // no must
        // zones add end
    }

    @Override
    public void setBars(){
        super.setBars();

        bars.add("drillspeed", e -> {
            FarmIsoEntity entity = (FarmIsoEntity)e;

            return new Bar(() -> Core.bundle.format("bar.drillspeed", Strings.fixed(entity.lastDrillSpeed * 60 * entity.timeScale, 2)), () -> Pal.ammo, () -> entity.warmup);
        });
    }

    @Override
    public void drawCracks(Tile tile){}

    @Override
    public void draw(Tile tile){
        ShapeRenderer.drawDiamond(tile.x, tile.y, tile.block().size);

        FarmIsoEntity entity = tile.ent();
        Vec2 pos = ISOUtils.tileToWorldCoords(tile.block().offsetTile() + tile.x + 0.5f, tile.block().offsetTile() + tile.y - 0.5f);
        float dx = pos.x + (region.getWidth() * Draw.getScl() - (tile.block().size - 1) * TILE_WIDTH) / 2f;
        float dy = pos.y + (region.getHeight() * Draw.getSclH() - (tile.block().size - 1) * TILE_HEIGHT) / 2f;
        Draw.rectScaleH(region, dx, dy);
        // 农作物绘制
        for(int i = 0, index = 0; i < 5; i++) {
            pos = ISOUtils.tileToWorldCoords(tile.x + cropOffsetIndex[index++], tile.y + cropOffsetIndex[index++]);
            int cropIndex = entity.cropAniIndex[cropDrawIndex[i]];
            TextureRegion _region = cropRegions[cropIndex];
            dx = pos.x + (_region.getWidth() * Draw.getScl() - (1) * TILE_WIDTH) / 2f;
            dy = pos.y + (_region.getHeight() * Draw.getSclH() - (1) * TILE_HEIGHT) / 2f;
            Draw.rectScaleH(_region, dx, dy);
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
        FarmIsoEntity entity = tile.ent();

        if(entity.dominantItem != null){
            float dx = tile.drawx() - size * tilesize/2f, dy = tile.drawy() + size * tilesize/2f;
            Draw.mixcol(Color.darkGray, 1f);
            Draw.rect(spawnItem.icon(Cicon.small), dx, dy - 1);           // entity.dominantItem
            Draw.reset();
            Draw.rect(spawnItem.icon(Cicon.small), dx, dy);           // entity.dominantItem
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

        FarmIsoEntity entity = tile.ent();
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
            // zones add begon
            entity.setProcessPercent(entity.progress / (drillTime[entity.level()] + hardnessDrillMultiplier * entity.dominantItem.hardness));
            // zones add end

//            if(Mathf.chance(Time.delta() * updateEffectChance * entity.warmup))
//                Effects.effect(updateEffect, entity.x + Mathf.range(size * 2f), entity.y + Mathf.range(size * 2f));
        }else{  // 仓库已满停止工作
            entity.lastDrillSpeed = 0f;
            entity.warmup = Mathf.lerpDelta(entity.warmup, 0f, warmupSpeed);
            return;
        }

        // 采集完毕生成物品
        if(entity.dominantItems > 0 && entity.progress >= drillTime[entity.level()] + hardnessDrillMultiplier * entity.dominantItem.hardness && tile.entity.items.total() < itemCapacity[entity.level()]){

            offloadNear(tile, spawnItem, drillCount[entity.level()]);        //offloadNear(tile, entity.dominantItem);

            useContent(tile, spawnItem);       //  useContent(tile, entity.dominantItem);

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
        return drops != null && drops.name.equals(tileItem.name);
    }

    @ZAdd
    @Override
    protected Array<ItemStack> getDumpItems(TileEntity entity) {
        Array<ItemStack> dumpItems = super.getDumpItems(entity);

        dumpItems.add(new ItemStack(spawnItem, Math.min(carryItemDump[entity.level()], entity.items.get(spawnItem))));
        return dumpItems;
    }



    /**
     *  矿机 瓦砾实体
     * */
    public static class FarmIsoEntity extends WorkerTileEntity {
        /** 当前进度*/
        float progress;
        /***/
        int index;
        /** 温度*/
        float warmup;
        /** 钻探时间*/
        float drillTime;
        /** 最后钻探速度*/
        float lastDrillSpeed;

        /** 支配物品数量*/
        int dominantItems;
        /** 支配物品*/
        Item dominantItem;

        // zones add begon  animation data
        /** 进度百分比Max100*/
        int processPer;
        int[] cropAniIndex = new int[6];
        private void setProcessPercent(float per) {
            int curProcessPer = per > 1 ? 25 : (int) (per * 25);
            if (curProcessPer != processPer) {
                processPer = curProcessPer;
                int col = processPer / 5;
                cropAniIndex[col] = processPer - col * 5;
                for (int i = col; --i >= 0 ;) {
                    cropAniIndex[i] = 4;
                }
                for (int i = cropAniIndex.length; --i > col; ) {
                    cropAniIndex[i] = 0;
                }
            }
        }
        // zones add end
    }


    // zones add begon
    // zones add end
}
