package mindustry.ui;

import arc.*;
import mindustry.annotations.Annotations.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.graphics.g2d.TextureAtlas.*;
import arc.scene.style.*;
import arc.scene.ui.Button.*;
import arc.scene.ui.CheckBox.*;
import arc.scene.ui.Dialog.*;
import arc.scene.ui.ImageButton.*;
import arc.scene.ui.KeybindDialog.*;
import arc.scene.ui.Label.*;
import arc.scene.ui.ScrollPane.*;
import arc.scene.ui.Slider.*;
import arc.scene.ui.TextButton.*;
import arc.scene.ui.TextField.*;
import mindustry.gen.*;
import mindustry.graphics.*;

import static mindustry.gen.Tex.*;

@StyleDefaults
public class Styles{
    public static Drawable black, black9, black8, black6, black3, none, flatDown, flatOver;
    public static ButtonStyle defaultb, waveb;
    public static TextButtonStyle defaultt, squaret, nodet, cleart, discordt, infot, clearPartialt, clearTogglet, clearToggleMenut, togglet, transt;
    public static ImageButtonStyle defaulti, nodei, righti, emptyi, emptytogglei, selecti, cleari, clearFulli, clearPartiali, clearPartial2i, clearTogglei, clearTransi, clearToggleTransi, clearTogglePartiali;
    public static ScrollPaneStyle defaultPane, horizontalPane, smallPane;
    public static KeybindDialogStyle defaultKeybindDialog;
    public static SliderStyle defaultSlider, vSlider;
    public static LabelStyle defaultLabel, outlineLabel, label1;
    public static TextFieldStyle defaultField, areaField;
    public static CheckBoxStyle defaultCheck;
    public static DialogStyle defaultDialog, fullDialog;
    // zones add begon
    // 权倾天下 add begon
    public static ImageButtonStyle blockeditor1i, blockeditor2i, blockeditor3i ;
    // 权倾天下 add end

    // zones add end

    public static void load(){
        // zones add begon
        loadSG();
        // zones add end
        black = whiteui.tint(0f, 0f, 0f, 1f);
        black9 = whiteui.tint(0f, 0f, 0f, 0.9f);
        black8 = whiteui.tint(0f, 0f, 0f, 0.8f);
        black6 = whiteui.tint(0f, 0f, 0f, 0.6f);
        black3 = whiteui.tint(0f, 0f, 0f, 0.3f);
        none = whiteui.tint(0f, 0f, 0f, 0f);
        flatDown = createFlatDown();
        flatOver = whiteui.tint(Color.valueOf("454545"));

        // zones add begon
        blockeditor1i = new ImageButtonStyle(){{
            down = blockedirotb1se;
            up = blockedirotb1bg;
            imageDisabledColor = Color.gray;
            imageUpColor = Color.white;
        }};
        blockeditor2i = new ImageButtonStyle(){{
            down = blockedirotb2se;
            up = blockedirotb2bg;
            imageDisabledColor = Color.gray;
            imageUpColor = Color.white;
        }};

        label1 = new LabelStyle(){{
            font = Fonts.def;
            fontColor = Color.white;
            this.background = white9s2;
        }};
        // zones add end

        defaultb = new ButtonStyle(){{
            down = buttonDown;
            up = button;
            over = buttonOver;
            disabled = buttonDisabled;
        }};
        
        waveb = new ButtonStyle(){{
            up = buttonEdge4;
            over = buttonEdgeOver4;
            disabled = buttonEdge4;
        }};

        defaultt = new TextButtonStyle(){{
            over = buttonOver;
            disabled = buttonDisabled;
            font = Fonts.def;
            fontColor = Color.white;
            disabledFontColor = Color.gray;
            down = buttonDown;
            up = button;
        }};
        squaret = new TextButtonStyle(){{
            font = Fonts.def;
            fontColor = Color.white;
            disabledFontColor = Color.gray;
            over = buttonSquareOver;
            disabled = buttonDisabled;
            down = buttonSquareDown;
            up = buttonSquare;
        }};
        nodet = new TextButtonStyle(){{
            disabled = button;
            font = Fonts.def;
            fontColor = Color.white;
            disabledFontColor = Color.gray;
            up = buttonOver;
            over = buttonDown;
        }};
        cleart = new TextButtonStyle(){{
            over = flatOver;
            font = Fonts.def;
            fontColor = Color.white;
            disabledFontColor = Color.gray;
            down = flatOver;
            up = black;
        }};
        discordt = new TextButtonStyle(){{
            font = Fonts.def;
            fontColor = Color.white;
            up = discordBanner;
        }};
        infot = new TextButtonStyle(){{
            font = Fonts.def;
            fontColor = Color.white;
            up = infoBanner;
        }};
        clearPartialt = new TextButtonStyle(){{
            down = whiteui;
            up = pane;
            over = flatDown;
            font = Fonts.def;
            fontColor = Color.white;
            disabledFontColor = Color.gray;
        }};
        transt = new TextButtonStyle(){{
            down = flatDown;
            up = none;
            over = flatOver;
            font = Fonts.def;
            fontColor = Color.white;
            disabledFontColor = Color.gray;
        }};
        clearTogglet = new TextButtonStyle(){{
            font = Fonts.def;
            fontColor = Color.white;
            checked = flatDown;
            down = flatDown;
            up = black;
            over = flatOver;
            disabled = black;
            disabledFontColor = Color.gray;
        }};
        clearToggleMenut = new TextButtonStyle(){{
            font = Fonts.def;
            fontColor = Color.white;
            checked = flatDown;
            down = flatDown;
            up = clear;
            over = flatOver;
            disabled = black;
            disabledFontColor = Color.gray;
        }};
        togglet = new TextButtonStyle(){{
            font = Fonts.def;
            fontColor = Color.white;
            checked = buttonDown;
            down = buttonDown;
            up = button;
            over = buttonOver;
            disabled = buttonDisabled;
            disabledFontColor = Color.gray;
        }};

        defaulti = new ImageButtonStyle(){{
            down = buttonDown;
            up = button;
            over = buttonOver;
            imageDisabledColor = Color.gray;
            imageUpColor = Color.white;
            disabled = buttonDisabled;
        }};
        nodei = new ImageButtonStyle(){{
            up = buttonOver;
            over = buttonDown;
        }};
        righti = new ImageButtonStyle(){{
            over = buttonRightOver;
            down = buttonRightDown;
            up = buttonRight;
        }};
        emptyi = new ImageButtonStyle(){{
            imageDownColor = Pal.accent;
            imageUpColor = Color.white;
        }};
        emptytogglei = new ImageButtonStyle(){{
            imageCheckedColor = Color.white;
            imageDownColor = Color.white;
            imageUpColor = Color.gray;
        }};
        selecti = new ImageButtonStyle(){{
            checked = buttonSelect;
            up = none;
        }};
        cleari = new ImageButtonStyle(){{
            down = flatOver;
            up = black;
            over = flatOver;
        }};
        clearFulli = new ImageButtonStyle(){{
            down = whiteui;
            up = pane;
            over = flatDown;
        }};
        clearPartiali = new ImageButtonStyle(){{
            down = flatDown;
            up = none;
            over = flatOver;
        }};
        clearPartial2i = new ImageButtonStyle(){{
            down = whiteui;
            up = pane;
            over = flatDown;
        }};
        clearTogglei = new ImageButtonStyle(){{
            down = flatDown;
            checked = flatDown;
            up = black;
            over = flatOver;
        }};
        clearTransi = new ImageButtonStyle(){{
            down = flatDown;
            up = black6;
            over = flatOver;
            disabled = black8;
            imageDisabledColor = Color.lightGray;
            imageUpColor = Color.white;
        }};
        clearToggleTransi = new ImageButtonStyle(){{
            down = flatDown;
            checked = flatDown;
            up = black6;
            over = flatOver;
        }};
        clearTogglePartiali = new ImageButtonStyle(){{
            down = flatDown;
            checked = flatDown;
            up = none;
            over = flatOver;
        }};

        defaultPane = new ScrollPaneStyle(){{
            vScroll = scroll;
            vScrollKnob = scrollKnobVerticalBlack;
        }};
//        defaultPane = new ScrollPaneStyle(){{           // zones default
//            vScroll = zscroll6;
//            vScrollKnob = scrollKnobVerticalBlack;
//        }};
        horizontalPane = new ScrollPaneStyle(){{
            vScroll = scroll;
            vScrollKnob = scrollKnobVerticalBlack;
            hScroll = scrollHorizontal;
            hScrollKnob = scrollKnobHorizontalBlack;
        }};
        smallPane = new ScrollPaneStyle(){{
            vScroll = clear;
            vScrollKnob = scrollKnobVerticalThin;
        }};

        defaultKeybindDialog = new KeybindDialogStyle(){{
            keyColor = Pal.accent;
            keyNameColor = Color.white;
            controllerColor = Color.lightGray;
        }};

        defaultSlider = new SliderStyle(){{
            background = slider;
            knob = sliderKnob;
            knobOver = sliderKnobOver;
            knobDown = sliderKnobDown;
        }};
        vSlider = new SliderStyle(){{
            background = sliderVertical;
            knob = sliderKnob;
            knobOver = sliderKnobOver;
            knobDown = sliderKnobDown;
        }};

        defaultLabel = new LabelStyle(){{
            font = Fonts.def;
            fontColor = Color.white;
        }};
        outlineLabel = new LabelStyle(){{
            font = Fonts.outline;
            fontColor = Color.white;
        }};

        defaultField = new TextFieldStyle(){{
            font = Fonts.chat;
            fontColor = Color.white;
            disabledFontColor = Color.gray;
            disabledBackground = underlineDisabled;
            selection = Tex.selection;
            background = underline;
            invalidBackground = underlineRed;
            cursor = Tex.cursor;
            messageFont = Fonts.def;
            messageFontColor = Color.gray;
        }};
        areaField = new TextFieldStyle(){{
            font = Fonts.chat;
            fontColor = Color.white;
            disabledFontColor = Color.gray;
            selection = Tex.selection;
            background = underline;
            cursor = Tex.cursor;
            messageFont = Fonts.def;
            messageFontColor = Color.gray;
        }};

        defaultCheck = new CheckBoxStyle(){{
            checkboxOn = checkOn;
            checkboxOff = checkOff;
            checkboxOnOver = checkOnOver;
            checkboxOver = checkOver;
            checkboxOnDisabled = checkOnDisabled;
            checkboxOffDisabled = checkDisabled;
            font = Fonts.def;
            fontColor = Color.white;
            disabledFontColor = Color.gray;
        }};

        defaultDialog = new DialogStyle(){{
            stageBackground = black9;
            titleFont = Fonts.def;
            background = windowEmpty;
            titleFontColor = Pal.accent;
        }};
        fullDialog = new DialogStyle(){{
            stageBackground = black;
            titleFont = Fonts.def;
            background = windowEmpty;
            titleFontColor = Pal.accent;
        }};
    }

    private static Drawable createFlatDown(){
        AtlasRegion region = Core.atlas.find("flat-down-base");
        int[] splits = region.splits;

        ScaledNinePatchDrawable copy = new ScaledNinePatchDrawable(new NinePatch(region, splits[0], splits[1], splits[2], splits[3])){
            public float getLeftWidth(){ return 0; }
            public float getRightWidth(){ return 0; }
            public float getTopHeight(){ return 0; }
            public float getBottomHeight(){ return 0; }
        };
        copy.setMinWidth(0);
        copy.setMinHeight(0);
        copy.setTopHeight(0);
        copy.setRightWidth(0);
        copy.setBottomHeight(0);
        copy.setLeftWidth(0);
        return copy;
    }


    // zones add begon
    public static TextureRegionDrawable blockedirotb1bg;
    public static TextureRegionDrawable blockedirotb1se;
    public static TextureRegionDrawable blockedirotb2bg;
    public static TextureRegionDrawable blockedirotb2se;
    public static TextureRegionDrawable blockedirotbg;
    // ico begon
    public static TextureRegionDrawable blockedirotico[] = new TextureRegionDrawable[16];
    public static TextureRegionDrawable blockedirotico1;
    public static TextureRegionDrawable blockedirotico2;
    public static TextureRegionDrawable blockedirotico3;
    public static TextureRegionDrawable blockedirotico4;
    public static TextureRegionDrawable blockedirotico5;
    public static TextureRegionDrawable blockedirotico6;
    // ico end
    public static TextureRegionDrawable block1;
    public static TextureRegionDrawable block2;
    public static TextureRegionDrawable block3;
    public static TextureRegionDrawable block4;
    public static TextureRegionDrawable block5;

    public static NinePatchDrawable testNinepatch;
    public static NinePatchDrawable testNinepatch2;

    public static void loadSG() {
        blockedirotb1bg = (arc.scene.style.TextureRegionDrawable) arc.Core.atlas.drawable("UIRes_852");
        blockedirotb1se = (arc.scene.style.TextureRegionDrawable) arc.Core.atlas.drawable("UIRes_854");
        blockedirotb2bg = (arc.scene.style.TextureRegionDrawable) arc.Core.atlas.drawable("UIRes_847");
        blockedirotb2se = (arc.scene.style.TextureRegionDrawable) arc.Core.atlas.drawable("UIRes_849");
        blockedirotbg = (arc.scene.style.TextureRegionDrawable) arc.Core.atlas.drawable("UIRes_843");
        // ico begon
        blockedirotico1 = (arc.scene.style.TextureRegionDrawable) arc.Core.atlas.drawable("UIRes_857");     // 移动
        blockedirotico2 = (arc.scene.style.TextureRegionDrawable) arc.Core.atlas.drawable("UIRes_859");     // 升级
        blockedirotico3 = (arc.scene.style.TextureRegionDrawable) arc.Core.atlas.drawable("UIRes_861");     // 立即完成
        blockedirotico4 = (arc.scene.style.TextureRegionDrawable) arc.Core.atlas.drawable("UIRes_881");     // 设置
        blockedirotico5 = (arc.scene.style.TextureRegionDrawable) arc.Core.atlas.drawable("UIRes_236-1");     // 工作
        blockedirotico6 = (arc.scene.style.TextureRegionDrawable) arc.Core.atlas.drawable("UIRes_256-1");     // 休息
        blockedirotico[0] = blockedirotico1;
        blockedirotico[1] = blockedirotico2;
        blockedirotico[2] = blockedirotico3;
        blockedirotico[3] = blockedirotico4;
        blockedirotico[4] = blockedirotico5;
        blockedirotico[5] = blockedirotico6;
        // ico end
        block1 = (arc.scene.style.TextureRegionDrawable) arc.Core.atlas.drawable("UIRes_886");
        block2 = (arc.scene.style.TextureRegionDrawable) arc.Core.atlas.drawable("UIRes_888");
        block3 = (arc.scene.style.TextureRegionDrawable) arc.Core.atlas.drawable("UIRes_893");
        block4 = (arc.scene.style.TextureRegionDrawable) arc.Core.atlas.drawable("UIRes_896");
        block5 = (arc.scene.style.TextureRegionDrawable) arc.Core.atlas.drawable("UIRes_898");

        TextureRegion regions[] = createNinePatchTemp(arc.Core.atlas.find("UIRes_255"), arc.Core.atlas.find("UIRes_257"), arc.Core.atlas.find("UIRes_256"));
        testNinepatch = new NinePatchDrawable(new NinePatch(regions));
        regions = createNinePatchTemp(arc.Core.atlas.find("UIRes_260"), arc.Core.atlas.find("UIRes_262"), arc.Core.atlas.find("UIRes_261"));
        testNinepatch2 = new NinePatchDrawable(new NinePatch(regions));
    }

    private static TextureRegion[] createNinePatchTemp(TextureRegion... regions) {
        if (regions.length == 3) {
            TextureRegion tempregions[] = new TextureRegion[] {
                    null, null, null,
                    regions[0], regions[1], regions[2],
                    null, null, null
            };
            regions = tempregions;
        }

        int textureWidht = regions[3].getWidth() + regions[4].getWidth() + regions[5].getWidth();
        int textureHeight = regions[3].getHeight();
        Pixmap pixmap = new Pixmap(textureWidht, textureHeight, Pixmap.Format.RGBA8888);
        pixmap.drawPixmap(regions[3].getTexture().getTextureData().getPixmap(), 0, 0);
        pixmap.drawPixmap(regions[4].getTexture().getTextureData().getPixmap(), regions[3].getWidth(), 0);
        pixmap.drawPixmap(regions[5].getTexture().getTextureData().getPixmap(), regions[3].getWidth() + regions[4].getWidth(), 0);
        Texture texture = new Texture(pixmap);
        TextureRegion temp = regions[3];
        regions[3] = new TextureRegion(texture, 0, 0, temp.getWidth(), temp.getHeight());
        temp = regions[4];
        regions[4] = new TextureRegion(texture, regions[3].getWidth(), 0, temp.getWidth(), temp.getHeight());
        temp = regions[5];
        regions[5] = new TextureRegion(texture, regions[3].getWidth() + regions[4].getWidth(), 0, temp.getWidth(), temp.getHeight());

        return regions;
    }
    // zones add end
}
