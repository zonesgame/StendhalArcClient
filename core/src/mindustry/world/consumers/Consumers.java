package mindustry.world.consumers;

import arc.struct.*;
import arc.func.Boolf;
import arc.util.Structs;
import mindustry.Vars;
import mindustry.entities.type.TileEntity;
import mindustry.type.*;
import mindustry.world.blocks.power.ConditionalConsumePower;
import mindustry.world.meta.BlockStats;

/**
 *  消费物品 管理器
 * */
public class Consumers{
    /** 消费物品类型列表*/
    private Consume[] map = new Consume[ConsumeType.values().length];
    /** 消费物品结果*/
    private Consume[] results, /** 可选择消费物品结果*/optionalResults;

    /** 物品过滤器*/
    public final Bits itemFilters = new Bits(Vars.content.items().size);
    /** 流体过滤器*/
    public final Bits liquidfilters = new Bits(Vars.content.liquids().size);

    public void init(){
        results = Structs.filter(Consume.class, map, m -> m != null);
        optionalResults = Structs.filter(Consume.class, map, m -> m != null && m.isOptional());

        for(Consume cons : results){
            cons.applyItemFilter(itemFilters);
            cons.applyLiquidFilter(liquidfilters);
        }
    }

    /** 获取电力消费过滤器*/
    public ConsumePower getPower(){
        return get(ConsumeType.power);
    }

    /** 是否消耗电力*/
    public boolean hasPower(){
        return has(ConsumeType.power);
    }

    /** 获取流体消费过滤器*/
    public ConsumeLiquid liquid(Liquid liquid, float amount){
        return add(new ConsumeLiquid(liquid, amount));
    }

    /**
     *  创建一个电力消费过滤器直接使用而不使用缓存.<><p/>
     * Creates a consumer which directly uses power without buffering it.
     * @param powerPerTick The amount of power which is required each tick for 100% efficiency.
     * @return the created consumer object.
     */
    public ConsumePower power(float powerPerTick){
        return add(new ConsumePower(powerPerTick, 0.0f, false));
    }

    /** 创建一个只在满足条件时只消耗能量的消费者.<p/>Creates a consumer which only consumes power when the condition is met. */
    public ConsumePower powerCond(float usage, Boolf<TileEntity> cons){
        return add(new ConditionalConsumePower(usage, cons));
    }

    /**
     *  创建一个电力消费过滤器.<p/>
     * Creates a consumer which stores power.
     * @param powerCapacity The maximum capacity in power units.
     */
    public ConsumePower powerBuffered(float powerCapacity){
        return add(new ConsumePower(0f, powerCapacity, true));
    }

    /** 创建物品消费过滤器*/
    public ConsumeItems item(Item item){
        return item(item, 1);
    }

    /** 创建物品消费过滤器*/
    public ConsumeItems item(Item item, int amount){
        return add(new ConsumeItems(new ItemStack[]{new ItemStack(item, amount)}));
    }

    /** 创建物品消费过滤器*/
    public ConsumeItems items(ItemStack... items){
        return add(new ConsumeItems(items));
    }

    /** 添加消费过滤器*/
    public <T extends Consume> T add(T consume){
        map[consume.type().ordinal()] = consume;
        return consume;
    }

    /** 移除指定类型消费过滤器*/
    public void remove(ConsumeType type){
        map[type.ordinal()] = null;
    }

    /** 是否包含指定类型消费过滤器.*/
    public boolean has(ConsumeType type){
        return map[type.ordinal()] != null;
    }

    /** 获取指定类型消费过滤器*/
    @SuppressWarnings("unchecked")
    public <T extends Consume> T get(ConsumeType type){
        if(map[type.ordinal()] == null){
            throw new IllegalArgumentException("Block does not contain consumer of type '" + type + "'!");
        }
        return (T)map[type.ordinal()];
    }

    /** 获取消费过滤器容器*/
    public Consume[] all(){
        return results;
    }

    /** 获取选择消费过滤器容器*/
    public Consume[] optionals(){
        return optionalResults;
    }

    /** 添加显示状态条*/
    public void display(BlockStats stats){
        for(Consume c : map){
            if(c != null){
                c.display(stats);
            }
        }
    }
}
