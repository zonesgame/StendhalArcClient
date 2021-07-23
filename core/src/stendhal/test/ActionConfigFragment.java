package stendhal.test;

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
import z.debug.Strs;

import static mindustry.Vars.*;

public class ActionConfigFragment extends Fragment {
    private final static float holdWithdraw = 20f;

    private Table table = new Table();

    private float holdTime = 0f;
    private boolean holding;

    private EntityView<?> entityView;
    public Cons2<String, EntityView<?>> doAction;

    @Override
    public void build(Group parent){
        if (parent.getName() != null)
        table.setName("inventory");
        table.setTransform(true);
        parent.setTransform(true);
        parent.addChild(table);
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
//        table.background(Tex.inventory);
        table.touchable(Touchable.enabled);
        table.update(() -> {

            if(state.is(State.menu) ){
                hide();
            }else{
                updateTablePosition();
            }
        });

        rebuildMenu(table, entityView.getActions());
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
//        Vec2 v = Core.input.mouseScreen(tile.drawx() + tile.block().size * tilesize / 2f, tile.drawy() + tile.block().size * tilesize / 2f);
//        table.pack();
//        table.setPosition(v.x, v.y, Align.topLeft);
    }

//    private Table menu;
    void rebuildMenu(Table menu, String[] actions){
//        Table menu = table;
        menu.clearChildren();
        {
//            menu.addImage(atlasS.find("StendhalSplash")).size(Core.graphics.getWidth(), Core.graphics.getHeight());
        }

        TextButton.TextButtonStyle style = Styles.cleart;

        menu.defaults().size(300f, 60f);

        for (String cmd : actions) {
            menu.addButton(Strs.get(cmd), style, () -> doAction.get(cmd, entityView));
            menu.row();
        }

        menu.setPosition(200, 200);

//        menu.addButton("$settings.graphics", style, () -> {});
//        menu.row();
//        menu.addButton("$settings.sound", style, () -> {});
//        menu.row();
//        menu.addButton("$settings.language", style, () -> {});
//
//        menu.row();
//        menu.addButton("$settings.data", style, () -> {});
    }
}
