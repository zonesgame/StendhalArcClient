package mindustry.world.meta;

/**
 *  路径标志.<p/>
 *  用于存储块block的标志flag<p/>
 * Stores special flags of blocks for easy querying.
 * */
public enum BlockFlag{
    /** 核心<p/>Enemy core; primary target for all units. */
    core,
    /** 集结点<p/>Rally point for units.*/
    rally,
    // zones add begon  ----
    /** 单位进攻目标点*/
//    waveRally,
    // zones add end  ----
    /** 制造工厂<p/>Producer of important goods. */
    producer,
    /** 炮塔<p/>A turret. */
    turret,
    /** 命令中心<p/>Only the command center block.*/
    comandCenter,
    /** 维修点<p/>Repair point. */
    repair,
    /** 机甲升级平台<p/>Upgrade pad. */
    mechPad,
    // zones add begon
    /** 粮仓*/
    granary,
    /** 仓库*/
    warehouse,
    // zones add end
    ;

    public final static BlockFlag[] all = values();
}
