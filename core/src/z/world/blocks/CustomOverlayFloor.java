package z.world.blocks;

/**
 *
 */

import arc.graphics.g2d.Draw;
import arc.math.Mathf;
import mindustry.world.Tile;
import mindustry.world.blocks.OverlayFloor;

/**
 * A type of floor that is overlaid on top of over floors.
 * */
public class CustomOverlayFloor extends OverlayFloor {

    public CustomOverlayFloor(String name){
        super(name);
    }

    @Override
    public void draw(Tile tile){
        Draw.rect(variantRegions[Mathf.randomSeed(tile.pos(), 0, Math.max(0, variantRegions.length - 1))], tile.worldx(), tile.worldy());
    }
}
