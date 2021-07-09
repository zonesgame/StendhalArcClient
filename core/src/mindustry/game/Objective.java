package mindustry.game;

import arc.scene.ui.layout.*;
import arc.util.ArcAnnotate.*;
import mindustry.game.Objectives.*;
import mindustry.type.*;

/**
 *  为游戏定义一个特定的目标.<p/>
 * Defines a specific objective for a game.
 * */
public interface Objective{

    /** @return 这个目标是否满足. whether this objective is met. */
    boolean complete();

    /**
     * @return 当这个目标完成时, 返回命令界面显示内容.
     * 当目标是 "完成10波" 时, 这将显示" 完成10波"
     * 如果这个目标不应该显示, 应该返回null<p/>
     * the string displayed when this objective is completed, in imperative form.
     * e.g. when the objective is 'complete 10 waves', this would display "complete 10 waves".
     * If this objective should not be displayed, should return null.
     */
    @Nullable String display();

    /** 为该目标构建一个显示界面.<p/>Build a display for this zone requirement.*/
    default void build(Table table){

    }

    /** 获取目标绑定战役地图*/
    default Zone zone(){
        return this instanceof ZoneObjective ? ((ZoneObjective)this).zone : null;
    }
}
