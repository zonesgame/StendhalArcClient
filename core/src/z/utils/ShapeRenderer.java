package z.utils;

import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.geom.Rect;
import arc.math.geom.Vec2;
import arc.z.util.ISOUtils;

import static mindustry.Vars.tileunit;

/**
 *
 */
public class ShapeRenderer {

    private static final float INTEGER_HALF = 0.5f;
    private static final Vec2 node1 = new Vec2(), node2 = new Vec2(), node3 = new Vec2(), node4 = new Vec2();


    private static final float[] drawsize = {
            0.25f * 1.2f, 0.25f,
            0.34f * 1.2f, 0.35f,
            0.5f * 1.2f, 0.5f,
            0.7f * 1.2f, 0.7f,
            0.9f * 1.2f, 0.9f,
    };
    private static final float drawspace = 0.1f;

    /**
     *  绘制方块选择效果, 以中心点为锚点进行绘制
     * @param size 绘制块的尺寸
     * @param direction 绘制三角形方向0左, 1上, 2右, 3下
     * */
    public static void drawBlockSelect(float tilex, float tiley, int size, int direction) {
        // 起始点设置为中心点锚点
        tilex -= INTEGER_HALF;
        tiley -= INTEGER_HALF;

        float addx = drawsize[(size - 1) * 2];
        float addy = drawsize[(size - 1) * 2 + 1];
        if (direction == 0) {       // 左
            tilex += drawspace;
            tiley += drawspace;
            ISOUtils.tileToWorldCoords(tilex, tiley, node1);
            ISOUtils.tileToWorldCoords(tilex + addx, tiley, node2);
            ISOUtils.tileToWorldCoords(tilex, tiley + addy, node3);
            Fill.tri(node1.x , node1.y, node2.x, node2.y, node3.x, node3.y);
        }
        else if (direction == 1) {      // 上
            tilex += tileunit - drawspace;
            tiley += drawspace;
            ISOUtils.tileToWorldCoords(tilex, tiley, node1);
            ISOUtils.tileToWorldCoords(tilex - addx, tiley, node2);
            ISOUtils.tileToWorldCoords(tilex, tiley + addy, node3);
            Fill.tri(node1.x , node1.y, node2.x, node2.y, node3.x, node3.y);
        }
        else if (direction == 2) {      // 右
            tilex += tileunit - drawspace;
            tiley += tileunit - drawspace;
            ISOUtils.tileToWorldCoords(tilex, tiley, node1);
            ISOUtils.tileToWorldCoords(tilex - addx, tiley, node2);
            ISOUtils.tileToWorldCoords(tilex, tiley - addy, node3);
            Fill.tri(node1.x , node1.y, node2.x, node2.y, node3.x, node3.y);
        }
        else {      // 下
            tilex += drawspace;
            tiley += tileunit - drawspace;
            ISOUtils.tileToWorldCoords(tilex, tiley, node1);
            ISOUtils.tileToWorldCoords(tilex + addx, tiley, node2);
            ISOUtils.tileToWorldCoords(tilex, tiley - addy, node3);
            Fill.tri(node1.x , node1.y, node2.x, node2.y, node3.x, node3.y);
        }
    }

    /** 通过起始点和结束点绘制菱形矩阵*/
    public static void drawDiamondPoint(float tx, float ty, float tx2, float ty2) {
        float scale = 1;
        float addx = 0;
        float addy = 0;
        ISOUtils.tileToWorldCoords(tx, ty, node1);
        ISOUtils.tileToWorldCoords(tx2, ty, node2);
        ISOUtils.tileToWorldCoords(tx2, ty2, node3);
        ISOUtils.tileToWorldCoords(tx, ty2, node4);

        Lines.line((node1.x + addx) * scale, (node1.y + addy) * scale, (node2.x + addx) * scale, (node2.y + addy) * scale);
        Lines.line((node2.x + addx) * scale, (node2.y + addy) * scale, (node3.x + addx) * scale, (node3.y + addy) * scale);
        Lines.line((node3.x + addx) * scale, (node3.y + addy) * scale, (node4.x + addx) * scale, (node4.y + addy) * scale);
        Lines.line((node4.x + addx) * scale, (node4.y + addy) * scale, (node1.x + addx) * scale, (node1.y + addy) * scale);

//        Fill.quad(node1.x, node1.y, node2.x, node2.y, node4.x, node4.y, node3.x, node3.y);
//        Draw.reset();
    }

    public static void drawDiamondUnit(float tilex, float tiley, float tilesize) {
        drawDiamondUnit(tilex, tiley, tilesize, tilesize, 0, 0);
    }
    public static void drawDiamondUnit(float tilex, float tiley, float tilew, float tileh) {
        drawDiamondUnit(tilex, tiley, tilew, tileh, 0, 0);
    }

    public static void drawDiamondUnit(Rect rect) {
        drawDiamondUnit(rect.x + rect.width / 2f, rect.y + rect.height / 2f, rect.width, rect.height, 0, 0);
    }

    /** 以中心点为锚点进行绘制, 比喻0,0, 实际位置为-0.5, -0.5*/
    public static void drawDiamondUnit(float tilex, float tiley, float tilew, float tileh, float addx, float addy) {
        float offsetx = tilew / 2f;
        float offsety = tileh / 2f;
        tilex -= offsetx;
        tiley -= offsety;

        float scale = 1;
//        Draw.color(Color.green);
        ISOUtils.tileToWorldCoords(tilex, tiley, node1);
        ISOUtils.tileToWorldCoords(tilex + tilew, tiley + tileh, node4);
        ISOUtils.tileToWorldCoords(tilex + tilew, tiley, node2);
        ISOUtils.tileToWorldCoords(tilex, tiley + tileh, node3);

        Lines.line((node1.x + addx) * scale, (node1.y + addy) * scale, (node2.x + addx) * scale, (node2.y + addy) * scale);
        Lines.line((node1.x + addx) * scale, (node1.y + addy) * scale, (node3.x + addx) * scale, (node3.y + addy) * scale);

        Lines.line((node4.x + addx) * scale, (node4.y + addy) * scale, (node2.x + addx) * scale, (node2.y + addy) * scale);
        Lines.line((node4.x + addx) * scale, (node4.y + addy) * scale, (node3.x + addx) * scale, (node3.y + addy) * scale);

//        Fill.quad(node1.x, node1.y, node2.x, node2.y, node4.x, node4.y, node3.x, node3.y);
//        Draw.reset();
    }

    public static void drawDiamond(float tilex, float tiley, float tilesize) {
        drawDiamond(tilex, tiley, tilesize, tilesize, 0, 0);
    }
    public static void drawDiamond(float tilex, float tiley, float tilew, float tileh) {
        drawDiamond(tilex, tiley, tilew, tileh, 0, 0);
    }
    /** 以中心点为锚点进行绘制, 比喻0,0, 实际位置为-0.5, -0.5*/
    public static void drawDiamond(float tilex, float tiley, float tilew, float tileh, float addx, float addy) {
        int offsetx = -( (int)tilew - 1) / 2;
        int offsety = -( (int)tileh - 1) / 2;
        tilex -= INTEGER_HALF - offsetx;
        tiley -= INTEGER_HALF - offsety;

        float scale = 1;
//        Draw.color(Color.green);
        ISOUtils.tileToWorldCoords(tilex, tiley, node1);
        ISOUtils.tileToWorldCoords(tilex + tilew, tiley + tileh, node4);
        ISOUtils.tileToWorldCoords(tilex + tilew, tiley, node2);
        ISOUtils.tileToWorldCoords(tilex, tiley + tileh, node3);

        Lines.line((node1.x + addx) * scale, (node1.y + addy) * scale, (node2.x + addx) * scale, (node2.y + addy) * scale);
        Lines.line((node1.x + addx) * scale, (node1.y + addy) * scale, (node3.x + addx) * scale, (node3.y + addy) * scale);

        Lines.line((node4.x + addx) * scale, (node4.y + addy) * scale, (node2.x + addx) * scale, (node2.y + addy) * scale);
        Lines.line((node4.x + addx) * scale, (node4.y + addy) * scale, (node3.x + addx) * scale, (node3.y + addy) * scale);

//        Fill.quad(node1.x, node1.y, node2.x, node2.y, node4.x, node4.y, node3.x, node3.y);
//        Draw.reset();
    }

    @Deprecated
    /** 以中心点为锚点进行绘制, 比喻0,0, 实际位置为-0.5, -0.5*/
    public static void drawDiamond(float tx, float ty, float tw, float th, float tilewidth, float tileheight, float addx, float addy) {
//        float tx = ;
//        float ty = ;
//        float tw = ;
//        float th = ;

        int offsetx = -( (int)tw - 1) / 2;
        int offsety = -( (int)th - 1) / 2;
        tx -= INTEGER_HALF - offsetx;
        ty -= INTEGER_HALF - offsety;

        float scale = 1;
//        Draw.color(Color.green);
        ISOUtils.tileToWorldCoords(tx, ty, tilewidth, tileheight, node1);
        ISOUtils.tileToWorldCoords(tx + tw, ty + th, tilewidth, tileheight, node4);
        ISOUtils.tileToWorldCoords(tx + tw, ty, tilewidth, tileheight, node2);
        ISOUtils.tileToWorldCoords(tx, ty + th, tilewidth, tileheight, node3);

        Lines.line((node1.x + addx) * scale, (node1.y + addy) * scale, (node2.x + addx) * scale, (node2.y + addy) * scale);
        Lines.line((node1.x + addx) * scale, (node1.y + addy) * scale, (node3.x + addx) * scale, (node3.y + addy) * scale);

        Lines.line((node4.x + addx) * scale, (node4.y + addy) * scale, (node2.x + addx) * scale, (node2.y + addy) * scale);
        Lines.line((node4.x + addx) * scale, (node4.y + addy) * scale, (node3.x + addx) * scale, (node3.y + addy) * scale);

//        Fill.quad(node1.x, node1.y, node2.x, node2.y, node4.x, node4.y, node3.x, node3.y);
//        Draw.reset();
    }

    /** 地编块选择绘制*/
    @Deprecated
    public static void drawDiamondEditor(float worldx, float worldy, float scaling) {
        // temp begon
        float tilex = 0;
        float tiley = 0;
        float tilew = 1;
        float tileh = 1;
        float addx = worldx;
        float addy = worldy;
        float T_WIDTH = scaling;
        float T_HEIGHT = scaling * ISOUtils.TILE_HEIGHT / ISOUtils.TILE_WIDTH;
        T_WIDTH = ISOUtils.TILE_WIDTH / scaling * 2;
        T_HEIGHT = ISOUtils.TILE_HEIGHT / scaling * 2;
        // temp end

        float offsetx = tilew / 2f;
        float offsety = tileh / 2f;
        tilex -= offsetx;
        tiley -= offsety;

        float scale = 1;
//        Draw.color(Color.green);
        ISOUtils.tileToWorldCoords(tilex, tiley, T_WIDTH, T_HEIGHT, node1);
        ISOUtils.tileToWorldCoords(tilex + tilew, tiley + tileh, T_WIDTH, T_HEIGHT, node4);
        ISOUtils.tileToWorldCoords(tilex + tilew, tiley, T_WIDTH, T_HEIGHT, node2);
        ISOUtils.tileToWorldCoords(tilex, tiley + tileh, T_WIDTH, T_HEIGHT, node3);

        Lines.line((node1.x + addx) * scale, (node1.y + addy) * scale, (node2.x + addx) * scale, (node2.y + addy) * scale);
        Lines.line((node1.x + addx) * scale, (node1.y + addy) * scale, (node3.x + addx) * scale, (node3.y + addy) * scale);

        Lines.line((node4.x + addx) * scale, (node4.y + addy) * scale, (node2.x + addx) * scale, (node2.y + addy) * scale);
        Lines.line((node4.x + addx) * scale, (node4.y + addy) * scale, (node3.x + addx) * scale, (node3.y + addy) * scale);
    }

    private static final float drawFillSpace = 0.0f;
    /**
     *  绘制填充菱形
     * */
    public static void drawFillDiamond(float tilex, float tiley, float tilew, float tileh) {
        int offsetx = -( (int)tilew - 1) / 2;
        int offsety = -( (int)tileh - 1) / 2;
        tilex -= INTEGER_HALF - offsetx;
        tiley -= INTEGER_HALF - offsety;

        tilex += drawFillSpace;
        tiley += drawFillSpace;
        tilew += -drawFillSpace * 2;
        tileh += -drawFillSpace * 2;
        // 起始点设置为中心点锚点
//        tilex -= INTEGER_HALF;
//        tiley -= INTEGER_HALF;
        ISOUtils.tileToWorldCoords(tilex, tiley, node1);
        ISOUtils.tileToWorldCoords(tilex + tilew, tiley + tileh, node4);
        ISOUtils.tileToWorldCoords(tilex + tilew, tiley, node2);
        ISOUtils.tileToWorldCoords(tilex, tiley + tileh, node3);

        float scale = 1;
        node1.scl(scale);
        node2.scl(scale);
        node3.scl(scale);
        node4.scl(scale);
        Fill.quad(node1.x , node1.y, node2.x, node2.y, node4.x, node4.y, node3.x, node3.y);
    }

}
