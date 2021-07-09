package z.entities.type.ai.state;

import arc.math.geom.Vec2;
import mindustry.entities.units.UnitState;
import z.ai.utils.SquadMember;
import z.entities.type.base.MachineUnit;

import static mindustry.Vars.tilesize;

/**
 * 策略组1, 状态检测分配处理
 */
public class IdleSLG1<E extends MachineUnit> implements UnitState<E> {

    /** 状态unit*/
//    private E unit;

    /** 构建 单位自定义攻击状态*/
    public IdleSLG1() {
//        this.unit = parent;
    }

    @Override
    public void enter(E unit) {
//        unit.target = null;     // 清空攻击目标
//        Log.info("enter IDLE state");
    }

    @Override
    public void exit(E unit) {
    }

    @Override
    public void update(E unit) {
        if (true) {     // 新状态测试
            SquadMember memberSquad = unit.squadMember;
            if (memberSquad.updateLocation()) {
                unit.setState(MachineUnit.moveSLG1);
            }

            return;
        }


        if (unit.type.weapon.getRecoil(unit, false) != 0) {       // attack中禁止移动
        }

        // 非攻击状态
        else {  // 移动到队伍目标
            SquadMember memberSquad = unit.squadMember;
            Vec2 target = memberSquad.getPosition();

            if (memberSquad.updateLocation() && unit.dst(target) > 4f / 4f / tilesize) {
                unit.moveTo(memberSquad.squadID, memberSquad.memberID);
                if (true) {
//                        Time.run(30, ()->{
                    if (unit.velocity().len() < 0.05f / tilesize) {
//                                setAniState(SGGroundUnit.State.IDLE);
                        memberSquad.updateLocationDelta();
                    }
//                        });
                }
            } else {
//                    setAniState(SGGroundUnit.State.IDLE);
                memberSquad.setUpdateLocation(false);
            }
        }
    }

}