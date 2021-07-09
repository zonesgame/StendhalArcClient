package z.debug;

import arc.ai.fma.FormationMember;
import arc.ai.utils.Location;
import arc.math.geom.Vec2;
import mindustry.entities.type.BaseUnit;
import z.ai.utils.Location2;

/**
 *
 */
public class DAIEntity extends BaseUnit implements FormationMember {

    public Vec2 position = new Vec2();
    public Location2 location = new Location2(position);

    public DAIEntity() {
        super();
    }

//    @Override
//    public SquadMember getFormationTarget() {
//        return null;
//    }

    @Override
    public Location getTargetLocation() {
        return location;
    }
}
