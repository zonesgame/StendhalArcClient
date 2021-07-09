package mindustry.entities.traits;

import arc.math.Mathf;

public interface HealthTrait{

    /** 设置生命为指定值*/
    void health(float health);

    /** 当前生命值*/
    float health();

    /** 最大生命值*/
    float maxHealth();

    boolean isDead();

    void setDead(boolean dead);

    default void kill(){
        health(-1);
        damage(1);
    }

    /** 应用撞击*/
    default void onHit(SolidTrait entity){
    }

    /** 应用死亡*/
    default void onDeath(){
    }

    /** 是否损坏状态*/
    default boolean damaged(){
        return health() < maxHealth() - 0.0001f;
    }

    default void damage(float amount){
        health(health() - amount);
        if(health() <= 0 && !isDead()){
            onDeath();
            setDead(true);
        }
    }

    default void clampHealth(){
        health(Mathf.clamp(health(), 0, maxHealth()));
    }

    /** 生命值百分比*/
    default float healthf(){
        return health() / maxHealth();
    }

    default void healBy(float amount){
        health(health() + amount);
        clampHealth();
    }

    /** 复活*/
    default void heal(){
        health(maxHealth());
        setDead(false);
    }
}
