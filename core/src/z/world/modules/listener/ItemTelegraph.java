package z.world.modules.listener;

import mindustry.type.Item;
import mindustry.world.modules.ItemModule;

/**
 *
 */
public interface ItemTelegraph {

    /**
     * 移除物品监听器
     */
    public void removeTelegraph(Item item, int amount);

//    default void remove(ItemStack stack){
//    }


    /**
     * 添加物品监听器
     */
    public void addTelegraph(Item item, int amount);

    default void addAllTelegraph(ItemModule items) {
    }
}
