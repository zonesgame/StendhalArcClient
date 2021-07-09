package z.entities.type.base;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Rect;
import arc.util.Time;
import arc.z.util.ZonesAnnotate;
import mindustry.game.Team;
import mindustry.type.UnitType;
import mindustry.world.blocks.Floor;
import z.entities.ani.SpriteAniControl;
import z.entities.traits.ShadowTrait;
import z.utils.ShapeRenderer;

import static mindustry.Vars.tilesize;
import static mindustry.gen.Tex.whiteui;
import static z.debug.ZDebug.enable_drawUnitCollision;
import static z.debug.ZDebug.enable_isoInput;
import static z.debug.ZDebug.use_shadowTrans;

/**
 *
 */
public class SpriteUnit extends BaseGroundUnit implements ShadowTrait {

//    private final float round = 360f;
//    private final int dirCount_8 = 8;
//    private SpriteAniControl aniControl = new SpriteAniControl();

    /** 阴影绘制使用*/
    private TextureRegion curRegion = null;
    private Rect curRect = new Rect();
    private float curx, cury;

    /** 状态动画索引*/
    private int stateIndex = 0;     // default idle     //     IDLE, WALK, ATTACK,
    /** 动画方向索引*/
    private int dirIndexLast = 0;

    private float idleTime;

    public SpriteUnit() {
    }

    @Override
    public void init(UnitType type, Team team) {
        super.init(type, team);
    }

    //    @Override
//    public void drawStats(){
//        super.drawStats();
////        Draw.color(Color.black, team.color, healthf() + Mathf.absin(Time.time(), Math.max(healthf() * 5f, 1f), 1f - healthf()));
////        Draw.rect(getPowerCellRegion(), x, y, rotation - 90);
////        Draw.color();
//
////        drawBackItems(item.amount > 0 ? 1f : 0f, false);
//
//        drawLight();
//    }

    @Override
    public void draw() {
        if (enable_drawUnitCollision) {
            ShapeRenderer.drawDiamondUnit(this.x, this.y, type.hitsize);
            Draw.color(Color.yellow);
            ShapeRenderer.drawDiamondUnit(this.x, this.y, type.hitsizeTile);
            Draw.color();
        }
        {
            //zones add begon
            float x = this.x;
            float y = this.y;
            float baseRotation = this.baseRotation;
            float rotation = this.rotation;
            if (enable_isoInput) {
                x = wpos.x;
                y = wpos.y;
                baseRotation = 360 - this.baseRotation + 45;
                rotation = 360 - this.rotation + 45;
            }
            // zones add end
            Draw.mixcol(Color.white, hitTime / hitDuration);

            float ft = Mathf.sin(walkTime * type.speed * 5f, 6f, 2f + type.hitsize / 15f);

            Floor floor = getFloorOn();

            if(floor.isLiquid){
                Draw.color(Color.white, floor.color, 0.5f);
            }

//            for(int i : Mathf.signs){
//                Draw.rect(type.legRegion,
//                        x + Angles.trnsx(baseRotation, ft * i),
//                        y + Angles.trnsy(baseRotation, ft * i),
//                        type.legRegion.getWidth() * i * Draw.scl, type.legRegion.getHeight() * Draw.scl - Mathf.clamp(ft * i, 0, 2), baseRotation - 90);
//            }

            if(floor.isLiquid){
                Draw.color(Color.white, floor.color, drownTime * 0.4f);
            }else{
                Draw.color(Color.white);
            }
            {
//                int stateIndex;     //     IDLE, WALK, ATTACK,
                int frame;
//                TextureRegion[][][] regions = type.spriteRegions;
//                Rect[][][] rects = type.spriteRects;
//                aniControl.setFrameData(Assets.spriteCenterPoint[stateIndex][dirIndex].length, (28f * 1f) / Assets.spriteCenterPoint[stateIndex][dirIndex].length).getKeyFrameIndex(stateDeltatime, true);

//                float dirAngle = round / dirCount_8;
//                float newAngle = (rotation + dirAngle / 2);
//                newAngle = newAngle % 360;
//                int dirIndex = (int) (newAngle / dirAngle);
                int dirIndex = SpriteAniControl.getFrameDirQQTX(rotation);

                float reloadPer = type.weapon.getReloadPerl(this);
                if (reloadPer != 1) {   // attack state
                    if (setAniState2(2)) {
                        dirIndexLast = dirIndex;
                    }
                    dirIndex = dirIndexLast;
//                    stateIndex = 2;
                    frame = SpriteAniControl.getFrameIndexFromScale(type.getFrameLength(stateIndex, dirIndexLast), false, reloadPer);
                }
                else if (walkTime != 0) {   // walk state
//                    stateIndex = 1;
                    setAniState(1);
                    frame = SpriteAniControl.getFrameIndexFromScale(type.getFrameLength(stateIndex, dirIndex), true, walkTime * type.speed * 1f);
                }
                else {      // idle state
//                    stateIndex = 0;
                    setAniState(0);
                    idleTime += Time.delta();
                    frame = SpriteAniControl.getFrameIndexFromScale(type.getFrameLength(stateIndex, dirIndex), true, idleTime * 0.03f);
                }

                // TextureRegion region;
                TextureRegion region = type.getFrameRegion(stateIndex, dirIndex, frame);
                Rect rect = type.getFrameRect(stateIndex, dirIndex, frame);
//                Draw.rectGdxOffset(region, x, y, -regionCenterpoint.x, -regionCenterpoint.y);
                float dx = x - rect.x;
                float dy = y - rect.y;
//                Draw.rectGdx(region, dx, dy, rect.width, rect.height);
                Draw.rectGdx(region, x, y, rect);
                {
                    this.curRegion = region;
                    this.curRect.set(rect);
                    this.curx = x;
                    this.cury = y;
                }
            }

//            Draw.rect(type.baseRegion, x, y, baseRotation - 90);
//
//            Draw.rect(type.region, x, y, rotation - 90);
//
//            for(int i : Mathf.signs){
//                float tra = rotation - 90, trY = -type.weapon.getRecoil(this, i > 0) + type.weaponOffsetY;
//                float w = -i * type.weapon.region.getWidth() * Draw.scl;
//                Draw.rect(type.weapon.region,
//                        x + Angles.trnsx(tra, getWeapon().width * i, trY),
//                        y + Angles.trnsy(tra, getWeapon().width * i, trY), w, type.weapon.region.getHeight() * Draw.scl, rotation - 90);
//            }

            Draw.mixcol();
        }
    }

    @ZonesAnnotate.ZAdd
    @Override
    public void drawShadow() {
        if (use_shadowTrans && curRegion != null) {
//            Draw.color(Color.red);
//            Draw.rectGdx(curRegion, curRect.x + 5, curRect.y + 5, curRect.width, curRect.height);
//            Draw.color();
//            curRect.x += 10;
//            curRect.y += 10;

//            Draw.shadowSG(curRegion, 50, -0, curRect.width * 1, curRect.height * 1);
//            Draw.rectGdx(curRegion, 50, -0, curRect.width * 1, curRect.height * 1);

            Draw.shadowSG(curRegion, curx, cury, curRect);
        }
    }

    @Override
    public void drawStats() {
        if (damaged()) {
            float x = this.x;
            float y = this.y;
            if (enable_isoInput) {
                x = wpos.x;
                y = wpos.y;
            }
            Draw.color(Color.black, team.color, healthf() + Mathf.absin(Time.time(), Math.max(healthf() * 5f, 1f), 1f - healthf()));
            Draw.rect(whiteui.getRegion(), x, y + (type.hitsize) * tilesize + 0.75f + 2, type.hitsize * healthf() * tilesize * 1.3f, 1.5f);       // zones editor
            Draw.color();
        }

        // zones add begon
        if (false) {     // selectState
            float x = wpos.x;
            float y = wpos.y;
//            Draw.color(Color.darkGray);
//            Fill.rect(x, y, drawSize() * Draw.scl * 0.7f, 8 * Draw.scl);
            Draw.color(Color.blue, 1f);
            Fill.poly(x, y +  (type.hitsize) * tilesize  * 1.3f + 0.75f + 2 + 1.5f, 3, type.hitsize * tilesize * 0.36f, -90);
            Draw.color();
        }
        // zones add end

        super.drawStats();
    }

    private void setAniState(int index) {
        if (index != stateIndex) {
            stateIndex = index;
            idleTime = 0;
        }
    }

    private boolean setAniState2(int index) {
        if (index != stateIndex) {
            stateIndex = index;
            idleTime = 0;
            return true;
        }
        return false;
    }
}
