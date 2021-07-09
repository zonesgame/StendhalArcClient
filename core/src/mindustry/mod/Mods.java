package mindustry.mod;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;

import arc.Core;
import arc.Events;
import arc.assets.Loadable;
import arc.files.Fi;
import arc.files.ZipFi;
import arc.func.Cons;
import arc.func.Cons2;
import arc.graphics.Color;
import arc.graphics.Pixmap;
import arc.graphics.Texture;
import arc.graphics.Texture.TextureFilter;
import arc.graphics.g2d.PixmapRegion;
import arc.graphics.g2d.TextureAtlas;
import arc.graphics.g2d.TextureAtlas.AtlasRegion;
import arc.scene.ui.Dialog;
import arc.struct.Array;
import arc.struct.ObjectMap;
import arc.struct.ObjectSet;
import arc.util.Align;
import arc.util.ArcAnnotate.Nullable;
import arc.util.Disposable;
import arc.util.I18NBundle;
import arc.util.Log;
import arc.util.Strings;
import arc.util.Structs;
import arc.util.Time;
import arc.util.io.PropertiesUtils;
import arc.util.io.Streams;
import arc.util.serialization.Json;
import arc.util.serialization.Jval;
import arc.util.serialization.Jval.Jformat;
import mindustry.core.Version;
import mindustry.ctype.Content;
import mindustry.ctype.ContentType;
import mindustry.ctype.UnlockableContent;
import mindustry.game.EventType.ClientLoadEvent;
import mindustry.game.EventType.ContentReloadEvent;
import mindustry.gen.Icon;
import mindustry.gen.Sounds;
import mindustry.graphics.MultiPacker;
import mindustry.graphics.MultiPacker.PageType;
import mindustry.graphics.Pal;
import mindustry.plugin.Plugin;
import mindustry.type.ErrorContent;
import mindustry.type.Publishable;
import mindustry.ui.Styles;

import static mindustry.Vars.content;
import static mindustry.Vars.data;
import static mindustry.Vars.headless;
import static mindustry.Vars.modDirectory;
import static mindustry.Vars.platform;
import static mindustry.Vars.tree;
import static mindustry.Vars.ui;

/**
 *  模组管理器
 * */
public class Mods implements Loadable{
    /** json数据*/
    private Json json = new Json();
    /** 脚本管理器*/
    private @Nullable Scripts scripts;
    /** 解析器*/
    private ContentParser parser = new ContentParser();
    /** 本地化文件*/
    private ObjectMap<String, Array<Fi>> bundles = new ObjectMap<>();
    /** 专用文件夹*/
    private ObjectSet<String> specialFolders = ObjectSet.with("bundles", "sprites", "sprites-override");

    /** 总脚本文件数量*/
    private int totalSprites;
    /***/
    private MultiPacker packer;

    /** 模组列表*/
    private Array<LoadedMod> mods = new Array<>();
    /***/
    private ObjectMap<Class<?>, ModMeta> metas = new ObjectMap<>();
    /** 需要加载*/
    private boolean requiresReload, /** 创建包*/createdAtlas;

    public Mods(){
        Events.on(ClientLoadEvent.class, e -> Core.app.post(this::checkWarnings));
        Events.on(ContentReloadEvent.class, e -> Core.app.post(this::checkWarnings));
    }

    /** 获取模组文件.<p/>Returns a file named 'config.json' in a special folder for the specified plugin.
     * Call this in init(). */
    public Fi getConfig(Mod mod){
        ModMeta load = metas.get(mod.getClass());
        if(load == null) throw new IllegalArgumentException("Mod is not loaded yet (or missing)!");
        return modDirectory.child(load.name).child("config.json");
    }

    /** 模组文件子目录.<p/>Returns a list of files per mod subdirectory. */
    public void listFiles(String directory, Cons2<LoadedMod, Fi> cons){
        eachEnabled(mod -> {
            Fi file = mod.root.child(directory);
            if(file.exists()){
                for(Fi child : file.list()){
                    cons.get(mod, child);
                }
            }
        });
    }

    /** @return 获取指定类型模组.<p/>the loaded mod found by class, or null if not found. */
    public @Nullable LoadedMod getMod(Class<? extends Mod> type){
        return mods.find(m -> m.enabled() && m.main != null && m.main.getClass() == type);//loaded.find(l -> l.mod != null && l.mod.getClass() == type);
    }

    /** 导入一个外部文件模组.<p/>Imports an external mod file.*/
    public void importMod(Fi file) throws IOException{
        Fi dest = modDirectory.child(file.name());
        if(dest.exists()){
            throw new IOException("A mod with the same filename already exists!");
        }

        file.copyTo(dest);
        try{
            mods.add(loadMod(dest));
            requiresReload = true;
            sortMods();
        }catch(IOException e){
            dest.delete();
            throw e;
        }catch(Throwable t){
            dest.delete();
            throw new IOException(t);
        }
    }

    /** Repacks all in-game sprites. */
    @Override
    public void loadAsync(){
        if(!mods.contains(LoadedMod::enabled)) return;
        Time.mark();

        packer = new MultiPacker();

        eachEnabled(mod -> {
            Array<Fi> sprites = mod.root.child("sprites").findAll(f -> f.extension().equals("png"));
            Array<Fi> overrides = mod.root.child("sprites-override").findAll(f -> f.extension().equals("png"));
            packSprites(sprites, mod, true);
            packSprites(overrides, mod, false);
            Log.debug("Packed {0} images for mod '{1}'.", sprites.size + overrides.size, mod.meta.name);
            totalSprites += sprites.size + overrides.size;
        });

        Log.debug("Time to pack textures: {0}", Time.elapsed());
    }

    /** 打包纹理*/
    private void packSprites(Array<Fi> sprites, LoadedMod mod, boolean prefix){
        for(Fi file : sprites){
            try(InputStream stream = file.read()){
                byte[] bytes = Streams.copyBytes(stream, Math.max((int)file.length(), 512));
                Pixmap pixmap = new Pixmap(bytes, 0, bytes.length);
                packer.add(getPage(file), (prefix ? mod.name + "-" : "") + file.nameWithoutExtension(), new PixmapRegion(pixmap));
                pixmap.dispose();
            }catch(IOException e){
                Core.app.post(() -> {
                    Log.err("Error packing images for mod: {0}", mod.meta.name);
                    e.printStackTrace();
                    if(!headless) ui.showException(e);
                });
                break;
            }
        }
        totalSprites += sprites.size;
    }

    @Override
    public void loadSync(){
        for(LoadedMod mod : mods){
            //try to load icon for each mod that can have one
            if(mod.root.child("icon.png").exists()){
                try{
                    mod.iconTexture = new Texture(mod.root.child("icon.png"));
                }catch(Throwable t){
                    Log.err("Failed to load icon for mod '" + mod.name + "'.", t);
                }
            }
        }

        if(packer == null) return;
        Time.mark();

        //get textures packed
        if(totalSprites > 0){
            if(!createdAtlas) Core.atlas = new TextureAtlas(Core.files.internal("sprites/sprites.atlas"));
            createdAtlas = true;

            for(AtlasRegion region : Core.atlas.getRegions()){
                PageType type = getPage(region);
                if(!packer.has(type, region.name)){
                    packer.add(type, region.name, Core.atlas.getPixmap(region));
                }
            }

            TextureFilter filter = Core.settings.getBool("linear") ? TextureFilter.Linear : TextureFilter.Nearest;

            //flush so generators can use these sprites
            packer.flush(filter, Core.atlas);

            //generate new icons
            for(Array<Content> arr : content.getContentMap()){
                arr.each(c -> {
                    if(c instanceof UnlockableContent && c.minfo.mod != null){
                        UnlockableContent u = (UnlockableContent)c;
                        u.createIcons(packer);
                    }
                });
            }

            Core.atlas = packer.flush(filter, new TextureAtlas());
            Core.atlas.setErrorRegion("error");
            Log.debug("Total pages: {0}", Core.atlas.getTextures().size);
        }

        packer.dispose();
        packer = null;
        Log.debug("Time to update textures: {0}", Time.elapsed());
    }

    private PageType getPage(AtlasRegion region){
        return
            region.getTexture() == Core.atlas.find("white").getTexture() ? PageType.main :
            region.getTexture() == Core.atlas.find("stone1").getTexture() ? PageType.environment :
            region.getTexture() == Core.atlas.find("clear-editor").getTexture() ? PageType.editor :
            region.getTexture() == Core.atlas.find("zone-groundZero").getTexture() ? PageType.zone :
            region.getTexture() == Core.atlas.find("whiteui").getTexture() ? PageType.ui :
            PageType.main;
    }

    private PageType getPage(Fi file){
        String parent = file.parent().name();
        return
            parent.equals("environment") ? PageType.environment :
            parent.equals("editor") ? PageType.editor :
            parent.equals("zones") ? PageType.zone :
            parent.equals("ui") || file.parent().parent().name().equals("ui") ? PageType.ui :
            PageType.main;
    }

    /** 移除一个模组文件.<p/>Removes a mod file and marks it for requiring a restart. */
    public void removeMod(LoadedMod mod){
        if(mod.root instanceof ZipFi){
            mod.root.delete();
        }

        boolean deleted = mod.file.isDirectory() ? mod.file.deleteDirectory() : mod.file.delete();

        if(!deleted){
            ui.showErrorMessage("$mod.delete.error");
            return;
        }
        mods.remove(mod);
        requiresReload = true;
    }

    /** 获取脚本管理器*/
    public Scripts getScripts(){
        if(scripts == null) scripts = platform.createScripts();
        return scripts;
    }

    /** @return 脚本管理器是否实例化.<p/>whether the scripting engine has been initialized. */
    public boolean hasScripts(){
        return scripts != null;
    }

    /** 是否需要重新加载*/
    public boolean requiresReload(){
        return requiresReload;
    }

    /** 从文件夹中加载所有的mods, 但不调用它们的任何方法.<p/>Loads all mods from the folder, but does not call any methods on them.*/
    public void load(){
        for(Fi file : modDirectory.list()){
            if(!file.extension().equals("jar") && !file.extension().equals("zip") && !(file.isDirectory() && (file.child("mod.json").exists() || file.child("mod.hjson").exists()))) continue;

            Log.debug("[Mods] Loading mod {0}", file);
            try{
                LoadedMod mod = loadMod(file);
                mods.add(mod);
            }catch(Throwable e){
                Log.err("Failed to load mod file {0}. Skipping.", file);
                Log.err(e);
            }
        }

        //load workshop mods now
        for(Fi file : platform.getWorkshopContent(LoadedMod.class)){
            try{
                LoadedMod mod = loadMod(file);
                mods.add(mod);
                mod.addSteamID(file.name());
            }catch(Throwable e){
                Log.err("Failed to load mod workshop file {0}. Skipping.", file);
                Log.err(e);
            }
        }

        resolveModState();
        sortMods();

        buildFiles();
    }

    /** 排序模组*/
    private void sortMods(){
        //sort mods to make sure servers handle them properly and they appear correctly in the dialog
        mods.sort(Structs.comps(Structs.comparingInt(m -> m.state.ordinal()), Structs.comparing(m -> m.name)));
    }

    /** 分解模组状态*/
    private void resolveModState(){
        mods.each(this::updateDependencies);

        for(LoadedMod mod : mods){
            mod.state =
                !mod.isSupported() ? ModState.unsupported :
                mod.hasUnmetDependencies() ? ModState.missingDependencies :
                !mod.shouldBeEnabled() ? ModState.disabled :
                ModState.enabled;
        }
    }

    /** 更新模组依赖关系*/
    private void updateDependencies(LoadedMod mod){
        mod.dependencies.clear();
        mod.missingDependencies.clear();
        mod.dependencies = mod.meta.dependencies.map(this::locateMod);

        for(int i = 0; i < mod.dependencies.size; i++){
            if(mod.dependencies.get(i) == null){
                mod.missingDependencies.add(mod.meta.dependencies.get(i));
            }
        }
    }

    /** 排序*/
    private void topoSort(LoadedMod mod, Array<LoadedMod> stack, ObjectSet<LoadedMod> visited){
        visited.add(mod);
        mod.dependencies.each(m -> !visited.contains(m), m -> topoSort(m, stack, visited));
        stack.add(mod);
    }

    /** @return 模组正确依赖关系返回.<p/>mods ordered in the correct way needed for dependencies. */
    private Array<LoadedMod> orderedMods(){
        ObjectSet<LoadedMod> visited = new ObjectSet<>();
        Array<LoadedMod> result = new Array<>();
        eachEnabled(mod -> {
            if(!visited.contains(mod)){
                topoSort(mod, result, visited);
            }
        });
        return result;
    }

    /** 本地化模组*/
    public LoadedMod locateMod(String name){
        return mods.find(mod -> mod.enabled() && mod.name.equals(name));
    }

    /** 构建本地化文件*/
    private void buildFiles(){
        for(LoadedMod mod : orderedMods()){
            boolean zipFolder = !mod.file.isDirectory() && mod.root.parent() != null;
            String parentName = zipFolder ? mod.root.name() : null;
            for(Fi file : mod.root.list()){
                //ignore special folders like bundles or sprites
                if(file.isDirectory() && !specialFolders.contains(file.name())){
                    //TODO calling child/parent on these files will give you gibberish; create wrapper class.
                    file.walk(f -> tree.addFile(mod.file.isDirectory() ? f.path().substring(1 + mod.file.path().length()) :
                        zipFolder ? f.path().substring(parentName.length() + 1) : f.path(), f));
                }
            }

            //load up bundles.
            Fi folder = mod.root.child("bundles");
            if(folder.exists()){
                for(Fi file : folder.list()){
                    if(file.name().startsWith("bundle") && file.extension().equals("properties")){
                        String name = file.nameWithoutExtension();
                        bundles.getOr(name, Array::new).add(file);
                    }
                }
            }
        }

        //add new keys to each bundle
        I18NBundle bundle = Core.bundle;
        while(bundle != null){
            String str = bundle.getLocale().toString();
            String locale = "bundle" + (str.isEmpty() ? "" : "_" + str);
            for(Fi file : bundles.getOr(locale, Array::new)){
                try{
                    PropertiesUtils.load(bundle.getProperties(), file.reader());
                }catch(Throwable e){
                    Log.err("Error loading bundle: " + file + "/" + locale, e);
                }
            }
            bundle = bundle.getParent();
        }
    }

    /** 检查与内容相关的所有警告,并显示相关对话框. 只在客户端.<p/>Check all warnings related to content and show relevant dialogs. Client only. */
    private void checkWarnings(){
        //show 'scripts have errored' info
        if(scripts != null && scripts.hasErrored()){
           Core.settings.getBoolOnce("scripts-errored2", () -> ui.showErrorMessage("$mod.scripts.unsupported"));
        }

        //show list of errored content
        if(mods.contains(LoadedMod::hasContentErrors)){
            ui.loadfrag.hide();
            new Dialog(""){{

                setFillParent(true);
                cont.margin(15);
                cont.add("$error.title");
                cont.row();
                cont.addImage().width(300f).pad(2).colspan(2).height(4f).color(Color.scarlet);
                cont.row();
                cont.add("$mod.errors").wrap().growX().center().get().setAlignment(Align.center);
                cont.row();
                cont.pane(p -> {
                    mods.each(m -> m.enabled() && m.hasContentErrors(), m -> {
                        p.add(m.name).color(Pal.accent).left();
                        p.row();
                        p.addImage().fillX().pad(4).color(Pal.accent);
                        p.row();
                        p.table(d -> {
                            d.left().marginLeft(15f);
                            for(Content c : m.erroredContent){
                                d.add(c.minfo.sourceFile.nameWithoutExtension()).left().padRight(10);
                                d.addImageTextButton("$details", Icon.downOpen, Styles.transt, () -> {
                                    new Dialog(""){{
                                        setFillParent(true);
                                        cont.pane(e -> e.add(c.minfo.error)).grow();
                                        cont.row();
                                        cont.addImageTextButton("$ok", Icon.left, this::hide).size(240f, 60f);
                                    }}.show();
                                }).size(190f, 50f).left().marginLeft(6);
                                d.row();
                            }
                        }).left();
                        p.row();
                    });
                });

                cont.row();
                cont.addButton("$ok", this::hide).size(300, 50);
            }}.show();
        }
    }

    /** 是否包含内容错误*/
    public boolean hasContentErrors(){
        return mods.contains(LoadedMod::hasContentErrors);
    }

    /** 重新加载内容.<p/>Reloads all mod content. How does this even work? I refuse to believe that it functions correctly.*/
    public void reloadContent(){
        //epic memory leak
        //TODO make it less epic
        Core.atlas = new TextureAtlas(Core.files.internal("sprites/sprites.atlas"));
        createdAtlas = true;

        mods.each(LoadedMod::dispose);
        mods.clear();
        Core.bundle =  I18NBundle.createBundle(Core.files.internal("bundles/bundle"), Core.bundle.getLocale());
        load();
        Sounds.dispose();
        Sounds.load();
        Core.assets.finishLoading();
        if(scripts != null){
            scripts.dispose();
            scripts = null;
        }
        content.clear();
        content.createBaseContent();
        content.loadColors();
        loadScripts();
        content.createModContent();
        loadAsync();
        loadSync();
        content.init();
        content.load();
        content.loadColors();
        data.load();
        Core.atlas.getTextures().each(t -> t.setFilter(Core.settings.getBool("linear") ? TextureFilter.Linear : TextureFilter.Nearest));
        requiresReload = false;

        Events.fire(new ContentReloadEvent());
    }

    /** 加载脚本. 必须主线程调用<p/>This must be run on the main thread! */
    public void loadScripts(){
        Time.mark();

        try{
            eachEnabled(mod -> {
                if(mod.root.child("scripts").exists()){
                    content.setCurrentMod(mod);
                    //if there's only one script file, use it (for backwards compatibility); if there isn't, use "main.js"
                    Array<Fi> allScripts = mod.root.child("scripts").findAll(f -> f.extEquals("js"));
                    Fi main = allScripts.size == 1 ? allScripts.first() : mod.root.child("scripts").child("main.js");
                    if(main.exists() && !main.isDirectory()){
                        try{
                            if(scripts == null){
                                scripts = platform.createScripts();
                            }
                            scripts.run(mod, main);
                        }catch(Throwable e){
                            Core.app.post(() -> {
                                Log.err("Error loading main script {0} for mod {1}.", main.name(), mod.meta.name);
                                e.printStackTrace();
                            });
                        }
                    }else{
                        Core.app.post(() -> {
                            Log.err("No main.js found for mod {0}.", mod.meta.name);
                        });
                    }
                }
            });
        }finally{
            content.setCurrentMod(null);
        }

        Log.debug("Time to initialize modded scripts: {0}", Time.elapsed());
    }

    /** 创建模组中需要的所有内容.<p/>Creates all the content found in mod files. */
    public void loadContent(){

        class LoadRun implements Comparable<LoadRun>{
            final ContentType type;
            final Fi file;
            final LoadedMod mod;

            public LoadRun(ContentType type, Fi file, LoadedMod mod){
                this.type = type;
                this.file = file;
                this.mod = mod;
            }

            @Override
            public int compareTo(LoadRun l){
                int mod = this.mod.name.compareTo(l.mod.name);
                if(mod != 0) return mod;
                return this.file.name().compareTo(l.file.name());
            }
        }

        Array<LoadRun> runs = new Array<>();

        for(LoadedMod mod : orderedMods()){
            if(mod.root.child("content").exists()){
                Fi contentRoot = mod.root.child("content");
                for(ContentType type : ContentType.all){
                    Fi folder = contentRoot.child(type.name().toLowerCase() + "s");
                    if(folder.exists()){
                        for(Fi file : folder.findAll(f -> f.extension().equals("json") || f.extension().equals("hjson"))){
                            runs.add(new LoadRun(type, file, mod));
                        }
                    }
                }
            }
        }

        //make sure mod content is in proper order
        runs.sort();
        for(LoadRun l : runs){
            Content current = content.getLastAdded();
            try{
                //this binds the content but does not load it entirely
                Content loaded = parser.parse(l.mod, l.file.nameWithoutExtension(), l.file.readString("UTF-8"), l.file, l.type);
                Log.debug("[{0}] Loaded '{1}'.", l.mod.meta.name, (loaded instanceof UnlockableContent ? ((UnlockableContent)loaded).localizedName : loaded));
            }catch(Throwable e){
                if(current != content.getLastAdded() && content.getLastAdded() != null){
                    parser.markError(content.getLastAdded(), l.mod, l.file, e);
                }else{
                    ErrorContent error = new ErrorContent();
                    parser.markError(error, l.mod, l.file, e);
                }
            }
        }

        //this finishes parsing content fields
        parser.finishParsing();
    }

    /** 处理内容错误*/
    public void handleContentError(Content content, Throwable error){
        parser.markError(content, error);
    }

    /** @return 模组信息列表.<p/>a list of mods and versions, in the format name:version. */
    public Array<String> getModStrings(){
        return mods.select(l -> !l.meta.hidden && l.enabled()).map(l -> l.name + ":" + l.meta.version);
    }

    /** 设置模组开启状态.<p/>Makes a mod enabled or disabled. shifts it.*/
    public void setEnabled(LoadedMod mod, boolean enabled){
        if(mod.enabled() != enabled){
            Core.settings.putSave("mod-" + mod.name + "-enabled", enabled);
            requiresReload = true;
            mod.state = enabled ? ModState.enabled : ModState.disabled;
            mods.each(this::updateDependencies);
            sortMods();
        }
    }

    /** @return 客户端丢失的模组.<p/>the mods that the client is missing.
     * The inputted array is changed to contain the extra mods that the client has but the server doesn't.*/
    public Array<String> getIncompatibility(Array<String> out){
        Array<String> mods = getModStrings();
        Array<String> result = mods.copy();
        for(String mod : mods){
            if(out.remove(mod)){
                result.remove(mod);
            }
        }
        return result;
    }

    /** 记载模组列表*/
    public Array<LoadedMod> list(){
        return mods;
    }

    /** 通过主类, 迭代每个模组.<p/>Iterates through each mod with a main class. */
    public void eachClass(Cons<Mod> cons){
        mods.each(p -> p.main != null, p -> contextRun(p, () -> cons.get(p.main)));
    }

    /** 迭代每个启用模组.<p/>Iterates through each enabled mod. */
    public void eachEnabled(Cons<LoadedMod> cons){
        mods.each(LoadedMod::enabled, cons);
    }

    /***/
    public void contextRun(LoadedMod mod, Runnable run){
        try{
            run.run();
        }catch(Throwable t){
            throw new RuntimeException("Error loading mod " + mod.meta.name, t);
        }
    }

    /**
     * 加载一个mod文件+ meta, 但不将其添加到列表中. 请注意,目录可以作为mods加载.<p/>
     * Loads a mod file+meta, but does not add it to the list.
     * Note that directories can be loaded as mods.*/
    private LoadedMod loadMod(Fi sourceFile) throws Exception{
        Fi zip = sourceFile.isDirectory() ? sourceFile : new ZipFi(sourceFile);
        if(zip.list().length == 1 && zip.list()[0].isDirectory()){
            zip = zip.list()[0];
        }

        Fi metaf = zip.child("mod.json").exists() ? zip.child("mod.json") : zip.child("mod.hjson").exists() ? zip.child("mod.hjson") : zip.child("plugin.json");
        if(!metaf.exists()){
            Log.warn("Mod {0} doesn't have a 'mod.json'/'mod.hjson'/'plugin.json' file, skipping.", sourceFile);
            throw new IllegalArgumentException("No mod.json found.");
        }

        ModMeta meta = json.fromJson(ModMeta.class, Jval.read(metaf.readString()).toString(Jformat.plain));
        String camelized = meta.name.replace(" ", "");
        String mainClass = meta.main == null ? camelized.toLowerCase() + "." + camelized + "Mod" : meta.main;
        String baseName = meta.name.toLowerCase().replace(" ", "-");

        if(mods.contains(m -> m.name.equals(baseName))){
            throw new IllegalArgumentException("A mod with the name '" + baseName + "' is already imported.");
        }

        Mod mainMod;

        Fi mainFile = zip;
        String[] path = (mainClass.replace('.', '/') + ".class").split("/");
        for(String str : path){
            if(!str.isEmpty()){
                mainFile = mainFile.child(str);
            }
        }

        //make sure the main class exists before loading it; if it doesn't just don't put it there
        if(mainFile.exists()){
            //other platforms don't have standard java class loaders
            if(!headless && Version.build != -1){
                throw new IllegalArgumentException("Java class mods are currently unsupported outside of custom builds.");
            }

            URLClassLoader classLoader = new URLClassLoader(new URL[]{sourceFile.file().toURI().toURL()}, ClassLoader.getSystemClassLoader());
            Class<?> main = classLoader.loadClass(mainClass);
            metas.put(main, meta);
            mainMod = (Mod)main.getDeclaredConstructor().newInstance();
        }else{
            mainMod = null;
        }

        //all plugins are hidden implicitly
        if(mainMod instanceof Plugin){
            meta.hidden = true;
        }

        return new LoadedMod(sourceFile, zip, mainMod, meta);
    }

    /**
     *  表示从jar文件加载的插件.<p/>
     * Represents a plugin that has been loaded from a jar file.
     * */
    public static class LoadedMod implements Publishable, Disposable{
        /** The location of this mod's zip file/folder on the disk. */
        public final Fi file;
        /** The root zip file; points to the contents of this mod. In the case of folders, this is the same as the mod's file. */
        public final Fi root;
        /** The mod's main class; may be null. */
        public final @Nullable Mod main;
        /** Internal mod name. Used for textures. */
        public final String name;
        /** This mod's metadata. */
        public final ModMeta meta;
        /** This mod's dependencies as already-loaded mods. */
        public Array<LoadedMod> dependencies = new Array<>();
        /** All missing dependencies of this mod as strings. */
        public Array<String> missingDependencies = new Array<>();
        /** Script files to run. */
        public Array<Fi> scripts = new Array<>();
        /** Content with intialization code. */
        public ObjectSet<Content> erroredContent = new ObjectSet<>();
        /** Current state of this mod. */
        public ModState state = ModState.enabled;
        /** Icon texture. Should be disposed. */
        public @Nullable Texture iconTexture;

        public LoadedMod(Fi file, Fi root, Mod main, ModMeta meta){
            this.root = root;
            this.file = file;
            this.main = main;
            this.meta = meta;
            this.name = meta.name.toLowerCase().replace(" ", "-");
        }

        public boolean enabled(){
            return state == ModState.enabled || state == ModState.contentErrors;
        }

        public boolean shouldBeEnabled(){
            return Core.settings.getBool("mod-" + name + "-enabled", true);
        }

        public boolean hasUnmetDependencies(){
            return !missingDependencies.isEmpty();
        }

        public boolean hasContentErrors(){
            return !erroredContent.isEmpty();
        }

        /** @return whether this mod is supported by the game verison */
        public boolean isSupported(){
            if(Version.build <= 0 || meta.minGameVersion == null) return true;
            if(meta.minGameVersion.contains(".")){
                String[] split = meta.minGameVersion.split("\\.");
                if(split.length == 2){
                    return Version.build >= Strings.parseInt(split[0], 0) && Version.revision >= Strings.parseInt(split[1], 0);
                }
            }
            return Version.build >= Strings.parseInt(meta.minGameVersion, 0);
        }

        @Override
        public void dispose(){
            if(iconTexture != null){
                iconTexture.dispose();
            }
        }

        @Override
        public String getSteamID(){
            return Core.settings.getString(name + "-steamid", null);
        }

        @Override
        public void addSteamID(String id){
            Core.settings.put(name + "-steamid", id);
            Core.settings.save();
        }

        @Override
        public void removeSteamID(){
            Core.settings.remove(name + "-steamid");
            Core.settings.save();
        }

        @Override
        public String steamTitle(){
            return meta.name;
        }

        @Override
        public String steamDescription(){
            return meta.description;
        }

        @Override
        public String steamTag(){
            return "mod";
        }

        @Override
        public Fi createSteamFolder(String id){
            return file;
        }

        @Override
        public Fi createSteamPreview(String id){
            return file.child("preview.png");
        }

        @Override
        public boolean prePublish(){
            if(!file.isDirectory()){
                ui.showErrorMessage("$mod.folder.missing");
                return false;
            }

            if(!file.child("preview.png").exists()){
                ui.showErrorMessage("$mod.preview.missing");
                return false;
            }

            return true;
        }

        @Override
        public String toString(){
            return "LoadedMod{" +
            "file=" + file +
            ", root=" + root +
            ", name='" + name + '\'' +
            '}';
        }
    }

    /**
     *  插件元数据信息.<p/>
     * Plugin metadata information.
     * */
    public static class ModMeta{
        public String name, displayName, author, description, version, main, minGameVersion;
        public Array<String> dependencies = Array.with();
        /** Hidden mods are only server-side or client-side, and do not support adding new content. */
        public boolean hidden;

        public String displayName(){
            return displayName == null ? name : displayName;
        }
    }

    public enum ModState{
        enabled,
        contentErrors,
        missingDependencies,
        unsupported,
        disabled,
    }
}
