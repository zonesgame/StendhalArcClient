package stendhal.test;

import java.util.Locale;

import arc.*;
import arc.Input.*;
import arc.scene.event.VisibilityListener;
import arc.struct.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.scene.*;
import arc.scene.ui.*;
import arc.scene.ui.Label.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import games.stendhal.client.gui.KTextEdit;
import games.stendhal.client.gui.NotificationChannel;
import games.stendhal.client.gui.NotificationChannelManager;
import games.stendhal.client.gui.j2DClient;
import games.stendhal.client.gui.wt.core.SettingChangeAdapter;
import games.stendhal.client.gui.wt.core.WtWindowManager;
import games.stendhal.common.NotificationType;
import mindustry.*;
import mindustry.game.EventType;
import mindustry.gen.*;
import mindustry.input.*;
import mindustry.ui.*;
import mindustry.ui.fragments.Fragment;
import temp.Debug;

import static arc.Core.*;
import static mindustry.Vars.net;
import static mindustry.Vars.*;

public class T_ChatFragment extends Fragment{
    private static final int PRIVATE_TAB_COLOR = 0xdcdcffff;

    private NotificationChannelManager channelManager;

    private Table root;
    private Table group;

    @Override
    public void build(Group parent) {
        channelManager = j2DClient.get().channelManager;

        root = new Table();
        root.setFillParent(true);
//        langs.marginRight(24f).marginLeft(24f);
        parent.addChild(root);

        group = root.table(Tex.white9s1).get();

        setSelectChannel(initChannel());

//        onResize(root, this::rebuild);
//        shown(root, this::rebuild);

//        ButtonGroup<TextButton> group = new ButtonGroup<>();
//
//        for(Locale loc : locales){
//            TextButton button = new TextButton(Strings.capitalize(displayNames.get(loc, loc.getDisplayName(loc))), Styles.clearTogglet);
//            button.clicked(() -> {
//                if(getLocale().equals(loc)) return;
//                Core.settings.put("locale", loc.toString());
//                Core.settings.save();
//                Log.info("Setting locale: {0}", loc.toString());
//                ui.showInfo("$language.restart");
//            });
//            langs.add(button).group(group).update(t -> t.setChecked(loc.equals(getLocale()))).size(400f, 50f).row();
//        }
//
//        parent.addChild(pane);
//        cont.add(pane);

//        root.visible(false);
    }


    private void rebuild() {
//        System.out.println("resize.................................");
    }


    /**
     *  返回显示通道
     * @return default "Main"通道
     * */
    private NotificationChannel initChannel() {
        NotificationChannel mainChannel = setupMainChannel();           // "Main"
        channelManager.addChannel(mainChannel);

        // ** Private channel **
        NotificationChannel personal = setupPersonalChannel();          // "Personal"
        channelManager.addChannel(personal);

        return mainChannel;
    }

    private void setSelectChannel(NotificationChannel channel) {
        if (channel == null )
            return;

        if (channelManager.getVisibleChannel() != null) {
            channelManager.getVisibleChannel().getChannel().setVisible(()->false);
//            TextButton channelButton = group.findActor(channelManager.getVisibleChannel().getName());
//            if (channelButton != null) {
//                channelButton.setProgrammaticChangeEvents(false);
//                channelButton.setChecked(false);
//                channelButton.setProgrammaticChangeEvents(true);
//            }
        }
        this.channelManager.setVisibleChannel(channel);         // not must
        channel.getChannel().setVisible(()->true);

//        TextField textField = group.findActor(script.FIELD);
//        if (textField != null)
//            textField.setMessageText(Core.getLocal(TAG, channel.getName()) + " " + Core.getLocal(TAG, "chat") + ":");
//
//        TextButton channelButton = group.findActor(channel.getName());
//        if (channelButton != null) {
//            channelButton.setProgrammaticChangeEvents(false);
//            channelButton.setChecked(true);
//            channelButton.setProgrammaticChangeEvents(true);
//        }
    }

    private NotificationChannel setupPersonalChannel() {
        KTextEdit edit = new KTextEdit("Personal", group);
        /*
         * Give it a different background color to make it different from the
         * main chat log.
         */
        edit.setDefaultBackground(new Color(PRIVATE_TAB_COLOR));
        /*
         * Types shown by default in the private/group tab. Admin messages
         * should occur everywhere, of course, and not be possible to be
         * disabled in preferences.
         */
        String personalDefault = NotificationType.PRIVMSG.toString() + ","
                + NotificationType.CLIENT + "," + NotificationType.GROUP + ","
                + NotificationType.TUTORIAL + "," + NotificationType.SUPPORT;

        return new NotificationChannel("Personal", edit, false, personalDefault);
    }

    private NotificationChannel setupMainChannel() {
        KTextEdit edit = new KTextEdit("Main", group);
        NotificationChannel channel = new NotificationChannel("Main", edit, true, "");

        // Follow settings changes for the main channel
        // 事件过滤消息
        channel.setTypeFiltering(NotificationType.HEAL, false);
        channel.setTypeFiltering(NotificationType.POISON, false);
        // ui.healingmessage
        // ui.poisonmessage
        return channel;
    }

}
