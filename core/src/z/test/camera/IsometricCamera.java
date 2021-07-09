package z.test.camera;

import arc.graphics.Camera;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.z.util.ISOUtils;

public class IsometricCamera extends Camera {

  private final Vec2 tmp = new Vec2();
  public final Vec2 position = new Vec2();

  private final Vec2 pixOffset  = new Vec2();
  private final Vec2 tileOffset = new Vec2();

  private final Builder builder = new Builder();

  public IsometricCamera() {}

  public void offset(float x, float y) {
    pixOffset.set(x, y);
    toWorld(pixOffset.x, pixOffset.y, tileOffset.setZero());
  }

  public Vec2 getTileOffset(Vec2 dst) {
    return dst.set(tileOffset);
  }

  public Vec2 getPixOffset(Vec2 dst) {
    return dst.set(pixOffset);
  }

  //  @Override
  public void translate(Vec2 vec) {
    translate(vec.x, vec.y);
  }

  //  @Override
  public void translate(float x, float y) {
    position.add(x, y);
    toScreen(position.x, position.y, tmp);
    super.position.set(tmp);
  }

  public void set(Vec2 vec) {
    set(vec.x, vec.y);
  }

  public void set(float x, float y) {
    position.set(x, y);
    toScreen(position.x, position.y, tmp);
    super.position.set(tmp);
  }

  /**
   * Converts tile coords to screen coords.
   */
  public Vec2 toScreen(Vec2 worldCoords) {
    return toScreen(worldCoords.x, worldCoords.y, worldCoords);
  }

  /**
   * Converts tile coords to screen coords.
   */
  public Vec2 toScreen(float x, float y, Vec2 dst) {
    ISOUtils.tileToWorldCoords(x, y, dst);
    return dst.add(pixOffset);
  }

  /**
   * Converts screen coords to tile coords.
   */
  public Vec2 toWorld(Vec2 screenCoords) {
    return toWorld(screenCoords.x, screenCoords.y, screenCoords);
  }

  /**
   * Converts screen coords to tile coords.
   */
  public Vec2 toWorld(float x, float y, Vec2 dst) {
    x /= Tile.SUBTILE_WIDTH50;
    y /= Tile.SUBTILE_HEIGHT50;
    dst.x = ( x - y) / 2 - tileOffset.x;
    dst.y = (-x - y) / 2 - tileOffset.y;
    return dst;
  }

  /**
   * Rounds tile coords to the floor integer tile coords from the specified float tile coords.
   */
  public Vec2 toTile(Vec2 worldCoords) {
    return toTile(worldCoords.x, worldCoords.y, worldCoords);
  }

  /**
   * Rounds tile coords to the floor integer tile coords from the specified float tile coords.
   */
  public Vec2 toTile(float x, float y, Vec2 dst) {
    x += tileOffset.x;
    y += tileOffset.y;
    dst.x = x < 0 ? Mathf.floor(x) : Mathf.floorPositive(x);
    dst.y = y < 0 ? Mathf.floor(y) : Mathf.floorPositive(y);
    return dst;
  }

  /**
   * Rounds tile coords to the closest integer tile coords from the specified float tile coords.
   */
  public Vec2 toTile50(Vec2 worldCoords) {
    return toTile50(worldCoords.x, worldCoords.y, worldCoords);
  }

  /**
   * Rounds tile coords to the closest integer tile coords from the specified float tile coords.
   */
  public Vec2 toTile50(float x, float y, Vec2 dst) {
    x += tileOffset.x;
    y += tileOffset.y;
    dst.x = x < 0 ? Mathf.round(x) : Mathf.roundPositive(x);
    dst.y = y < 0 ? Mathf.round(y) : Mathf.roundPositive(y);
    return dst;
  }

  public Vec2 screenToWorld(float x, float y, Vec2 dst) {
    dst.set(x, y);
    unproject(dst);
    return toWorld(dst);
  }

  public Vec2 screenToTile(float x, float y, Vec2 dst) {
    screenToWorld(x, y, dst);
    return toTile(dst);
  }

  public Builder agg(Vec2 vec) {
    return builder.reset(vec);
  }

  public final class Builder {
    Vec2 vec;

    public Builder reset(Vec2 vec) {
      this.vec = vec;
      return this;
    }

    public Builder unproject() {
      IsometricCamera.this.unproject(vec);
      return this;
    }

    public Builder project() {
      IsometricCamera.this.project(vec);
      return this;
    }

    public Builder toScreen() {
      IsometricCamera.this.toScreen(vec);
      return this;
    }

    public Builder toWorld() {
      IsometricCamera.this.toWorld(vec);
      return this;
    }

    public Builder toTile() {
      IsometricCamera.this.toTile(vec);
      return this;
    }

    public Vec2 ret() {
      return vec;
    }
  }
}
