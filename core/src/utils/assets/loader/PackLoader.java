package utils.assets.loader;

import com.badlogic.gdx.graphics.g2d.Gdx2DPixmap;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import arc.Core;
import arc.assets.*;
import arc.assets.loaders.FileHandleResolver;
import arc.assets.loaders.TextureLoader;
import arc.graphics.g2d.TextureAtlas;
import arc.graphics.g2d.TextureRegion;
import arc.struct.*;
import arc.files.*;
import arc.graphics.Pixmap.*;
import arc.graphics.*;
import arc.graphics.Texture.*;
import arc.util.io.Streams;
import arc.util.serialization.XmlReader;
import mindustry.Vars;
import temp.Debug;
import utils.assets.Pack;
import z.debug.assets.Coding;
import z.debug.assets.ResourceMappingClass;

public class PackLoader extends DecodeAsynchronousAssetLoader<Pack, PackLoader.PackParameter> {

    class SyncData {
        Class classtype;
        String path;
        Pixmap pixmap;
        ResourceMappingClass para;
//        TextureFilter minFilter;
//        TextureFilter magFilter;
    }


    private XmlReader reader;

    public PackLoader (FileHandleResolver resolver) {
        super(resolver);

        reader = new XmlReader();
    }


    private SyncData[] syncDataArray;

    @Override
    public void loadAsync (AssetManager manager, String fileName, Fi file, PackParameter parameter) {
        Coding coding = Coding.values()[Integer.parseInt(file.extension().substring(1)) - 1];
        byte[] xmlbytes = getZonesAssets(coding, file.readBytes());
        XmlReader.Element root = reader.parse(new String(xmlbytes));
        syncDataArray = new SyncData[root.getChildCount()];

        Fi packfile = Core.files.internal(file.pathWithoutExtension() + ".pack");
        byte[] bytes = packfile.readBytes();
        InputStream inputStream = null;
        try {
            inputStream = new GZIPInputStream(new ByteArrayInputStream(bytes), bytes.length);

            for (int i = 0; i < root.getChildCount(); i++) {
                XmlReader.Element node = root.getChild(i);
                int nodeSize = node.getIntAttribute("size", 0);
                Coding nodeCoding = Coding.valueOf(node.getAttribute("coding").toUpperCase());
                String nodeStrPar = node.getAttribute("parameter", null);
                ResourceMappingClass nodeParEnu = null;
                if (nodeStrPar != null) {
                    nodeParEnu = ResourceMappingClass.valueOf(nodeStrPar.toUpperCase());
//                    nodeParameter = (TextureLoaderZones.TextureParameter) PARAMETERS[nodeParEnu.ordinal()];
                }
                String nodePath = node.getAttribute("path");
                ResourceMappingClass nodeTypeEnu = ResourceMappingClass.valueOf(node.getAttribute("type").toUpperCase());
                Class nodeTypeClass = CLASSES[nodeTypeEnu.ordinal()];

                byte[] nodBytes = new byte[nodeSize];
                inputStream.read(nodBytes);
                nodBytes = getZonesAssets(nodeCoding, nodBytes);

                syncDataArray[i] = new SyncData();
                syncDataArray[i].path = nodePath;
                syncDataArray[i].classtype = nodeTypeClass;
                syncDataArray[i].para = nodeParEnu != null ? nodeParEnu : (parameter != null ? parameter.textureFilter : null);
                syncDataArray[i].pixmap = new Pixmap(new Gdx2DPixmap(nodBytes, 0, nodBytes.length, 0));
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Streams.close(inputStream);
        }
    }

    @Override
    public Pack loadSync (AssetManager manager, String fileName, Fi file, PackParameter parameter) {
        for (SyncData syncData : syncDataArray) {
            Texture nodeTexture = new Texture(syncData.pixmap);
            TextureRegion region = new TextureRegion(nodeTexture);
//            byteManager.addAsset(syncData.path, syncData.classtype, region);
            TextureAtlas atlas = Vars.atlasS;
//            if (Debug.NOTE1)
//                ;
            atlas.addRegion(syncData.path, region);

            if (syncData.para != null) {
                TextureLoader.TextureParameter nodeParameter = (TextureLoader.TextureParameter) PARAMETERS[syncData.para.ordinal()];
                nodeTexture.setFilter(nodeParameter.minFilter, nodeParameter.magFilter);
                nodeTexture.setWrap(nodeParameter.wrapU, nodeParameter.wrapV);
            }
        }

        syncDataArray = null;
        return new Pack(true);
    }

    @Override
    public Array<AssetDescriptor> getDependencies (String fileName, Fi file, PackParameter parameter) {
        return null;
    }

    static public class PackParameter extends AssetLoaderParameters<Pack> {
        public ResourceMappingClass textureFilter = null;
//        public TextureFilter magFilter = null;
//        public TextureWrap wrapU = null;
//        public TextureWrap wrapV = null;

        public PackParameter() {
        }

        public PackParameter(ResourceMappingClass textureFilter) {
            this.textureFilter = textureFilter;
        }
    }
}