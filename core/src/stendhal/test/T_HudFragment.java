package stendhal.test;

import arc.Core;
import arc.Events;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.input.KeyCode;
import arc.math.Interpolation;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.scene.Element;
import arc.scene.Group;
import arc.scene.actions.Actions;
import arc.scene.event.Touchable;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.Button;
import arc.scene.ui.Image;
import arc.scene.ui.ImageButton;
import arc.scene.ui.ImageButton.ImageButtonStyle;
import arc.scene.ui.TextField;
import arc.scene.ui.layout.Scl;
import arc.scene.ui.layout.Stack;
import arc.scene.ui.layout.Table;
import arc.struct.Array;
import arc.util.Align;
import arc.util.Scaling;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.annotations.Annotations.Loc;
import mindustry.annotations.Annotations.Remote;
import mindustry.core.GameState.State;
import mindustry.ctype.ContentType;
import mindustry.ctype.UnlockableContent;
import mindustry.entities.Units;
import mindustry.entities.type.BaseUnit;
import mindustry.entities.type.Player;
import mindustry.game.EventType.ResizeEvent;
import mindustry.game.EventType.Trigger;
import mindustry.game.Team;
import mindustry.gen.Call;
import mindustry.gen.Icon;
import mindustry.gen.Sounds;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.input.Binding;
import mindustry.net.Packets.AdminAction;
import mindustry.type.UnitType;
import mindustry.ui.Bar;
import mindustry.ui.Cicon;
import mindustry.ui.IntFormat;
import mindustry.ui.Minimap;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.FloatingDialog;
import mindustry.ui.fragments.Fragment;
import mindustry.ui.fragments.PlacementFragment;
import temp.Debug;
import z.ui.fragments.GroupFragment;

import static mindustry.Vars.content;
import static mindustry.Vars.control;
import static mindustry.Vars.enableUnitEditing;
import static mindustry.Vars.mobile;
import static mindustry.Vars.net;
import static mindustry.Vars.netClient;
import static mindustry.Vars.netServer;
import static mindustry.Vars.player;
import static mindustry.Vars.spawner;
import static mindustry.Vars.state;
import static mindustry.Vars.ui;
import static mindustry.Vars.world;

/**
 *  游戏内容界面
 * */
public class T_HudFragment extends Fragment {
//    /** 放置块界面*/
//    public final PlacementFragment blockfrag = new PlacementFragment();
//    /** 队伍操控界面*/
//    public final GroupFragment groupfrag = new GroupFragment();
//
//    private ImageButton flip;
//    private Table lastUnlockTable;
//    private Table lastUnlockLayout;
//    private boolean shown = true;
//    private float dsize = 47.2f;
//
//    /** 界面文本*/
//    private String hudText = "";
//    /** 是否显示界面文本*/
//    private boolean showHudText;
//
//    private long lastToast;

    private Table root;


    /** 游戏界面构建*/
    public void build(Group parent) {
        //menu at top left

        parent.fill(Tex.white9s1, cont -> {
            root = cont;
            cont.setName("overlaymarker");
            cont.top().left();

            cont.update(() -> {
                if(Core.input.keyTap(Binding.toggle_menus) && !ui.chatfrag.shown() && !Core.scene.hasDialog() && !(Core.scene.getKeyboardFocus() instanceof TextField)){
//                    toggleMenus();
                }
            });

        });

        // children build
        ui.miniFrag.build(root);
        ui.chatFrag.build(root);

        parent.fill(t -> {
//            t.visible(() -> Core.settings.getBool("minimap") && !state.rules.tutorial);
//            //minimap
//            t.add(new Minimap());
//            t.row();
//            //position
//            t.label(() -> world.toTile(player.x) + "," + world.toTile(player.y))
//                    .visible(() -> Core.settings.getBool("position") && !state.rules.tutorial);
//            t.top().right();
        });

        //spawner warning
//        parent.fill(t -> {
//            t.touchable(Touchable.disabled);
//            t.table(Styles.black, c -> c.add("$nearpoint")
//                    .update(l -> l.setColor(Tmp.c1.set(Color.white).lerp(Color.scarlet, Mathf.absin(Time.time(), 10f, 1f))))
//                    .get().setAlignment(Align.center, Align.center))
//                    .margin(6).update(u -> u.color.a = Mathf.lerpDelta(u.color.a, Mathf.num(spawner.playerNear()), 0.1f)).get().color.a = 0f;
//        });

//        parent.fill(t -> {
//            t.visible(() -> netServer.isWaitingForPlayers());
//            t.table(Tex.button, c -> c.add("$waiting.players"));
//        });


        //tutorial text
//        parent.fill(t -> {
//            Runnable resize = () -> {
//                t.clearChildren();
//                t.top().right().visible(() -> state.rules.tutorial);
//                t.stack(new Button(){{
//                            marginLeft(48f);
//                            labelWrap(() -> control.tutorial.stage.text() + (control.tutorial.canNext() ? "\n\n" + Core.bundle.get("tutorial.next") : "")).width(!Core.graphics.isPortrait() ? 400f : 160f).pad(2f);
//                            clicked(() -> control.tutorial.nextSentence());
//                            setDisabled(() -> !control.tutorial.canNext());
//                        }},
//                        new Table(f -> {
//                            f.left().addImageButton(Icon.left, Styles.emptyi, () -> {
//                                control.tutorial.prevSentence();
//                            }).width(44f).growY().visible(() -> control.tutorial.canPrev());
//                        }));
//            };
//
//            resize.run();
//            Events.on(ResizeEvent.class, e -> resize.run());
//        });

        //paused table
//        parent.fill(t -> {
//            t.top().visible(() -> state.isPaused()).touchable(Touchable.disabled);
//            t.table(Tex.buttonTrans, top -> top.add("$paused").pad(5f));
//        });

        //'saving' indicator
//        parent.fill(t -> {
//            t.bottom().visible(() -> control.saves.isSaving());
//            t.add("$saveload").style(Styles.outlineLabel);
//        });
//
//        parent.fill(p -> {
//            p.top().table(Styles.black3, t -> t.margin(4).label(() -> hudText)
//                    .style(Styles.outlineLabel)).padTop(10).visible(p.color.a >= 0.001f);
//            p.update(() -> {
//                p.color.a = Mathf.lerpDelta(p.color.a, Mathf.num(showHudText), 0.2f);
//                if(state.is(State.menu)){
//                    p.color.a = 0f;
//                    showHudText = false;
//                }
//            });
//            p.touchable(Touchable.disabled);
//        });

//        blockfrag.build(parent);
//        groupfrag.build(parent);
    }


}
