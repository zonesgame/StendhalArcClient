package z.world.blocks.storage;

import arc.struct.Array;
import arc.z.util.ZonesAnnotate.ZAdd;
import mindustry.Vars;
import mindustry.content.UnitTypes;
import mindustry.entities.type.TileEntity;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.type.UnitType;
import mindustry.world.Block;
import mindustry.world.Edges;
import mindustry.world.Tile;
import z.entities.type.WorkerTileEntity;
import z.entities.type.base.BlockUnit;
import z.utils.FinalCons;
import z.world.blocks.distribution.ConveyorIso;

import static z.entities.type.base.BlockUnit.WorkerType.GETTING;
import static z.entities.type.base.BlockUnit.WorkerType.PATROL;
import static z.entities.type.base.BlockUnit.WorkerType.TRANSPORT;

/**
 *  Caesar继承扩展块, 提供Worker支持功能.
 */
public class BlockIso extends Block {
    /** 工人工作计时器ID.<p/>Dump timer ID.*/
    protected final int timerWorker = timers++;
    /** 工作时间5 = 12 times/sec.<p/>How often to try dumping items in ticks, e.g. 5 = 12 times/sec*/
    protected final int workerTime = (int) (FinalCons.second * 8);       // zones editor default final

    public BlockIso(String name) {
        super(name);
    }

    /** 尝试巡逻工作*/
    @ZAdd
    public boolean tryPatrol(Tile tile) {
        TileEntity entityTemp = tile.entity;
        if(entityTemp == null) return false;
        if ( !(entityTemp instanceof WorkerTileEntity)) return false;

        WorkerTileEntity entity = (WorkerTileEntity) entityTemp;

        Array<Tile> proximity = entity.proximity();
        int dump = tile.rotation();

        if(proximity.size == 0) return false;

        for(int i = 0; i < proximity.size; i++){
            Tile other = proximity.get((i + dump) % proximity.size);
            Tile in = Edges.getFacingEdge(tile, other);

            // zones add begon
            if(other.getTeam() == tile.getTeam() && (other.block() instanceof ConveyorIso)){
                // temp code begon
                BlockUnit unit = entity.getWorker(UnitTypes.worker, Vars.player.getTeam());
                unit.setSpawner(other);
                unit.set(other.getX(), other.getY());
//                unit.add();
                unit.obtain(PATROL, entity, null);
                incrementDump(tile, proximity.size);
                return true;
            }
            // zones add end

            incrementDump(tile, proximity.size);
        }

        return false;
    }

    /** 工人尝试去获取物品*/
    @ZAdd
    public boolean tryObtain(Tile tile, Array<ItemStack> itemStacks) {
        WorkerTileEntity entity = tile.ent();
        if(entity == null) return false;

        if(itemStacks == null || itemStacks.size == 0) return false;

        Array<Tile> proximity = entity.proximity();
        int dump = tile.rotation();

        if(proximity.size == 0) return false;

        for(int i = 0; i < proximity.size; i++){
            Tile other = proximity.get((i + dump) % proximity.size);
            Tile in = Edges.getFacingEdge(tile, other);

            // zones add begon
            if(other.getTeam() == tile.getTeam() && (other.block() instanceof ConveyorIso)){
                // temp code begon
//                BlockUnit unit = (BlockUnit) UnitTypes.worker.create(Vars.player.getTeam());
                BlockUnit unit = entity.getWorker(UnitTypes.worker, Vars.player.getTeam());
                unit.setSpawner(other);
                unit.set(other.getX(), other.getY());
//                unit.add();
                unit.obtain(GETTING, entity, itemStacks);
                incrementDump(tile, proximity.size);
                return true;
            }
            // zones add end

            incrementDump(tile, proximity.size);
        }

        return false;
    }

    /** 工人尝试去获取物品*/
//    @ZAdd
//    public boolean tryObtain(Tile tile) {
//        return tryObtain(tile, tile.ent() == null ? null : tile.ent().cons.getConsume());
//    }

    /**
     * 尝试在瓦砾附近倾泻特定物品.<p/>Try dumping a specific item near the tile.
     * @param itemStacks Item to dump. Can be null to dump anything.
     */
    @ZAdd
    public boolean tryDump(Tile tile, Array<ItemStack> itemStacks){
//        TileEntity entity = tile.entity;
        WorkerTileEntity entity = tile.ent();
        if(entity == null || !hasItems || tile.entity.items.total() == 0 || (itemStacks != null && !entity.items.has(itemStacks)))
            return false;

        Array<Tile> proximity = entity.proximity();
        int dump = tile.rotation();

        if(proximity.size == 0) return false;

        for(int i = 0; i < proximity.size; i++){
            Tile other = proximity.get((i + dump) % proximity.size);
            Tile in = Edges.getFacingEdge(tile, other);

            if(other.getTeam() == tile.getTeam() && (other.block() instanceof ConveyorIso)){
                // temp code begon
                BlockUnit unit = entity.getWorker(UnitTypes.worker, Vars.player.getTeam());
                unit.setSpawner(other);
                unit.set(other.getX(), other.getY());
//                unit.addItem(dumpItem, amount);
                unit.obtain(TRANSPORT, entity, itemStacks);
//                    tile.entity.items.remove(todump, count);
                incrementDump(tile, proximity.size);
                return true;
            }

            incrementDump(tile, proximity.size);
        }

        return false;
    }

    /**
     * 尝试在瓦砾附近倾泻特定物品.<p/>Try dumping a specific item near the tile.
     * @param todump Item to dump. Can be null to dump anything.
     */
//    @Override
//    public boolean tryDump(Tile tile, Item todump){
//        return tryDump(tile, todump, 1);
//    }

    @Override
    /** 如果没有可用的, 试着把这个物品放到一个附近的容器里. 容器, 它被添加到块的库存中.<p/>
     * Tries to put this item into a nearby container, if there are no available
     * containers, it gets added to the block's inventory.
     */
    public void offloadNear(Tile tile, Item item){
        handleItem(item, tile, tile);
    }

    @ZAdd
    public void offloadNear(Tile tile, Item item, int count){
        handleStack(item, count, tile, null);
    }





    /** 工人尝试去获取物品*/
    @ZAdd
    public boolean tryObtain(Tile tile, Array<ItemStack> itemStacks, UnitType unitType) {
        WorkerTileEntity entity = tile.ent();
        if(entity == null) return false;

        if(itemStacks == null || itemStacks.size == 0) return false;

        Array<Tile> proximity = entity.proximity();
        int dump = tile.rotation();

        if(proximity.size == 0) return false;

        for(int i = 0; i < proximity.size; i++){
            Tile other = proximity.get((i + dump) % proximity.size);
            Tile in = Edges.getFacingEdge(tile, other);

            // zones add begon
            if(other.getTeam() == tile.getTeam() && (other.block() instanceof ConveyorIso)){
                // temp code begon
//                BlockUnit unit = (BlockUnit) UnitTypes.worker.create(Vars.player.getTeam());
                BlockUnit unit = entity.getWorker(unitType, Vars.player.getTeam());
                unit.setSpawner(other);
                unit.set(other.getX(), other.getY());
//                unit.add();
                unit.obtain(GETTING, entity, itemStacks);
                incrementDump(tile, proximity.size);
                return true;
            }
            // zones add end

            incrementDump(tile, proximity.size);
        }

        return false;
    }


}
