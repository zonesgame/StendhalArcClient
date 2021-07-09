package z.tools.serialize;

import arc.Core;
import arc.graphics.g2d.TextureAtlas;
import arc.graphics.g2d.TextureRegion;
import arc.math.geom.Rect;
import arc.struct.ObjectMap;
import arc.util.serialization.JsonReader;
import arc.util.serialization.XmlReader.Element;
import z.debug.assets.PackLoader;

import static z.tools.serialize.XmlSerialize.*;

/**
 *
 */
public class UnitLoader {

    private JsonReader jsonReader;
    private JSON json;

    /** 存储返还对象的临时数据*/
    private final ObjectMap<String, Object> tempPool;

    private XmlSerialize parent;

    public UnitLoader(XmlSerialize xmlSerialize) {
        this.parent = xmlSerialize;

        tempPool = new ObjectMap<>(8);
        jsonReader = parent.jsonReader;
        json = parent.json;
    }


    public ObjectMap loadAnimation(Element configFile) {
        tempPool.clear();

        Element root = configFile;          //  getElement(configFile);
        if ( !root.hasChild("animation")) return null;

        root = root.getChildByName("animation");
        if ( root.hasChild("caesar")) {     //
            initCaesarRegions(root.getChildByName("caesar"));
        }
        if ( root.hasChild("qqtx")) {
            initQQTXRegions(root.getChildByName("qqtx"));
        }

        return tempPool;
    }


    private void initCaesarRegions(Element root) {
//        Element aniNode = root.getChildByName("animation");
//        if (aniNode == null) return;
        Element aniNode = root;

        TextureAtlas atlas = Core.atlas;
        TextureRegion[][][] spriteRegions;

        int ani = aniNode.getIntAttribute("ani", aniNode.getChildCount());
        int dir = 0;
        int frame = 0;
//        if (aniNode.hasAttribute("dir") || aniNode.hasAttribute("frame"))
        {
            dir = aniNode.getIntAttribute("dir", dir);
            frame = aniNode.getIntAttribute("frame", frame);
//            spriteRegions = new TextureRegion[ani][dir][frame];
            spriteRegions = new TextureRegion[ani][][];
        }

        for (int i = 0, len = aniNode.getChildCount(); i < len; i++) {
            Element rNode = aniNode.getChild(i);

            int index = rNode.getIntAttribute("index", i);
            {   // 加载子动画自定义数据
                dir = rNode.getIntAttribute("dir", dir);
                frame = rNode.getIntAttribute("frame", frame);
                spriteRegions[index] = new TextureRegion[dir][frame];
            }

            String aniName = rNode.getText();
            for (int d = 0, k = 0; d < dir; d++) {
                for (int f = 0; f < frame; f++) {
                    spriteRegions[index][d][f] = atlas.find(aniName + ++k);
                }
            }
        }

        tempPool.put(qqtxRegions, spriteRegions);
    }

    private void initQQTXRegions(Element root) {
        if ( root.hasChild("regions")) {
            String[] resourceFoled = root.getChildByName("regions").getText().split(",");
            TextureRegion[][][] upgradeRegions = new TextureRegion[resourceFoled.length][][];         //
            Rect[][][] upgradeRects = new Rect[resourceFoled.length][][];    //
            PackLoader loader = PackLoader.getInstance();
            for (int i = 0, len = resourceFoled.length; i < len; i++) {
                upgradeRegions[i] = loader.packs.get(resourceFoled[i].trim());
                upgradeRects[i] = loader.rects.get(resourceFoled[i].trim());
            }

            tempPool.put(qqtxRegions, upgradeRegions);
            tempPool.put(qqtxRects, upgradeRects);
        }

        if ( root.hasChild("background")) {
            String[] resourceFoled = root.getChildByName("background").getText().split(",");
            PackLoader loader = PackLoader.getInstance();
            Rect bgRect = loader.rects.get(resourceFoled[0].trim())[Integer.parseInt(resourceFoled[1].trim())][Integer.parseInt(resourceFoled[2].trim())];
            TextureRegion bgRegion =  loader.packs.get(resourceFoled[0].trim())[Integer.parseInt(resourceFoled[1].trim())][Integer.parseInt(resourceFoled[2].trim())];

            tempPool.put(qqtxBG, bgRegion);
            tempPool.put(qqtxBGR, bgRect);
        }

        if ( root.hasChild("anis")) {
            String[] resourceFoled = root.getChildByName("anis").getText().split(",");
            TextureRegion[][][] aniRegions = new TextureRegion[resourceFoled.length][][];         //
            Rect[][][] aniRects = new Rect[resourceFoled.length][][];    //
            PackLoader loader = PackLoader.getInstance();
            for (int i = 0, len = resourceFoled.length; i < len; i++) {
                aniRegions[i] = loader.packs.get(resourceFoled[i].trim());
                aniRects[i] = loader.rects.get(resourceFoled[i].trim());
            }

            tempPool.put(qqtxRegions, aniRegions);
            tempPool.put(qqtxRects, aniRects);
        }
    }

}
