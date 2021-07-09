package z.entities.traits;

import arc.math.Mathf;

/**
 *  单位和块的等级数据
 * */
public interface LevelTrait{

    /** 设置等级为指定值*/
//    void level(int health);
    default void level(int lev) {        // develop
    }

    /** 当前等级*/
//    int level();
    default int level() {   // develop
        return 0;
    }

    /** 最大等级*/
//    int maxLevel();
    default int maxLevel() {    // develop
        return 0;
    }

    /** 索引等级*/
//    default int levelIndex() {
//        return level() -1;
//    }

    /** 是否最大等级*/
    default boolean isLevelFull() {
        return level() == maxLevel();
    }

    /** 等级值百分比*/
    default float levelf(){
        return (level()+1) / (maxLevel() + 1f);
    }

    default void clampLevel(){
        level(Mathf.clamp(level(), 0, maxLevel()));
    }

    default void levelBy(int amount){
        level(level() + amount);
        clampLevel();
    }

    /**不修改当前level*/
    default int copyLevelBy(int amount){
        return Mathf.clamp(level() + amount, 0, maxLevel());
    }

}
