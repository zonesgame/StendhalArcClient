package stendhal.test;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import arc.*;
import arc.input.*;
import arc.math.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import arc.util.async.Threads;
import arc.util.serialization.*;
import games.stendhal.client.StendhalClient;
import games.stendhal.client.gui.ProgressBar;
import games.stendhal.client.gui.login.LoginDialog;
import games.stendhal.client.gui.login.Profile;
import marauroa.client.BannedAddressException;
import marauroa.client.LoginFailedException;
import marauroa.client.TimeoutException;
import marauroa.common.Logger;
import marauroa.common.net.InvalidVersionException;
import marauroa.common.net.message.MessageS2CLoginNACK;
import mindustry.*;
import mindustry.annotations.Annotations.*;
import mindustry.core.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.net.*;
import mindustry.net.Packets.*;
import mindustry.ui.*;
import mindustry.ui.dialogs.FloatingDialog;
import mindustry.ui.dialogs.PaletteDialog;
import temp.Debug;

import static arc.Core.settings;
import static mindustry.Vars.*;
import static z.debug.Strs.str29;
import static z.debug.Strs.str30;
import static z.debug.Strs.str31;
import static z.debug.Strs.str32;
import static z.debug.Strs.str33;
import static z.debug.Strs.str34;
import static z.utils.FinalCons.SETTING_KEYS.lastLogin;
import static z.utils.FinalCons.SETTING_KEYS.savePassword;

public class T_JoinDialog extends FloatingDialog {

    private Array<Profile> profiles = new Array<>();
    private Profile profile;

    private Table remote = new Table();
    private Table groupParent = new Table();

    @SuppressWarnings("unchecked")
    private void loadServers(){
        Array<String> serializeServers = Core.settings.getObject("server-list", Array.class, Array::new);
        if (serializeServers.size > 0) {
            for (String s : serializeServers) {
                profiles.add(Profile.decode(s));
            }
            profile = profiles.first();
            System.out.println("1111111111   " + profiles.size);
        }
        else if (Debug.NOTE2) {
            System.out.println("222222222222222222");
            profile = Profile.decode(settings.getString(lastLogin, ""));
            profiles.add(profile);
            for (int i = 10; --i > 0; ) {
                Profile p = Profile.decode(profile.encode());
                p.setHost(p.getHost() + i);
                profiles.add(p);
            }
        }


//
//        profile.setHost((serverField.getText()).trim());
//
//        try {
//            profile.setPort(Integer.parseInt(serverPortField.getText().trim()));
//
//            // Support for saving port number. Only save when input is a number
//            // intensifly@gmx.com
//
//        } catch (final NumberFormatException ex) {
//            JOptionPane.showMessageDialog(this,
//                    "That is not a valid port number. Please try again.",
//                    "Invalid port", JOptionPane.WARNING_MESSAGE);
//            return;
//        }
//
//        profile.setUser(usernameField.getText().trim());
//        profile.setPassword(new String(passwordField.getPassword()));
    }

    // end-------------------------------

    public T_JoinDialog(){
        super("$joingame");
        loadServers();

        if(!steam) buttons.add().width(60f);
        buttons.add().growX().width(-1);

        addCloseButton();

        buttons.add().growX().width(-1);
        if(!steam){
            buttons.addButton("?", () -> ui.showInfo("$join.info")).size(60f, 64f).width(-1);
        }

        shown(() -> {
            setup();
//            refreshAll();

            if(!steam){
//                Core.app.post(() -> Core.settings.getBoolOnce("joininfo", () -> ui.showInfo("$join.info")));
            }
        });

        onResize(() -> {
            setup();
//            refreshAll();
        });
    }

    private String name1 = "NAME1";
    private String name2 = "NAME2";
    private String name3 = "NAME3";

    void setup(){
        float w = targetWidth();

        if (true) {
            cont.clear();
            groupParent.clear();

            if (true) {
                remote.clear();
                section("test1", remote);
            }

            // 拖动框添加
            ScrollPane pane = new ScrollPane(groupParent);
            pane.setFadeScrollBars(true);
            pane.setScrollingDisabled(true, false);

            {
                setupRemote();
            }

            // 服务器地址
            groupParent.table(t -> {
                t.add(str29).padRight(10);        // 添加table
                t.addField(profile.getHost(), text -> {      // 添加输入框
                    if(Debug.NOTE2)
                        ;
                    profile.setHost(text);
                }).grow().pad(8).get().setMaxLength(maxNameLength);
            }).width(w).height(70f).pad(4);
            groupParent.row();

            // 服务器端口
            groupParent.table(t -> {
                t.add(str30).padRight(10);
                t.addField(String.valueOf(profile.getPort()), text -> {
                    if (Debug.NOTE2)
                        ;
                    profile.setPort(Integer.parseInt(text));
                }).grow().pad(8).get().setMaxLength(maxNameLength);
            }).width(w).height(70f).pad(4);
            groupParent.row();

            // 账户名
            groupParent.table(t -> {
                t.add(str31).padRight(10);
                t.addField(profile.getUser(), text -> {
                    if (Debug.NOTE2)
                        ;
                    profile.setUser(text);
                }).grow().pad(8).get().setMaxLength(maxNameLength);
            }).width(w).height(70f).pad(4);
            groupParent.row();

            // 密码
            groupParent.table(t -> {
                t.add(str32).padRight(10);
                t.addField(profile.getPassword(), text -> {
                    if (Debug.NOTE2)
                        ;
                    profile.setPassword(text);
                }).grow().pad(8).get().setMaxLength(maxNameLength);
            }).width(w).height(70f).pad(4);
            groupParent.row();

            // 选择框添加
            CheckBox box = new CheckBox(str34);
            box.update(() -> box.setChecked(settings.getBool(savePassword)));
            box.changed(() -> {
                settings.put(savePassword, box.isChecked());
                settings.save();
            });
            box.left();
            groupParent.add(box).left().padTop(12f);
            groupParent.row();

            // 添加事件按钮
//            buttons.addImageTextButton(name3, Icon.left, ()->{}).size(210f, 64f);       // 连接服务器
//            buttons.defaults().size(210f, 64f);
//            buttons.addImageTextButton("$back", Icon.left, this::hide).size(210f, 64f);

//            cont.add(pane);
            cont.add(pane).width(w + 38).pad(0);
            cont.row();
            return;
        }
    }


    @Override
    public void addCloseButton() {
        super.addCloseButton();
//        buttons.defaults().size(210f, 64f);
//        buttons.addImageTextButton("$back", Icon.left, this::hide).size(210f, 64f);
        buttons.addImageTextButton(str33, Icon.left, () -> {
            Profile savefile = Profile.decode(profile.encode());
            if ( !settings.getBool(savePassword)) {
                savefile.setPassword("");
            }
            settings.put(lastLogin, savefile.encode());
            settings.save();

            ui.loadfrag.show();      // Core.app.post(ui.loadfrag::show);       // ui.loadfrag.show();
//            Core.app.post(() -> connect(profile));
            Time.run(1f, () -> {
//                connect(profile);
                Threads.thread(() -> connect(profile));
//                connectThread(profile);
//                ui.loadfrag.hide();
            });
//            Core.app.post(() ->connect(profile));
        }).size(210f, 64f);

//        addCloseListener();
    }

    float targetWidth(){
        return Math.min(Core.graphics.getWidth() / Scl.scl() * 0.9f, 500f);//Core.graphics.isPortrait() ? 350f : 500f;
    }



    /*
     * Run the connection procces in separate thread. added by TheGeneral
     */
    public void connectThread(final Profile profile) {
        final Thread t = new Thread(new ConnectRunnable(profile), "Login");
        t.start();
    }

    /**
     * Connect to a server using a given profile.
     *
     * @param profile profile used for login
     */
    public void connect(final Profile profile) {
        // We are not in EDT
//        SwingUtilities.invokeLater(new Runnable() {
//            @Override
//            public void run() {
//                progressBar = new ProgressBar(LoginDialog.this);
//                progressBar.start();
//            }
//        });
//        Core.app.post(ui.load::show);

        StendhalClient client = StendhalClient.get();
        try {
            client.connect(profile.getHost(), profile.getPort());

            // for each major connection milestone call step(). progressBar is
            // created in EDT, so it is not guaranteed non null in the main
            // thread.
//            SwingUtilities.invokeLater(new Runnable() {
//                @Override
//                public void run() {
//                    progressBar.step();
//                }
//            });
        } catch (final Exception ex) {
            // if something goes horribly just cancel the progressbar
//            SwingUtilities.invokeLater(new Runnable() {
//                @Override
//                public void run() {
//                    progressBar.cancel();
//                    setEnabled(true);
//                }
//            });
            ui.loadfrag.hide();

            String message = "unable to connect to server";

            if (profile != null) {
                message = message + " " + profile.getHost() + ":" + profile.getPort();
            } else {
                message = message + ", because profile was null";
            }
            Logger.getLogger(LoginDialog.class).error(message, ex);
            ui.showInfoText("Connection failed", "Unable to connect to server. Did you misspell the server name?");
//            handleError("Unable to connect to server. Did you misspell the server name?", "Connection failed");
            return;
        }

        try {
            client.setAccountUsername(profile.getUser());
            client.setCharacter(profile.getCharacter());
            client.login(profile.getUser(), profile.getPassword(), profile.getSeed());
//            SwingUtilities.invokeLater(new Runnable() {
//                @Override
//                public void run() {
//                    progressBar.finish();
//                    // workaround near failures in AWT at openjdk (tested on openjdk-1.6.0.0)
//                    try {
//                        setVisible(false);
//                    } catch (NullPointerException npe) {
//                        Logger.getLogger(LoginDialog.class).error("Error probably related to bug in JRE occured", npe);
//                        LoginDialog.this.dispose();
//                    }
//                }
//            });
//            Time.runTask(0, () -> ui.load.hide());
            ui.loadfrag.hide();

        } catch (final InvalidVersionException e) {
            ui.showConfirm("Invalid version", "You are running an incompatible version of Stendhal. Please update", ()->{});
//            handleError("You are running an incompatible version of Stendhal. Please update",
//                    "Invalid version");
        } catch (final TimeoutException e) {
            ui.showInfoText("Error Logging In", "Server is not available right now.\nThe server may be down or, if you are using a custom server,\nyou may have entered its name and port number incorrectly.");
//            handleError("Server is not available right now.\nThe server may be down or, if you are using a custom server,\nyou may have entered its name and port number incorrectly.",
//                    "Error Logging In");
        } catch (final LoginFailedException e) {
            ui.showConfirm("Login failed", e.getMessage(), () -> {
                if (e.getReason() == MessageS2CLoginNACK.Reasons.SEED_WRONG) {
                    Core.app.exit();
                }
            });
//            handleError(e.getMessage(), "Login failed");
//            if (e.getReason() == MessageS2CLoginNACK.Reasons.SEED_WRONG) {
//                System.exit(1);
//            }
        } catch (final BannedAddressException e) {
            ui.showInfoText( "IP Banned", "Your IP is banned.");
//            handleError("Your IP is banned.",
//                    "IP Banned");
        }
    }

    void section(String label, Table servers){
        Table hosts = groupParent;

        Collapser coll = new Collapser(servers, Core.settings.getBool("collapsed-" + label, false));
        coll.setDuration(0.1f);

        hosts.table(name -> {
            name.add(label).pad(10).growX().left().color(Pal.accent);
            name.addImageButton(Icon.downOpen, Styles.emptyi, () -> {
                coll.toggle(false);
                Core.settings.putSave("collapsed-" + label, coll.isCollapsed());
            }).update(i -> i.getStyle().imageUp = (!coll.isCollapsed() ? Icon.upOpen : Icon.downOpen)).size(40f).right().padRight(10f);
        }).growX();
        hosts.row();
        hosts.addImage().growX().pad(5).padLeft(10).padRight(10).height(3).color(Pal.accent);
        hosts.row();
        hosts.add(coll).width(targetWidth());
        hosts.row();
    }

    void setupRemote(){
        Array<Profile> servers = profiles;
        remote.clear();

        for(Profile server : servers){
            //why are java lambdas this bad
            TextButton[] buttons = {null};

            TextButton button = buttons[0] = remote.addButton("[accent]" + server.getHost(), Styles.cleart, () -> {
                if(!buttons[0].childrenPressed()){
//                    if(server.lastHost != null){
//                        safeConnect(server.ip, server.port, server.lastHost.version);
//                    }else{
//                        connect(server.ip, server.port);
//                    }
                }
            }).width(targetWidth()).pad(4f).get();

            button.getLabel().setWrap(true);

            Table inner = new Table();
            button.clearChildren();
            button.add(inner).growX();

            inner.add(button.getLabel()).growX();

            inner.addImageButton(Icon.upOpen, Styles.emptyi, () -> {
                moveRemote(server, -1);

            }).margin(3f).padTop(6f).top().right();

            inner.addImageButton(Icon.downOpen, Styles.emptyi, () -> {
                moveRemote(server, +1);

            }).margin(3f).padTop(6f).top().right();

//            inner.addImageButton(Icon.refresh, Styles.emptyi, () -> {
////                refreshServer(server);
//            }).margin(3f).padTop(6f).top().right();
//
//            inner.addImageButton(Icon.pencil, Styles.emptyi, () -> {
////                renaming = server;
////                add.show();
//            }).margin(3f).padTop(6f).top().right();

            inner.addImageButton(Icon.trash, Styles.emptyi, () -> {
                ui.showConfirm("$confirm", "$server.delete", () -> {
                    servers.remove(server, true);
                    saveServers();
                    setupRemote();
//                    refreshRemote();
                });
            }).margin(3f).pad(6).top().right();

            button.row();

//            server.content = button.table(t -> {}).grow().get();

            remote.row();
        }
    }

    void moveRemote(Profile server, int sign){
        Array<Profile> servers = profiles;
        int index = servers.indexOf(server);

        if(index + sign < 0) return;
        if(index + sign > servers.size - 1) return;

        servers.remove(index);
        servers.insert(index + sign, server);

        saveServers();
        setupRemote();
        for(Profile other : servers){
//            if(other.lastHost != null){
//                setupServer(other, other.lastHost);
//            }else{
//                refreshServer(other);
//            }
        }
    }

    private void saveServers(){
        Array<String> serializeServers = new Array<>();
        for (Profile p : profiles) {
            serializeServers.add(p.encode());
        }
        Core.settings.putObject("server-list", serializeServers);
        Core.settings.save();
        Array<String> aa = Core.settings.getObject("server-list", Array.class, Array::new);
        System.out.println(aa.size);
    }


    /**
     * Server connect thread runnable.
     */
    private final class ConnectRunnable implements Runnable {
        private final Profile profile;

        /**
         * Create a new ConnectRunnable.
         *
         * @param profile profile used for connection
         */
        private ConnectRunnable(final Profile profile) {
            this.profile = profile;
        }

        @Override
        public void run() {
            connect(profile);
        }
    }

}