package z.entities.type.ai.state;

import mindustry.entities.Units;
import mindustry.entities.type.TileEntity;
import mindustry.entities.type.Unit;
import mindustry.entities.units.UnitState;
import z.entities.type.base.MachineUnit;

/**
 * 策略组1, 状态检测分配处理
 */
public class GlobalSLG1<E extends MachineUnit> implements UnitState<E> {

    public GlobalSLG1() {
    }

    @Override
    public void enter(E unit) {
    }

    @Override
    public void exit(E unit) {
    }

    @Override
    public void update(E unit) {
        {   // 更新单位攻击目标
            {   // super function
                if(unit.target == null || (unit.target instanceof Unit && (unit.target.isDead() || unit.target.getTeam() == unit.getTeam()))
                        || (unit.target instanceof TileEntity && ((TileEntity)unit.target).tile.entity == null)){
                    unit.target = null;
                }
            }

            if(Units.invalidateTarget(unit.target, unit.getTeam(), unit.x, unit.y, Float.MAX_VALUE)){
                unit.target = null;
            }

            if(unit.retarget()){
                unit.targetClosest();
            }
        }

        {     // 伤害检测处理

        }

        {   //  Attack状态切换检测
            if(!Units.invalidateTarget(unit.target, unit)){
                if(unit.dst(unit.target) < unit.getWeapon().bullet.range()){
                    {
                        testAttackStateLogic(unit);
                    }
                }
//                else {
//                    unit.squadMember.setUpdateLocation(true);
//                    unit.setState(MachineUnit.moveSLG1);
//                }

//                unit.squadMember.setUpdateLocation(true);
            }
        }
    }

    private void testAttackStateLogic(E unit) {
        if (unit.defStrategy == MachineUnit.TeamStrategy.S_ATTACK) {
//            System.out.println("attack state     " + unit.getTeam().name);
            unit.setState(MachineUnit.attackSLG1);
        }
//        else if (unit.defStrategy == MachineUnit.TeamStrategy.S_RETREAT) {
////            System.out.println("retreat  state   " + unit.getTeam().name);
//            unit.setState(MachineUnit.moveSLG1);
//        }
//        else if (unit.getStrategy() == MachineUnit.TeamStrategy.S_IDEL) {
////            System.out.println("idle state     " + unit.getTeam().name);
//            unit.setState(MachineUnit.idleSLG1);
//        }

//        if(!Units.invalidateTarget(unit.target, unit)){
//            if(unit.dst(unit.target) < unit.getWeapon().bullet.range()){
//
//                unit.rotate(unit.angleTo(unit.target));
//
//                if(Angles.near(unit.angleTo(unit.target), unit.rotation, 13f)){
//                    BulletType ammo = unit.getWeapon().bullet;
//
//                    Vec2 to = Predict.intercept(unit, unit.target, ammo.speed);
//
//                    unit.getWeapon().update(unit, to.x, to.y);
//                }
//            }
//
////            unit.squadMember.setUpdateLocation(true);
//        }
    }

}