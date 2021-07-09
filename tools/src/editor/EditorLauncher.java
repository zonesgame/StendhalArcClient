package editor;

import arc.ApplicationCore;
import arc.ApplicationListener;
import arc.Core;
import arc.Events;
import arc.assets.AssetDescriptor;
import arc.assets.AssetManager;
import arc.assets.Loadable;
import arc.graphics.Texture;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.SpriteBatch;
import arc.graphics.g2d.TextureAtlas;
import arc.math.Mathf;
import arc.util.Time;
import mindustry.Vars;
import mindustry.core.FileTree;
import mindustry.game.EventType;
import mindustry.maps.MapPreviewLoader;
import mindustry.mod.Mods;
import mindustry.net.Net;
import mindustry.ui.Fonts;

import static arc.Core.assets;
import static arc.Core.atlas;
import static arc.Core.batch;
import static mindustry.Vars.mapExtension;
import static mindustry.Vars.mods;
import static mindustry.Vars.platform;
import static mindustry.Vars.tree;

public class EditorLauncher extends ApplicationCore {
    private static final int loadingFPS = 20;

    private float smoothProgress;
    private long lastTime;
    private long beginTime;
    private boolean finished = false;

    @Override
    public void setup(){
        if (true) {
            Vars.loadLogger();
            Vars.loadFileLogger();
            batch = new SpriteBatch();
            assets = new AssetManager();

            testInit();
            return;
        }

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

    }

    private void testInit () {

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
        super.update();
        draw();
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

    void draw(){
    }
}
