package z.world.blocks.production;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import arc.Core;
import arc.func.Cons;
import arc.func.Prov;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.struct.Array;
import arc.struct.ObjectMap;
import arc.z.util.ISOUtils;
import arc.z.util.ZonesAnnotate.ZAdd;
import arc.z.util.ZonesAnnotate.ZField;
import arc.z.util.ZonesAnnotate.ZMethod;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.entities.Effects.Effect;
import mindustry.entities.type.TileEntity;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.type.LiquidStack;
import mindustry.ui.Bar;
import mindustry.world.Tile;
import mindustry.world.consumers.Consume;
import mindustry.world.consumers.ConsumeItems;
import mindustry.world.consumers.ConsumeLiquidBase;
import mindustry.world.consumers.ConsumeType;
import mindustry.world.consumers.Consumers;
import mindustry.world.meta.BlockStat;
import mindustry.world.meta.StatUnit;
import z.entities.ani.SpriteAniControl;
import z.entities.type.WorkerTileEntity;
import z.tools.serialize.XmlSerialize;
import z.utils.FinalCons;
import z.world.blocks.defense.WorkBlock;

import static arc.z.util.ISOUtils.TILE_HEIGHT;
import static arc.z.util.ISOUtils.TILE_WIDTH;

/**
 *  一般工厂块
 * */
public class GenericCrafterIso extends WorkBlock {
    /** 生产物品*/
    @ZField
    public ItemStack[][] outputItem = { {} };
    /** 生产流体*/
    @ZField
    public LiquidStack[][] outputLiquid;

    /** 生产时间*/
    @ZField
    public float craftTime[] = {80};
    /** 存储生产单位倍数*/
    @ZField
    public int [] consumesMultiple = {3};      // zones add
    /** 生产效果*/
    public Effect craftEffect = Fx.none;
    /** 更新效果*/
    public Effect updateEffect = Fx.none;
    /** 更新效果几率*/
    public float updateEffectChance = 0.04f;

    /***/
    public Cons<Tile> drawer = null;
    /***/
    public Prov<TextureRegion[]> drawIcons = null;
    // 纹理数据zones add begon
    /** 生产所需物品*/
    private Item[][] itemConsume = {};
    /***/
    private SpriteAniControl aniControl;
    /***/
    private int[] offset;
    /** 动画纹理*/
    private TextureRegion[][] aniRegions;
    /** 粘土坑淹没纹理*/
    private TextureRegion[] storageRegions;
    // zones add end

    public GenericCrafterIso(String name){
        super(name);
        update = true;
        solid = true;
        hasItems = true;
//        health[0] = 60;
//        idleSound = Sounds.machine;
        sync = true;
        idleSoundVolume = 0.03f;
        entityType = GenericCrafterIsoEntity::new;
        // custom begon
        dumpTime = (int) (FinalCons.second * 3);
        layer = Layer.overlay;
        // custom end
    }

    private String configFile;
    public GenericCrafterIso(String name, String configFile){
        this(name);
        this.configFile = configFile;
        Vars.xmlSerialize.loadBlockConfig(configFile, this);
    }

    @Override
    public void init(){
        outputsLiquid = outputLiquid != null;
        super.init();
    }

    @Override
    public void load(){
        super.load();
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

        if (true) { // 覆盖原有状态条
            bars.add("items", entity -> new Bar(() -> Core.bundle.format("bar.items", entity.items.total()), () -> Pal.items, () -> {
                float total = 0;
                for (ItemStack itemStack : outputItem[entity.level()]) {
                    total += entity.items.get(itemStack.item);
                }
                return total / (float) itemCapacity[entity.level()];
            }));
        }
    }

    @Override
    public void setStats(){
        if(consumes[0].has(ConsumeType.liquid)){
            ConsumeLiquidBase cons = consumes[0].get(ConsumeType.liquid);
            cons.timePeriod = craftTime[0];
        }

        super.setStats();
        stats.add(BlockStat.productionTime, craftTime[0] / 60f, StatUnit.seconds);

        if(outputItem != null){
            stats.add(BlockStat.output, outputItem[0][0]);
        }

        if(outputLiquid != null){
            stats.add(BlockStat.output, outputLiquid[0][0].liquid, outputLiquid[0][0].amount, false);
        }
    }

    @Override
    public boolean shouldIdleSound(Tile tile){
//        return tile.entity.cons.valid();
        return false;   // zones add
    }

    /** 生产满足条件*/
    @Override
    public boolean shouldConsume(Tile tile){
        if(outputItem != null){
            float total = 0;
            for (ItemStack itemStack : outputItem[tile.entity.level()]) {
                total += tile.entity.items.get(itemStack.item);
            }
            if (total >= itemCapacity[tile.entity.level()])
                return false;
        }
        return outputLiquid == null || !(tile.entity.liquids.get(outputLiquid[0][0].liquid) >= liquidCapacity - 0.001f);
    }

    @Override
    public void update(Tile tile){
        super.update(tile);

        GenericCrafterIsoEntity entity = tile.ent();
        if(!entity.setWorkState || !entity.working)   // 无法执行工作 退出
            return;

        if(entity.cons.valid()){

            entity.progress += getProgressIncrease(entity, craftTime[entity.level()]);
            entity.totalProgress += entity.delta();
            entity.warmup = Mathf.lerpDelta(entity.warmup, 1f, 0.02f);

//            if(Mathf.chance(Time.delta() * updateEffectChance)){
//                Effects.effect(updateEffect, entity.x + Mathf.range(size * 4f), entity.y + Mathf.range(size * 4));
//            }
        }else{
            entity.warmup = Mathf.lerp(entity.warmup, 0f, 0.02f);
        }

        if(entity.progress >= 1f){
            entity.cons.trigger();

            if(outputItem != null){
                for (ItemStack itemStack : outputItem[entity.level()]) {
                    useContent(tile, itemStack.item);
                    offloadNear(tile, itemStack.item, itemStack.amount);
                }
            }

            if(outputLiquid != null){
                for (LiquidStack liquidStack : outputLiquid[entity.level()]) {
                    useContent(tile, liquidStack.liquid);
                    handleLiquid(tile, tile, liquidStack.liquid, liquidStack.amount);
                }
            }

//            Effects.effect(craftEffect, tile.drawx(), tile.drawy());
            entity.progress = 0f;
        }

        if(outputItem != null && tile.entity.timer.get(timerDump, dumpTime) && entity.offerWorker()){
            tryDump(tile, getDumpItems(entity));
        }

        if(outputLiquid != null){
            tryDumpLiquid(tile, outputLiquid[0][0].liquid);
        }

        // 更新获取生产物品. zones add begon
        if(outputItem != null && tile.entity.timer.get(timerWorker, workerTime) && entity.offerWorker()){
            tryObtain(tile, getObtainItems(entity));
        }
        // zones add end
    }

    @Override
    public void draw(Tile tile){
        if(drawer != null){
            drawer.get(tile);
        }

        {   // no else
            Vec2 pos = ISOUtils.tileToWorldCoords(offsetTile() + tile.x , offsetTile() + tile.y );
            float dx = pos.x + (region.getWidth() * Draw.getScl() - (size) * TILE_WIDTH) / 2f;
            float dy = pos.y + (region.getHeight() * Draw.getSclH() - (size) * TILE_HEIGHT) / 2f;
            Draw.rectScaleH(region, dx, dy);

            GenericCrafterIsoEntity entity = tile.ent();
            // 工作动画绘制
            if(entity.setWorkState && entity.working && entity.cons.valid()){
//                if (show)
                Draw.rectScaleH(aniRegions[0][aniControl.getTimeFrame()], dx + offset[0] * Draw.getScl(), dy + offset[1] * Draw.getScl());
            }
            {   // 储藏物品绘制
                for (int i = 0, len = itemConsume[entity.level()].length; i < len; i++) {
                    Item item = itemConsume[entity.level()][i];
                    if (entity.items.has(item)) {
                        int index = (int) Math.max (entity.items.get(item) / (float)itemCapacity[entity.level()] * 2, 2);
                        index = Math.min(index, aniRegions[i + 1].length - 1);
                        Draw.rectScale(aniRegions[i+1][index], dx + offset[i * 2+2] * Draw.getScl(), dy + offset[i * 2+3] * Draw.getScl());
                    }
                }
            }

//            if (entity.lastDrillSpeed > 0) {
//                Draw.rectScale(aniRegions[0][aniControl.getTimeFrame()], dx + offsetx * Draw.getScl(), dy + offsety * Draw.getScl());
//            }
        }
    }

    @Override
    public TextureRegion[] generateIcons(){
        return drawIcons == null ? super.generateIcons() : drawIcons.get();
    }

    @Override
    public boolean outputsItems(){
        return outputItem != null && outputItem[0].length != 0;
    }

    @Override
    public int getMaximumAccepted(Tile tile, Item item){
        return itemCapacity[tile.entity.level()];
    }


    // zones add begon
//    private int[]offset;
//    private void loadConfigFile(String file) {
//        if (file == null)   return;
//        XmlReader.Element root = Vars.xmlReader.parse(Core.files.internal(configFile));
//        if ( !root.hasChild("animation")) return;
//
//        TextureAtlas atlas = Core.atlas;
//        XmlReader.Element aniNode = root.getChildByName("animation").getChildByName("caesar");
//        aniRegions = new TextureRegion[aniNode.getChildCount()][];
//        offset = new int[aniRegions.length * 2];
//        for (int i = aniNode.getChildCount(); --i >= 0;) {
//            XmlReader.Element node = aniNode.getChild(i);
////            int index = node.getIntAttribute("index");
//            int frame = node.getIntAttribute("frame");
//            String name = node.getText();
//            TextureRegion[] _regions = new TextureRegion[frame];
//            for (int f = 0; f < frame;) {
//                _regions[f] = atlas.find(name + ++f);
//            }
//            aniRegions[i] = _regions;
//
//            {// 动画偏移量
//                String offsetStr = node.getAttribute("offset", null);
//                if (offsetStr != null) {
//                    int indexof = offsetStr.indexOf(',');
//                    offset[i * 2] = Integer.parseInt(offsetStr.substring(0, indexof));
//                    offset[i * 2 + 1] = Integer.parseInt(offsetStr.substring(indexof + 1));
//                }
//            }
//        }
//
//        aniControl = new SpriteAniControl().setFrameData(aniRegions[0].length, 10);
//    }

//    protected void setItemConsume(ConsumeItems items) {
//        itemConsume = new Item[items.items.length];
//        for (int i = 0, len = itemConsume.length; i < len; i++) {
//            itemConsume[i] = items.items[i].item;
//        }
//    }

    @ZMethod
    public void needItemConsume(ItemStack[]... items) {
        if (true || consumes.length != items.length) {
            consumes = new Consumers[items.length];
            for (int i = 0; i < consumes.length; i++) {
                consumes[i] = new Consumers();
            }

            itemConsume = new Item[consumes.length][];
        }

        for (int i = 0; i < consumes.length; i++) {
            ConsumeItems consumeItems = consumes[i].items(items[i]);
            itemConsume[i] = new Item[consumeItems.items.length];
            for (int j = 0; j < itemConsume[i].length; j++) {
                itemConsume[i][j] = consumeItems.items[j].item;
            }
        }
    }

    @ZAdd
    @Override
    protected Array<ItemStack> getDumpItems(TileEntity entity) {
        Array<ItemStack> dumpItems = super.getDumpItems(entity);

        int dumpAmount = carryItemDump[entity.level()];
        for (ItemStack itemStack : outputItem[entity.level()]) {
            int minAmount = Math.min(dumpAmount, entity.items.get(itemStack.item));
            dumpItems.add(new ItemStack(itemStack.item, minAmount));
            dumpAmount -= minAmount;
        }
        return dumpItems;
    }

    @ZAdd
    private Array<ItemStack> getObtainItems(TileEntity entity) {
        Array<ItemStack> obtainItems = super.getDumpItems(entity);

        {
            int multiple = 1;
            int obtainAmount = carryItemObtain[entity.level()];
            breakout : while (obtainAmount > 0) {
                for(Consume cons : entity.block.consumes[entity.level()].all()){
                    if(cons.isOptional()) continue;
                    if ( !(cons instanceof ConsumeItems)) continue;

                    for (ItemStack itemStack : ((ConsumeItems) cons).items) {
                        float craftPer = entity.items.get(itemStack.item) / (float)itemStack.amount;
                        if (craftPer < multiple) {
                            int minAmount = Math.min(itemStack.amount, obtainAmount);
                            obtainItems.add(new ItemStack(itemStack.item, minAmount));

                            obtainAmount -= minAmount;
                            if (obtainAmount <= 0)
                                break  breakout;
                        }
                    }
                }

                multiple++;
                if (multiple >= consumesMultiple[entity.level()] + 1) break;
            }

            // 合并重复物品项
            for (int i = 0; i < obtainItems.size - 1; i++) {
                ItemStack itemStack = obtainItems.get(i);

                for (int j = i + 1; j < obtainItems.size; j++) {
                    if (obtainItems.get(i).item == obtainItems.get(j).item) {
                        obtainItems.get(i).amount += obtainItems.get(j).amount;
                        obtainItems.remove(j);
                        j--;
                    }
                }
            }
        }

        return obtainItems;
    }
    // zones add end



    /**
     *  一般工厂 瓦砾实体
     * */
    public static class GenericCrafterIsoEntity extends WorkerTileEntity {
        /** 处理进度*/
        public float progress;
        /** 总进度*/
        public float totalProgress;
        /** 温度*/
        public float warmup;

        @Override
        public void write(DataOutput stream) throws IOException{
            super.write(stream);
            stream.writeFloat(progress);
            stream.writeFloat(warmup);
        }

        @Override
        public void read(DataInput stream, byte revision) throws IOException{
            super.read(stream, revision);
            progress = stream.readFloat();
            warmup = stream.readFloat();
        }
    }
}
