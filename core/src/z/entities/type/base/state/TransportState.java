//package z.entities.type.base.state;
//
//import arc.math.Mathf;
//import arc.math.geom.Vec2;
//import arc.struct.Array;
//import arc.util.Time;
//import mindustry.entities.Units;
//import mindustry.entities.units.UnitState;
//import z.entities.type.WorkerTileEntity;
//import z.entities.type.base.BlockUnit;
//import z.utils.FinalCons;
//
///**
// *
// */
//public class TransportState implements UnitState {
//
//    private BlockUnit parentUnit;
//
//    public TransportState(BlockUnit unit) {
//        parentUnit = unit;
//    }
//
//    public void update() {
//        if (fail) {
//            {  // 路径寻找失败返还物品
//                dump(spawnerTile.ent());
//            }
//            if (spawnEntity != null && spawnEntity instanceof WorkerTileEntity)
//                ((WorkerTileEntity) spawnEntity).freeWorker(BlockUnit.this);
//            else
//                remove();
//            return;
//        }   // 对象回收入容器池
//        if (searchTarget) {
//            searchTarget = false;
//
//        }   // 重新探测移动目标
//
//        if (!initPath && aStarPath.getCount() > 0) {
//            initPath = true;
//            targets = new Array.ArrayIterable<>(aStarPath.nodes).iterator();
//            targets.next(); // consume src position
//            if (targets.hasNext()) {
//                curMoveTarget.set(targets.next().getPosition());
//            } else {
//                curMoveTarget.set(lastPosition());
//            }
//        }   // 初始化AStar路径
//
//        if (curMoveTarget.isZero()) return;
//
//        velocity.add(vec.trns(angleTo(curMoveTarget), type.speed * Time.delta()));
//        if (true || Units.invalidateTarget(target, BlockUnit.this)) {
//            rotation = Mathf.slerpDelta(rotation, baseRotation, type.rotatespeed);
//            aniControl.update();    // 更新动画
//        }
//
//        if (lastPosition().dst(curMoveTarget) < 0.05f) {
//            if (targets.hasNext()) {
//                curMoveTarget.set(targets.next().getPosition());
//            } else {
//                curMoveTarget.set(Vec2.ZERO);
//                dump(storageEntity);
//                Time.run(FinalCons.second * 1, () -> fail = true);
//            }   // 完成路径移动
//        }
//    }
//}
