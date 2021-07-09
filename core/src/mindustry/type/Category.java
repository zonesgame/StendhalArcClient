package mindustry.type;

/**
 *  UI界面分类菜单
 * */
public enum Category{
    /** 炮塔块.<p/>Offensive turrets. */
    turret,
    /** 矿机块.<p/>Blocks that produce raw resources, such as drills. */
    production,
    /** 运输块.<p/>Blocks that move items around. */
    distribution,
    /** 流体块.<p/>Blocks that move liquids around. */
    liquid,
    /** 电力块.<p/>Blocks that generate or transport power. */
    power,
    /** 防御块.<p/>Walls and other defensive structures. */
    defense,
    /** 工厂块.<p/>Blocks that craft things. */
    crafting,
    /** 单位块.<p/>Blocks that create units. */
    units,
    /** 机甲升级块.<p/>Things that upgrade the player such as mech pads. */
    upgrade,
    /** 储存或被动效应的块.<p/>Things for storage or passive effects. */
    effect;

    public static final Category[] all = values();

    /** 上一个种类*/
    public Category prev(){
        return all[(this.ordinal() - 1 + all.length) % all.length];
    }

    /** 下一个种类*/
    public Category next(){
        return all[(this.ordinal() + 1) % all.length];
    }
}
