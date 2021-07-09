package z.debug;

import arc.ai.utils.Location;
import arc.math.geom.Vec2;

/**
 *
 */
public class TargetPoint implements Location<Vec2> {

    public Vec2 pos = new Vec2();
    public float angle = 90;

    public TargetPoint(float x, float y) {
        pos.set(x, y);
    }

    public void setPos (float x, float y) {
        pos.set(x, y);
    }
    public void setAngle(float angle) {
        this.angle = angle;
    }

    @Override
    public Vec2 getPosition() {
        return pos;
    }

    @Override
    public float getOrientation() {
        return angle;
    }

    @Override
    public void setOrientation(float orientation) {
        this.angle += orientation;
//        if (angle > 360) angle = 0;
//        if (angle < 0) angle = 360;
    }

    @Override
    public float vectorToAngle(Vec2 vector) {
        return vector.angle();
    }

    @Override
    public Vec2 angleToVector(Vec2 outVector, float angle) {
        return null;
    }

    @Override
    public Location<Vec2> newLocation() {
        return null;
    }
}
