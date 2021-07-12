package utils.assets.loader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import arc.assets.AssetLoaderParameters;
import arc.assets.loaders.AsynchronousAssetLoader;
import arc.assets.loaders.FileHandleResolver;
import arc.assets.loaders.TextureLoader;
import arc.audio.Music;
import arc.audio.Sound;
import arc.graphics.Pixmap;
import arc.graphics.Texture;
import arc.graphics.g2d.BitmapFont;
import arc.graphics.g2d.TextureAtlas;
import arc.graphics.g2d.TextureRegion;
import arc.util.io.Streams;
import utils.assets.Pack;
import z.debug.assets.Base128;
import z.debug.assets.Coding;
import z.debug.assets.ResourceMappingClass;

public abstract   class DecodeAsynchronousAssetLoader<T, P extends AssetLoaderParameters<T>> extends AsynchronousAssetLoader<T, P> {

    public DecodeAsynchronousAssetLoader(FileHandleResolver resolver) {
        super(resolver);
    }

    protected byte[] getZonesAssets (Coding encoder, byte[] bytes) {
        String encodeName =  encoder.name();
        for (int i = 0, len = encodeName.length(); i < len; i++) {
            char c = encodeName.charAt(i);
            if (c == 'Z')
                bytes = getGZip(bytes);
            else if (c == 'B')
                bytes = getDecode(bytes);
        }

        return bytes;
    }

    private byte[] getDecode (byte[] bytes) {
        return Base128.getDecoder().decode(bytes);
    }

    private byte[] getGZip (byte[] bytes) {
        InputStream inputStream = null;
        try {
            inputStream = new GZIPInputStream(new ByteArrayInputStream(bytes), bytes.length);
            bytes = Streams.copyBytes(inputStream, bytes.length);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Streams.close(inputStream);
        }
        return bytes;
    }




    protected static final Class[] CLASSES = new Class[ResourceMappingClass.values().length];
    protected static final AssetLoaderParameters[] PARAMETERS = new AssetLoaderParameters[CLASSES.length];

    static {
        CLASSES[ResourceMappingClass.PIXMAP.ordinal()] = Pixmap.class;
        CLASSES[ResourceMappingClass.TEXTURE.ordinal()] = Texture.class;
        CLASSES[ResourceMappingClass.TEXTURE_LINEAR.ordinal()] = Texture.class;
        CLASSES[ResourceMappingClass.TEXTUREREGION.ordinal()] = TextureRegion.class;
        CLASSES[ResourceMappingClass.TEXTUREATLAS.ordinal()] = TextureAtlas.class;
        CLASSES[ResourceMappingClass.SOUND.ordinal()] = Sound.class;
        CLASSES[ResourceMappingClass.MUSIC.ordinal()] = Music.class;
        CLASSES[ResourceMappingClass.BITMAPFONT.ordinal()] = BitmapFont.class;
        CLASSES[ResourceMappingClass.PACK.ordinal()] = Pack.class;

        // 默认加载属性
        PARAMETERS[ResourceMappingClass.PACK.ordinal()] = new PackLoader.PackParameter(ResourceMappingClass.TEXTURE_LINEAR);
//        PARAMETERS[ResourceMappingClass.PIXMAP.ordinal()] = new PixmapLoaderZones.PixmapParameter();
//        PARAMETERS[ResourceMappingClass.TEXTURE.ordinal()] = new TextureLoaderZones.TextureParameter();
        PARAMETERS[ResourceMappingClass.TEXTURE_LINEAR.ordinal()] = new TextureLoader.TextureParameter() {
            {
                minFilter = Texture.TextureFilter.Linear;
                magFilter = Texture.TextureFilter.Linear;
            }
        };
//        PARAMETERS[ResourceMappingClass.TEXTUREREGION.ordinal()] = new TextureRegionLoaderZones.TextureRegionParameter();
//        PARAMETERS[ResourceMappingClass.TEXTUREATLAS.ordinal()] = new TextureAtlasLoaderZones.TextureAtlasParameter();
//        PARAMETERS[ResourceMappingClass.SOUND.ordinal()] = new SoundLoader.SoundParameter();
//        PARAMETERS[ResourceMappingClass.MUSIC.ordinal()] = new MusicLoader.MusicParameter();
//        PARAMETERS[ResourceMappingClass.BITMAPFONT.ordinal()] = new BitmapFontLoaderZones.BitmapFontParameter();
//
//        // 扩展加密属性配置
//        PARAMETERS[ResourceMappingClass.TEXTURE_ZB.ordinal()] = new TextureLoaderZones.TextureParameter(Coding.ZB);
//        PARAMETERS[ResourceMappingClass.TEXTUREATLAS_ZB.ordinal()] = new TextureAtlasLoaderZones.TextureAtlasParameter(Coding.ZB, true);
    }
}
