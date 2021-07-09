package mindustry.world.meta;

import arc.Core;

/**
 *  一个特定的状态类别.<p/>
 * A specific category for a stat.
 * */
public enum StatCategory{
    /** 一般*/
    general,
    /** 电力*/
    power,
    /** 流体*/
    liquids,
    /** 物品*/
    items,
    /** 生产*/
    crafting,
    /** 射击*/
    shooting,
    /** 选择*/
    optional;

    public String localized(){
        return Core.bundle.get("category." + name());
    }
}
