package mindustry.entities.traits;

import arc.math.geom.Position;
import mindustry.game.Team;

/**
 *  目标属性接口.<p/>
 * Base interface for targetable entities.
 */
public interface TargetTrait extends Position, VelocityTrait{

    /** 死亡状态*/
    boolean isDead();

    /** 目标队伍*/
    Team getTeam();

    /** 目标X轴速度*/
    default float getTargetVelocityX(){
        if(this instanceof SolidTrait){
            return ((SolidTrait)this).getDeltaX();
        }
        return velocity().x;
    }

    /** 目标Y轴速度*/
    default float getTargetVelocityY(){
        if(this instanceof SolidTrait){
            return ((SolidTrait)this).getDeltaY();
        }
        return velocity().y;
    }

    /**
     * 实体是否为有效目标.<p/>Whether this entity is a valid target.
     */
    default boolean isValid(){
        return !isDead();
    }
}
