package z.system;

import arc.Events;
import arc.struct.Array;
import mindustry.content.Items;
import mindustry.game.EventType;
import mindustry.type.Item;
import mindustry.world.modules.ItemModule;
import z.world.modules.listener.CaesarItemTelegraph;
import z.world.modules.listener.ItemTelegraph;

/**
 *
 */
public class ItemsSystem {

    /** 粮仓接收物品类型*/
    private Array<Item> acceptTypeGranary;
    /** 仓库接收物品类型*/
    private Array<Item> acceptTypeWarehouse;
    /** 市场接收物品类型*/
//    private Array<Item> acceptTypeMarket;
    /** 游戏使用的所有接收物品类型*/
    private Array<Item> acceptTypeAll;

    /** 玩家物品容器, 依附于CoreBlockEntity*/
    public /*final*/ ItemModule allItems;
    /** 存放缓存物品(无处存放物品or核心物品)*/
    public /*final*/ ItemModule cacheItems;
    /** 粮仓物品*/
    public /*final*/ ItemModule granaryItems;
    /** 仓库物品*/
    public /*final*/ ItemModule warehouseItems;

    /** 仓库, 粮仓物品添加移除的监听器*/
    public /*final*/ ItemTelegraph allItemTelegraph, /** 核心物品监听器*/coreItemTelegraph, /** 粮仓物品监听器*/granaryItemTelegraph, /** 仓库物品监听器*/warehouseItemTelegraph;

    public ItemsSystem() {

        Events.on(EventType.ClearCacheEvent.class, event -> init());
    }

    private void init() {
        allItems = new ItemModule();
        cacheItems = new ItemModule();
        granaryItems = new ItemModule();
        warehouseItems = new ItemModule();

        allItemTelegraph = new CaesarItemTelegraph(allItems);
        coreItemTelegraph = new CaesarItemTelegraph(cacheItems);
        granaryItemTelegraph = new CaesarItemTelegraph(granaryItems);
        warehouseItemTelegraph = new CaesarItemTelegraph(warehouseItems);
    }

    public boolean isFood(Item item) {
        return item == null ? false : getGranaryAccept().contains(item);
    }

    public Array<Item> getAllAccept(){
        if(acceptTypeAll == null) {
            acceptTypeAll = new Array<>(getWarehouseAccept());
        }

        return acceptTypeAll;
    }

    public Array<Item> getWarehouseAccept(){
        if(acceptTypeWarehouse == null) {
            acceptTypeWarehouse = Array.withArrays(
                    // 农场物品
                    Items.wheat, Items.vegetables, Items.fruit, Items.olives, Items.vines, Items.meat,
                    Items.wine, Items.oil, Items.iron, Items.timber, Items.clay, Items.marble, Items.weapons, Items.furniture, Items.pottery, Items.fish
            );
        }

        return acceptTypeWarehouse;
    }

    public Array<Item> getGranaryAccept(){
        if(acceptTypeGranary == null){
            acceptTypeGranary = Array.withArrays(Items.wheat, Items.vegetables, Items.fruit, Items.meat, Items.fish);
        }

        return acceptTypeGranary;
    }

//    public Array<Item> getMarketAccept(){
//        if(acceptTypeMarket == null) {
//            acceptTypeMarket = Array.withArrays(
//                    // 农场物品
//                    Items.wheat, Items.vegetables, Items.fruit, Items.olives, Items.vines, Items.meat,
//                    Items.wine, Items.oil, Items.iron, Items.timber, Items.clay, Items.marble, Items.weapons, Items.furniture, Items.pottery, Items.fish
//            );
//        }
//
//        return acceptTypeMarket;
//    }

}
