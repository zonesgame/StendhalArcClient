package z.debug;

import arc.Core;
import arc.files.Fi;
import arc.graphics.Texture;
import arc.graphics.g2d.TextureAtlas;
import arc.graphics.g2d.TextureRegion;
import arc.math.geom.Vec2;
import z.debug.assets.PackLoader;

import static z.debug.ZDebug.disable_packLoad;
import static z.debug.ZDebug.enable_editorIso;

/**
 *
 */
public class Assets {

    public static void debugInitRegions(TextureAtlas atlas) {
        TextureRegion tempRegion = loadRegion("debug/node1/rally.png");
        atlas.addRegion("rally", tempRegion);
//        atlas.addRegion("rally-icon", tempRegion);
//        atlas.addRegion("rally-icon-editor", tempRegion);
//        atlas.addRegion("editor-rally", tempRegion);
//        atlas.addRegion("block-rally-full", tempRegion);

        initUIico(atlas);
        initEditorIco(atlas);
        initOthers(atlas);
        initUnits(atlas);
//        initISORegions(atlas);
//        loadISOBuild(atlas);
        if ( !disable_packLoad) {   // 初始化pack资源
            // build
            PackLoader.getInstance().loadAsync(Core.files.internal("debug/pack/build.c5"));
            PackLoader.getInstance().loadRegions(Core.files.internal("debug/pack/build.txt"), false);
            // Actor
            PackLoader.getInstance().loadAsync(Core.files.internal("debug/pack/Actor.c5"));
            PackLoader.getInstance().loadRegions(Core.files.internal("debug/pack/Actor.txt"), true);
        }
        initCaesar(atlas);
    }

    public static TextureRegion loadRegion(String path) {
        Fi handle = Core.files.internal(path);
        if (handle == null)
            handle = Core.files.absolute(path);
        Texture texture = new Texture(handle);
        texture.setFilter(Texture.TextureFilter.Linear);

        return new TextureRegion(texture);
    }

    public static Texture loadTexture(String path) {
        Fi handle = Core.files.internal(path);
        if (handle == null)
            handle = Core.files.absolute(path);
        Texture texture = new Texture(handle);
        texture.setFilter(Texture.TextureFilter.Linear);

        return texture;
    }

    private static void initUnits(TextureAtlas atlas) {
        TextureRegion region1, region2, region3;
        region1 = loadRegion("debug/units/testUnit.png");
        region2 = loadRegion("debug/units/testUnit-base.png");
        region3 = loadRegion("debug/units/testUnit-leg.png");

        atlas.addRegion("testUnit", region1);
        atlas.addRegion("testUnit-base", region2);
        atlas.addRegion("testUnit-leg", region3);
        atlas.addRegion("testUnit2", region1);
        atlas.addRegion("testUnit2-base", region2);
        atlas.addRegion("testUnit2-leg", region3);
    }

    private static void initUIico(TextureAtlas atlas) {
        Fi root = Core.files.internal("debug/ui/UIRes");
        for (Fi handle : root.list()) {
            String filename = handle.nameWithoutExtension();
            int indexof = filename.indexOf(" ");
            String atlasKey = handle.parent().name() + "_" + filename.substring(indexof + 1);
            TextureRegion region = loadRegion(handle.path());
            atlas.addRegion(atlasKey, region);
        }
    }

    private static void initEditorIco(TextureAtlas atlas) {
        if (enable_editorIso) {
            TextureAtlas editorAtlas = new TextureAtlas("sprites/editorSprites.atlas");
            for (String name : editorAtlas.getRegionMap().keys()) {
                if (atlas.has(name)) {
                    System.out.println("Assets has editor region: " + name);
                }
                else
                    atlas.addRegion(name, editorAtlas.find(name));
            }
        }
    }

    private static void initOthers(TextureAtlas atlas) {
        TextureRegion region;

        // 升级图标纹理初始化
        region = loadRegion("debug/ui/BuildIng/image 15.png");
        atlas.addRegion("upgrade1", region);
        region = loadRegion("debug/ui/BuildIng/image 18.png");
        atlas.addRegion("upgrade2", region);
        region = loadRegion("debug/ui/BuildIng/image 13.png");
        atlas.addRegion("upgrade3", region);
        region = loadRegion("debug/ui/BuildIng/image 20.png");
        atlas.addRegion("upgrade4", region);
        region = loadRegion("debug/ui/BuildIng/256-1.png");
        atlas.addRegion("upgrade5", region);
        region = loadRegion("debug/ui/BuildIng/236-1.png");
        atlas.addRegion("upgrade6", region);

//        bgImage = new TextureRegion[11 * 11];
//        for (int i = bgImage.length; --i >= 0; ) {
//            bgImage[i] = new TextureRegion(new Texture(Core.files.internal("debug/map/50/map_" + (i+1) + ".jpg")));
//            atlas.addRegion("_map_" + (i+1), bgImage[i]);
//        }
    }

    public static void initSprites() {
        final String processFoled = "debug/units/10101/";
        TextureAtlas atlas;

        Fi processHandle = Core.files.internal(processFoled);
        String name = processHandle.name();
        String keys[] = {"A", "B", "C", "D", "E", "F", "G", "H"};
        int[] directionIndex = {2, 1, 0, 7, 6, 5, 4, 3};

        int txtIndex = 0;
        int[] txtData = getTxtData(processHandle.child(name + ".txt"));
        int fileCount = txtData[txtIndex++];
        atlas = new TextureAtlas(processHandle.child(name + ".atlas"));
        spriteRegions = new TextureRegion[fileCount][][];
        spriteCenterPoint = new Vec2[fileCount][][];

        for (int h = 0; h < fileCount; h++) {

            int typeID = txtData[txtIndex++];
            int dir = txtData[txtIndex++];
            int frame = txtData[txtIndex++];
            int dirCount = dir == 5 ? 8 : dir;
            spriteRegions[h] = new TextureRegion[dirCount][frame];
            spriteCenterPoint[h] = new Vec2[dirCount][frame];
            for (int _d = 0; _d < dir; _d++) {

                int d = directionIndex[_d];
                for (int f = 0; f < frame; f++) {
                    int offsetX = txtData[txtIndex++];
                    int offsetY = txtData[txtIndex++];
                    spriteCenterPoint[h][d][f] = new Vec2(offsetX, offsetY);

                    String atlasName = name + "_" + typeID + "_" + keys[_d] + f;
                    spriteRegions[h][d][f] = atlas.find(atlasName);
                }

//                animations[h][d] = new Animation(0.11f, frames);
//                animations[h][d].setPlayMode(Animation.PlayMode.LOOP);
            }

            if (dirCount == 8) {    // init other direction
                for (int _d = dir; _d < dirCount; _d++) {
                    int d = directionIndex[_d];
                    int needDirection = dirCount - _d;
                    TextureRegion[] needRegions = spriteRegions[h][directionIndex[needDirection]];
                    Vec2[] needOffsets = spriteCenterPoint[h][directionIndex[needDirection]];
                    int frames = needRegions.length;
//                    TextureRegion[] aniRegions = new TextureRegion[frames];

                    for (int f = 0; f < frames; f++) {
                        spriteCenterPoint[h][d][f] = new Vec2(needOffsets[f]);
                        spriteCenterPoint[h][d][f].x = (needRegions[f].getWidth() - spriteCenterPoint[h][d][f].x);

                        spriteRegions[h][d][f] = new TextureRegion(needRegions[f]);
                        spriteRegions[h][d][f].flip(true, false);
                    }

//                    animations[h][d] = new Animation(0.11f, aniRegions);
//                    animations[h][d].setPlayMode(Animation.PlayMode.LOOP);
                }
            }
        }
    }

    private static int[] getTxtData(Fi handle) {
        String[] tempValue = handle.readString().split(",");
        int value[] = new int[tempValue.length];
        for (int i = 0; i < value.length; i++) {
            value[i] = Integer.parseInt(tempValue[i]);
        }
        return value;
    }

    private static void initCaesar(TextureAtlas atlas) {

//        atlas.addRegion("ground1_1", loadRegion("debug/blocks/environment/tile/land1a_00001.png"));
////        atlas.addRegion("ground1_1", loadRegion(path + "246-1.png"));
//        atlas.addRegion("ground1_2", loadRegion(path + "247-1.png"));
//        atlas.addRegion("ground1_3", loadRegion(path + "248-1.png"));
//        atlas.addRegion("ground1_4", loadRegion(path + "249-1.png"));
//
//        atlas.addRegion("forest1_1", loadRegion(path + "297-1.png"));
//        atlas.addRegion("forest1_2", loadRegion(path + "298-1.png"));
//        atlas.addRegion("forest1_3", loadRegion(path + "299-1.png"));
//        atlas.addRegion("forest1_4", loadRegion(path + "300-1.png"));
//
//        atlas.addRegion("grass1_1", loadRegion(path + "274-1.png"));
//        atlas.addRegion("grass1_2", loadRegion(path + "275-1.png"));
//        atlas.addRegion("grass1_3", loadRegion(path + "277-1.png"));
//        atlas.addRegion("grass1_4", loadRegion(path + "278-1.png"));
//
//        atlas.addRegion("cropland1_1", loadRegion(path + "263-1.png"));
//        atlas.addRegion("cropland1_2", loadRegion(path + "264-1.png"));
//        atlas.addRegion("cropland1_3", loadRegion(path + "265-1.png"));
//        atlas.addRegion("cropland1_4", loadRegion(path + "266-1.png"));
//
//        atlas.addRegion("water1_1", loadRegion(path + "364-1.png"));
//        atlas.addRegion("water1_2", loadRegion(path + "365-1.png"));
//        atlas.addRegion("water1_3", loadRegion(path + "366-1.png"));
//        atlas.addRegion("water1_4", loadRegion(path + "367-1.png"));
//
//        atlas.addRegion("mountain_1", loadRegion(path + "201-1.png"));
//        atlas.addRegion("mountain_2", loadRegion(path + "218-1.png"));
//        atlas.addRegion("mountain_3", loadRegion(path + "219-1.png"));
//        atlas.addRegion("mountain_4", loadRegion(path + "220-1.png"));
//
//        atlas.addRegion("drill1_1", loadRegion(path + "2883-1.png"));
//        atlas.addRegion("drill1_node1", loadRegion(path + "2904-1.png"));
//        atlas.addRegion("drill1_node2", loadRegion(path + "2884-1.png"));

//        // 道路添加
//        atlas.addRegion("road_1", loadRegion(path + "642-1.png"));
//        atlas.addRegion("road_2", loadRegion(path + "643-1.png"));
//        atlas.addRegion("road_3", loadRegion(path + "644-1.png"));
//        atlas.addRegion("road_4", loadRegion(path + "645-1.png"));
//        atlas.addRegion("road_5", loadRegion(path + "646-1.png"));
//        atlas.addRegion("road_6", loadRegion(path + "647-1.png"));
//        atlas.addRegion("road_7", loadRegion(path + "648-1.png"));
//        atlas.addRegion("road_8", loadRegion(path + "649-1.png"));
//        atlas.addRegion("road_9", loadRegion(path + "650-1.png"));
//        atlas.addRegion("road_10", loadRegion(path + "651-1.png"));
//        atlas.addRegion("road_11", loadRegion(path + "652-1.png"));
//        atlas.addRegion("road_12", loadRegion(path + "653-1.png"));
//        atlas.addRegion("road_13", loadRegion(path + "654-1.png"));
//        atlas.addRegion("road_14", loadRegion(path + "655-1.png"));
//        atlas.addRegion("road_15", loadRegion(path + "656-1.png"));
//        atlas.addRegion("road_16", loadRegion(path + "657-1.png"));

        // 农场数据, 游戏使用非开发数据 begon
//        atlas.addRegion("farm_base", loadRegion(path + "2883-1.png"));
//        atlas.addRegion("farm_11", loadRegion(path + "2884-1.png"));
//        atlas.addRegion("farm_12", loadRegion(path + "2885-1.png"));
//        atlas.addRegion("farm_13", loadRegion(path + "2886-1.png"));
//        atlas.addRegion("farm_14", loadRegion(path + "2887-1.png"));
//        atlas.addRegion("farm_15", loadRegion(path + "2888-1.png"));
//        atlas.addRegion("farm_21", loadRegion(path + "2889-1.png"));
//        atlas.addRegion("farm_22", loadRegion(path + "2890-1.png"));
//        atlas.addRegion("farm_23", loadRegion(path + "2891-1.png"));
//        atlas.addRegion("farm_24", loadRegion(path + "2892-1.png"));
//        atlas.addRegion("farm_25", loadRegion(path + "2893-1.png"));
//        atlas.addRegion("farm_31", loadRegion(path + "2894-1.png"));
//        atlas.addRegion("farm_32", loadRegion(path + "2895-1.png"));
//        atlas.addRegion("farm_33", loadRegion(path + "2896-1.png"));
//        atlas.addRegion("farm_34", loadRegion(path + "2897-1.png"));
//        atlas.addRegion("farm_35", loadRegion(path + "2898-1.png"));
//        atlas.addRegion("farm_41", loadRegion(path + "2899-1.png"));
//        atlas.addRegion("farm_42", loadRegion(path + "2900-1.png"));
//        atlas.addRegion("farm_43", loadRegion(path + "2901-1.png"));
//        atlas.addRegion("farm_44", loadRegion(path + "2902-1.png"));
//        atlas.addRegion("farm_45", loadRegion(path + "2903-1.png"));
//        atlas.addRegion("farm_51", loadRegion(path + "2904-1.png"));
//        atlas.addRegion("farm_52", loadRegion(path + "2905-1.png"));
//        atlas.addRegion("farm_53", loadRegion(path + "2906-1.png"));
//        atlas.addRegion("farm_54", loadRegion(path + "2907-1.png"));
//        atlas.addRegion("farm_55", loadRegion(path + "2908-1.png"));
//        atlas.addRegion("farm_61", loadRegion(path + "2909-1.png"));
//        atlas.addRegion("farm_62", loadRegion(path + "2910-1.png"));
//        atlas.addRegion("farm_63", loadRegion(path + "2911-1.png"));
//        atlas.addRegion("farm_64", loadRegion(path + "2912-1.png"));
//        atlas.addRegion("farm_65", loadRegion(path + "2913-1.png"));
        // end

        // GUI物品图标 begon
        Fi root = Core.files.internal("debug/gui/items/");
        for (Fi handle : root.list()) {
            String atlasKey = handle.nameWithoutExtension();
            TextureRegion region = loadRegion(handle.path());
            atlas.addRegion(atlasKey, region);
        }
        // GUI物品图标 end

        // 民居 begon
        root = Core.files.internal("debug/blocks/housing/");
        for (Fi handle : root.list()) {
            String atlasKey = handle.nameWithoutExtension();
            TextureRegion region = loadRegion(handle.path());
            atlas.addRegion(atlasKey, region);
        }
        // 民居 end

        // 市场 begon
        root = Core.files.internal("debug/blocks/market/");
        for (Fi handle : root.list()) {
            String atlasKey = handle.nameWithoutExtension();
            TextureRegion region = loadRegion(handle.path());
            atlas.addRegion(atlasKey, region);
        }
        // 市场 end

        // 仓库 begon
        root = Core.files.internal("debug/blocks/warehouse");
        for (Fi handle : root.list()) {
            String atlasKey = handle.nameWithoutExtension();
            TextureRegion region = loadRegion(handle.path());
            atlas.addRegion(atlasKey, region);
        }
        // 仓库 end

        // 粮仓 begon
        root = Core.files.internal("debug/blocks/granary");
        for (Fi handle : root.list()) {
            String atlasKey = handle.nameWithoutExtension();
            TextureRegion region = loadRegion(handle.path());
            atlas.addRegion(atlasKey, region);
        }
        // 粮仓 end

        // 农场 begon
        root = Core.files.internal("debug/blocks/farm");
        for (Fi handle : root.list()) {
            String atlasKey = handle.nameWithoutExtension();
            TextureRegion region = loadRegion(handle.path());
            atlas.addRegion(atlasKey, region);
        }
        // 农场 end

        // 原料厂 begon
        root = Core.files.internal("debug/blocks/rawMaterials");
        for (Fi handle : root.list()) {
            String atlasKey = handle.nameWithoutExtension();
            TextureRegion region = loadRegion(handle.path());
            atlas.addRegion(atlasKey, region);
        }
        // 原料厂 end

        // 工厂 begon
        root = Core.files.internal("debug/blocks/workshops");
        for (Fi handle : root.list()) {
            String atlasKey = handle.nameWithoutExtension();
            TextureRegion region = loadRegion(handle.path());
            atlas.addRegion(atlasKey, region);
        }
        // 工厂 end

        // 道路 begon
        root = Core.files.internal("debug/blocks/roads");
        for (Fi handle : root.list()) {
            String atlasKey = handle.nameWithoutExtension();
            TextureRegion region = loadRegion(handle.path());
            atlas.addRegion(atlasKey, region);
        }
        // 道路 end

        // QQTX UI begon
        root = Core.files.internal("debug/ui/qqtx/");
        for (Fi handle : root.list()) {
            String atlasKey = handle.nameWithoutExtension();
            if (atlasKey.indexOf('.') != -1) {
                atlasKey = atlasKey.substring(0, atlasKey.indexOf('.'));
                TextureAtlas.AtlasRegion region_ = new TextureAtlas.AtlasRegion(loadRegion(handle.path()));
                region_.name = atlasKey;
                region_.splits = new int[]{3, 3, 3, 3};
                atlas.getRegions().add(region_);
                atlas.getRegionMap().put(atlasKey, region_);
            } else {
                TextureRegion region = loadRegion(handle.path());
                atlas.addRegion(atlasKey, region);
            }
        }
        // QQTX UI end

        // sprite begon
        // 工人 begon
        root = Core.files.internal("debug/units/carts");
        for (Fi foled : root.list()) {
            for (Fi handle : foled.list()) {
                String atlasKey = handle.nameWithoutExtension();
                TextureRegion region = loadRegion(handle.path());
                atlas.addRegion(atlasKey, region);
            }
        }
        // 工人 end

        // market begon
        root = Core.files.internal("debug/units/market");
        for (Fi foled : root.list()) {
            for (Fi handle : foled.list()) {
                String atlasKey = handle.nameWithoutExtension();
                TextureRegion region = loadRegion(handle.path());
                atlas.addRegion(atlasKey, region);
            }
        }
        // market end
        // sprite end

        // surface begon
        root = Core.files.internal("debug/blocks/environment");
        for (Fi handle : root.list()) {
            String atlasKey = handle.nameWithoutExtension();
            TextureRegion region = loadRegion(handle.path());
            atlas.addRegion(atlasKey, region);
        }
        // surface end

//        path = "debug/caesar/blocks/";
//        atlas.addRegion("surface1", loadRegion(path + "surface1.png"));
//        atlas.addRegion("surface2", loadRegion(path + "surface2.png"));
//        atlas.addRegion("surface3", loadRegion(path + "surface3.png"));
//        atlas.addRegion("surface4", loadRegion(path + "surface4.png"));
//        atlas.addRegion("surface5", loadRegion(path + "surface5.png"));
//        atlas.addRegion("surface6", loadRegion(path + "surface6.png"));
////        atlas.addRegion("surface-icon-editor", atlas.find("surface-icon-editor"));    // -icon-editor
//        atlas.addRegion("editor-surface1", atlas.find("editor-stone1"));    // -icon-editor
//
//        atlas.addRegion("timber1", loadRegion(path + "timber1.png"));
//        atlas.addRegion("timber2", loadRegion(path + "timber2.png"));
//        atlas.addRegion("timber3", loadRegion(path + "timber3.png"));
//        atlas.addRegion("timber4", loadRegion(path + "timber4.png"));
//        atlas.addRegion("timber5", loadRegion(path + "timber5.png"));
//        atlas.addRegion("timber6", loadRegion(path + "timber6.png"));
////        atlas.addRegion("editor-timber1", atlas.find("editor-grass1"));    // -icon-editor
//
//        atlas.addRegion("clay1", loadRegion(path + "clay1.png"));
//        atlas.addRegion("clay2", loadRegion(path + "clay2.png"));
//        atlas.addRegion("clay3", loadRegion(path + "clay3.png"));
//        atlas.addRegion("clay4", loadRegion(path + "clay4.png"));
//        atlas.addRegion("clay5", loadRegion(path + "clay5.png"));
//        atlas.addRegion("clay6", loadRegion(path + "clay6.png"));
//        atlas.addRegion("editor-clay1", atlas.find("editor-darksand-tainted-water"));    // -icon-editor    darksand-tainted-water
//
//        atlas.addRegion("farmland1", loadRegion(path + "farmland1.png"));
//        atlas.addRegion("farmland2", loadRegion(path + "farmland2.png"));
//        atlas.addRegion("farmland3", loadRegion(path + "farmland3.png"));
//        atlas.addRegion("farmland4", loadRegion(path + "farmland4.png"));
//        atlas.addRegion("farmland5", loadRegion(path + "farmland5.png"));
//        atlas.addRegion("farmland6", loadRegion(path + "farmland6.png"));
//        atlas.addRegion("editor-farmland1", atlas.find("editor-sand1"));    // -icon-editor
//
//        atlas.addRegion("sea1", loadRegion(path + "sea.png"));
//        atlas.addRegion("editor-sea1", atlas.find("editor-water"));    // -icon-editor
//
//        atlas.addRegion("marble1", loadRegion(path + "marble1.png"));
//        atlas.addRegion("marble2", loadRegion(path + "marble2.png"));
//        atlas.addRegion("marble3", loadRegion(path + "marble3.png"));
//        atlas.addRegion("marble4", loadRegion(path + "marble4.png"));
//        atlas.addRegion("marble5", loadRegion(path + "marble5.png"));
//        atlas.addRegion("marble6", loadRegion(path + "marble6.png"));
//        atlas.addRegion("marble7", loadRegion(path + "marble7.png"));
//        atlas.addRegion("marble8", loadRegion(path + "marble8.png"));
//        atlas.addRegion("editor-marble1", atlas.find("editor-holostone1"));    // -icon-editor
//
//        atlas.addRegion("iron1", loadRegion(path + "iron.png"));
//        atlas.addRegion("editor-iron1", atlas.find("editor-magmarock1"));    // -icon-editor

        // 地表Floor障碍物数据
        atlas.addRegion("landtree", loadRegion("F:\\Develop\\workspace\\ps\\DeadMeat100\\建筑d\\3141.png"));
    }


    public static TextureRegion[][][] spriteRegions;
    public static Vec2[][][] spriteCenterPoint;
    public static float[][][] spriteFrameDuration;

    public static TextureRegion[][][] buildRegions;
    public static Vec2[][][] buildCenterPoint;
    // ISO地图建筑数据end
}
