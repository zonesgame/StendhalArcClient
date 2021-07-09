package z.entities.type.ai.state;

import mindustry.entities.units.UnitState;
import z.entities.type.base.BaseGroundUnit;

/**
 *
 */
public class MachineState<E extends BaseGroundUnit> implements UnitState<E> {

    private E unit;

    public MachineState(E parent) {
        this.unit = parent;
    }

    @Override
    public void enter(E u) {
    }

    @Override
    public void exit(E u) {
    }

    @Override
    public void update(E u) {
        if (unit.target != null) {
            System.out.println("update squad state    " + unit.squad().getTarget().getPosition());
        }
    }

}
