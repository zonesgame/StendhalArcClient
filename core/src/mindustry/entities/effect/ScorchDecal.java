package mindustry.entities.effect;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.z.util.ISOUtils;
import mindustry.world.Tile;

import static mindustry.Vars.headless;
import static mindustry.Vars.world;
import static z.debug.ZDebug.enable_isoInput;

/**
 *  烧焦贴图
 * */
public class ScorchDecal extends Decal{
    private static final int scorches = 5;
    private static final TextureRegion[] regions = new TextureRegion[scorches];

    /** 世界坐标创建*/
    public static void create(float x, float y){
        if(headless) return;

        if(regions[0] == null || regions[0].getTexture().isDisposed()){
            for(int i = 0; i < regions.length; i++){
                regions[i] = Core.atlas.find("scorch" + (i + 1));
            }
        }

        Tile tile;
        if (enable_isoInput)
            tile = world.tile( x, y);
        else
            tile = world.tileWorld(x, y);

        if(tile == null || tile.floor().liquidDrop != null) return;

        ScorchDecal decal = new ScorchDecal();
        decal.set(x, y);
        decal.add();
    }

    @Override
    public void drawDecal(){
        // zones add begon
        float x = this.x;
        float y = this.y;
        if (enable_isoInput) {
            Vec2 pos = ISOUtils.tileToWorldCoords(x, y);
            x = pos.x;
            y = pos.y;
        }
        // zones add end
        for(int i = 0; i < 3; i++){
            TextureRegion region = regions[Mathf.randomSeed(id - i, 0, scorches - 1)];
            float rotation = Mathf.randomSeed(id + i, 0, 360);
            float space = 1.5f + Mathf.randomSeed(id + i + 1, 0, 20) / 10f;
            Draw.rect(region,
            x + Angles.trnsx(rotation, space),
            y + Angles.trnsy(rotation, space) + region.getHeight() / 2f * Draw.scl,
            region.getWidth() * Draw.scl,
            region.getHeight() * Draw.scl,
            region.getWidth() / 2f * Draw.scl, 0, rotation - 90);
        }
    }
}
