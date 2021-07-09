package z.world.modules.listener;

import mindustry.type.Item;
import mindustry.world.modules.ItemModule;

/**
 *
 */
public class CaesarItemTelegraph implements ItemTelegraph {

    private final ItemModule items;

    public CaesarItemTelegraph(ItemModule itemModule) {
        this.items = itemModule;
    }

    @Override
    public void removeTelegraph(Item item, int amount) {
        items.remove(item, amount);
    }

    @Override
    public void addTelegraph(Item item, int amount) {
        items.add(item, amount);
    }

    @Override
    public void addAllTelegraph(ItemModule items){
        items.addAll(items);
    }
}
