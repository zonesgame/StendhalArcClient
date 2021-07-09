package mindustry.graphics;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.TextureRegion;
import arc.graphics.gl.Shader;
import arc.scene.ui.layout.Scl;
import arc.util.ArcAnnotate.Nullable;
import arc.util.Time;

public class Shaders{
    public static Shadow shadow;
    public static BlockBuild blockbuild;
    public static @Nullable
    Shield shield;
    public static UnitBuild build;
    public static FogShader fog;
    public static MenuShader menu;
    public static LightShader light;
    public static SurfaceShader water, tar;

    /** 阴影绘制Shader*/
//    public static DiabloShader diablo;

    public static void init(){
        shadow = new Shadow();
        blockbuild = new BlockBuild();
        try{
            shield = new Shield();
        }catch(Throwable t){
            //don't load shield shader
            shield = null;
            t.printStackTrace();
        }
        build = new UnitBuild();
        fog = new FogShader();
        menu = new MenuShader();
        light = new LightShader();
        water = new SurfaceShader("water");
        tar = new SurfaceShader("tar");

//        diablo = new DiabloShader();
    }

    public static class LightShader extends LoadShader{
        public Color ambient = new Color(0.01f, 0.01f, 0.04f, 0.99f);

        public LightShader(){
            super("light", "default");
        }

        @Override
        public void apply(){
            setUniformf("u_ambient", ambient);
        }

    }

    public static class MenuShader extends LoadShader{
        float time = 0f;

        public MenuShader(){
            super("menu", "default");
        }

        @Override
        public void apply(){
            time = time % 158;

            setUniformf("u_resolution", Core.graphics.getWidth(), Core.graphics.getHeight());
            setUniformi("u_time", (int)(time += Core.graphics.getDeltaTime() * 60f));
            setUniformf("u_uv", Core.atlas.white().getU(), Core.atlas.white().getV());
            setUniformf("u_scl", Scl.scl(1f));
            setUniformf("u_uv2", Core.atlas.white().getU2(), Core.atlas.white().getV2());
        }
    }

    public static class FogShader extends LoadShader{
        public FogShader(){
            super("fog", "default");
        }
    }

    public static class UnitBuild extends LoadShader{
        public float progress, time;
        public Color color = new Color();
        public TextureRegion region;

        public UnitBuild(){
            super("unitbuild", "default");
        }

        @Override
        public void apply(){
            setUniformf("u_time", time);
            setUniformf("u_color", color);
            setUniformf("u_progress", progress);
            setUniformf("u_uv", region.getU(), region.getV());
            setUniformf("u_uv2", region.getU2(), region.getV2());
            setUniformf("u_texsize", region.getTexture().getWidth(), region.getTexture().getHeight());
        }
    }

    public static class Shadow extends LoadShader{
        public Color color = new Color();
        public TextureRegion region = new TextureRegion();
        public float scl;

        public Shadow(){
            super("shadow", "default");
        }

        @Override
        public void apply(){
            setUniformf("u_color", color);
            setUniformf("u_scl", scl);
            setUniformf("u_texsize", region.getTexture().getWidth(), region.getTexture().getHeight());
        }
    }

    public static class BlockBuild extends LoadShader{
        public Color color = new Color();
        public float progress;
        public TextureRegion region = new TextureRegion();

        public BlockBuild(){
            super("blockbuild", "default");
        }

        @Override
        public void apply(){
            setUniformf("u_progress", progress);
            setUniformf("u_color", color);
            setUniformf("u_uv", region.getU(), region.getV());
            setUniformf("u_uv2", region.getU2(), region.getV2());
            setUniformf("u_time", Time.time());
            setUniformf("u_texsize", region.getTexture().getWidth(), region.getTexture().getHeight());
        }
    }

    public static class Shield extends LoadShader{

        public Shield(){
            super("shield", "default");
        }

        @Override
        public void apply(){
            setUniformf("u_dp", Scl.scl(1f));
            setUniformf("u_time", Time.time() / Scl.scl(1f));
            setUniformf("u_offset",
            Core.camera.position.x - Core.camera.width / 2,
            Core.camera.position.y - Core.camera.height / 2);
            setUniformf("u_texsize", Core.camera.width,
            Core.camera.height);
        }
    }

    public static class SurfaceShader extends LoadShader{

        public SurfaceShader(String frag){
            super(frag, "default");
        }

        @Override
        public void apply(){
            setUniformf("camerapos", Core.camera.position.x - Core.camera.width / 2, Core.camera.position.y - Core.camera.height / 2);
            setUniformf("screensize", Core.camera.width, Core.camera.height);
            setUniformf("time", Time.time());
        }
    }

    /** 用于倾斜阴影绘制*/
//    public static class DiabloShader extends LoadShader {
//        private static final int PALETTE_TEXTURE_ID  = 1;
//        private static final int COLORMAP_TEXTURE_ID = 2;
//
//        private final int PALETTE_LOCATION;
//        private final int BLENDMODE_LOCATION;
//        private final int COLORMAP_LOCATION;
//        private final int COLORMAPID_LOCATION;
//        private final int GAMMA_LOCATION;
//
//        private Texture palette;
//        private Texture colormap;
//        private int blendMode;
//        private int colormapId;
//        private Color color = Color.white.cpy();
//        private float gamma = 1.0f;
//        private boolean disabled = false;
//
//        public DiabloShader(){
//            super("indexpalette3", "indexpalette3");
//
//            PALETTE_LOCATION    = getUniformLocation("ColorTable");
//            COLORMAP_LOCATION   = getUniformLocation("ColorMap");
//            BLENDMODE_LOCATION  = getUniformLocation("blendMode");
//            COLORMAPID_LOCATION = getUniformLocation("colormapId");
//            GAMMA_LOCATION      = getUniformLocation("gamma");
//        }
//
//        @Override
//        public void apply(){
//            setUniformi(PALETTE_LOCATION, PALETTE_TEXTURE_ID);
//            setUniformi(COLORMAP_LOCATION, COLORMAP_TEXTURE_ID);
//            setUniformi(COLORMAPID_LOCATION, colormapId);
//            setUniformf(GAMMA_LOCATION, gamma);
//            setUniformi(BLENDMODE_LOCATION, blendMode);
//        }
//
//        public DiabloShader setPalette(Texture palette) {
//            this.palette = palette;
//            applyPalette();
//            return this;
//        }
//
////        public void setColormap(Index colormap, int id) {
////            setColormap(colormap != null ? colormap.texture : null, id);
////        }
////
////        public void setColormap(Texture colormap, int id) {
////            if (id == 0 || colormap == null) {
////                resetColormap();
////                return;
////            }
////
////            this.colormap = colormap;
////            this.colormapId = id;
////            applyColormap();
////        }
//
//        public void resetColormap() {
//            colormapId = 0;
//        }
//
//        public void setBlendMode(int blendMode, Color tint) {
//            setBlendMode(blendMode, tint, false);
//        }
//
//        public void setBlendMode(int blendMode, Color tint, boolean force) {
//            setBlendMode(blendMode);
//            Draw.color(tint);
//        }
//
//        public void setBlendMode(int blendMode) {
//            if (this.blendMode != blendMode) {
//                this.blendMode = blendMode;
//            }
//        }
//
//        public void resetBlendMode() {
//            setBlendMode(BlendMode.ID, Color.white);
//        }
//
//        public void setAlpha(float a) {
//            color.a = a;
//            Draw.color(color);
//        }
//
//        public void resetColor() {
//            Draw.color(Color.white);
//        }
//
//        public float getGamma() {
//            return gamma;
//        }
//
//        public void setGamma(float gamma) {
//            if (this.gamma != gamma) {
//                this.gamma = gamma;
//            }
//        }
//
//
//        private void applyPalette() {
//            if (palette == null) return;
//            palette.bind(PALETTE_TEXTURE_ID);
//            Core.gl.glActiveTexture(GL20.GL_TEXTURE0);
//        }
//
//        private void applyColormap() {
//            colormap.bind(COLORMAP_TEXTURE_ID);
//            Core.gl.glActiveTexture(GL20.GL_TEXTURE0);
//        }
//
//        private void applyGamma() {
//        }
//
//    }


    public static class LoadShader extends Shader{
        public LoadShader(String frag, String vert){
            super(Core.files.internal("shaders/" + vert + ".vertex.glsl"), Core.files.internal("shaders/" + frag + ".fragment.glsl"));
        }
    }
}
