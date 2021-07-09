package mindustry.world.consumers;

import arc.scene.ui.layout.Table;
import arc.struct.Bits;
import mindustry.entities.type.TileEntity;
import mindustry.world.Tile;
import mindustry.world.meta.BlockStats;

/**
 *  一个抽象类, 它定义了一个块可以消耗的一种资源类型.<p/>
 * An abstract class that defines a type of resource that a block can consume.
 * */
public abstract class Consume{
    /** 可选择.<p/>If true, this consumer will not influence consumer validity. */
    protected boolean optional;
    /** 增加.<p/>If true, this consumer will be displayed as a boost input. */
    protected boolean booster;
    /** 更新*/
    protected boolean update = true;

    /**
     *  接收物品过滤.<p/>
     * Apply a filter to items accepted.
     * This should set all item IDs that are present in the filter to true.
     */
    public void applyItemFilter(Bits filter){

    }

    /**
     *  接收流体过滤.<p/>
     * Apply a filter to liquids accepted.
     * This should set all liquid IDs that are present in the filter to true.
     */
    public void applyLiquidFilter(Bits filter){

    }

    /** 选择设置*/
    public Consume optional(boolean optional, boolean boost){
        this.optional = optional;
        this.booster = boost;
        return this;
    }

    /** 增加设置*/
    public Consume boost(){
        return optional(true, true);
    }

    /** 更新设置*/
    public Consume update(boolean update){
        this.update = update;
        return this;
    }

    /** 选择状态*/
    public boolean isOptional(){
        return optional;
    }

    /** 增加状态*/
    public boolean isBoost(){
        return booster;
    }

    /** 更新状态*/
    public boolean isUpdate(){
        return update;
    }

    /** 消费品类型*/
    public abstract ConsumeType type();

    /** 构建UI*/
    public abstract void build(Tile tile, Table table);

    /** 手动触发时调用.<p/>Called when a consumption is triggered manually. */
    public void trigger(TileEntity entity){

    }

    /** 消费品图标*/
    public abstract String getIcon();

    /** 更新消费品*/
    public abstract void update(TileEntity entity);

    /** 是否有有效消费物品*/
    public abstract boolean valid(TileEntity entity);

    /** 添加显示状态条*/
    public abstract void display(BlockStats stats);
}
