package mindustry.world.meta;

import java.util.Locale;

import arc.Core;

/**
 *  描述一个块的类型状态<p/>
 * Describes one type of stat for a block.
 * */
public enum BlockStat{
    /** 生命*/
    health(StatCategory.general),
    /** 尺寸*/
    size(StatCategory.general),
    /** 建造时间*/
    buildTime(StatCategory.general),
    /** 建造消耗*/
    buildCost(StatCategory.general),

    /** 物品容量*/
    itemCapacity(StatCategory.items),
    /** 物品移动*/
    itemsMoved(StatCategory.items),
    /** 发射时间*/
    launchTime(StatCategory.items),

    /** 流体容量*/
    liquidCapacity(StatCategory.liquids),

    /** 电力容量*/
    powerCapacity(StatCategory.power),
    /** 电力使用*/
    powerUse(StatCategory.power),
    /** 电力伤害*/
    powerDamage(StatCategory.power),
    /** 电力范围*/
    powerRange(StatCategory.power),
    /** 电力连接数量*/
    powerConnections(StatCategory.power),
    /** 基础电力生产*/
    basePowerGeneration(StatCategory.power),

    /** 块*/
    tiles(StatCategory.crafting),
    /** 输入*/
    input(StatCategory.crafting),
    /** 输出*/
    output(StatCategory.crafting),
    /** 生产时间*/
    productionTime(StatCategory.crafting),
    /** 钻探层*/
    drillTier(StatCategory.crafting),
    /** 钻探速度*/
    drillSpeed(StatCategory.crafting),
    /** 最大单位数量*/
    maxUnits(StatCategory.crafting),

    /** 增长速度*/
    speedIncrease(StatCategory.shooting),
    /** 修理速度*/
    repairTime(StatCategory.shooting),
    /** 范围*/
    range(StatCategory.shooting),
    /** 射击范围*/
    shootRange(StatCategory.shooting),
    /** 误差*/
    inaccuracy(StatCategory.shooting),
    /** 射击数量*/
    shots(StatCategory.shooting),
    /** 装填*/
    reload(StatCategory.shooting),
    /** 电力射击*/
    powerShot(StatCategory.shooting),
    /** 攻击空中单位*/
    targetsAir(StatCategory.shooting),
    /** 攻击地面单位*/
    targetsGround(StatCategory.shooting),
    /** 伤害*/
    damage(StatCategory.shooting),
    /** 弹药*/
    ammo(StatCategory.shooting),

    /** 助推器*/
    booster(StatCategory.optional),
    /** 助推效果*/
    boostEffect(StatCategory.optional),
    /** 成功*/
    affinities(StatCategory.optional);

    public final StatCategory category;

    BlockStat(StatCategory category){
        this.category = category;
    }

    public String localized(){
        return Core.bundle.get("blocks." + name().toLowerCase(Locale.ROOT));
    }
}
