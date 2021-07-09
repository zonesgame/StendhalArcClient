package z.entities.type.ai.state;

import arc.math.geom.Vec2;
import mindustry.entities.type.TileEntity;
import mindustry.entities.units.UnitState;
import z.ai.utils.SquadMember;
import z.entities.type.base.BaseGroundUnit;

import static mindustry.Vars.tilesize;

/**
 *  单位队伍操作状态
 */
public class SquadState<E extends BaseGroundUnit> implements UnitState<E> {

    /** 执行状态单位*/
    private E unit;

    /** 构建 单位队伍状态*/
    public SquadState(E parent) {
        this.unit = parent;
    }

    @Override
    public void enter(E u){
        unit.target = null;          // 清空攻击目标. zones add
    }

    @Override
    public void exit(E u) {
    }

    @Override
    public void update(E u){
        TileEntity core = unit.getClosestEnemyCore();

        if (unit.type.weapon.getRecoil(unit, false) != 0) {       // attack中禁止移动
//                continue;
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
