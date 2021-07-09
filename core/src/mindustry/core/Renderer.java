package mindustry.core;

import arc.ApplicationListener;
import arc.Core;
import arc.Events;
import arc.files.Fi;
import arc.func.Cons;
import arc.graphics.Camera;
import arc.graphics.Color;
import arc.graphics.Pixmap;
import arc.graphics.PixmapIO;
import arc.graphics.g2d.Bloom;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.graphics.gl.FrameBuffer;
import arc.math.Angles;
import arc.math.Interpolation;
import arc.math.Mathf;
import arc.math.geom.Rect;
import arc.math.geom.Vec2;
import arc.scene.ui.layout.Scl;
import arc.struct.Array;
import arc.util.Buffers;
import arc.util.ScreenUtils;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.pooling.Pools;
import arc.z.util.ISOUtils;
import mindustry.content.Fx;
import mindustry.core.GameState.State;
import mindustry.entities.Effects;
import mindustry.entities.effect.GroundEffectEntity;
import mindustry.entities.effect.GroundEffectEntity.GroundEffect;
import mindustry.entities.traits.BelowLiquidTrait;
import mindustry.entities.traits.Entity;
import mindustry.entities.type.EffectEntity;
import mindustry.entities.type.Player;
import mindustry.entities.type.TileEntity;
import mindustry.entities.type.Unit;
import mindustry.game.EventType.DisposeEvent;
import mindustry.graphics.BlockRenderer;
import mindustry.graphics.CacheLayer;
import mindustry.graphics.Layer;
import mindustry.graphics.LightRenderer;
import mindustry.graphics.MinimapRenderer;
import mindustry.graphics.OverlayRenderer;
import mindustry.graphics.Pal;
import mindustry.graphics.Pixelator;
import mindustry.graphics.Shaders;
import mindustry.input.DesktopInput;
import mindustry.ui.Cicon;
import mindustry.world.blocks.defense.ForceProjector.ShieldEntity;
import z.ai.components.Squad;
import z.entities.traits.ShadowTrait;

import static arc.Core.camera;
import static arc.Core.graphics;
import static arc.Core.settings;
import static mindustry.Vars.blockunitGroup;
import static mindustry.Vars.bulletGroup;
import static mindustry.Vars.control;
import static mindustry.Vars.disableUI;
import static mindustry.Vars.effectGroup;
import static mindustry.Vars.groundEffectGroup;
import static mindustry.Vars.player;
import static mindustry.Vars.playerGroup;
import static mindustry.Vars.puddleGroup;
import static mindustry.Vars.screenshotDirectory;
import static mindustry.Vars.shieldGroup;
import static mindustry.Vars.state;
import static mindustry.Vars.systemStrategy;
import static mindustry.Vars.tilesize;
import static mindustry.Vars.ui;
import static mindustry.Vars.unitGroup;
import static mindustry.Vars.world;
import static z.debug.ZDebug.debug_drawPlayer;
import static z.debug.ZDebug.disable_cacheDraw;
import static z.debug.ZDebug.disable_draw;
import static z.debug.ZDebug.disable_floorRender;
import static z.debug.ZDebug.disable_staticWallRender;
import static z.debug.ZDebug.enable_drawDebug;
import static z.debug.ZDebug.enable_floorDebug;
import static z.debug.ZDebug.enable_isoInput;
import static z.debug.ZDebug.enable_qqtxbackImg;
import static z.debug.ZDebug.enable_shadowDraw;
import static z.debug.ZDebug.use_shadowTrans;

/**
 *  绘制系统
 * */
public class Renderer implements ApplicationListener{
    /** 块绘制器*/
    public final BlockRenderer blocks = new BlockRenderer();
    /** 小地图绘制器*/
    public final MinimapRenderer minimap = new MinimapRenderer();
    /** 上层绘制器*/
    public final OverlayRenderer overlays = new OverlayRenderer();
    /** 光源绘制器*/
    public final LightRenderer lights = new LightRenderer();
    /** 像素化*/
    public final Pixelator pixelator = new Pixelator();

    /** 护盾缓存*/
    public FrameBuffer shieldBuffer = new FrameBuffer(2, 2);
    /***/
    private Bloom bloom;
    /** 清除颜色*/
    private Color clearColor;
    /** 目标缩放*/
    private float targetscale = Scl.scl(4);
    /** 相机缩放*/
    private float camerascale = targetscale;
    /***/
    private float landscale = 0f, landTime;
    /***/
    private float minZoomScl = Scl.scl(0.01f);
    /***/
    private Rect rect = new Rect(), rect2 = new Rect();
    /** 屏幕震动*/
    private float shakeIntensity, shaketime;

    public Renderer(){
        camera = new Camera();
        Shaders.init();

        Effects.setScreenShakeProvider((intensity, duration) -> {
            shakeIntensity = Math.max(intensity, shakeIntensity);
            shaketime = Math.max(shaketime, duration);
        });

        Effects.setEffectProvider((effect, color, x, y, rotation, data) -> {
            if(effect == Fx.none) return;
            if(Core.settings.getBool("effects")){
                Rect view = camera.bounds(rect);
                Rect pos = rect2.setSize(effect.size).setCenter(x, y);

                if(view.overlaps(pos)){

                    if(!(effect instanceof GroundEffect)){
                        EffectEntity entity = Pools.obtain(EffectEntity.class, EffectEntity::new);
                        entity.effect = effect;
                        entity.color.set(color);
                        entity.rotation = rotation;
                        entity.data = data;
                        entity.id++;
                        entity.set(x, y);
                        if(data instanceof Entity){
                            entity.setParent((Entity)data);
                        }
                        effectGroup.add(entity);
                    }else{
                        GroundEffectEntity entity = Pools.obtain(GroundEffectEntity.class, GroundEffectEntity::new);
                        entity.effect = effect;
                        entity.color.set(color);
                        entity.rotation = rotation;
                        entity.id++;
                        entity.data = data;
                        entity.set(x, y);
                        if(data instanceof Entity){
                            entity.setParent((Entity)data);
                        }
                        groundEffectGroup.add(entity);
                    }
                }
            }
        });

        clearColor = new Color(0f, 0f, 0f, 1f);
    }

    @Override
    public void init(){
        if(settings.getBool("bloom")){
            setupBloom();
        }
    }

    @Override
    public void update(){
        Color.white.set(1f, 1f, 1f, 1f);

        camerascale = Mathf.lerpDelta(camerascale, targetscale, 0.1f);

        if(landTime > 0){
            landTime -= Time.delta();
            landscale = Interpolation.pow5In.apply(minZoomScl, Scl.scl(4f), 1f - landTime / Fx.coreLand.lifetime);
            camerascale = landscale;
        }

        camera.width = graphics.getWidth() / camerascale;
        camera.height = graphics.getHeight() / camerascale;

        if(state.is(State.menu)){
            landTime = 0f;
            graphics.clear(Color.black);
        }else{
            Vec2 position = Tmp.v3.set(player);
            if (enable_isoInput)
                position.set(player.wpos);

            if(player.isDead()){
                TileEntity core = player.getClosestCore();
                if(core != null){
                    if(player.spawner == null){
                        camera.position.lerpDelta(core.x, core.y, 0.08f);
                    }else{
                        camera.position.lerpDelta(position, 0.08f);
                    }
                }
            }else if(control.input instanceof DesktopInput && !state.isPaused()){
                camera.position.lerpDelta(position, 0.08f);
            }

            updateShake(0.75f);
            if(pixelator.enabled()){
                pixelator.drawPixelate();
            }else{
                draw();
            }
        }
    }

    public float landScale(){
        return landTime > 0 ? landscale : 1f;
    }

    @Override
    public void dispose(){
        minimap.dispose();
        shieldBuffer.dispose();
        blocks.dispose();
        if(bloom != null){
            bloom.dispose();
            bloom = null;
        }
        Events.fire(new DisposeEvent());
    }

    @Override
    public void resize(int width, int height){
        if(settings.getBool("bloom")){
            setupBloom();
        }
    }

    @Override
    public void resume(){
        if(settings.getBool("bloom") && bloom != null){
            bloom.resume();
        }
    }

    void setupBloom(){
        try{
            if(bloom != null){
                bloom.dispose();
                bloom = null;
            }
            bloom = new Bloom(true);
            bloom.setClearColor(0f, 0f, 0f, 0f);
        }catch(Exception e){
            e.printStackTrace();
            settings.put("bloom", false);
            settings.save();
            ui.showErrorMessage("$error.bloom");
        }
    }

    public void toggleBloom(boolean enabled){
        if(enabled){
            if(bloom == null){
                setupBloom();
            }
        }else{
            if(bloom != null){
                bloom.dispose();
                bloom = null;
            }
        }
    }

    void updateShake(float scale){
        if(shaketime > 0){
            float intensity = shakeIntensity * (settings.getInt("screenshake", 4) / 4f) * scale;
            camera.position.add(Mathf.range(intensity), Mathf.range(intensity));
            shakeIntensity -= 0.25f * Time.delta();
            shaketime -= Time.delta();
            shakeIntensity = Mathf.clamp(shakeIntensity, 0f, 100f);
        }else{
            shakeIntensity = 0f;
        }
    }

    /** 绘制游戏内容*/
    public void draw(){
        camera.update();

        if(Float.isNaN(camera.position.x) || Float.isNaN(camera.position.y)){       // 更新相机位置
            camera.position.x = player.x;
            camera.position.y = player.y;
            if (enable_isoInput) {
                camera.position.set(ISOUtils.tileToWorldCoords(player));
            }
        }

        graphics.clear(clearColor);     // 清除画布

        if(!graphics.isHidden() && (Core.settings.getBool("animatedwater") || Core.settings.getBool("animatedshields")) && (shieldBuffer.getWidth() != graphics.getWidth() || shieldBuffer.getHeight() != graphics.getHeight())){
            shieldBuffer.resize(graphics.getWidth(), graphics.getHeight());
        }   // 重置护盾尺寸

        Draw.proj(camera.projection()); // 绑定绘制尺寸
        {   // zones add code
//            blocks.processBlocks();
            if ( disable_cacheDraw) {
                Draw.color();
//                Draw.reset();
//                Draw.shader();
//                blocks.drawBlocks(Layer.normal);
//                blocks.floor.drawFloor();
//                Draw.flush();
            }
        }

        if ( !disable_floorRender && !disable_cacheDraw)
        blocks.floor.drawFloor();       // 绘制地板
        if ( enable_floorDebug) {
            blocks.floorDebug.drawFloor();
        }
        if(enable_qqtxbackImg) {
            blocks.floorDebug.drawBackgroundImage();
        }

        groundEffectGroup.draw(e -> e instanceof BelowLiquidTrait);
        puddleGroup.draw();
        groundEffectGroup.draw(e -> !(e instanceof BelowLiquidTrait));

        blocks.processBlocks();     // zones editor

        if ( !disable_draw)
        blocks.drawShadows();
        Draw.color();

        if ( !disable_staticWallRender && !disable_cacheDraw) {
            blocks.floor.beginDraw();
            blocks.floor.drawLayer(CacheLayer.walls);
            blocks.floor.endDraw();
        }
        if (enable_floorDebug) {
            blocks.floorDebug.drawLayer(CacheLayer.walls);
        }

        if (enable_isoInput) {
            blocks.drawBlocks(Layer.background);
        }
        blocks.drawBlocks(Layer.block);
        if ( !disable_draw)
        blocks.drawFog();

        blocks.drawDestroyed();

        Draw.shader(Shaders.blockbuild, true);
        blocks.drawBlocks(Layer.placement);
        Draw.shader();

        blocks.drawBlocks(Layer.overlay);

        drawGroundShadows();

        drawAllTeams(false);

        blocks.drawBlocks(Layer.turret);

        drawFlyerShadows();

        blocks.drawBlocks(Layer.power);
        blocks.drawBlocks(Layer.lights);

        drawAllTeams(true);

        Draw.flush();
        if(bloom != null){
            bloom.capture();
        }

        bulletGroup.draw();
        effectGroup.draw();

        Draw.flush();
        if(bloom != null){
            bloom.render();
        }

        if (enable_drawDebug) {
            for (Cons drawer : debugDrawer) {
                drawer.get(null);
            }
//            if (debugDrawerRemove.size > 0) {
//                debugDrawer.removeAll(debugDrawerRemove);
//            }
            Draw.flush();
        }

        overlays.drawBottom();
        playerGroup.draw(p -> p.isLocal, Player::drawBuildRequests);

        if(shieldGroup.countInBounds() > 0){
            if(settings.getBool("animatedshields") && Shaders.shield != null){
                Draw.flush();
                shieldBuffer.begin();
                graphics.clear(Color.clear);
                shieldGroup.draw();
                shieldGroup.draw(shield -> true, ShieldEntity::drawOver);
                Draw.flush();
                shieldBuffer.end();
                Draw.shader(Shaders.shield);
                Draw.color(Pal.accent);
                Draw.rect(Draw.wrap(shieldBuffer.getTexture()), camera.position.x, camera.position.y, camera.width, -camera.height);
                Draw.color();
                Draw.shader();
            }else{
                shieldGroup.draw(shield -> true, ShieldEntity::drawSimple);
            }
        }

        overlays.drawTop();

        playerGroup.draw(p -> !p.isDead() || debug_drawPlayer, Player::drawName);

        // zones add begon
        /* 绘制队伍数据*/
//        System.out.println("TimeTest: " + Time.time());
        Squad[] playerSquads = systemStrategy.getTeamSquads(player.getTeam().id);
        for (Squad squad : playerSquads) {
            squad.draw();
        }
        // zones add end

        if(state.rules.lighting){
            lights.draw();
        }

        drawLanding();

        Draw.color();
        Draw.flush();
    }

    /** 绘制登录*/
    private void drawLanding(){
        if(landTime > 0 && player.getClosestCore() != null){
            float fract = landTime / Fx.coreLand.lifetime;
            TileEntity entity = player.getClosestCore();

            TextureRegion reg = entity.block.icon(Cicon.full);
            float scl = Scl.scl(4f) / camerascale;
            float s = reg.getWidth() * Draw.scl * scl * 4f * fract;

            Draw.color(Pal.lightTrail);
            Draw.rect("circle-shadow", entity.x, entity.y, s, s);

            Angles.randLenVectors(1, (1f- fract), 100, 1000f * scl * (1f-fract), (x, y, fin, fout) -> {
                Lines.stroke(scl * fin);
                Lines.lineAngle(entity.x + x, entity.y + y, Mathf.angle(x, y), (fin * 20 + 1f) * scl);
            });

            Draw.color();
            Draw.mixcol(Color.white, fract);
            Draw.rect(reg, entity.x, entity.y, reg.getWidth() * Draw.scl * scl, reg.getHeight() * Draw.scl * scl, fract * 135f);

            Draw.reset();
        }
    }

    /** 绘制地面单位阴影*/
    private void drawGroundShadows(){
        if ( !enable_shadowDraw) return;

        if (use_shadowTrans) {
            Draw.color(Draw.SHADOW_TINT);
            unitGroup.draw(unit -> !unit.isDead() && unit instanceof ShadowTrait, unit -> {
                ((ShadowTrait) unit).drawShadow();
            });
            Draw.color();
            return;
        }

        Draw.color(0, 0, 0, 0.4f);
        float rad = 1.6f;

        Cons<Unit> draw = u -> {
            float size = Math.max(u.getIconRegion().getWidth(), u.getIconRegion().getHeight()) * Draw.scl;
            Draw.rect("circle-shadow", u.x, u.y, size * rad, size * rad);
        };

        unitGroup.draw(unit -> !unit.isDead(), draw::get);
        if (enable_isoInput) {
            blockunitGroup.draw(unit -> !unit.isDead(), draw::get);
        }

        if(!playerGroup.isEmpty()){
            playerGroup.draw(unit -> !unit.isDead() || debug_drawPlayer, draw::get);
        }

        Draw.color();
    }

    /** 绘制飞行单位阴影*/
    private void drawFlyerShadows(){
        float trnsX = -12, trnsY = -13;
        Draw.color(0, 0, 0, 0.22f);

        unitGroup.draw(unit -> unit.isFlying() && !unit.isDead(), baseUnit -> baseUnit.drawShadow(trnsX, trnsY));
        if (enable_isoInput) {
            blockunitGroup.draw(unit -> unit.isFlying() && !unit.isDead(), baseUnit -> baseUnit.drawShadow(trnsX, trnsY));
        }
        playerGroup.draw(unit -> unit.isFlying() && ( !unit.isDead() || debug_drawPlayer), player -> player.drawShadow(trnsX, trnsY));

        Draw.color();
    }

    /** 绘制所有队伍单位*/
    private void drawAllTeams(boolean flying){
        unitGroup.draw(u -> u.isFlying() == flying && !u.isDead(), Unit::drawUnder);
        if (enable_isoInput) {
            blockunitGroup.draw(u -> u.isFlying() == flying && !u.isDead(), Unit::drawUnder);
        }
        playerGroup.draw(p -> p.isFlying() == flying && ( !p.isDead() || debug_drawPlayer), Unit::drawUnder);

        unitGroup.draw(u -> u.isFlying() == flying && !u.isDead(), Unit::drawAll);
        if (enable_isoInput) {
            blockunitGroup.draw(u -> u.isFlying() == flying && !u.isDead(), Unit::drawAll);
        }
        playerGroup.draw(p -> p.isFlying() == flying, Unit::drawAll);

        unitGroup.draw(u -> u.isFlying() == flying && !u.isDead(), Unit::drawOver);
        if(enable_isoInput) {
            blockunitGroup.draw(u -> u.isFlying() == flying && !u.isDead(), Unit::drawOver);
        }
        playerGroup.draw(p -> p.isFlying() == flying, Unit::drawOver);
    }

    /** 缩放相机*/
    public void scaleCamera(float amount){
        targetscale += amount;
        clampScale();
    }

    /** 计算相机缩放*/
    public void clampScale(){
        float s = Scl.scl(1f);
        targetscale = Mathf.clamp(targetscale, s * 1.5f, Math.round(s * 6));
    }

    /** 获取相机目标缩放值*/
    public float getScale(){
        return targetscale;
    }

    /** 设置相机缩放值*/
    public void setScale(float scl){
        targetscale = scl;
        clampScale();
    }

    public void zoomIn(float duration){
        landscale = minZoomScl;
        landTime = duration;
    }

    /** 屏幕截图*/
    public void takeMapScreenshot(){
        drawGroundShadows();

        int w = world.width() * tilesize, h = world.height() * tilesize;
        int memory = w * h * 4 / 1024 / 1024;

        if(memory >= 65){
            ui.showInfo("$screenshot.invalid");
            return;
        }

        FrameBuffer buffer = new FrameBuffer(w, h);

        float vpW = camera.width, vpH = camera.height, px = camera.position.x, py = camera.position.y;
        disableUI = true;
        camera.width = w;
        camera.height = h;
        camera.position.x = w / 2f + tilesize / 2f;
        camera.position.y = h / 2f + tilesize / 2f;
        Draw.flush();
        buffer.begin();
        draw();
        Draw.flush();
        buffer.end();
        disableUI = false;
        camera.width = vpW;
        camera.height = vpH;
        camera.position.set(px, py);
        buffer.begin();
        byte[] lines = ScreenUtils.getFrameBufferPixels(0, 0, w, h, true);
        for(int i = 0; i < lines.length; i += 4){
            lines[i + 3] = (byte)255;
        }
        buffer.end();
        Pixmap fullPixmap = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        Buffers.copy(lines, 0, fullPixmap.getPixels(), lines.length);
        Fi file = screenshotDirectory.child("screenshot-" + Time.millis() + ".png");
        PixmapIO.writePNG(file, fullPixmap);
        fullPixmap.dispose();
        ui.showInfoFade(Core.bundle.format("screenshot", file.toString()));

        buffer.dispose();
    }


    // zones add begon
    /** 调试绘制数据*/
    private static Array<Cons> debugDrawer = new Array<>(true, 16);
//    private static Array<Cons> debugDrawerRemove = new Array<>();
    private float _time;

    public static void addDebugDraw(Cons cons, float removeTime) {
        debugDrawer.add(cons);
        Time.run(removeTime, () -> debugDrawer.remove(cons));
    }

    public static void addDebugDraw(Cons cons) {
        addDebugDraw(cons, 30);
    }
    // zones add end

}
