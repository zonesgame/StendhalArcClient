package mindustry.entities.traits;

import mindustry.entities.EntityGroup;

/**
 *  实体接口
 * */
public interface Entity extends MoveTrait{

    /** 获取ID*/
    int getID();

    /** 重置ID*/
    void resetID(int id);

    /** 更新*/
    default void update(){}

    /** 执行移除*/
    default void removed(){}

    /** update==true execute执行添加*/
    default void added(){}

    /** 目标群组*/
    EntityGroup targetGroup();

    /** update==true execute 添加*/
    @SuppressWarnings("unchecked")
    default void add(){
        if(targetGroup() != null){
            targetGroup().add(this);
        }
    }

    /** 移除*/
    @SuppressWarnings("unchecked")
    default void remove(){
        if(getGroup() != null){
            getGroup().remove(this);
        }

        setGroup(null);
    }

    /** 获取目标群组*/
    EntityGroup getGroup();

    /** 设置目标群组*/
    void setGroup(EntityGroup group);

    /** 实体是否添加入目标群组*/
    default boolean isAdded(){
        return getGroup() != null;
    }
}
