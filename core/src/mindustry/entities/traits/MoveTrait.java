package mindustry.entities.traits;

import arc.math.geom.Position;

/**
 *  移动属性. 坐标单位为瓦砾
 * */
public interface MoveTrait extends Position{

    /** 坐标单位tile*/
    void setX(float x);

    /** 坐标单位tile*/
    void setY(float y);

    /** 执行更新步长移动, 坐标单位tile*/
    default void moveBy(float x, float y){
        setX(getX() + x);
        setY(getY() + y);
    }

    /** 坐标单位tile*/
    default void set(float x, float y){
        setX(x);
        setY(y);
    }
}
