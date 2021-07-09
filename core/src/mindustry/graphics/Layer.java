package mindustry.graphics;

public enum Layer{
    // zones add begon
    /** 背景图层*/
    background,
    // zones add end

    /** 基础块图层.<p/>Base block layer. */
    block,
    /** 放置图层.<p/>for placement */
    placement,
    /** 第一个叠加层. 诸如运输机之类的东西.<p/>First overlay. Stuff like conveyor items. */
    overlay,
    /** 炮塔图层.<p/>"High" blocks, like turrets. */
    turret,
    /** 电力图层.<p/>Power lasers. */
    power,
    /** 光源图层.<p/>Extra layer that's always on top.*/
    lights
}
