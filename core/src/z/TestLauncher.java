package z;

import arc.ApplicationCore;
import arc.ApplicationListener;
import arc.Core;
import arc.Events;
import arc.assets.AssetDescriptor;
import arc.assets.AssetManager;
import arc.assets.Loadable;
import arc.graphics.Color;
import arc.graphics.Pixmap;
import arc.graphics.Texture;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.PixmapRegion;
import arc.graphics.g2d.SpriteBatch;
import arc.graphics.g2d.TextureAtlas;
import arc.graphics.g2d.TextureRegion;
import arc.input.KeyCode;
import arc.math.Mathf;
import arc.scene.ui.layout.Scl;
import arc.util.Time;
import arc.util.async.Threads;
import mindustry.Vars;
import mindustry.core.FileTree;
import mindustry.game.EventType;
import mindustry.graphics.Shaders;
import mindustry.maps.MapPreviewLoader;
import mindustry.mod.Mods;
import mindustry.net.Net;
import mindustry.ui.Fonts;
import z.ai.components.Squad;
import z.debug.DAIEntity;
import z.debug.TargetPoint;

import static arc.Core.assets;
import static arc.Core.atlas;
import static arc.Core.batch;
import static mindustry.Vars.mapExtension;
import static mindustry.Vars.mods;
import static mindustry.Vars.platform;
import static mindustry.Vars.tree;

/**
 *
 */
public class TestLauncher extends ApplicationCore {
    private static final int loadingFPS = 20;

    private float smoothProgress;
    private long lastTime;
    private long beginTime;
    private boolean finished = false;

    @Override
    public void setup(){
        Events.fire(new EventType.ClientCreateEvent());

        Vars.loadLogger();
        Vars.loadFileLogger();
        beginTime = Time.millis();

        Time.setDeltaProvider(() -> {
            float result = Core.graphics.getDeltaTime() * 60f;
            return (Float.isNaN(result) || Float.isInfinite(result)) ? 1f : Mathf.clamp(result, 0.0001f, 60f / 10f);
        });

        batch = new SpriteBatch();
        assets = new AssetManager();
        assets.setLoader(Texture.class, "." + mapExtension, new MapPreviewLoader());

        tree = new FileTree();

        assets.load("sprites/error.png", Texture.class);
        atlas = TextureAtlas.blankAtlas();
        Vars.net = new Net(platform.getNet());
        mods = new Mods();

        Fonts.loadSystemCursors();

//        assets.load(new Vars());

        Fonts.loadDefaultFont();

        assets.load(new AssetDescriptor<>("sprites/sprites.atlas", TextureAtlas.class)).loaded = t -> {
            atlas = (TextureAtlas)t;
            Fonts.mergeFontAtlas(atlas);
        };

        Shaders.init();
    }

    @Override
    public void add(ApplicationListener module){
        super.add(module);

        //autoload modules when necessary
        if(module instanceof Loadable){
            assets.load((Loadable)module);
        }
    }

    @Override
    public void resize(int width, int height){
        if(assets == null) return;

        if(!finished){
            Draw.proj().setOrtho(0, 0, width, height);
        }else{
            super.resize(width, height);
        }
    }

    @Override
    public void update(){
        if(!finished){

            if(assets.update(1000 / loadingFPS)){
                finished = true;
            }
        }else{
            super.update();
            drawLoading();
        }

        int targetfps = Core.settings.getInt("fpscap", 120);

        if(targetfps > 0 && targetfps <= 240){
            long target = (1000 * 1000000) / targetfps; //target in nanos
            long elapsed = Time.timeSinceNanos(lastTime);
            if(elapsed < target){
                Threads.sleep((target - elapsed) / 1000000, (int)((target - elapsed) % 1000000));
            }
        }

        lastTime = Time.nanos();
    }

    @Override
    public void init(){
        setup();
    }

    @Override
    public void resume(){
        if(finished){
            super.resume();
        }
    }

    @Override
    public void pause(){
        if(finished){
            super.pause();
        }
    }

    Squad squad;
    TargetPoint targetPoint = new TargetPoint( 500, 300);
    float deltatime = 0;
    int key = Squad.FormationPatternType.values().length;
    int k = 0;
    void drawLoading(){
        smoothProgress = Mathf.lerpDelta(smoothProgress, assets.getProgress(), 0.1f);

        Core.graphics.clear(Color.white);
        Draw.proj().setOrtho(0, 0, Core.graphics.getWidth(), Core.graphics.getHeight());

        if (true) {
            TextureRegion region1 = new TextureRegion(new Texture("D:\\Downloads\\OneDrive\\桌面\\temp.png"));
            TextureRegion region;
            {
                Pixmap pixmap = new Pixmap(region1.getWidth() - 200, region1.getHeight() - 200);
                pixmap.draw(new PixmapRegion(region1.getTexture().getTextureData().getPixmap(),
                        100, 100, region1.getWidth() - 200, region1.getHeight() - 200));
                pixmap.draw(new PixmapRegion(region1.getTexture().getTextureData().getPixmap(),
                        200, 200, region1.getWidth() - 200, region1.getHeight() - 200));
                region = new TextureRegion(new Texture(pixmap));
//                pixmap.dispose();
            }
            float x = 200;
            float y = 200;
            float width = region.getWidth() * 0.5f;
            float height = region.getHeight() * 0.5f;

//            Draw.shader(diablo);
//            diablo.setPalette(null);
//            if (true) diablo.setBlendMode(BlendMode.SOLID,  new Color(0, 0, 0, 0.50f));
            Draw.color(new Color(0, 0, 0, 0.50f));

            Draw.shadowSG(region, x, y, width, height);
            Draw.shadowSG(region, x + 200, y + 200, width, height);

            Draw.color();
//            if (true) diablo.resetBlendMode();
            Draw.shader();
            Draw.flush();

            Draw.rectGdx(region, x, y, width, height);

            Draw.reset();
            Draw.flush();
            return;
        }

        float height = Scl.scl(50f);

        Draw.color(Color.black);
        Fill.poly(Core.graphics.getWidth()/2f, Core.graphics.getHeight()/2f, 6, Mathf.dst(Core.graphics.getWidth()/2f, Core.graphics.getHeight()/2f) * smoothProgress);

        Draw.color(Color.red);
        Draw.rect("white", 200, 200, 50, 50);

        Draw.reset();
        Draw.flush();

        if (true)
            return;

        {
            if (squad == null) {
                squad = new Squad();
                squad.addMember(new DAIEntity());
                squad.addMember(new DAIEntity());
                squad.addMember(new DAIEntity());
                squad.addMember(new DAIEntity());
                squad.addMember(new DAIEntity());

            }
//            squad.update();
//            squad.formation.updateSlotAssignments();
        }

//
        Draw.reset();

        Draw.color(Color.red);
        for (Object entity : squad.getMembers()) {
            DAIEntity entity1 = (DAIEntity) entity;
//            System.out.println(entity.position);
            Fill.circle(entity1.position.x, entity1.position.y,  8);
        }
        Draw.reset();

        {   // 输入事件处理
            if (Core.input.keyRelease(KeyCode.NUMPAD_1)) {
                targetPoint.setOrientation(20);
            }
            if (Core.input.keyRelease(KeyCode.NUMPAD_2)) {
                targetPoint.setOrientation(-20);
            }

            if (Core.input.keyRelease(KeyCode.LEFT)) {
                targetPoint.pos.x -= 20;
            }
            if (Core.input.keyRelease(KeyCode.RIGHT)) {
                targetPoint.pos.x += 20;
            }
            if (Core.input.keyRelease(KeyCode.UP)) {
                targetPoint.pos.y += 20;
            }
            if (Core.input.keyRelease(KeyCode.DOWN)) {
                targetPoint.pos.y -= 20;
            }

            if (Core.input.keyRelease(KeyCode.NUMPAD_7)) {
                squad.addMember(new DAIEntity());
            }
            if (Core.input.keyRelease(KeyCode.NUMPAD_8)) {
//               if (squad.members.size > 0) {
//                   squad.removeMember(squad.members.first());
//               }
            }

            if (Core.input.keyRelease(KeyCode.NUMPAD_3)) {
                k++;
                if (k >= key) k = 0;
                squad.setFormationPattern(Squad.FormationPatternType.values()[k]);
            }

            if (Core.input.keyRelease(KeyCode.NUMPAD_4)) {
            }
            if (Core.input.keyRelease(KeyCode.NUMPAD_5)) {
            }
        }

//        float w = graphics.getWidth()*0.6f;
//
//        Draw.color(Color.black);
//        Fill.rect(graphics.getWidth()/2f, graphics.getHeight()/2f, w, height);
//
//        Draw.color(Pal.accent);
//        Fill.crect(graphics.getWidth()/2f-w/2f, graphics.getHeight()/2f - height/2f, w * smoothProgress, height);
//
//        for(int i : Mathf.signs){
//            Fill.tri(graphics.getWidth()/2f + w/2f*i, graphics.getHeight()/2f + height/2f, graphics.getWidth()/2f + w/2f*i, graphics.getHeight()/2f - height/2f, graphics.getWidth()/2f + w/2f*i + height/2f*i, graphics.getHeight()/2f);
//        }
//
//        if(assets.isLoaded("outline")){
//            BitmapFont font = assets.get("outline");
//            font.draw((int)(assets.getProgress() * 100) + "%", graphics.getWidth() / 2f, graphics.getHeight() / 2f + Scl.scl(10f), Align.center);
//            font.draw(bundle.get("loading", "").replace("[accent]", ""), graphics.getWidth() / 2f, graphics.getHeight() / 2f + height / 2f + Scl.scl(20), Align.center);
//
//            if(assets.getCurrentLoading() != null){
//                String name = assets.getCurrentLoading().fileName.toLowerCase();
//                String key = name.contains("script") ? "scripts" : name.contains("content") ? "content" : name.contains("mod") ? "mods" : name.contains("msav") ||
//                        name.contains("maps") ? "map" : name.contains("ogg") || name.contains("mp3") ? "sound" : name.contains("png") ? "image" : "system";
//                font.draw(bundle.get("load." + key, ""), graphics.getWidth() / 2f, graphics.getHeight() / 2f - height / 2f - Scl.scl(10f), Align.center);
//            }
//        }
        Draw.flush();
    }
}
