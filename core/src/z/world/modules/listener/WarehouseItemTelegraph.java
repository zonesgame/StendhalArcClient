package z.world.modules.listener;

import mindustry.type.Item;
import mindustry.type.ItemStack;
import z.world.blocks.storage.WarehouseIso.WarehouseIsoEntity;

/**
 *
 */
public class WarehouseItemTelegraph implements ItemTelegraph {

    private final WarehouseIsoEntity entity;

    public WarehouseItemTelegraph(WarehouseIsoEntity warehouseEntity) {
        this.entity = warehouseEntity;
    }

    @Override
    public void removeTelegraph(Item item, int amount) {
        int removeCount = amount;
        for (int i = entity.warehouseContent.length; --i >= 0;) {
            ItemStack itemStack = entity.warehouseContent[i];
            if (itemStack.item == item && itemStack.amount > 0) {
                int subValue = Math.min(removeCount, itemStack.amount);
                itemStack.amount -= subValue;
                removeCount -= subValue;
                if (removeCount == 0)
                    return;
            }
        }
    }

    @Override
    public void addTelegraph(Item item, int amount) {
        int maxAcceptSolt = entity.getMaximumAcceptedWarehouseSolt();
        int addCount = amount;

        for (ItemStack itemStack : entity.warehouseContent) {
            if (itemStack.amount == 0
                    || (itemStack.item == item && itemStack.amount < maxAcceptSolt) ) {

                int removeCount = Math.min(maxAcceptSolt - itemStack.amount, addCount);
                itemStack.item = item;
                itemStack.amount += removeCount;
                addCount -= removeCount;
                if (addCount == 0)
                    return;
            }
        }
        // 仓库已满强制添加物品
        for (ItemStack itemStack : entity.warehouseContent) {
            if (itemStack.item == item) {
                itemStack.item = item;
                itemStack.amount += addCount;
                return;
            }
        }
    }

//    @Override
//    public void addAllTelegraph(ItemModule itemModule) {
//    }

}
