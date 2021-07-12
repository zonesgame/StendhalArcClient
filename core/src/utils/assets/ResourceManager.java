package utils.assets;

import arc.assets.AssetManager;
import arc.assets.loaders.BitmapFontLoader;
import arc.assets.loaders.CubemapLoader;
import arc.assets.loaders.I18NBundleLoader;
import arc.assets.loaders.MusicLoader;
import arc.assets.loaders.PixmapLoader;
import arc.assets.loaders.ShaderProgramLoader;
import arc.assets.loaders.SoundLoader;
import arc.assets.loaders.TextureAtlasLoader;
import arc.assets.loaders.TextureLoader;
import arc.assets.loaders.resolvers.InternalFileHandleResolver;
import arc.audio.Music;
import arc.audio.Sound;
import arc.graphics.Cubemap;
import arc.graphics.Pixmap;
import arc.graphics.Texture;
import arc.graphics.g2d.BitmapFont;
import arc.graphics.g2d.TextureAtlas;
import arc.graphics.gl.Shader;
import arc.util.I18NBundle;
import arc.util.async.AsyncExecutor;
import utils.assets.loader.PackLoader;

public class ResourceManager extends AssetManager {

    public ResourceManager() {
        super(new InternalFileHandleResolver());

        setLoader(Pack.class, new PackLoader(new InternalFileHandleResolver()));
    }
}
