package z.core;

import mindustry.annotations.Annotations;
import mindustry.content.Fx;
import mindustry.entities.Effects;
import mindustry.entities.type.Player;
import mindustry.world.Tile;
import mindustry.world.blocks.storage.CoreBlock;

/**
 *
 */
public class Remotes {

    /**
     * 单位重生回调
     * @apiNote from mindustry.world.blocks.storage.CoreBlock
     * */
    @Annotations.Remote(called = Annotations.Loc.server)
    public static void onUnitRespawn(Tile tile, Player player){
        if(player == null || tile.entity == null) return;

        CoreBlock.CoreEntity entity = tile.ent();
        Effects.effect(Fx.spawn, entity);
        entity.progress = 0;
        entity.spawnPlayer = player;
        entity.spawnPlayer.onRespawn(tile);
        entity.spawnPlayer.applyImpulse(0, 8f);
        entity.spawnPlayer = null;
    }

}
