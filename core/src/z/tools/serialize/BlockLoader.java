package z.tools.serialize;

import arc.Core;
import arc.graphics.g2d.TextureAtlas;
import arc.graphics.g2d.TextureRegion;
import arc.math.geom.Rect;
import arc.struct.ObjectMap;
import arc.util.serialization.XmlReader;
import arc.util.serialization.XmlReader.Element;
import z.debug.assets.PackLoader;

import static z.debug.ZDebug.disable_packLoad;
import static z.tools.serialize.XmlSerialize.qqtxANI;
import static z.tools.serialize.XmlSerialize.qqtxANIR;
import static z.tools.serialize.XmlSerialize.qqtxBG;
import static z.tools.serialize.XmlSerialize.qqtxBGR;
import static z.tools.serialize.XmlSerialize.qqtxRects;
import static z.tools.serialize.XmlSerialize.qqtxRegions;

/**
 *
 */
public class BlockLoader {

    private final String fieldType = "type";

//    private JsonReader jsonReader;
//    private JSON json;

    /** 存储返还对象的临时数据*/
    private final ObjectMap<String, Object> tempPool;

    private XmlSerialize parent;

    public BlockLoader(XmlSerialize xmlSerialize) {
        this.parent = xmlSerialize;

        tempPool = new ObjectMap<>(8);
//        jsonReader = parent.jsonReader;
//        json = parent.json;
    }


    public void loadConfigFile(Element configFile, Object instance) {
        Element root = configFile;      // getElement(configFile);
        if ( !root.hasChild("attribute"))   return;

        root = root.getChildByName("attribute");
        if ( root.hasChild("fields")) {     // 加载配置属性数组
            parent.loadFields(instance, root.getChildByName("fields"));
        }
        if( root.hasChild("methods")) {  // 加载方法数据
            parent.loadMethods(instance, root.getChildByName("methods"));
        }
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


//    private void loadFields(Object instance, Element root) {
//        try { //  // 属性数据加载测试
//            for(int i = 0, len = root.getChildCount(); i < len; i++) {
//                XmlReader.Element fieldNode = root.getChild(i);
//                String type = fieldNode.getAttribute(fieldType, null);
//                JsonValue value = jsonReader.parse(fieldNode.getText());
//                String fieldName = value.child.name;
//                if(fieldName == null) fieldName = fieldNode.getName();
//                if(type != null) {
//                    Object obj = null;
//                    if( (obj = parent.getCustomObject(type, value)) != null) {
//                        parent.executeField(instance, fieldName, obj);
//                    } else {
//                        json.readFieldCustom(instance, fieldName, type, value);
//                    }
//                }
//                else {
//                    json.readField(instance, fieldName, fieldName, value);
//                }
//            }
//        } catch (Exception e) {
//            Log.warn("XmlSerialize: " + root.getName(), e);
//        }
//    }

    private void initCaesarRegions(Element root) {
        TextureAtlas atlas = Core.atlas;
        TextureRegion[][] aniRegions = new TextureRegion[root.getChildCount()][];
        int[] offset = new int[aniRegions.length * 2];
        for (int i = 0, len = root.getChildCount(); i < len; i++) {
            XmlReader.Element node = root.getChild(i);
//            int index = node.getIntAttribute("index");
            int frame = node.getIntAttribute("frame");
            String name = node.getText();
            TextureRegion[] _regions = new TextureRegion[frame];
            for (int f = 0; f < frame;) {
                _regions[f] = atlas.find(name + ++f);
            }
            aniRegions[i] = _regions;

            {// 动画偏移量
                String offsetStr = node.getAttribute("offset", null);
                if (offsetStr != null) {
                    int indexof = offsetStr.indexOf(',');
                    offset[i * 2] = Integer.parseInt(offsetStr.substring(0, indexof));
                    offset[i * 2 + 1] = Integer.parseInt(offsetStr.substring(indexof + 1));
                }
            }
        }

        tempPool.put(parent.aniRegions, aniRegions);
        tempPool.put(parent.offset, offset);
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

        if (disable_packLoad)   return;

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

            tempPool.put(qqtxANI, aniRegions);
            tempPool.put(qqtxANIR, aniRects);
        }
    }
}
