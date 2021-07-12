package stendhal;

import arc.ApplicationCore;
import arc.ApplicationListener;
import arc.Core;
import arc.Events;
import arc.assets.AssetDescriptor;
import arc.assets.AssetManager;
import arc.assets.Loadable;
import arc.assets.loaders.MusicLoader;
import arc.assets.loaders.SoundLoader;
import arc.audio.Music;
import arc.audio.Sound;
import arc.graphics.Color;
import arc.graphics.Texture;
import arc.graphics.g2d.BitmapFont;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.SpriteBatch;
import arc.graphics.g2d.TextureAtlas;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.scene.ui.layout.Scl;
import arc.struct.Array;
import arc.util.Align;
import arc.util.Log;
import arc.util.Time;
import arc.util.async.Threads;
import games.stendhal.client.PerceptionDispatcher;
import games.stendhal.client.StendhalClient;
import games.stendhal.client.UserContext;
import marauroa.client.ClientFramework;
import mindustry.Vars;
import mindustry.core.ContentLoader;
import mindustry.core.Control;
import mindustry.core.FileTree;
import mindustry.core.Logic;
import mindustry.core.NetClient;
import mindustry.core.NetServer;
import mindustry.core.Platform;
import mindustry.core.Renderer;
import mindustry.core.UI;
import mindustry.ctype.Content;
import mindustry.game.EventType;
import mindustry.game.EventType.ClientCreateEvent;
import mindustry.game.EventType.ClientLoadEvent;
import mindustry.gen.Musics;
import mindustry.gen.Sounds;
import mindustry.graphics.Pal;
import mindustry.maps.Map;
import mindustry.maps.MapPreviewLoader;
import mindustry.mod.Mod;
import mindustry.mod.Mods;
import mindustry.net.Net;
import mindustry.ui.Fonts;
import stendhal.test.T_ClientApplication;
import stendhal.test.textClient;
import utils.assets.Pack;
import utils.assets.ResourceManager;
import z.ai.units.StrategySystem;
import z.debug.Assets;
import z.debug.assets.PackLoader;
import z.system.GroundSystem;
import z.system.ItemsSystem;
import z.system.TroopsSystem;
import z.system.WorkerSystem;

import static arc.Core.app;
import static arc.Core.assets;
import static arc.Core.atlas;
import static arc.Core.batch;
import static arc.Core.bundle;
import static arc.Core.graphics;
import static mindustry.Vars.content;
import static mindustry.Vars.control;
import static mindustry.Vars.logic;
import static mindustry.Vars.mapExtension;
import static mindustry.Vars.maps;
import static mindustry.Vars.mods;
import static mindustry.Vars.netClient;
import static mindustry.Vars.netServer;
import static mindustry.Vars.platform;
import static mindustry.Vars.renderer;
import static mindustry.Vars.schematics;
import static mindustry.Vars.systemGround;
import static mindustry.Vars.systemItems;
import static mindustry.Vars.systemStrategy;
import static mindustry.Vars.systemTroops;
import static mindustry.Vars.systemWorker;
import static mindustry.Vars.tree;
import static mindustry.Vars.ui;

public abstract class StendhalClientLauncher extends ApplicationCore implements Platform{
    private static final int loadingFPS = 20;

    private float smoothProgress;
    private long lastTime;
    private long beginTime;
    private boolean finished = false;

    @Override
    public void setup(){
        Events.fire(new ClientCreateEvent());

        Vars.loadLogger();
        Vars.loadFileLogger();
        Vars.platform = this;
        beginTime = Time.millis();

        Time.setDeltaProvider(() -> {
            float result = Core.graphics.getDeltaTime() * 60f;
            return (Float.isNaN(result) || Float.isInfinite(result)) ? 1f : Mathf.clamp(result, 0.0001f, 60f / 10f);
        });

        batch = new SpriteBatch();
        assets = new ResourceManager();     // default new AssetManager();
//        assets.setLoader(Texture.class, "." + mapExtension, new MapPreviewLoader());

        tree = new FileTree();
        assets.setLoader(Sound.class, new SoundLoader(tree));
        assets.setLoader(Music.class, new MusicLoader(tree));

        assets.load("sprites/error.png", Texture.class);
        atlas = TextureAtlas.blankAtlas();
        Vars.net = new Net(platform.getNet());
        mods = new Mods();

        Fonts.loadSystemCursors();

        assets.load(new Vars());

        Fonts.loadDefaultFont();

        assets.load(new AssetDescriptor<>("sprites/sprites.atlas", TextureAtlas.class)).loaded = t -> {
            atlas = (TextureAtlas)t;
            Fonts.mergeFontAtlas(atlas);
            {   // zones add function
//                Assets.debugInitRegions(atlas);
            }
        };
        {
            Vars.atlasS = TextureAtlas.blankAtlas();
            assets.load("stendhal/pack/tileset.c3", Pack.class);
//            PackLoader.getInstance().loadAsync(Core.files.internal("stendhal/pack/tileset.c3"));
        }

//        assets.loadRun("maps", Map.class, () -> maps.loadPreviews());

//        Musics.load();
//        Sounds.load();

//        assets.loadRun("contentcreate", Content.class, () -> {
//            content.createBaseContent();
//            content.loadColors();
//        }, () -> {
//            mods.loadScripts();
//            content.createModContent();
//        });

//        add(logic = new Logic());
//        add(control = new Control());
//        add(renderer = new Renderer());
//        add(ui = new UI());
//        add(netServer = new NetServer());
//        add(netClient = new NetClient());
//
//        assets.load(mods);
//        assets.load(schematics);
//
//        assets.loadRun("contentinit", ContentLoader.class, () -> {
//            content.init();
//            content.load();
//        });

        // zones add begon
//        if(systemGround == null) systemGround = new GroundSystem();
//        if(systemWorker == null) systemWorker = new WorkerSystem();
//        if(systemItems == null) systemItems = new ItemsSystem();
//        if(systemTroops == null) systemTroops = new TroopsSystem();
//        if (systemStrategy == null) systemStrategy = new StrategySystem();
        // zones add end


        if (true) {
            add(Vars.clientScence = new T_ClientApplication());
//            netClient.temp_connect();
//            try {
//                textClient.main(null);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
        }
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
            drawLoading();

            if(assets.update(1000 / loadingFPS)){
                Log.info("Total time to load: {0}", Time.timeSinceMillis(beginTime));
                for(ApplicationListener listener : modules){
                    listener.init();
                }
                mods.eachClass(Mod::init);
                finished = true;
    /*            Events.fire(new ClientLoadEvent());*/
                // zones add begon
                {
                    Events.fire(new EventType.ClearCacheEvent());
//                    if(systemGround == null) systemGround = new GroundSystem();
//                    if(systemWorker == null) systemWorker = new WorkerSystem();
//                    if(systemItems == null) systemItems = new ItemsSystem();
                }
                // zones add end
                super.resize(graphics.getWidth(), graphics.getHeight());
                app.post(() -> app.post(() -> app.post(() -> app.post(() -> super.resize(graphics.getWidth(), graphics.getHeight())))));

                {   // print assets path
                    Vars.clientScence.callInit();
//                    Array<TextureRegion> regions = assets.getAll(TextureRegion.class, new Array<TextureRegion>());
//                    for (String keyname : assets.getAssetNames()) {
//                        Log.info("____  " + keyname);
//                    }
//                    for (TextureAtlas.AtlasRegion region : Vars.atlasS.getRegions()) {
//                        if (region.name.contains("fireplace_2.png")) {
//                            Log.info(region.name + "______________");
//                        }
//                    }
                }
            }
        }else{
            super.update();
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
        // zones add begon
        Assets.initSprites();
        // zones add end
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

    void drawLoading(){
        smoothProgress = Mathf.lerpDelta(smoothProgress, assets.getProgress(), 0.1f);

        Core.graphics.clear(Pal.darkerGray);
        Draw.proj().setOrtho(0, 0, Core.graphics.getWidth(), Core.graphics.getHeight());

        float height = Scl.scl(50f);

        Draw.color(Color.black);
        Fill.poly(graphics.getWidth()/2f, graphics.getHeight()/2f, 6, Mathf.dst(graphics.getWidth()/2f, graphics.getHeight()/2f) * smoothProgress);
        Draw.reset();

        float w = graphics.getWidth()*0.6f;

        Draw.color(Color.black);
        Fill.rect(graphics.getWidth()/2f, graphics.getHeight()/2f, w, height);

        Draw.color(Pal.accent);
        Fill.crect(graphics.getWidth()/2f-w/2f, graphics.getHeight()/2f - height/2f, w * smoothProgress, height);

        for(int i : Mathf.signs){
            Fill.tri(graphics.getWidth()/2f + w/2f*i, graphics.getHeight()/2f + height/2f, graphics.getWidth()/2f + w/2f*i, graphics.getHeight()/2f - height/2f, graphics.getWidth()/2f + w/2f*i + height/2f*i, graphics.getHeight()/2f);
        }

        if(assets.isLoaded("outline")){
            BitmapFont font = assets.get("outline");
            font.draw((int)(assets.getProgress() * 100) + "%", graphics.getWidth() / 2f, graphics.getHeight() / 2f + Scl.scl(10f), Align.center);
            font.draw(bundle.get("loading", "").replace("[accent]", ""), graphics.getWidth() / 2f, graphics.getHeight() / 2f + height / 2f + Scl.scl(20), Align.center);

            if(assets.getCurrentLoading() != null){
                String name = assets.getCurrentLoading().fileName.toLowerCase();
                String key = name.contains("script") ? "scripts" : name.contains("content") ? "content" : name.contains("mod") ? "mods" : name.contains("msav") ||
                        name.contains("maps") ? "map" : name.contains("ogg") || name.contains("mp3") ? "sound" : name.contains("png") ? "image" : "system";
                font.draw(bundle.get("load." + key, ""), graphics.getWidth() / 2f, graphics.getHeight() / 2f - height / 2f - Scl.scl(10f), Align.center);
            }
        }
        Draw.flush();
    }
}
