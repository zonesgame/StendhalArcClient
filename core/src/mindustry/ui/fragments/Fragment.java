package mindustry.ui.fragments;

import arc.Core;
import arc.Events;
import arc.scene.Element;
import arc.scene.Group;
import arc.scene.event.VisibilityListener;
import mindustry.game.EventType;

public abstract class Fragment{
    public abstract void build(Group parent);
}
