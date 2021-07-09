package z.entities.type.ai.state;

import mindustry.Vars;
import mindustry.ai.Pathfinder;
import mindustry.entities.type.TileEntity;
import mindustry.entities.units.UnitState;
import mindustry.world.Tile;
import z.entities.type.base.BaseGroundUnit;

/**
 *  地面单位自定义攻击状态
 */
public class AttackCustomState<E extends BaseGroundUnit> implements UnitState<E> {

    /** 状态unit*/
    private E unit;

    /** 构建 单位自定义攻击状态*/
    public AttackCustomState(E parent) {
        this.unit = parent;
    }

    @Override
    public void enter(E u) {
        unit.target = null;     // 清空攻击目标
    }

    @Override
    public void exit(E u) {
    }

    @Override
    public void update(E u) {
        TileEntity core = unit.getClosestEnemyCore();       // 获取最近敌方核心

        if (unit.type.weapon.getRecoil(unit, false) != 0) {       // attack中禁止移动
//                continue;
        }
        //  非攻击状态处理
        else if (core == null) {    //  移动到敌方重生点. player创建单位ai
            Tile closestSpawn = unit.getClosestSpawner();
            if (closestSpawn == null || !unit.withinDst(closestSpawn, Vars.state.rules.dropZoneRadius + 85f)) {
                unit.moveToCore(Pathfinder.PathTarget.enemyCores);
            }
        }
        else {    //  移动到敌方邻近核心, 回合spawn单位ai
            float dst = unit.dst(core);

            if (dst < unit.getWeapon().bullet.range() / 1.1f) {
                unit.target = core;
            }

            if (dst > unit.getWeapon().bullet.range() * 0.5f) {
                unit.moveToCore(Pathfinder.PathTarget.enemyCores);
            }
        }
    }

}
