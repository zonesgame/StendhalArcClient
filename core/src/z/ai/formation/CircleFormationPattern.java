package z.ai.formation;

import arc.ai.fma.FormationPattern;
import arc.ai.utils.Location;
import arc.math.Mathf;
import arc.math.geom.Vec2;

/**
 *  圆形队列 模式
 */
public class CircleFormationPattern implements FormationPattern<Vec2> {

    /** 成员数量 */
    private int numberOfSlots;
    /** 成员半径 */
    private float memberRadius;

    /** Creates a {@code DefensiveCircleFormationPattern}
     * @param memberRadius */
    public CircleFormationPattern(float memberRadius) {
        this.memberRadius = memberRadius;
    }

    /** 设置最大成员数量*/
    @Override
    public void setNumberOfSlots (int numberOfSlots) {
        this.numberOfSlots = numberOfSlots;
    }

    /** 计算指定槽成员位置*/
    @Override
    public Location<Vec2> calculateSlotLocation (Location<Vec2> outLocation, int slotNumber) {
        if (numberOfSlots > 1) {
            // Place the slot around the circle based on its slot number
            float angleAroundCircle = (Mathf.PI2 * slotNumber) / numberOfSlots;

            // The radius depends on the radius of the member,
            // and the number of members in the circle:
            // we want there to be no gap between member's shoulders.
            float radius = memberRadius / (float)Math.sin(Math.PI / numberOfSlots);

            // Fill location components based on the angle around circle.
            outLocation.angleToVector(outLocation.getPosition(), angleAroundCircle).scl(radius);

            // The members should be facing out
            outLocation.setOrientation(angleAroundCircle);
        }
        else {
            outLocation.getPosition().setZero();
            outLocation.setOrientation(Mathf.PI2 * slotNumber);
        }

        // Return the slot location
        // zones add OffensiveCircleFormationPattern code
        outLocation.setOrientation(outLocation.getOrientation() + Mathf.PI);
        return outLocation;
    }

    /** 是否支持指定槽数量*/
    @Override
    public boolean supportsSlots (int slotCount) {
        // In this case we support any number of slots.
        return true;
    }

}
