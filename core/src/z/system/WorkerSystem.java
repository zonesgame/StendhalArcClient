package z.system;

import arc.struct.Array;
import arc.util.Structs;
import mindustry.Vars;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.world.Tile;
import mindustry.world.meta.BlockFlag;
import z.entities.type.WorkerTileEntity;
import z.entities.type.base.BlockUnit;
import z.world.blocks.caesar.HousingIso;
import z.world.blocks.defense.WorkBlock;

import static mindustry.Vars.indexer;
import static mindustry.Vars.systemItems;
import static z.debug.ZDebug.forceAddItem;

/**
 *
 */
public class WorkerSystem {
    //工作优先级
    public static final int PRIORITY_1 = 0;
    public static final int PRIORITY_2 = 1;
    public static final int PRIORITY_3 = 2;
    public static final int PRIORITY_4 = 3;
    public static final int PRIORITY_5 = 4;

    /** 是否紧急状态*/
    private boolean stateOfEmergency = false;
    /** 当前工作优先级*/
    private int curPriority = PRIORITY_5;

    /** 农民数量*/
    public int peasant, peasantUse, peasantMax, peasantMaxNext;
    /** 工人数量*/
    public int worker, workerUse, workerMax, workerMaxNext;

    /** 最大工人数量*/
    public int maxCount;
    /** 当前工人数量*/
    public int curCount;
    /** 空闲工人数量*/
    public int idleCount;

    /** 工人死亡恢复速度*/
    public float recoveryRate;

//    public WorkerPool<BlockUnit> workerPool;

    public WorkerSystem() {
//        workerPool = new WorkerPool<BlockUnit>();
    }

    public void updateSystem() {
        {   // 工人数量更新
            peasantMax = peasantUse = peasantMaxNext;
            workerMax = workerUse = workerMaxNext;
            peasantMaxNext = workerMaxNext = 0;
        }

        if(stateOfEmergency) {
            stateOfEmergency = false;
            if(curPriority > PRIORITY_1) --curPriority;
        } else {
            if(curPriority < PRIORITY_5) ++curPriority;
        }
    }

    /** Block工作状态, 处理农民使用情况, Block是否可以执行工作*/
    public boolean processWorking(Tile tile) {
        WorkerTileEntity entity = tile.ent();
        int needPeasant = ((WorkBlock) tile.block()).needPeasants[entity.level()];
        if(getPeasantUse() >= needPeasant){
            if(entity.priority <= curPriority){
                peasantUse -= needPeasant;
                return true;
            }
        }else{
            stateOfEmergency = true;
        }
//        if(entity.priority <= curPriority && getPeasantUse() >= needPeasant) {
//            peasantUse -= needPeasant;
//            return true;
//        } else {
//            stateOfEmergency = true;
//        }
        return false;
    }

    /** Unit工作状态, 是否可以提供工作单位*/
    public void processWorker(Tile tile) {
        workerUse -= tile.<WorkerTileEntity>ent().useWorker;
    }

    /** 处理房屋提供工人和农民情况*/
    public void processOffer(Tile tile) {
        HousingIso block = (HousingIso) tile.block();
        HousingIso.HousingIsoEntity entity = tile.ent();

        peasantMaxNext += block.getOfferPeasant(entity);
        workerMaxNext += block.getOfferWorker(entity);
    }

    public boolean offerWorker(Tile tile) {
        WorkerTileEntity entity = tile.ent();

        int needWorker = ((WorkBlock) tile.block()).workerMultiplier[entity.level()];
        if(getWorkerUse() >= needWorker){
            if(entity.priority <= curPriority){
                workerUse -= needWorker;
                return true;
            }
        }

        return false;
    }

    private int getPeasantUse() {
        return peasantUse;
    }

    private int getWorkerUse() {
        return workerUse;
    }


    /** 获取运输存储的目标Tile*/
    public Tile getTransportTile(BlockUnit unit, ItemStack item) {
        Tile returnValue = null;

        if (systemItems.isFood(item.item)) {
            Array<Tile> granaryArray = indexer.getAllied(unit.getTeam(), BlockFlag.granary).asArray().sort(Structs.comparingFloat(t -> t.dst(unit.x, unit.y)));
            granaryArray = unit.getNoDisableTarget(granaryArray);
            returnValue = transportTileNode(granaryArray, item.item);
            if(returnValue != null)
                return returnValue;
        }   // 粮仓处理 end

        Array<Tile> warehouseArray = indexer.getAllied(unit.getTeam(), BlockFlag.warehouse).asArray().sort(Structs.comparingFloat(t -> t.dst(unit.x, unit.y)));
        warehouseArray = unit.getNoDisableTarget(warehouseArray);
        returnValue = transportTileNode(warehouseArray, item.item);

        return returnValue;
    }

    /** 获取得到物品的存储目标Tile*/
    public Tile getGettingTile(BlockUnit unit, ItemStack itemGet) {
        Tile returnValue = null;

        if (Vars.systemItems.isFood(itemGet.item)) {
            Array<Tile> granaryArray = indexer.getAllied(unit.getTeam(), BlockFlag.granary).asArray().sort(Structs.comparingFloat(t -> t.dst(unit.x, unit.y)));
            granaryArray = unit.getNoDisableTarget(granaryArray);
            returnValue = gettingTileNode(granaryArray, itemGet.item, itemGet.amount);
            if (returnValue != null)
                return  returnValue;
        }

        Array<Tile> warehouseArray = indexer.getAllied(unit.getTeam(), BlockFlag.warehouse).asArray().sort(Structs.comparingFloat(t -> t.dst(unit.x, unit.y)));
        warehouseArray = unit.getNoDisableTarget(warehouseArray);
        returnValue = gettingTileNode(warehouseArray, itemGet.item, itemGet.amount);

        return returnValue;    // 未找到拥有物品
    }


    private Tile transportTileNode(Array<Tile> arr, Item itemAccept) {
//        for (Tile node : arr) {
        for (int i = 0; i < arr.size; i++) {  // 配置获取物品瓦砾
            Tile node = arr.get(i);
            if (node.block().getItemCaesar(itemAccept, node, node))   // 根据配置<获取>目标
                return node;
        }
        for (int i = 0; i < arr.size; i++) {  // 配置接收物品瓦砾
            Tile node = arr.get(i);
            if (node.block().acceptItemCaesar(itemAccept, node, node))   // 根据配置<接收>目标
                return node;
        }
        if (forceAddItem) {
            for (int i = 0; i < arr.size; i++) {      // 强制添加物品到能接收的块
                Tile node = arr.get(i);
                if (node.block().acceptItem(itemAccept, node, node))   // 强制设置目标
                    return node;
            }
        } // 处理 end

        return null;
    }

    private Tile gettingTileNode(Array<Tile> arr, Item itemGet, int amount) {
        if (arr.size == 0)  return null;

        Tile returnTile = null;
//        int curAmount = 0;
//
//        for (int i = 0; i < arr.size; i++) {
////            for (Tile node : arr) {
//            Tile node = arr.get(i);
//            int itemAmount = node.ent().items.get(itemGet);
//            if (itemAmount >= amount)
//                return node;
//
//            if (itemAmount > curAmount) {
//                curAmount = itemAmount;
//                returnTile = node;
//            }
//        }

        for (int i = 0; i < arr.size; i++) {
            Tile node = arr.get(i);
            if (node.ent().items.get(itemGet) >= amount)
                return node;

            if (returnTile == null || node.ent().items.get(itemGet) > returnTile.ent().items.get(itemGet)) {
                returnTile = node;
            }
        }

        return returnTile.entity.items.get(itemGet) > 0 ? returnTile : null;
    }

}
