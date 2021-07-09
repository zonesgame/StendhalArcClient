package z.world.blocks;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import mindustry.graphics.CacheLayer;
import mindustry.world.Block;
import mindustry.world.Tile;
import z.utils.ShapeRenderer;

/**
 *  Caesar扩展固体块
 * */
public class StaticBlock extends Block{
    protected int variants;

    TextureRegion large;
    TextureRegion[][] split;

    public StaticBlock(String name){
        super(name);
        breakable = true;
        alwaysReplace = true;

        breakable = alwaysReplace = false;
        solid = true;
        variants = 2;
        cacheLayer = CacheLayer.walls;
    }

    @Override
    public void draw(Tile tile){
        ShapeRenderer.drawDiamond(tile.x, tile.y, tile.block().size);
        if(variants > 0){
            Draw.rect(variantRegions[Mathf.randomSeed(tile.pos(), 0, Math.max(0, variantRegions.length - 1))], tile.worldx(), tile.worldy());
        }else{
            Vec2 pos = Vec2.TEMP2;
            pos.set(tile.drawxIso(), tile.drawyIso());
            Vec2 offset = regionCenter(0);
            Draw.rectGdxOffset(region, pos.x, pos.y, -offset.x, -offset.y);
//            Draw.rect(region, tile.worldx(), tile.worldy());
        }
    }

    @Override
    public TextureRegion[] generateIcons(){
        return variants == 0 ? super.generateIcons() : new TextureRegion[]{Core.atlas.find(name + "1")};
    }

    @Override
    public void load(){
        super.load();

        if(variants > 0){
            variantRegions = new TextureRegion[variants];

            for(int i = 0; i < variants; i++){
                variantRegions[i] = Core.atlas.find(name + (i + 1));
            }
        }
    }

    // temp begon
    protected Vec2[] regionsOffset;

    @Override
    public Vec2 regionCenter(int variant) {
        return regionsOffset[variant];
    }
    // temp end
}
