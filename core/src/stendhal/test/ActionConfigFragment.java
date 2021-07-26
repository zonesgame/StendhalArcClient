package stendhal.test;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JMenuItem;

import arc.*;
import arc.func.*;
import arc.graphics.g2d.*;
import arc.input.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.*;
import arc.scene.actions.*;
import arc.scene.event.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import games.stendhal.client.entity.Entity;
import games.stendhal.client.gui.j2d.entity.EntityView;
import mindustry.annotations.Annotations.*;
import mindustry.core.GameState.*;
import mindustry.entities.*;
import mindustry.entities.type.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.net.Administration.*;
import mindustry.net.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.ui.fragments.Fragment;
import mindustry.world.*;
import temp.java.awt.Rectangle;
import z.debug.Strs;

import static games.stendhal.client.IGameScreen.SIZE_UNIT_PIXELS;
import static mindustry.Vars.*;

public class ActionConfigFragment extends Fragment {
    private final static float holdWithdraw = 20f;

    private Table table;
    private Table menu;

    private float holdTime = 0f;
    private boolean holding;

    private EntityView<?> entityView;
    public Cons2<String, EntityView<?>> doAction;

    private boolean camerafollow = true;

    @Override
    public void build(Group parent){
        table = new Table();
        if (parent.getName() != null)
            table.setName("inventory");
        table.setTransform(true);
        parent.setTransform(true);
        parent.addChild(table);

        menu = new Table(Tex.button);
        menu.setFillParent(true);
//        table.clearChildren();
        table.add(menu).pad(0);
    }

    public void showFor(EntityView<?> view){
        if(this.entityView == view){
            hide();
            return;
        }
        this.entityView = view;
        if(entityView == null || view.getActions().length == 0 /*|| entityView.getEntity() == null */)
            return;
        rebuild(true);
    }

    public void hide(){
        if(table == null) return;

        table.actions(Actions.scaleTo(0f, 1f, 0.06f, Interpolation.pow3Out), Actions.run(() -> {
            table.clearChildren();
            table.clearListeners();
            table.update(null);
        }), Actions.visible(false));
        table.touchable(Touchable.disabled);
        this.entityView = null;
    }

    private void rebuild(boolean actions){

        table.clearChildren();
        table.clearActions();
        table.add(menu);        // zones add
//        table.background(Tex.inventory);
        table.touchable(Touchable.enabled);
        table.update(() -> {

            if(state.is(State.menu) ){
                hide();
            }else{
                updateTablePosition();
            }
        });

        rebuildMenu(entityView.getActions());
        updateTablePosition();

        table.visible(true);

        if(actions){
            table.setScale(0f, 1f);
            table.actions(Actions.scaleTo(1f, 1f, 0.07f, Interpolation.pow3Out));
        }else{
            table.setScale(1f, 1f);
        }
    }


    private void updateTablePosition(){
        if (entityView != null) {
            if (camerafollow) {
                T_GameScreen2 screen = T_GameScreen2.get();
                Rectangle drect = Rectangle.tmp;
                drect.setRect(entityView.getArea());
                screen.worldtoStage(drect);
                drect.width = (int) (drect.width * entityView.getEntity().getWidth());
                drect.height = (int) (drect.height * entityView.getEntity().getHeight());
                drect.x = drect.x + Math.max(drect.width / 2, SIZE_UNIT_PIXELS);
                drect.y = (int) (drect.y + table.getHeight() / 2 + drect.height / 2);
                if (entityView.getEntity().getHeight() > 1) {
                    drect.y -= entityView.getArea().getHeight() * (entityView.getEntity().getHeight() - 1);
                }
//                drect.translate(screen.mapOffsetPixX, screen.mapOffsetPixY);

                table.pack();
                table.setPosition(drect.x, drect.y, Align.topLeft);
//                Rec rect = Rect.tmp2.set(entityView.getEntity().);
//                System.out.println(drect.toString() + "    XXXX   " + entityView.getArea());
//                System.out.println(entityView.getEntity().getArea() + "    X     " + entityView.getArea());

            }
            else {

            }
        }
//        Vec2 v = Core.input.mouseScreen(tile.drawx() + tile.block().size * tilesize / 2f, tile.drawy() + tile.block().size * tilesize / 2f);
//        table.pack();
//        table.setPosition(v.x, v.y, Align.topLeft);
    }

//    private Table menu;
    void rebuildMenu(String[] actions){
//        Table menu = table;
        menu.clearChildren();
        {
//            menu.addImage(atlasS.find("StendhalSplash")).size(Core.graphics.getWidth(), Core.graphics.getHeight());
        }

        TextButton.TextButtonStyle style = Styles.cleart;

        menu.defaults().size(100, 30);

        populate(actions);
//        for (String cmd : actions) {
//            menu.addButton(Strs.get(cmd), style, () -> doAction.get(cmd, entityView)).get().getLabel().setWrap(false);
//            menu.row();
//        }

//        menu.setPosition(200, 200);

//        menu.addButton("$settings.graphics", style, () -> {});
//        menu.row();
//        menu.addButton("$settings.sound", style, () -> {});
//        menu.row();
//        menu.addButton("$settings.language", style, () -> {});
//
//        menu.row();
//        menu.addButton("$settings.data", style, () -> {});
    }

    private void addButton(String name, String command) {
        TextButton.TextButtonStyle style = Styles.cleart;
        menu.addButton(Strs.get(name), style, () -> {
            doAction.get(command, entityView);
            this.hide();
        }).get().getLabel().setWrap(false);
        menu.row();
    }

    /**
     * Populate the menu.
     *
     * @param items menu items
     */
    private void populate(final String[] items) {
        String labelname;
        String command = null;

        for (String item : items) {
            // Comma separated list of commands, not a single command
            if (item.indexOf(',') > 0) {
                String[] sublist = item.split(",");
                populate(sublist);
                continue;
            }

            if (item.startsWith("(*)")) {
//                icon = adminIcon;
                labelname = item.substring(3);
            } else {
//                icon = null;
                labelname = item;
            }
            /*
             * Deal with '|' definitions. (Coming from server side).
             * Before the break is the user representation, after it is the
             * usual representation of the actual command.
             *
             * That is, a teddy might have a menu item definition "Hug|Use".
             */
            int breakPoint = labelname.indexOf('|');
            if (breakPoint >= 0) {
                labelname = item.substring(0, breakPoint);
                command = item.substring(breakPoint + 1);
            }

            addButton(labelname, command == null ? labelname : command);
        }
    }

    public boolean isvisible() {
        return table.isVisible();
    }
}
