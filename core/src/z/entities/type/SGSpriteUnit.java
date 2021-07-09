package z.entities.type;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.util.Time;
import arc.z.util.ISOUtils;
import mindustry.world.blocks.Floor;
import z.debug.Assets;
import z.entities.ani.SpriteAniControl;
import z.entities.type.base.SGGroundUnit;

/**
 *
 */
public class SGSpriteUnit extends SGGroundUnit {

    private final float round = 360f;
    private final int dirCount_8 = 8;
    private SpriteAniControl aniControl = new SpriteAniControl();

    @Override
    public void drawStats(){
//        Draw.color(Color.black, team.color, healthf() + Mathf.absin(Time.time(), Math.max(healthf() * 5f, 1f), 1f - healthf()));
//        Draw.rect(getPowerCellRegion(), x, y, rotation - 90);
//        Draw.color();

//        drawBackItems(item.amount > 0 ? 1f : 0f, false);

        drawLight();
    }

    @Override
    public void draw() {
        // zones new add begon
        Vec2 pos = ISOUtils.tileToWorldCoords(this.x, this.y);
        float x = pos.x;
        float y = pos.y;
        // zones new add end
        Draw.mixcol(Color.white, hitTime / hitDuration);

        float ft = Mathf.sin(walkTime * type.speed * 5f, 6f, 2f + type.hitsize / 15f);

        Floor floor = getFloorOn();

//        if (floor.isLiquid) {
//            Draw.color(Color.white, floor.color, 0.5f);
//        }
//
//        for (int i : Mathf.signs) {     // 腿部绘制
//            Draw.rect(type.legRegion,
//                    x + Angles.trnsx(baseRotation, ft * i),
//                    y + Angles.trnsy(baseRotation, ft * i),
//                    type.legRegion.getWidth() * i * Draw.scl, type.legRegion.getHeight() * Draw.scl - Mathf.clamp(ft * i, 0, 2), baseRotation - 90);
//        }

        if (floor.isLiquid) {
            Draw.color(Color.white, floor.color, drownTime * 0.4f);
        } else {
            Draw.color(Color.white);
        }

//        Draw.rect(type.baseRegion, x, y, baseRotation - 90);

//        Draw.rect(type.region, x, y, rotation - 90);
        {   // zones add code
            // 三国对象绘制区域
            stateDeltatime += Time.delta();
            // 动画状态获取
            int stateIndex = stateSprite.ordinal();

            // 方向获取
            float dirAngle = round / dirCount_8;
            float newAngle = (rotation + dirAngle / 2);
            newAngle = newAngle % 360;
            int dirIndex = (int) (newAngle / dirAngle);

            // 获取动画纹理
            int frame = aniControl.setFrameData(Assets.spriteCenterPoint[stateIndex][dirIndex].length, (28f * 1f) / Assets.spriteCenterPoint[stateIndex][dirIndex].length).getKeyFrameIndex(stateDeltatime, true);

            TextureRegion region = Assets.spriteRegions[stateIndex][dirIndex][frame];
            Vec2 regionCenterpoint = Assets.spriteCenterPoint[stateIndex][dirIndex][frame];
            Draw.rectGdxOffsetScaleY(region, x, y, -regionCenterpoint.x, -regionCenterpoint.y);

            if (selectState) {
                Draw.color(Color.darkGray);
                Fill.rect(x, y, drawSize() * Draw.scl * 0.7f, 8 * Draw.scl);
                Draw.color(Color.scarlet);
                Fill.rect(x, y, drawSize() * Draw.scl * 0.7f, 8 * Draw.scl);
            }
        }

//        for (int i : Mathf.signs) { // 绘制武器
//            float tra = rotation - 90, trY = -type.weapon.getRecoil(this, i > 0) + type.weaponOffsetY;
//            float w = -i * type.weapon.region.getWidth() * Draw.scl;
//            Draw.rect(type.weapon.region,
//                    x + Angles.trnsx(tra, getWeapon().width * i, trY),
//                    y + Angles.trnsy(tra, getWeapon().width * i, trY), w, type.weapon.region.getHeight() * Draw.scl, rotation - 90);
//        }

        Draw.mixcol();
    }

}
