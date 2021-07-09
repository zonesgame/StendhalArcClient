package z.entities.type.ai.state;

import arc.math.geom.Vec2;
import mindustry.entities.units.UnitState;
import z.ai.utils.SquadMember;
import z.entities.type.base.MachineUnit;

import static mindustry.Vars.tilesize;

/**
 * 策略组1, 移动状态处理
 */
public class MoveSLG1<E extends MachineUnit> implements UnitState<E> {

    public MoveSLG1() {
    }

    @Override
    public void enter(E unit) {
//        Log.info("enter  MOVE state");
    }

    @Override
    public void exit(E unit) {
    }

    @Override
    public void update(E unit) {
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
                // 更新单位状态
                unit.setState(MachineUnit.idleSLG1);
            }
        }
    }

}