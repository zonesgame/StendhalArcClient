package z.world.blocks;

import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.geom.Vec2;
import mindustry.content.Blocks;
import mindustry.world.Tile;
import mindustry.world.blocks.Floor;

import static arc.graphics.g2d.Draw.scl;
import static mindustry.Vars.systemGround;

/**
 *  岩石 块
 * */
public class GroundFloor extends Floor {
//    private int variants;
    private Vec2[] offset;

    public GroundFloor(String name){
        super(name);
        breakable = false;
        alwaysReplace = false;
        playerUnmineable = true;
        // 纹理数据 begon
        // temp begon
        int [] tempOffset = systemGround.getOffset(name);
        variants = tempOffset.length / 2;
        offset = new Vec2[variants];
        for (int i = variants; --i >= 0; ) {
            offset[i] = new Vec2(tempOffset[i * 2], tempOffset[i * 2 + 1]);
        }
        // temp end
        // end
    }

    @Override
    public void draw(Tile tile){
        {
            int regionIndex = systemGround.getVariants(tile.x, tile.y);
            float dx = tile.worldx() + offset[regionIndex].x * scl;
            float dy = tile.worldy() + offset[regionIndex].y * scl;


            TextureRegion region = variantRegions[regionIndex];
            Draw.rectGdx(region, dx, dy, region.getWidth() * scl, region.getHeight() * scl * 1.5f);
        }

//        drawEdges(tile);

        Floor floor = tile.overlay();
        if(floor != Blocks.air && floor != this){ //ore should never have itself on top, but it's possible, so prevent a crash in that case
            floor.draw(tile);
        }
    }

//    @Override
//    public TextureRegion[] generateIcons(){
//        return variants == 0 ? super.generateIcons() : new TextureRegion[]{Core.atlas.find(name + "1")};
//    }

//    @Override
//    public void load(){
//        super.load();
//
//        if(variants > 0){
//            variantRegions = new TextureRegion[variants];
//
//            for(int i = 0; i < variants; i++){
//                variantRegions[i] = Core.atlas.find(name + (i + 1));
//            }
//        }
//    }

    // zones add begon

    @Override
    public Vec2 regionCenter(int variant) {
        return offset[variant];
    }

    // zones add end

}
