package z.ai.utils;

import arc.math.geom.Vec2;

public final class Vector2Utils {

	private Vector2Utils() {
	}

	public static float vectorToAngle(Vec2 vector) {
		return vector.angleRad();
	}

	public static Vec2 angleToVector(Vec2 outVector, float angle) {
		outVector.x = -(float) Math.sin(angle);
		outVector.y = (float) Math.cos(angle);
		return outVector;
	}

}
