package z.input.unit;

import arc.Core;
import arc.input.KeyCode;
import arc.math.geom.Vec2;
import arc.struct.ObjectMap;
import arc.util.Time;
import arc.util.Tmp;
import arc.z.util.ISOUtils;
import mindustry.entities.type.BaseUnit;
import mindustry.input.Binding;
import mindustry.world.Tile;
import z.ai.components.Squad;
import z.entities.type.base.MachineUnit;

import static mindustry.Vars.systemStrategy;
import static mindustry.Vars.world;

/**
 *
 */
public class UnitInput {

    /** 双击事件步长*/
    private float doubleClickStepDelta = 30f;      // 0.5second

    /** 单位输入事件最后触发时间*/
    private ObjectMap<Binding, Float> lastUpTimes = new ObjectMap<>();

    /** 最后操作队伍*/
    private Squad<? extends BaseUnit> lastSquad = null;
    /** 当前操作队伍*/
    private Squad<? extends BaseUnit> curSquad = null;
    /** 指定按键绑定队伍*/
    private ObjectMap<Binding, Integer> bindingSquad = new ObjectMap<>();

    public UnitInput() {
        // test code begon
        bindingSquad.put(Binding.unit_select_01, 0);
        bindingSquad.put(Binding.unit_select_02, 1);
        bindingSquad.put(Binding.unit_select_03, 2);
        bindingSquad.put(Binding.unit_select_04, 3);
        bindingSquad.put(Binding.unit_select_05, 4);
        bindingSquad.put(Binding.unit_select_06, 5);
        bindingSquad.put(Binding.unit_select_07, 6);
        bindingSquad.put(Binding.unit_select_08, 7);
        bindingSquad.put(Binding.unit_select_09, 8);
        bindingSquad.put(Binding.unit_select_10, 9);
        // test code end
    }

    /** 主逻辑更新方法*/
    public boolean pollInput() {
//        Vec2 cursorTile = (int) (ISOUtils.worldToTileCenterX(worldPos.x, worldPos.y) + 0.0f);
        Vec2 cursorTile = ISOUtils.worldToTileCoords(Core.input.mouseWorld().add(0.0f, 0.0f));
        Tile selected = world.tile((int) cursorTile.x, (int) cursorTile.y);   // 光标当前选择瓦砾

        if (Core.input.keyTap(Binding.unit_action) && !Core.scene.hasMouse()) {
            moveTargetTile.setZero();

            if (lastSquad != null && lastSquad.valid() && lastSquad.isSelect) {
                if (selected != null) {
                    moveTargetTile.set(selected.getX(), selected.getY());
                    moveTargetScreen.set(Core.input.mouse());
                    moveTargetWorld.set(Core.input.mouseWorld());
                }
            }
        }

        else if (Core.input.keyRelease(Binding.unit_action) && !Core.scene.hasMouse()) {
            if ( !moveTargetTile.isZero()) {
                if (Tmp.v22.set(moveTargetScreen).sub(Core.input.mouse()).len() > Math.max(Core.graphics.getWidth(), Core.graphics.getHeight()) * 0.05f) {
                    float angle = Tmp.v22.set(moveTargetScreen).sub(Core.input.mouse()).angle();
                    angle = (angle + 180) % 360;
                    float angleIso = (((360 - 45) + 0) % 360) + 90;   // iso 坐标
                    lastSquad.setTarget(moveTargetTile.getX(), moveTargetTile.getY());
                } else {
                    lastSquad.setTarget(moveTargetTile.getX(), moveTargetTile.getY());
                }
            }
        }

        return false;
    }

    public boolean keyDownEvent(KeyCode keycode) {
        boolean returnvalue = false;

        // 单位选择按键处理
        for (Binding bindKey : bindingSquad.keys()) {
            if (keycode == bindKey.defaultValue(null)) {
                Squad squad = systemStrategy.getSquad(bindingSquad.get(bindKey));
                if ( squad.valid()) {
                    if (squad.isSelect && doubleClickStepCheck(bindKey)) {   // 双击事件判断
                        squad.doubleClick();
                    } else {
                        squad.isSelect = true;
                    }
                }

                setLastSquad(squad);
                return (returnvalue = false);
            }
        }

        // 取消单位处理
        if (keycode == Binding.cancel_unitSelect.defaultValue(null)) {
            setLastSquad(null);
            return (returnvalue = false);
        }
        // 队伍调整为ATTACK策略
        if (keycode == Binding.group_strategy_attack.defaultValue(null)) {
            {   // test code
                if (lastSquad != null) {
                    for (BaseUnit unit : lastSquad.getMembers()) {
                        if (unit instanceof MachineUnit) {
                            ((MachineUnit) unit).defStrategy = MachineUnit.TeamStrategy.S_ATTACK;
                        }
                    }
                }
            }
            return (returnvalue = false);
        }
        // 队伍调整为RETREAT策略
        if (keycode == Binding.group_strategy_retreat.defaultValue(null)) {
            {   // test code
//                System.out.println("成员数量   " + (curSquad != null ? curSquad.getMembers().size : 0) + "           " + (lastSquad == null ? 0 : lastSquad.getMembers().size) );
                if (lastSquad != null) {
                    for (BaseUnit unit : lastSquad.getMembers()) {
                        if (unit instanceof MachineUnit) {
                            ((MachineUnit) unit).defStrategy = MachineUnit.TeamStrategy.S_RETREAT;
                        }
                    }
                }
            }
            return (returnvalue = false);
        }
        // 队伍调整为IDLE策略
//        if (keycode == Binding.group_strategy_idle.defaultValue(null)) {
//            {   // test code
//                if (lastSquad != null) {
//                    for (BaseUnit unit : lastSquad.getMembers()) {
//                        if (unit instanceof MachineUnit) {
//                            ((MachineUnit) unit).setStrategy( MachineUnit.TeamStrategy.S_IDEL);
//                        }
//                    }
//                }
//            }
//            return (returnvalue = false);
//        }

        return returnvalue;
    }


    public boolean keyUpEvent(KeyCode keycode) {
        //  更新up时间为双击事件保留.
        for (Binding bindKey : bindingSquad.keys()) {
            if (keycode == bindKey.defaultValue(null)) {
                lastUpTimes.put(bindKey, Time.time());
                break;
            }
        }

        return false;
    }

    private void setLastSquad(Squad squad) {
        if (lastSquad != null && lastSquad != squad)
            lastSquad.isSelect = false;
        lastSquad = squad;
    }

    private boolean doubleClickStepCheck(Binding bindingKey) {
        float lastTime = lastUpTimes.get(bindingKey, Float.NaN);
        return lastTime != Float.NaN && Time.time() - lastTime < doubleClickStepDelta;
    }

    // 队伍移动数据 begon
    @Deprecated
    private Vec2 moveTargetTile = new Vec2();
    @Deprecated
    private Vec2 moveTargetWorld = new Vec2();
    @Deprecated
    private Vec2 moveTargetScreen = new Vec2();
    // 队伍移动数据 end
}
