package mindustry.entities.traits;

import arc.util.Interval;
import mindustry.type.Weapon;

/**
 *  射击属性接口
 * */
public interface ShooterTrait extends VelocityTrait, TeamTrait{

    /** 射击间隔计时器*/
    Interval getTimer();

    /** 获取射击计时器索引. 左手或右手.*/
    int getShootTimer(boolean left);

    /** 获取武器*/
    Weapon getWeapon();
}
