package mindustry.ui.fragments;

import arc.*;
import arc.scene.event.*;
import arc.scene.ui.layout.*;
import arc.z.util.ZonesAnnotate;
import mindustry.*;
import stendhal.test.ActionConfigFragment;

/** Fragment for displaying overlays such as block inventories. */
public class OverlayFragment{
    public final BlockInventoryFragment inv;
    public final BlockConfigFragment config;

    private WidgetGroup group = new WidgetGroup();

    public OverlayFragment(){
        group.touchable(Touchable.childrenOnly);
        inv = new BlockInventoryFragment();
        config = new BlockConfigFragment();
        // stendhal
        actions = new ActionConfigFragment();
    }

    public void add(){
        group.setFillParent(true);
//        Vars.ui.hudGroup.addChildBefore(Core.scene.find("overlaymarker"), group);
        Core.scene.add(group);

        inv.build(group);
        config.build(group);
        // stendhal
        actions.build(group);
    }

    public void remove(){
        group.remove();
    }


    // stendhal begon
    public final ActionConfigFragment actions;

    @ZonesAnnotate.ZAdd
    public void lookAction(String[] actions) {

    }
    // stendhal end
}
