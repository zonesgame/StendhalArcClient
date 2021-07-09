package mindustry.entities.traits;

import arc.math.geom.Vec2;
import arc.util.Time;

/**
 *  速度属性处理
 * */
public interface VelocityTrait extends MoveTrait{

    /** 速度*/
    Vec2 velocity();

    /** 应用作用力*/
    default void applyImpulse(float x, float y){
        velocity().x += x / mass();
        velocity().y += y / mass();
    }

    /** 最大速度*/
    default float maxVelocity(){
        return Float.MAX_VALUE;
    }

    /** 质量*/
    default float mass(){
        return 1f;
    }

    /** 拖累作用力*/
    default float drag(){
        return 0f;
    }

    /** 更新速度*/
    default void updateVelocity(){
        velocity().scl(1f - drag() * Time.delta());

        if(this instanceof SolidTrait){
            ((SolidTrait)this).move(velocity().x * Time.delta(), velocity().y * Time.delta());
        }else{
            moveBy(velocity().x * Time.delta(), velocity().y * Time.delta());
        }
    }
}
