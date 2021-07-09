package mindustry.world.blocks.production;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import arc.func.Cons;
import arc.func.Prov;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.util.Time;
import mindustry.content.Fx;
import mindustry.entities.Effects;
import mindustry.entities.Effects.Effect;
import mindustry.entities.type.TileEntity;
import mindustry.gen.Sounds;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.type.LiquidStack;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.consumers.ConsumeLiquidBase;
import mindustry.world.consumers.ConsumeType;
import mindustry.world.meta.BlockStat;
import mindustry.world.meta.StatUnit;

/**
 *  一般工厂块
 * */
public class GenericCrafter extends Block{
    /** 生产物品*/
    public ItemStack outputItem;
    /** 生产流体*/
    public LiquidStack outputLiquid;

    /** 生产时间*/
    public float craftTime = 80;
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

    public GenericCrafter(String name){
        super(name);
        update = true;
        solid = true;
        hasItems = true;
        health[0] = 60;
        idleSound = Sounds.machine;
        sync = true;
        idleSoundVolume = 0.03f;
        entityType = GenericCrafterEntity::new;
    }

    @Override
    public void setStats(){
        if(consumes[0].has(ConsumeType.liquid)){
            ConsumeLiquidBase cons = consumes[0].get(ConsumeType.liquid);
            cons.timePeriod = craftTime;
        }

        super.setStats();
        stats.add(BlockStat.productionTime, craftTime / 60f, StatUnit.seconds);

        if(outputItem != null){
            stats.add(BlockStat.output, outputItem);
        }

        if(outputLiquid != null){
            stats.add(BlockStat.output, outputLiquid.liquid, outputLiquid.amount, false);
        }
    }

    @Override
    public boolean shouldIdleSound(Tile tile){
        return tile.entity.cons.valid();
    }

    @Override
    public void init(){
        outputsLiquid = outputLiquid != null;
        super.init();
    }

    @Override
    public void draw(Tile tile){
        if(drawer == null){
            super.draw(tile);
        }else{
            drawer.get(tile);
        }
    }

    @Override
    public TextureRegion[] generateIcons(){
        return drawIcons == null ? super.generateIcons() : drawIcons.get();
    }

    @Override
    public void update(Tile tile){
        GenericCrafterEntity entity = tile.ent();

        if(entity.cons.valid()){

            entity.progress += getProgressIncrease(entity, craftTime);
            entity.totalProgress += entity.delta();
            entity.warmup = Mathf.lerpDelta(entity.warmup, 1f, 0.02f);

            if(Mathf.chance(Time.delta() * updateEffectChance)){
                Effects.effect(updateEffect, entity.x + Mathf.range(size * 4f), entity.y + Mathf.range(size * 4));
            }
        }else{
            entity.warmup = Mathf.lerp(entity.warmup, 0f, 0.02f);
        }

        if(entity.progress >= 1f){
            entity.cons.trigger();

            if(outputItem != null){
                useContent(tile, outputItem.item);
                for(int i = 0; i < outputItem.amount; i++){
                    offloadNear(tile, outputItem.item);
                }
            }

            if(outputLiquid != null){
                useContent(tile, outputLiquid.liquid);
                handleLiquid(tile, tile, outputLiquid.liquid, outputLiquid.amount);
            }

            Effects.effect(craftEffect, tile.drawx(), tile.drawy());
            entity.progress = 0f;
        }

        if(outputItem != null && tile.entity.timer.get(timerDump, dumpTime)){
            tryDump(tile, outputItem.item);
        }

        if(outputLiquid != null){
            tryDumpLiquid(tile, outputLiquid.liquid);
        }
    }

    @Override
    public boolean outputsItems(){
        return outputItem != null;
    }

    @Override
    public boolean shouldConsume(Tile tile){
        if(outputItem != null && tile.entity.items.get(outputItem.item) >= itemCapacity[tile.entity.level()]){
            return false;
        }
        return outputLiquid == null || !(tile.entity.liquids.get(outputLiquid.liquid) >= liquidCapacity - 0.001f);
    }

    @Override
    public int getMaximumAccepted(Tile tile, Item item){
        return itemCapacity[tile.entity.level()];
    }


    /**
     *  一般工厂 瓦砾实体
     * */
    public static class GenericCrafterEntity extends TileEntity{
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
