package mindustry.world.meta;

import arc.func.*;
import mindustry.*;

/**
 *  块显示状态
 * */
public enum BuildVisibility{
    /** 隐藏*/
    hidden(() -> false),
    /** 显示*/
    shown(() -> true),
    /** 调试*/
    debugOnly(() -> false),
    /** 沙盒*/
    sandboxOnly(() -> Vars.state.rules.infiniteResources),
    /** 竞技*/
    campaignOnly(() -> Vars.world.isZone()),
    /** 仅光源显示*/
    lightingOnly(() -> Vars.state.rules.lighting);

    /** 显示状态*/
    private final Boolp visible;

    /** 获取显示状态*/
    public boolean visible(){
        return visible.get();
    }

    BuildVisibility(Boolp visible){
        this.visible = visible;
    }
}
