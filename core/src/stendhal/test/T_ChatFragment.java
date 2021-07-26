package stendhal.test;

import java.util.Locale;

import arc.*;
import arc.Input.*;
import arc.struct.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.scene.*;
import arc.scene.ui.*;
import arc.scene.ui.Label.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import mindustry.*;
import mindustry.gen.*;
import mindustry.input.*;
import mindustry.ui.*;
import mindustry.ui.fragments.Fragment;
import temp.Debug;

import static arc.Core.*;
import static mindustry.Vars.net;
import static mindustry.Vars.*;

public class T_ChatFragment extends Fragment{
    @Override
    public void build(Group parent) {
        Table langs = new Table();
        langs.marginRight(24f).marginLeft(24f);
        ScrollPane pane = new ScrollPane(langs);
        pane.setFadeScrollBars(false);

        ButtonGroup<TextButton> group = new ButtonGroup<>();

        for(Locale loc : locales){
            TextButton button = new TextButton(Strings.capitalize(displayNames.get(loc, loc.getDisplayName(loc))), Styles.clearTogglet);
            button.clicked(() -> {
                if(getLocale().equals(loc)) return;
                Core.settings.put("locale", loc.toString());
                Core.settings.save();
                Log.info("Setting locale: {0}", loc.toString());
                ui.showInfo("$language.restart");
            });
            langs.add(button).group(group).update(t -> t.setChecked(loc.equals(getLocale()))).size(400f, 50f).row();
        }

        parent.addChild(pane);
//        cont.add(pane);
    }

}
