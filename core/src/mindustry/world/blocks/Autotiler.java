package mindustry.world.blocks;

import java.util.Arrays;

import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.math.geom.Point2;
import arc.util.ArcAnnotate.Nullable;
import arc.util.Eachable;
import mindustry.entities.traits.BuilderTrait.BuildRequest;
import mindustry.world.Block;
import mindustry.world.Edges;
import mindustry.world.Tile;

/**
 *  自动整理机
 * */
public interface Autotiler{
    /**
     *  整理数据存储者
     * */
    class AutotilerHolder{
        /** 混合结果容器*/
        static final int[] blendresult = new int[3];
        /** 定向器容器*/
        static final BuildRequest[] directionals = new BuildRequest[4];
    }

    /** 获取瓦砾*/
    default @Nullable int[] getTiling(BuildRequest req, Eachable<BuildRequest> list){
        if(req.tile() == null) return null;
        BuildRequest[] directionals = AutotilerHolder.directionals;

        Arrays.fill(directionals, null);
        list.each(other -> {
            if(other.breaking || other == req) return;

            int i = 0;
            for(Point2 point : Geometry.d4){
                int x = req.x + point.x, y = req.y + point.y;
                if(x >= other.x -(other.block.size - 1) / 2 && x <= other.x + (other.block.size / 2) && y >= other.y -(other.block.size - 1) / 2 && y <= other.y + (other.block.size / 2)){
                    directionals[i] = other;
                }
                i++;
            }
        });

        return buildBlending(req.tile(), req.rotation, directionals, req.worldContext);
    }

    /** 外部调用获取混合结果*/
    default int[] buildBlending(Tile tile, int rotation, BuildRequest[] directional, boolean world){
        int[] blendresult = AutotilerHolder.blendresult;
        blendresult[0] = 0;
        blendresult[1] = blendresult[2] = 1;
        int num =         // 0右, 1下, 2左, 3上
        (blends(tile, rotation, directional, 2, world) && blends(tile, rotation, directional, 1, world) && blends(tile, rotation, directional, 3, world)) ? 0 :
        (blends(tile, rotation, directional, 1, world) && blends(tile, rotation, directional, 3, world)) ? 1 :
        (blends(tile, rotation, directional, 1, world) && blends(tile, rotation, directional, 2, world)) ? 2 :
        (blends(tile, rotation, directional, 3, world) && blends(tile, rotation, directional, 2, world)) ? 3 :
        blends(tile, rotation, directional, 1, world) ? 4 :
        blends(tile, rotation, directional, 3, world) ? 5 :
        -1;
        transformCase(num, blendresult);
        return blendresult;
    }

    default void transformCase(int num, int[] bits){
        if(num == 0){
            bits[0] = 3;
        }else if(num == 1){
            bits[0] = 4;
        }else if(num == 2){
            bits[0] = 2;
        }else if(num == 3){
            bits[0] = 2;
            bits[2] = -1;
        }else if(num == 4){
            bits[0] = 1;
            bits[2] = -1;
        }else if(num == 5){
            bits[0] = 1;
        }
    }

    /** 混合*/
    default boolean blends(Tile tile, int rotation, @Nullable BuildRequest[] directional, int direction, boolean checkWorld){
        int realDir = Mathf.mod(rotation - direction, 4);
        if(directional != null && directional[realDir] != null){
            BuildRequest req = directional[realDir];
            if(blends(tile, rotation, req.x, req.y, req.rotation, req.block)){
                return true;
            }
        }
        return checkWorld && blends(tile, rotation, direction);
    }

    default boolean blends(Tile tile, int rotation, int direction){
        Tile other = tile.getNearby(Mathf.mod(rotation - direction, 4));
        if(other != null) other = other.link();
        return other != null && other.getTeam() == tile.getTeam() && blends(tile, rotation, other.x, other.y, other.rotation(), other.block());
    }

    default boolean blendsArmored(Tile tile, int rotation, int otherx, int othery, int otherrot, Block otherblock){
        return (Point2.equals(tile.x + Geometry.d4(rotation).x, tile.y + Geometry.d4(rotation).y, otherx, othery)
                || ((!otherblock.rotate && Edges.getFacingEdge(otherblock, otherx, othery, tile) != null &&
                Edges.getFacingEdge(otherblock, otherx, othery, tile).relativeTo(tile) == rotation) || (otherblock.rotate && Point2.equals(otherx + Geometry.d4(otherrot).x, othery + Geometry.d4(otherrot).y, tile.x, tile.y))));
    }

    default boolean lookingAt(Tile tile, int rotation, int otherx, int othery, int otherrot, Block otherblock){
        return (Point2.equals(tile.x + Geometry.d4(rotation).x, tile.y + Geometry.d4(rotation).y, otherx, othery)
        || (!otherblock.rotate || Point2.equals(otherx + Geometry.d4(otherrot).x, othery + Geometry.d4(otherrot).y, tile.x, tile.y)));
    }

    /** 混合*/
    boolean blends(Tile tile, int rotation, int otherx, int othery, int otherrot, Block otherblock);

    // zones add begon
    /** 权倾天下Wall调用获取混合结果*/
    default int[] buildBlendingSG(Tile tile, int rotation, BuildRequest[] directional, boolean world){
        int[] blendresult = AutotilerHolder.blendresult;
        blendresult[0] = 0;
        blendresult[1] = blendresult[2] = 1;
//        int num =         // 0右, 1下, 2左, 3上
//                (blends(tile, rotation, directional, 2, world) && blends(tile, rotation, directional, 3, world)) ? 3 :          //  上
//                        (blends(tile, rotation, directional, 0, world) && blends(tile, rotation, directional, 2, world)) ? 4 :      // 左, 右 检测
//                                (blends(tile, rotation, directional, 1, world) && blends(tile, rotation, directional, 3, world)) ? 5 :  // 上, 下    检测
//                                        (blends(tile, rotation, directional, 0, world) && blends(tile, rotation, directional, 1, world)) ? 6 :      // 下
//                                                blends(tile, rotation, directional, 2, world) ? 1 :
//                                                        blends(tile, rotation, directional, 3, world) ? 2 :
//                                                                0;

        // 0右, 1下, 2左, 3上
        boolean right = blends(tile, rotation, directional, 0, world);
        boolean down = blends(tile, rotation, directional, 1, world);
        boolean left = blends(tile, rotation, directional, 2, world);
        boolean up = blends(tile, rotation, directional, 3, world);

        int i = 0;
        if (right) i++;
        if (left) i++;
        if (up) i++;
        if (down) i++;

        int num = 0;
        switch (i) {
            case 1:
//                if (left)
//                    num = 1;
//                else if (up)
//                    num = 2;
//                break;
            case 2:
                if (left && right)
                    num = 4;
                else if (up && down)
                    num = 5;
                else if (left && up)
                    num = 3;
                else if (left)
                    num = 1;
                else if (up)
                    num = 2;
                break;

            case 3:
                if (!right || !down)
                    num = 3;
                else if (!left)
                    num = 2;
                else if (!up)
                    num = 1;
                break;

            case 4:
                num = 3;
                break;
        }

        blendresult[0] = num;
        return blendresult;
    }

    /** 权倾天下道路调用获取混合结果*/
    default int[] buildBlendingRoad(Tile tile, int rotation, BuildRequest[] directional, boolean world){
        int[] blendresult = AutotilerHolder.blendresult;
        blendresult[0] = 0;
        blendresult[1] = blendresult[2] = 1;

        // 0右, 1下, 2左, 3上
        boolean right = blends(tile, rotation, directional, 0, world);
        boolean down = blends(tile, rotation, directional, 1, world);
        boolean left = blends(tile, rotation, directional, 2, world);
        boolean up = blends(tile, rotation, directional, 3, world);

        int i = 0;
        if (right) i++;
        if (left) i++;
        if (up) i++;
        if (down) i++;

        int num = 0;
        switch (i) {
            case 1:
                if (right)
                    num = 6;
                else if (left)
                    num = 8;
                else if (up)
                    num = 7;
                else if(down)
                    num = 9;
                break;

            case 2:
                if (right) {
                    if (left)
                        num = 0;
                    else if (up)
                        num = 2;
                    else if (down)
                        num = 5;
                }
                if (left) {
                    if (up)
                        num = 3;
                    else if (down)
                        num = 4;
                }
                if (up) {
                    if (down)
                        num = 1;
                }
                break;

            case 3:
                if (!right)
                    num = 11;
                else if (!left)
                    num = 13;
                else if (!up)
                    num = 12;
                else if (!down)
                    num = 10;
                break;

            case 4:
                num = 14;
                break;

            default:
                num = 7;
        }

        blendresult[0] = num;
        return blendresult;
    }
    // zones add end
}
