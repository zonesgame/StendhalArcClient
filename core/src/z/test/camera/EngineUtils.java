//package z.test.camera;
//
//import arc.math.geom.Point2;
//import arc.math.geom.Vec2;
//
//public class EngineUtils {
//
//  public static final int TILE_WIDTH    = 80;        // default 84;
//  public static final int TILE_HEIGHT   = 60;        // default 66;
//  public static final int TILE_WIDTH50  = TILE_WIDTH  >> 1;// >> 1;
//  public static final int TILE_HEIGHT50 = TILE_HEIGHT >> 1;// >> 1;
//  public static final int TILE_WIDTH25  = TILE_WIDTH50  >> 1;// >> 1;
//  public static final int TILE_HEIGHT25 = TILE_HEIGHT50 >> 1;// >> 1;
//  private static final float INTEGER_HALF = 0.5f;
//
//  private EngineUtils() {}
//
//  // 瓦砾坐标转化为世界坐标 begon
//
//  public static float tileToWorldX(float tx, float ty) {
//    return (tx - ty) * TILE_WIDTH50;
//  }
//  public static float tileToWorldY(float tx, float ty) {
//    return (tx + ty) * TILE_HEIGHT50;
//  }
//
//  public static Vec2 tileToWorldCoords(Vec2 tile, Vec2 dst) {
//    return tileToWorldCoords(tile.x, tile.y, dst);
//  }
//
//  // temp begon
//  public static Vec2 tileToWorldCoordsCenter(float tx, float ty, float tw, float th, Vec2 dst) {
//    int offsetx = -( (int)tw - 1) / 2;
//    int offsety = -( (int)th - 1) / 2;
//    tx += tw / 2f - INTEGER_HALF + offsetx;
//    ty += th / 2f - INTEGER_HALF + offsety;
//    dst.x = +(tx + ty) * TILE_WIDTH50;
//    dst.y = +(tx - ty) * TILE_HEIGHT50;    // +y轴向上延申,-y轴向上延申
//    return dst;
//  }
//  // temp end
//
//  public static Vec2 tileToWorldCoords(float tx, float ty, Vec2 dst) {
//    dst.x = +(tx + ty) * TILE_WIDTH50;
//    dst.y = +(tx - ty) * TILE_HEIGHT50;    // +y轴向上延申,-y轴向上延申
//    return dst;
//  }
//
//  public static Point2 tileToWorldCoords(Point2 world, Point2 dst) {
//    return tileToWorldCoords(world.x, world.y, dst);
//  }
//
//  public static Point2 tileToWorldCoords(int tx, int ty, Point2 dst) {
//    dst.x = +(tx + ty) * TILE_WIDTH50;
//    dst.y = +(tx - ty) * TILE_HEIGHT50;   // +y轴向上延申,-y轴向上延申
//    return dst;
//  }
//  // 瓦砾坐标转化为世界坐标 end
//
//  // 世界坐标转化为瓦砾坐标 begon
//
//  public static float worldToTileCenterX(float wx, float wy) {
//    float value = worldToTileX(wx, wy);
//    return value > 0 ? value + INTEGER_HALF : value - INTEGER_HALF;
////    return worldToTileX(wx - 0, wy - 0);
//  }
//  public static float worldToTileCenterY(float wx, float wy) {
//    float value = worldToTileY(wx, wy);
//    return value > 0 ? value + INTEGER_HALF : value - INTEGER_HALF;
////    return worldToTileY(wx - 0, wy - 0);
//  }
//
//  public static float worldToTileX(float wx, float wy) {
//    return INTEGER_HALF * (wy / TILE_HEIGHT50 + wx / TILE_WIDTH50);
//  }
//  public static float worldToTileY(float wx, float wy) {
//    return INTEGER_HALF * (-wy / TILE_HEIGHT50 + wx / TILE_WIDTH50);
//  }
//
//  public static Vec2 worldToTileCoords(Vec2 tile, Vec2 dst) {
//    return worldToTileCoords(tile.x, tile.y, dst);
//  }
//
//  public static Vec2 worldToTileCoords(float wx, float wy, Vec2 dst) {
//    dst.x = INTEGER_HALF * (wy / TILE_HEIGHT50 + wx / TILE_WIDTH50);
//    dst.y = INTEGER_HALF * (-wy / TILE_HEIGHT50 + wx / TILE_WIDTH50);
//    return dst;
//  }
//
//  public static Point2 worldToTileCoords(Point2 world, Point2 dst) {
//    return worldToTileCoords(world.x, world.y, dst);
//  }
//
//  public static Point2 worldToTileCoords(int wx, int wy, Point2 dst) {
//    dst.x = (int) (INTEGER_HALF * (wy / TILE_HEIGHT50 + wx / TILE_WIDTH50));
//    dst.y = (int) (INTEGER_HALF * (-wy / TILE_HEIGHT50 + wx / TILE_WIDTH50));
//    return dst;
//  }
//  // 世界坐标转化为瓦砾坐标 end
//}
