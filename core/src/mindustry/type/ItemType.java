package mindustry.type;

/**
 *  物品的分类, 用于核心的是否接收.
 * */
public enum ItemType{
    /** Not used for anything besides crafting inside blocks. */
    resource,
    /** Can be used for constructing blocks. Only materials are accepted into the core. */
    material
}
