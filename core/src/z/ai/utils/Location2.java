package z.ai.utils;

import arc.ai.utils.Location;

import arc.math.geom.Vec2;

/**
 * {@code Location2} represents an object having a 2D position and an
 * orientation.
 * 
 * @author davebaol
 */
public class Location2 implements Location<Vec2> {

	private Vec2 position;
	private float orientation;

	public Location2() {
		this(new Vec2(), 0);
	}

	public Location2(Vec2 position) {
		this(position, 0);
	}

	public Location2(Vec2 position, float orientation) {
		this.position = position;
		this.orientation = orientation;
	}

	@Override
	public Vec2 getPosition() {
		return position;
	}

	@Override
	public float getOrientation() {
		return orientation;
	}

	@Override
	public void setOrientation(float orientation) {
		this.orientation = orientation;
	}

	@Override
	public Location2 newLocation() {
		return new Location2();
	}

	@Override
	public float vectorToAngle(Vec2 vector) {
		return Vector2Utils.vectorToAngle(vector);
	}

	@Override
	public Vec2 angleToVector(Vec2 outVector, float angle) {
		return Vector2Utils.angleToVector(outVector, angle);
	}


	// zones add begon
	public void setPosition(float x, float y) {
		position.set(x, y);
	}

	public float getX() {
		return position.x;
	}

	public float getY() {
		return position.y;
	}
	// zones add end
}
