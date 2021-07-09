package z.system;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.geom.Vec2;
import arc.struct.Array;
import arc.z.util.ISOUtils;
import mindustry.Vars;
import mindustry.core.Renderer;
import mindustry.entities.type.BaseUnit;
import mindustry.world.Tile;
import z.ai.components.Squad;
import z.ai.utils.SquadMember;
import z.entities.type.base.BaseGroundUnit;

import static mindustry.Vars.player;
import static mindustry.Vars.systemStrategy;

/**
 *  战斗单位管理系统
 */
public class TroopsSystem {

    // 开发测试时临时数据 begon
    @Deprecated
    public boolean selectUnitState = false;
    @Deprecated
    public Array<BaseGroundUnit> selectUnits = new Array<>();
    /** 单位选择起始位置*/
    @Deprecated
    public int unitX, unitY;
    // 开发测试临时数据 end


    public TroopsSystem() {
//        workerPool = new WorkerPool<BlockUnit>();
    }

    public void updateSystem() {
//        System.out.println(selectUnits.size);
        if (selectUnits.size > 0) {
            Squad<BaseUnit> squad = systemStrategy.getSquad(player.getTeam(), 0);
//            System.out.println(squad.getMembers() + "      Memebers.");
//            System.out.println(squad.getTarget());
            Tile targetTile = Vars.pathfinder.squadTargetTile[player.getTeam().id][0][0];
            if (targetTile != null) {
//                System.out.println("Move Target....   " + targetTile.x + "  " + targetTile.y);
            }

            if (true) {
                Renderer.addDebugDraw((nulltile) -> {
                    Draw.color(Color.red);
                    Vec2 vpos = ISOUtils.tileToWorldCoords(squad.getTarget().getX(), squad.getTarget().getY());
//                    vpos.set(squad.getTarget().getX(), squad.getTarget().getY());
                    Lines.poly(vpos.x, vpos.y, 3, 4);
                    Draw.color();
                }, Float.MIN_VALUE);        // minValue execute onece

                // 绘制方向
                Renderer.addDebugDraw((nulltile) -> {
                    Draw.color(Color.green);
                    Vec2 vpos = ISOUtils.tileToWorldCoords(squad.getTarget().getX(), squad.getTarget().getY());
//                    vpos.set(squad.getTarget().getX(), squad.getTarget().getY());
                    Lines.lineAngle(vpos.x, vpos.y, squad.getTarget().getOrientation(), 8);
                    Draw.color();
                }, Float.MIN_VALUE);        // minValue execute onece
                // Iso方向
                Renderer.addDebugDraw((nulltile) -> {
                    Draw.color(Color.cyan);
                    Vec2 vpos = ISOUtils.tileToWorldCoords(squad.getTarget().getX(), squad.getTarget().getY());
//                    vpos.set(squad.getTarget().getX(), squad.getTarget().getY());
                    float angle = 360 - squad.getTarget().getOrientation() + 45;
                    Lines.lineAngle(vpos.x, vpos.y, angle, 8);
                    Draw.color();
                }, Float.MIN_VALUE);        // minValue execute onece

                for (BaseUnit unit : squad.getMembers()) {
                    SquadMember member = unit.squadMember;

                    Renderer.addDebugDraw((nulltile) -> {
                        Draw.color(Color.red);
                        Vec2 vpos = ISOUtils.tileToWorldCoords(member.getTargetLocation().getPosition());
//                        vpos.set(member.getTargetLocation().getPosition());
                        Lines.circle(vpos.x, vpos.y, 4);
                        Draw.color();
                    }, Float.MIN_VALUE);        // minValue execute onece
                }

                return;
            }

            for (BaseGroundUnit unit : selectUnits) {
                Renderer.addDebugDraw((nulltile) -> {
                    Draw.color(Color.red);
                    Lines.circle(unit.wpos.x, unit.wpos.y, 4);
                    Draw.color();
                }, Float.MIN_VALUE);        // minValue execute onece
            }
//            System.out.println(selectUnits.first().selectState + "   -------- node0");
        }
    }

//    public Squad[] getTeamSquads(Team team) {
//        return systemStrategy.squadGroup[team.id];
//    }
//
//    public Squad[] getTeamSquads() {
//        return systemStrategy.squadGroup[player.getTeam().id];
//    }
//
//    public Squad getSquad(Team team, int squadIndex) {
//        return systemStrategy.squadGroup[team.id][squadIndex];
//    }
//
//    public Squad getSquad(int squadIndex) {
//        return systemStrategy.squadGroup[player.getTeam().id][squadIndex];
//    }

}
