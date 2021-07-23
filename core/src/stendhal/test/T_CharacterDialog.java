package stendhal.test;

import java.util.Locale;

import javax.swing.JOptionPane;

import arc.*;
import arc.graphics.Color;
import arc.graphics.Pixmap;
import arc.graphics.Texture;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.scene.event.Touchable;
import arc.scene.style.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.Array;
import arc.util.*;
import arc.util.async.Threads;
import arc.z.util.ZonesAnnotate;
import games.stendhal.client.OutfitStore;
import games.stendhal.client.StendhalClient;
import games.stendhal.client.ZoneInfo;
import games.stendhal.client.entity.Player;
import games.stendhal.client.entity.StatusID;
import games.stendhal.client.gui.OutfitColor;
import games.stendhal.client.gui.j2d.Blend;
import games.stendhal.client.gui.login.Profile;
import games.stendhal.client.sprite.ImageSprite;
import games.stendhal.client.sprite.QueueSprite;
import games.stendhal.client.sprite.SequenceSprite;
import games.stendhal.client.sprite.Sprite;
import games.stendhal.client.sprite.SpriteStore;
import games.stendhal.client.stendhal;
import marauroa.client.BannedAddressException;
import marauroa.client.TimeoutException;
import marauroa.common.game.CharacterResult;
import marauroa.common.game.RPObject;
import marauroa.common.net.InvalidVersionException;
import mindustry.*;
import mindustry.core.GameState;
import mindustry.entities.bullet.BulletType;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.maps.*;
import mindustry.ui.*;
import mindustry.ui.dialogs.FloatingDialog;
import temp.Debug;
import z.debug.Strs;

import static arc.Core.settings;
import static games.stendhal.client.gui.j2d.entity.Player2DView.ZOMBIE_COLOR;
import static mindustry.Vars.state;
import static mindustry.Vars.ui;
import static z.debug.Strs.str33;
import static z.debug.Strs.str35;
import static z.debug.Strs.str36;
import static z.utils.FinalCons.PreFix.pfCharaName;
import static z.utils.FinalCons.SETTING_KEYS.lastLogin;
import static z.utils.FinalCons.SETTING_KEYS.savePassword;

//   Events.fire(new ClientLoadEvent());
public class T_CharacterDialog extends FloatingDialog {
//    private MapPlayDialog dialog = new MapPlayDialog();
    private java.util.Map<String, RPObject> characters;

    public T_CharacterDialog(){
        super(Strs.get(str35));
        addCloseButton();
        shown(this::setup);
        onResize(this::setup);
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        keepClientAlive(delta);
    }

    void setup(){
        clearChildren();
        add(titleTable);
        row();
        stack(cont, buttons).grow();
        buttons.bottom();
        cont.clear();

        Table characterGroup = new Table();
        characterGroup.marginRight(14);
        characterGroup.marginBottom(55f);
        ScrollPane pane = new ScrollPane(characterGroup);
        pane.setFadeScrollBars(true);

        int maxwidth = Mathf.clamp((int)(Core.graphics.getWidth() / Scl.scl(200)), 1, 4);       // MAX8
        float images = 146f;

        int i = 0;
        characterGroup.defaults().width(170).fillY().top().pad(4f);
        {
//            String name = characters.keySet().iterator().next();
//            RPObject rpObject = characters.get(name);
//            for (int k = 0; k < 5; k++) {

//            }
            for (String name : characters.keySet()) {
                RPObject rpObject = characters.get(name);

                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(name);
                if (rpObject.has("name") && rpObject.has("level")) {
                    stringBuilder.append('\n');
                    stringBuilder.append("Level: " + rpObject.get("level"));
                }

                Player player = new Player() {
                    @Override
                    protected void onPosition(double x, double y) {
                    }
                    @Override
                    protected void addSounds(String groupName, String categoryName, String... soundNames) {
                    }
                };
                player.initialize(rpObject);
                Drawable playerDrawable = getPlayerDrawable(player);

                if(i % maxwidth == 0){
                    characterGroup.row();
                }

                if (true) {
                    ImageButton image = new ImageButton(playerDrawable);
                    image.margin(5);
                    image.top();

                    Image img = image.getImage();
                    img.remove();

//                image.row();
//                image.table(t -> {
//                    t.left();
//                    for(Gamemode mode : Gamemode.all){
//                        TextureRegionDrawable icon = Vars.ui.getIcon("mode" + Strings.capitalize(mode.name()) + "Small");
//                        if(Core.atlas.isFound(icon.getRegion())){
//                            t.addImage(icon).size(16f).pad(4f);
//                        }
//                    }
//                }).left();
                    image.row();
                    Label label = image.add(stringBuilder.toString()).pad(1f).growX().wrap().left().get();
                    label.setAlignment(Align.center);
                    label.setEllipsis(true);
                    image.row();
                    image.addImage(Tex.whiteui, Pal.heal).growX().pad(3).height(4f);
                    image.row();
                    image.add(img).size(images);

//                BorderImage border = new BorderImage(map.safeTexture(), 3f);
//                border.setScaling(Scaling.fit);
//                image.replaceImage(border);

                image.clicked(() -> {       // event process
                    T_CharacterDialog.this.touchable(Touchable.disabled);
                    Core.app.post(()->{
                        chooseCharacter(name);
//                        T_CharacterDialog.this.touchable(Touchable.enabled);
                    });
                });

                    characterGroup.add(image).pad(10);
                } else {
                    characterGroup.addImageTextButton(stringBuilder.toString(), playerDrawable, ()->{});
                }

                i++;
            }
            cont.add(pane).uniformX();
        }
    }

    @Override
    public void addCloseButton() {
        super.addCloseButton();
        buttons.addImageTextButton(str36, Icon.left, () -> {
//            System.out.println("aaaaaaaaaaaaaaaaa");
//            Vars.ui.showText("AAAA", "BBBBBBB");
            ui.showTextInput("Please enter the name of your character (only letters allowed)",
                    "Create Character:", "",
                    name ->{
                        try {
                            // TODO: error handling, exceptions and return of false
                            CharacterResult result = StendhalClient.get().createCharacter(name.toLowerCase(Locale.ENGLISH), new RPObject());
                            if (result.getResult().failed()) {
//                                JOptionPane.showMessageDialog(parent, result.getResult().getText());
                                ui.showInfoText(result.getResult().getText(), "");
                            } else {
//                                parent.setVisible(false);
                            }
                        } catch (TimeoutException e) {
                            Log.err( e);
//                            parent.handleError("Your connection timed out, please login again.", "Choose Character");
                            ui.showInfoText("Choose Character", "Your connection timed out, please login again.");
                        } catch (InvalidVersionException e) {
                            Log.err(e);
//                            parent.handleError("Your version of Stendhal is incompatible with the server.", "Choose Character");
                            ui.showInfoText("Choose Character", "Your version of Stendhal is incompatible with the server.");
                        } catch (BannedAddressException e) {
                            Log.err( e);
//                            parent.handleError("Please login again.", "Choose Character");
                            ui.showInfoText("Please login again.", "Choose Character");
                        }
                    }
            );
        }).size(210f, 64f);
//        buttons.addImageTextButton("$back", Icon.left, this::hide).size(210f, 64f);

//        addCloseListener();
    }

    @ZonesAnnotate.ZAdd
    public void show(java.util.Map<String, RPObject> characters) {
        this.characters = characters;
        // 更新登录记录
        ui.joinDialog.recordLogionHistory();
        show();
    }


    private Sprite[] getAnimationSprite(Player player) {
        final OutfitStore store = OutfitStore.get();
        Sprite outfit;

        try {
            OutfitColor color = OutfitColor.get(player.getRPObject());
            ZoneInfo info = ZoneInfo.get();

            final String strcode = player.getExtOutfit();
            final int code = player.getOutfit();

            if (strcode == null) {
                final int body = code % 100;
                final int dress = code / 100 % 100;
                final int head = (int) (code / Math.pow(100, 2) % 100);
                final int hair = (int) (code / Math.pow(100, 3) % 100);
                final int detail = (int) (code / Math.pow(100, 4) % 100);

                final StringBuilder sb = new StringBuilder();
                sb.append("body=" + body);
                sb.append(",dress=" + dress);
                sb.append(",head=" + head);
                sb.append(",hair=" + hair);
                sb.append(",detail=" + detail);

                outfit = store.getAdjustedOutfit(sb.toString(), color, info.getZoneColor(), info.getColorMethod());
            } else {
                outfit = store.getAdjustedOutfit(strcode, color, info.getZoneColor(), info.getColorMethod());
            }

            if (player.hasStatus(StatusID.ZOMBIE)) {
                outfit = SpriteStore.get().modifySprite(outfit, ZOMBIE_COLOR, Blend.TrueColor, null);
            }
        } catch (final RuntimeException e) {
            Log.warn("Cannot build outfit. Setting failsafe outfit.", e);
            outfit = store.getFailsafeOutfit();
        }

        if (Debug.NOTE1)
            ;
        return ((QueueSprite) outfit).getImages();
//        return addShadow(outfit);
    }

    private Drawable getPlayerDrawable(Player player) {
        Sprite[] sprites = getAnimationSprite(player);
        Array<TextureRegionDrawable> drawables = new Array<>();
        for (Sprite sprite : sprites) {
            if (sprite instanceof ImageSprite) {
                int direction = 2;  // 初始化方向数据
                int PLAYER_WIDTH = 48;
                int PLAYER_HEIGHT = 64;
                Sprite spriteSplit = SpriteStore.get().getTile(sprite, PLAYER_WIDTH, direction * PLAYER_HEIGHT, PLAYER_WIDTH, PLAYER_HEIGHT);
                TextureRegion region = ((ImageSprite) spriteSplit).getImage();
                drawables.add(new TextureRegionDrawable(region));
            }
        }

        return new SequenceDrawable<TextureRegionDrawable>(drawables.toArray(TextureRegionDrawable.class));
    }


    /**
     * Called when a character is selected.
     *
     * @param character player selected by the user
     */
    private void chooseCharacter(final String character) {
        try {
            StendhalClient.get().chooseCharacter(character);
            Events.fire(new EventType.ClientConnectOverEvent());
//            stendhal.setDoLogin();
            T_CharacterDialog.this.hide();
        } catch (TimeoutException e) {
            Log.err( e);
//            handleError("Your connection timed out, please login again.", "Choose Character");
            ui.showInfoText("Choose Character", "Your connection timed out, please login again.");
        } catch (InvalidVersionException e) {
            Log.err(e);
//            handleError("Your version of Stendhal is incompatible with the server.", "Choose Character");
            ui.showInfoText("Choose Character", "Your version of Stendhal is incompatible with the server.");
        } catch (BannedAddressException e) {
            Log.err(e);
//            handleError("Please login again.", "Choose Character");
            ui.showInfoText("Choose Character", "Please login again.");
        }
    }

    private float aliveStepup = 5, alivetime;
    private void keepClientAlive(float delta){
        alivetime += delta;

        if(alivetime >= aliveStepup){
            StendhalClient.get().sendKeepAlive();
            alivetime %= aliveStepup;
        }
    }

    @Override
    public void hide() {
        super.hide();
//        if ( !state.is(GameState.State.menu)){
//            ui.joinDialog.hide();
//            ui.accountDialog.hide();
//        }
    }
}
