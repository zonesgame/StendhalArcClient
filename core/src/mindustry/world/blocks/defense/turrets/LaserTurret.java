package mindustry.world.blocks.defense.turrets;

import arc.math.*;
import arc.util.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.entities.type.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.consumers.*;
import mindustry.world.meta.*;
import mindustry.world.meta.values.*;

import static mindustry.Vars.tilesize;

public class LaserTurret extends PowerTurret{
    public float firingMoveFract = 0.25f;
    public float shootDuration = 100f;

    public LaserTurret(String name){
        super(name);
        canOverdrive = false;

        consumes[0].add(new ConsumeLiquidFilter(liquid -> liquid.temperature <= 0.5f && liquid.flammability < 0.1f, 0.01f)).update(false);
        coolantMultiplier = 1f;
        entityType = LaserTurretEntity::new;
    }

    @Override
    public void setStats(){
        super.setStats();

        stats.remove(BlockStat.booster);
        stats.add(BlockStat.input, new BoosterListValue(reload, consumes[0].<ConsumeLiquidBase>get(ConsumeType.liquid).amount, coolantMultiplier, false, l -> consumes[0].liquidfilters.get(l.id)));
        stats.remove(BlockStat.damage);
        //damages every 5 ticks, at least in meltdown's case
        stats.add(BlockStat.damage, shootType.damage * 60f / 5f, StatUnit.perSecond);
    }

    @Override
    public void update(Tile tile){
        super.update(tile);

        LaserTurretEntity entity = tile.ent();

        if(entity.bulletLife > 0 && entity.bullet != null){
            tr.trns(entity.rotation, size * tilesize / 2f, 0f);
            entity.bullet.rot(entity.rotation);
            entity.bullet.set(tile.drawx() + tr.x, tile.drawy() + tr.y);
            entity.bullet.time(0f);
            entity.heat = 1f;
            entity.recoil = recoil;
            entity.bulletLife -= Time.delta();
            if(entity.bulletLife <= 0f){
                entity.bullet = null;
            }
        }
    }

    @Override
    protected void updateShooting(Tile tile){
        LaserTurretEntity entity = tile.ent();

        if(entity.bulletLife > 0 && entity.bullet != null){
            return;
        }

        if(entity.reload >= reload && (entity.cons.valid() || tile.isEnemyCheat())){
            BulletType type = peekAmmo(tile);

            shoot(tile, type);

            entity.reload = 0f;
        }else{
            Liquid liquid = entity.liquids.current();
            float maxUsed = consumes[0].<ConsumeLiquidBase>get(ConsumeType.liquid).amount;

            float used = baseReloadSpeed(tile) * (tile.isEnemyCheat() ? maxUsed : Math.min(entity.liquids.get(liquid), maxUsed * Time.delta())) * liquid.heatCapacity * coolantMultiplier;
            entity.reload += used;
            entity.liquids.remove(liquid, used);

            if(Mathf.chance(0.06 * used)){
                Effects.effect(coolEffect, tile.drawx() + Mathf.range(size * tilesize / 2f), tile.drawy() + Mathf.range(size * tilesize / 2f));
            }
        }
    }

    @Override
    protected void turnToTarget(Tile tile, float targetRot){
        LaserTurretEntity entity = tile.ent();

        entity.rotation = Angles.moveToward(entity.rotation, targetRot, rotatespeed * entity.delta() * (entity.bulletLife > 0f ? firingMoveFract : 1f));
    }

    @Override
    protected void bullet(Tile tile, BulletType type, float angle){
        LaserTurretEntity entity = tile.ent();

        entity.bullet = Bullet.create(type, tile.entity, tile.getTeam(), tile.drawx() + tr.x, tile.drawy() + tr.y, angle);
        entity.bulletLife = shootDuration;
    }

    @Override
    public boolean shouldActiveSound(Tile tile){
        LaserTurretEntity entity = tile.ent();

        return entity.bulletLife > 0 && entity.bullet != null;
    }

    class LaserTurretEntity extends TurretEntity{
        Bullet bullet;
        float bulletLife;
    }
}
