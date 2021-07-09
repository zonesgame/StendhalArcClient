package mindustry.world.blocks.defense.turrets;

import arc.math.Mathf;
import mindustry.entities.bullet.BulletType;
import mindustry.world.Tile;
import mindustry.world.meta.BlockStat;
import mindustry.world.meta.StatUnit;

import static mindustry.Vars.tilesize;
import static mindustry.Vars.tileunit;
import static z.debug.ZDebug.enable_isoInput;

/**
 *  双管 物品炮塔块.
 * */
public class DoubleTurret extends ItemTurret{
    public float shotWidth = enable_isoInput ? 2f / tilesize : 2f;

    public DoubleTurret(String name){
        super(name);
        shots = 2;
    }

    @Override
    public void setStats(){
        super.setStats();

        stats.remove(BlockStat.reload);
        stats.add(BlockStat.reload, 60f / reload, StatUnit.none);
    }

    @Override
    protected void shoot(Tile tile, BulletType ammo){
        TurretEntity entity = tile.ent();
        entity.shots++;

        int i = Mathf.signs[entity.shots % 2];

        if (enable_isoInput) {
            tr.trns(entity.rotation - 90, shotWidth * i, size * tileunit / 2f);
        } else {
            tr.trns(entity.rotation - 90, shotWidth * i, size * tilesize / 2);
        }
        bullet(tile, ammo, entity.rotation + Mathf.range(inaccuracy));

        effects(tile);
        useAmmo(tile);
    }
}
