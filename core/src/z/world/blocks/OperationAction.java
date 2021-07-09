package z.world.blocks;

import arc.graphics.Color;
import z.debug.Strs;

/**
 *
 */
public enum OperationAction {
    /** 移动*/
    MOVE( 0, Strs.str1),      // "UIRes_857"
    /** 升级*/
    UPGRADE( 1, Strs.str2),       // "UIRes_859"
    /** 取消升级*/
    UNUPGRADE( 1, Strs.str2),       // "UIRes_859"
    /** 立即完成*/
    ACCOMPLISH( 2, Strs.str8),
    /** 配置*/
    SETTING(3, Strs.str9),
    /** 描述扩展信息*/
    EXTENDEDINFORMATION(-1, null),
    /** 工作*/
    WORKING(4, Strs.str18),
    /** 休息*/
    REST(5, Strs.str19),
    ;

    private OperationAction(int iconame, String displayname) {
        this.icoindex = iconame;
        this.displayName = displayname;
    }

    public int icoindex;
    public String displayName;
    public Color nameColor = Color.white;
    private int typeBg = 0;

}
