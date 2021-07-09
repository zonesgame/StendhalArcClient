package z.world.blocks.defense;

import arc.Core;
import arc.func.Cons;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.struct.Array;
import arc.struct.ObjectMap;
import arc.z.util.ZonesAnnotate.ZAdd;
import arc.z.util.ZonesAnnotate.ZField;
import mindustry.entities.type.TileEntity;
import mindustry.graphics.Pal;
import mindustry.type.ItemStack;
import mindustry.ui.Bar;
import mindustry.world.Tile;
import z.debug.Strs;
import z.entities.type.WorkerTileEntity;
import z.world.blocks.OperationAction;

import static mindustry.Vars.systemWorker;

/**
 *  提供工作块
 * */
public class WorkBlock extends UpgradeBlock {

    /**  工作状态检测计时器*/
//    protected final int timerWorking = timers++;
    /** 工作状态检测时间*/
//    protected final int workingTime = (int) (FinalCons.second * 4);       // zones editor default final

    /** 每一个工作的精灵需要的worker单位数量, (默认值为1, 一个单位提供一个农民)*/
    @ZAdd
    public int[] workerMultiplier = {1};
    /** 需要农民数量*/
    @ZField
    public int[] needPeasants = {0};
    /**提供工人数量*/
    @ZField
    public int[] forWork = {0};
    /**携带生产物品最大数量*/
    @ZField
    public int[] carryItemDump = {1};
    /** 携带生产需要物品最大数量*/
    @ZField
    public int[] carryItemObtain = {1};

    /** 倾泻物品缓存池*/
    private Array<ItemStack> dumpItems = new Array<>(4);

    private TextureRegion sleepingIco, noWorkingIco;

    public WorkBlock(String name) {
        super(name);
    }

    @Override
    public void load() {
        super.load();   // 禁止加载先前无用资源
        sleepingIco = Core.atlas.find("upgrade5");
        noWorkingIco = Core.atlas.find("upgrade6");
    }

    @Override
    public void update(Tile tile){
        WorkerTileEntity entity = tile.ent();

        if( entity.setWorkState) {
            entity.working = systemWorker.processWorking(tile);
            systemWorker.processWorker(tile);      // 更新工人使用情况
        }
    }

//    public int needPeasant(int lev) {
//        return needPeasants[lev];
//    }

    @Override
    public void setBars(){
        super.setBars();

        bars.add("working", e -> {
            WorkerTileEntity entity = (WorkerTileEntity)e;
            return new Bar(
                    () -> Core.bundle.format(Strs.str23, !entity.setWorkState ? Strs.str25 : entity.working ? Strs.str24 : Strs.str20),     // 显示文本
                    () -> Pal.engine,   // 颜色
                    () -> !entity.setWorkState ? 0 : entity.working ? 1 : 0.5f);    // 进度条
        });
    }

    /** 绘制升级状态图标*/
    @Override
    public void drawLayer2(Tile tile) {
        super.drawLayer2(tile);

        // 绘制升级状态图标 begon
        WorkerTileEntity entity = tile.ent();
        if (entity == null) return;
        TextureRegion region;

        if ( !entity.setWorkState)
            region = sleepingIco;
        else if( !entity.working)
            region = noWorkingIco;
        else
            return;

        float dx = tile.drawxIso();
        float dy = tile.drawyIso();
        float width1 = region.getWidth() * Draw.scl * 2;
        float height1 = region.getHeight() * Draw.scl * 2;
        Draw.rectGdx(region, dx - width1 / 2f, dy - height1 / 2f + 10, width1, height1);
        // 绘制升级状态图标 end
    }

    @ZAdd
    @Override
    public <T extends OperationAction> ObjectMap<OperationAction, Cons<T>> getOperationActions(TileEntity tileEntity) {
        ObjectMap<OperationAction, Cons<T>> operationActions = super.getOperationActions(tileEntity);

        WorkerTileEntity entity = (WorkerTileEntity) tileEntity;
        if (entity.setWorkState) {  // 工作状态
            operationActions.put(OperationAction.REST, actionRest -> {
                entity.setWorkState = false;
            });
        } else {  // 休息状态
            operationActions.put(OperationAction.WORKING, actionRest -> {
                entity.setWorkState = true;
            });
        }

        return operationActions;
    }

    @ZAdd
    protected void attributeUpgrade(TileEntity entity) {
        super.attributeUpgrade(entity);
        ((WorkerTileEntity) entity).maxWorker = forWork[entity.copyLevelBy(1)];
    }

    /** 获取一个空的物品堆栈容器, 并不仅限于倾泻物品使用*/
    @ZAdd
    protected <T extends TileEntity> Array<ItemStack> getDumpItems(T entity) {
        dumpItems.clear();
        return dumpItems;
    }

}
