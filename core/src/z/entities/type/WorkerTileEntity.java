package z.entities.type;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import arc.util.ArcRuntimeException;
import mindustry.Vars;
import mindustry.entities.type.TileEntity;
import mindustry.game.Team;
import mindustry.type.UnitType;
import mindustry.world.Block;
import mindustry.world.Tile;
import z.entities.type.base.BlockUnit;
import z.system.WorkerSystem;
import z.world.blocks.defense.WorkBlock;

/**
 *
 */
public class WorkerTileEntity<B extends Block> extends UpgradeTileEntity {
    /** 工作优先级*/
    public int priority = WorkerSystem.PRIORITY_1;
    /** UI中设置工人的工作状态*/
    public boolean setWorkState = true;

    /** Block状态, 当前工作状态*/
    public boolean working;

    /** Unit状态, 倾泻物品状态*/
    public boolean dumping;
    /** Unit状态, 获取生产物品状态*/
    public boolean obtaining;
    /** Unit状态, 巡逻状态*/
    public boolean patroling;

    public int maxWorker = 0;
    public int useWorker = 0;
//    protected Array<BlockUnit> workerArr = new Array<>(maxWorker);
    /** 绑定移动目标块*/
    protected B moveTargetBlock;

    @Override
    public TileEntity init(Tile tile, boolean shouldAdd) {
        super.init(tile, shouldAdd);

        if(block instanceof WorkBlock)
            this.maxWorker = ((WorkBlock) block).forWork[level()];
        return this;
    }

    /** 是否可以提供工作工人*/
    public boolean offerWorker() {
        return useWorker < maxWorker   // !Block到达最大工作单位
                && Vars.systemWorker.offerWorker(tile);    //  !工人单位不足
    }

    public BlockUnit getWorker(UnitType unitType, Team team) {
//        if ( !offerWorker()) return null;   // Block到达最大工作单位
//        if ( !Vars.systemWorker.offerWorker(tile)) return null;      // 工人单位不足
        if (true && !offerWorker()) // debug
            throw new ArcRuntimeException("WorkerTileEntity runtimeexception getWorker()!");

        BlockUnit unit = (BlockUnit) unitType.create(team);
        unit.add();

        useWorker += ((WorkBlock) block).workerMultiplier[level()];
        return unit;
    }

    /** 将工人返还缓存池*/
    public void freeWorker(BlockUnit unit) {
        unit.remove();

        useWorker -= Math.min(useWorker, ((WorkBlock) block).workerMultiplier[level()]);
    }


    /** 从缓存池获取一个工人*/
//    public BlockUnit getWorker() {
//        this.working = true;
//        BlockUnit unit = Vars.systemWorker.workerPool.obtain();
//        workerArr.add(unit);
//        unit.add();
//        ++useWorker;
//        return unit;
//    }

//    /** 将工人返还缓存池*/
//    public void freeWorker(BlockUnit unit) {
//        this.working = false;
////        Vars.systemWorker.workerPool.free(unit);
//        workerArr.remove(unit);
//        --useWorker;
//        unit.remove();
//    }

    // test new function begon
//    public BlockUnit getWorker(UnitType unitType) {
//        this.working = true;
//        BlockUnit unit = (BlockUnit) unitType.create(Vars.player.getTeam());
//        workerArr.add(unit);
//        unit.add();
//        ++useWorker;
//        return unit;
//    }
    // test new function end

    @Override
    public void write(DataOutput stream) throws IOException {
        super.write(stream);
    }

    @Override
    public void read(DataInput stream, byte revision) throws IOException {
        super.read(stream, revision);
    }
}
