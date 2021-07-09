package z.world.blocks;

/**
 *
 */

import arc.graphics.g2d.Draw;
import arc.math.Mathf;
import mindustry.entities.type.Player;
import mindustry.world.Tile;
import mindustry.world.blocks.OverlayFloor;

/**
 * A type of floor that is overlaid on top of over floors.
 * */
public class SpawnPlayerOverlayFloor extends OverlayFloor {

    public SpawnPlayerOverlayFloor(String name) {
        super(name);

//        this.entityType = SpawnPlayerOverlayFloorEntity::new;
    }

    public void onUnitRespawn(Tile tile, Player player){
        if(player == null || tile == null) return;

//        SpawnPlayerOverlayFloorEntity entity = tile.ent();
//        Effects.effect(Fx.spawn, entity);
//        entity.progress = 0;
//        entity.spawnPlayer = player;
        player.onRespawn(tile);
//        entity.spawnPlayer.applyImpulse(0, 8f);
//        entity.spawnPlayer = null;
    }

    @Override
    public void draw(Tile tile) {
        Draw.rect(variantRegions[Mathf.randomSeed(tile.pos(), 0, Math.max(0, variantRegions.length - 1))], tile.worldx(), tile.worldy());
    }


//    public class SpawnPlayerOverlayFloorEntity extends TileEntity implements SpawnerTrait {      //
//        public Player spawnPlayer;
//        public float progress;
//        public float time;
//        public float heat;
//
//        @Override
//        public boolean hasUnit(Unit unit) {
//            return unit == spawnPlayer;
//        }
//
//        @Override
//        public void updateSpawning(Player player) {
//            if (!netServer.isWaitingForPlayers() && spawnPlayer == null) {
//                spawnPlayer = player;
//                progress = 0f;
//                player.beginRespawning(this);
//            }
//        }
//    }
}
