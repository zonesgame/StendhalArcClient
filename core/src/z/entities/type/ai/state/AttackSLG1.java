package z.entities.type.ai.state;

import arc.math.Angles;
import arc.math.geom.Vec2;
import mindustry.entities.Predict;
import mindustry.entities.Units;
import mindustry.entities.bullet.BulletType;
import mindustry.entities.units.UnitState;
import z.entities.type.base.MachineUnit;

/**
 *  攻击策略1
 */
public class AttackSLG1<E extends MachineUnit> implements UnitState<E> {

    /** 构建 单位自定义攻击状态*/
    public AttackSLG1() {
    }

    @Override
    public void enter(E unit) {
//        unit.target = null;     // 清空攻击目标
//        updateWeapon(unit);
//        System.out.println("enter attack...  " + unit.getTeam().name);
    }

    @Override
    public void exit(E unit) {
    }

    @Override
    public void update(E unit) {
        if (unit.defStrategy != MachineUnit.TeamStrategy.S_ATTACK
                && unit.type.weapon.getRecoil(unit, false) == 0 ) {     // 攻击状态更新完毕

            unit.setState(MachineUnit.idleSLG1);
            return;
        }


        if (true) {
            updateWeapon(unit);
            return;
        }

        if(!Units.invalidateTarget(unit.target, unit)){
            if(unit.dst(unit.target) < unit.getWeapon().bullet.range()){

                unit.rotate(unit.angleTo(unit.target));

                if(Angles.near(unit.angleTo(unit.target), unit.rotation, 13f)){
                    BulletType ammo = unit.getWeapon().bullet;

                    Vec2 to = Predict.intercept(unit, unit.target, ammo.speed);

                    unit.getWeapon().update(unit, to.x, to.y);
                }
            }

//            unit.squadMember.setUpdateLocation(true);
        }
    }

    private void updateWeapon(E unit) {
        if(!Units.invalidateTarget(unit.target, unit)){
            if(unit.dst(unit.target) < unit.getWeapon().bullet.range()){

                unit.rotate(unit.angleTo(unit.target));

                if(Angles.near(unit.angleTo(unit.target), unit.rotation, 13f)){
                    BulletType ammo = unit.getWeapon().bullet;

                    Vec2 to = Predict.intercept(unit, unit.target, ammo.speed);

                    unit.getWeapon().update(unit, to.x, to.y);
                }
            } else {
                unit.setState(MachineUnit.idleSLG1);
            }

//            unit.squadMember.setUpdateLocation(true);
        } else {
            unit.setState(MachineUnit.idleSLG1);
        }
    }

}
