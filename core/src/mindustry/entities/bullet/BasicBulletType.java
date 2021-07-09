package mindustry.entities.bullet;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.geom.Vec2;
import arc.z.util.ISOUtils;
import mindustry.entities.type.Bullet;
import mindustry.graphics.Pal;

import static mindustry.Vars.tilesize;
import static z.debug.ZDebug.enable_isoInput;

/**
 *  一个扩展的子弹框架, 对大多数基于ammo的子弹从炮塔和单位发射.<p/>
 * An extended BulletType for most ammo-based bullets shot from turrets and units.
 * */
public class BasicBulletType extends BulletType{
    /** 背部颜色*/
    public Color backColor = Pal.bulletYellowBack, /** 前部颜色*/frontColor = Pal.bulletYellow;
    /** 子弹宽度(绘制使用)*/
    public float bulletWidth = 5f, /** 子弹高度(绘制使用)*/bulletHeight = 7f;
    /** 子弹衰减*/
    public float bulletShrink = 0.5f;
    /** 子弹纹理key*/
    public String bulletSprite;

    /** 背部纹理*/
    public TextureRegion backRegion;
    /** 前部纹理*/
    public TextureRegion frontRegion;

    public BasicBulletType(float speed, float damage, String bulletSprite){
        super(speed, damage);
        this.bulletSprite = bulletSprite;
    }

    /** For mods. */
    public BasicBulletType(){
        this(enable_isoInput ? 1f / tilesize : 1f, 1f, "bullet");
    }

    @Override
    public void load(){
        backRegion = Core.atlas.find(bulletSprite + "-back");
        frontRegion = Core.atlas.find(bulletSprite);
    }

    @Override
    public void draw(Bullet b){
        // zones add begon
        float x = b.x;
        float y = b.y;
        float rotation = b.rot();
        if (enable_isoInput) {
            Vec2 pos = ISOUtils.tileToWorldCoords(b);
            x = pos.x;
            y = pos.y;
            rotation = 360 - b.rot() + 45;
        }
        // zones add end
        float height = bulletHeight * ((1f - bulletShrink) + bulletShrink * b.fout());

        Draw.color(backColor);
        Draw.rect(backRegion, x, y, bulletWidth, height, rotation - 90);        // zones edirot b.rot()
        Draw.color(frontColor);
        Draw.rect(frontRegion, x, y, bulletWidth, height, rotation - 90);
        Draw.color();
    }
}
