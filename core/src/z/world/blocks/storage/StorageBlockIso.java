package z.world.blocks.storage;

import arc.util.ArcAnnotate.Nullable;
import mindustry.entities.type.TileEntity;
import mindustry.type.Item;
import mindustry.world.Tile;
import z.entities.type.WorkerTileEntity;
import z.world.blocks.defense.WorkBlock;

/**
 *  仓库 块
 * */
public abstract class StorageBlockIso extends WorkBlock {        // default Block

    public StorageBlockIso(String name){
        super(name);
        hasItems = true;
//        entityType = StorageBlockIsoEntity::new;
    }

    @Override
    public boolean acceptItem(Item item, Tile tile, Tile source){
        StorageBlockIsoEntity entity = tile.ent();
        return entity.linkedCore != null ? entity.linkedCore.block().acceptItem(item, entity.linkedCore, source) : tile.entity.items.get(item) < getMaximumAccepted(tile, item);
    }

    @Override
    public int getMaximumAccepted(Tile tile, Item item){
        return itemCapacity[tile.entity.level()];
    }

    @Override
    public void drawSelect(Tile tile){
        StorageBlockIsoEntity entity = tile.ent();
        if(entity.linkedCore != null){
            entity.linkedCore.block().drawSelect(entity.linkedCore);
        }
    }

    @Override
    public boolean outputsItems(){
        return false;
    }

    /**
     *  移除一个物品并返回它. 如果物品不为null,则应该返回该物品. 如果没有指定物品, 返回null.<p/>
     * Removes an item and returns it. If item is not null, it should return the item.
     * Returns null if no items are there.
     */
    public Item removeItem(Tile tile, Item item){
        TileEntity entity = tile.entity;

        if(item == null){
            return entity.items.take();
        }else{
            if(entity.items.has(item)){
                entity.items.remove(item, 1);
                return item;
            }

            return null;
        }
    }

    /**
     *  返回这个存储块是否有指定的物品. 如果指定物品为null, 检测容器是否为空.<p/>
     * Returns whether this storage block has the specified item.
     * If the item is null, it should return whether it has ANY items.
     */
    public boolean hasItem(Tile tile, Item item){
        TileEntity entity = tile.entity;
        if(item == null){
            return entity.items.total() > 0;
        }else{
            return entity.items.has(item);
        }
    }


    /**
     *  存储块 瓦砾实体
     * */
    public abstract class StorageBlockIsoEntity extends WorkerTileEntity {         // default TileEntity
        /** 绑定核心*/
        protected @Nullable
        Tile linkedCore;

        /** 接收物品数量*/
        public abstract int acceptCount(Item item, int amount);
//        @ZonesAnnotate.ZonesAdd
//        public void addItem(Item item, int amount) {
//            this.items.add(item, amount);
//        }
//
//        @ZonesAnnotate.ZonesAdd
//        public void removeItem(Item item, int amount) {
//            this.items.remove(item, amount);
//        }
    }
}
