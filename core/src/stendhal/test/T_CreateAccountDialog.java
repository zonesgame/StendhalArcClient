package stendhal.test;

import java.awt.Window;

import javax.swing.JOptionPane;

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
import games.stendhal.client.gui.login.CreateAccountDialog;
import games.stendhal.client.stendhal;
import games.stendhal.client.update.ClientGameConfiguration;
import marauroa.client.BannedAddressException;
import marauroa.client.LoginFailedException;
import marauroa.client.TimeoutException;
import marauroa.common.game.AccountResult;
import marauroa.common.net.InvalidVersionException;
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
import z.debug.Strs;
import z.utils.Tools;

import static arc.Core.settings;
import static mindustry.Vars.*;
import static z.debug.Strs.str34;
import static z.debug.Strs.str44;
import static z.debug.Strs.str45;
import static z.debug.Strs.str46;
import static z.debug.Strs.str47;
import static z.debug.Strs.str50;
import static z.debug.Strs.str51;
import static z.debug.Strs.str52;
import static z.debug.Strs.str53;
import static z.debug.Strs.str54;
import static z.debug.Strs.str55;
import static z.utils.FinalCons.SETTING_KEYS.savePassword;
import static z.utils.FinalCons.SETTING_KEYS.showPassword;

public class T_CreateAccountDialog extends FloatingDialog {
    Dialog add;
    Table local = new Table();
    Table remote = new Table();
    Table global = new Table();
    Table hosts = new Table();
    int totalHosts;

    private TextField passwordField;
    private TextField retypePasswordField;
    private TextField portField;

    private String servername = "stendhalgame.org";
    private int serverport = 32160;
    private String username = "zonesa";
    private String password = "a123456";
    private String retypePassword = "a123456";
    private String email = "vxcvfdssd@outloo.com";

    transient boolean continueemail = false;

    /** Descriptions of error conditions. */
    private String badEmailTitle, badEmailReason, badPasswordReason;

    public T_CreateAccountDialog(){
        super(Strs.get(str44));


        if(!steam) buttons.add().width(60f);
        buttons.add().growX().width(-1);

        addCloseButton();

//        buttons.add().growX().width(-1);
//        if(!steam){
//            buttons.addButton("?", () -> ui.showInfo("$join.info")).size(60f, 64f).width(-1);
//        }

//        add = new FloatingDialog("$joingame.title");
//        add.cont.add("$joingame.ip").padRight(5f).left();
//
//        TextField field = add.cont.addField(Core.settings.getString("ip"), text -> {
//            Core.settings.put("ip", text);
//            Core.settings.save();
//        }).size(320f, 54f).get();
//
//        platform.addDialog(field, 100);
//
//        add.cont.row();
//        add.buttons.defaults().size(140f, 60f).pad(4f);
//        add.buttons.addButton("$cancel", add::hide);
//        add.buttons.addButton("$ok", () -> {
//            if(renaming == null){
//                Server server = new Server();
//                server.setIP(Core.settings.getString("ip"));
//                servers.add(server);
//                saveServers();
//                setupRemote();
//                refreshRemote();
//            }else{
//                renaming.setIP(Core.settings.getString("ip"));
//                saveServers();
//                setupRemote();
//                refreshRemote();
//            }
//            add.hide();
//        }).disabled(b -> Core.settings.getString("ip").isEmpty() || net.active());
//
//        add.shown(() -> {
//            add.title.setText(renaming != null ? "$server.edit" : "$server.add");
//            if(renaming != null){
//                field.setText(renaming.displayIP());
//            }
//        });
//
//        keyDown(KeyCode.F5, this::refreshAll);

        shown(() -> {
            setup();
//            refreshAll();

            if(!steam){
                Core.app.post(() -> Core.settings.getBoolOnce("joininfo", () -> ui.showInfo("$join.info")));
            }
        });

        onResize(() -> {
            setup();
//            refreshAll();
        });
    }

    void setup(){
        local.clear();
        remote.clear();
        global.clear();
        float w = targetWidth();

        hosts.clear();

        if (true) {
            cont.clear();
            {
                servername = ClientGameConfiguration.get("DEFAULT_SERVER");
                serverport = Tools.getNumber(ClientGameConfiguration.get("DEFAULT_PORT"), serverport);
            }

            Table groupParent = new Table();
            groupParent.setFillParent(true);

            // 拖动框添加
            ScrollPane pane = new ScrollPane(groupParent);
            pane.setFadeScrollBars(true);
            pane.setScrollingDisabled(false, false);

            // server name
            groupParent.table(t -> {
                t.add(Strs.get(str46)).padRight(10);        // 添加table
                t.addField(servername, text -> {      // 添加输入框
                    this.servername = text;
                }).grow().pad(8).get().setMaxLength(maxNameLength);
            }).width(w).height(60f).pad(4);
            groupParent.row();

            // server port
            groupParent.table(t -> {
                t.add(Strs.get(str47)).padRight(10);        // 添加table
                portField = t.addField(String.valueOf(serverport), text -> {      // 添加输入框
                    if ( Tools.portCheck(text)) {
                        this.serverport = Tools.getNumber(text, this.serverport);
                    } else {
                        Core.app.post(()->{
                            portField.setText(String.valueOf(serverport));
                        });
                        ui.showInfoText("Invalid port", "That is not a valid port number. Please try again(1024~49151).");
                    }
                }).grow().pad(8).get();
                portField.setMaxLength(maxNameLength);
            }).width(w).height(60f).pad(4);
            groupParent.row();

            // username
            groupParent.table(t -> {
                t.add(Strs.get(str50)).padRight(10);        // 添加table
                t.addField(username, text -> {      // 添加输入框
                    this.username = text;
                }).grow().pad(8).get().setMaxLength(maxNameLength);
            }).width(w).height(60f).pad(4);
            groupParent.row();

            // password
            groupParent.table(t -> {
                t.add(Strs.get(str51)).padRight(10);        // 添加table
                passwordField = t.addField(password, text -> {      // 添加输入框
                    this.password = text;
                }).grow().pad(8).get();
                passwordField.setMaxLength(maxNameLength);
                passwordField.setPasswordMode( !settings.getBool(showPassword, true));
                passwordField.setPasswordCharacter('*');
            }).width(w).height(60f).pad(4);
            groupParent.row();

            // retype password
            groupParent.table(t -> {
                t.add(Strs.get(str52)).padRight(10);        // 添加table
                retypePasswordField = t.addField(retypePassword, text -> {      // 添加输入框
                    this.retypePassword = text;
                }).grow().pad(8).get();
                retypePasswordField.setMaxLength(maxNameLength);
                retypePasswordField.setPasswordMode( !settings.getBool(showPassword, true));
                retypePasswordField.setPasswordCharacter('*');
            }).width(w).height(60f).pad(4);
            groupParent.row();

            // email
            groupParent.table(t -> {
                t.add(Strs.get(str53)).padRight(10);        // 添加table
                t.addField(email, text -> {      // 添加输入框
                    this.email = text;
                }).grow().pad(8).get().setMaxLength(maxNameLength);
            }).width(w).height(60f).pad(4);
            groupParent.row();

            // show password
            CheckBox box = new CheckBox(Strs.get(str54));         // save password
            box.update(() -> box.setChecked(settings.getBool(showPassword, true)));
            box.changed(() -> {
                passwordField.setPasswordMode( !box.isChecked());
                retypePasswordField.setPasswordMode( !box.isChecked());
                settings.put(showPassword, box.isChecked());
                settings.save();
            });
            box.left();
            groupParent.add(box).right().padRight(40).padTop(10);
            groupParent.row();

            // 添加描述文本
            groupParent.table( t -> {
                t.add(Strs.get(str55)).width(w).growX().padTop(10).padBottom(100).disabled(true).get().setWrap(true);
            }).width(w);

            groupParent.row();

            // 添加事件按钮
//            buttons.addImageTextButton(name3, Icon.left, ()->{}).size(210f, 64f);       // 连接服务器
//            buttons.defaults().size(210f, 64f);
//            buttons.addImageTextButton("$back", Icon.left, this::hide).size(210f, 64f);

            cont.add(pane);
            return;
        }

        ScrollPane pane = new ScrollPane(hosts);
        pane.setFadeScrollBars(false);
        pane.setScrollingDisabled(true, false);

//        setupRemote();

        cont.clear();
        cont.table(t -> {
            t.add("$name").padRight(10);
            if(!steam){
                t.addField(Core.settings.getString("name"), text -> {
                    player.name = text;
                    Core.settings.put("name", text);
                    Core.settings.save();
                }).grow().pad(8).get().setMaxLength(maxNameLength);
            }else{
                t.add(player.name).update(l -> l.setColor(player.color)).grow().pad(8);
            }

//            ImageButton button = t.addImageButton(Tex.whiteui, Styles.clearFulli, 40, () -> {
//                new PaletteDialog().show(color -> {
//                    player.color.set(color);
//                    Core.settings.put("color-0", color.rgba());
//                    Core.settings.save();
//                });
//            }).size(54f).get();
//            button.update(() -> button.getStyle().imageUpColor = player.color);
        }).width(w).height(70f).pad(4);
        cont.row();
        cont.add(pane).width(w + 38).pad(0);
        cont.row();
        cont.addCenteredImageTextButton("$server.add", Icon.add, () -> {
//            renaming = null;
            add.show();
        }).marginLeft(10).width(w).height(80f).update(button -> {
            float pw = w;
            float pad = 0f;
            if(pane.getChildren().first().getPrefHeight() > pane.getHeight()){
                pw = w + 30;
                pad = 6;
            }

            Cell cell = ((Table)pane.getParent()).getCell(button);

            if(!Mathf.equal(cell.minWidth(), pw)){
                cell.width(pw);
                cell.padLeft(pad);
                pane.getParent().invalidateHierarchy();
            }
        });
    }


    private void addEventButton() {

    }

    @Override
    public void addCloseButton() {
        super.addCloseButton();
//        buttons.defaults().size(210f, 64f);
//        buttons.addImageTextButton("$back", Icon.left, this::hide).size(210f, 64f);
        buttons.addImageTextButton(Strs.get(str45), Icon.left, ()->{
//            this.hide();
            onCreateAccount();
        }).size(210f, 64f);
        buttons.add().growX().width(-1);

//        buttons.add().growX().width(-1);
//        addCloseListener();
    }
//    void addGlobalHost(Host host){
//        global.background(null);
//        float w = targetWidth();
//
//        global.row();
//
//        TextButton button = global.addButton("", Styles.cleart, () -> safeConnect(host.address, host.port, host.version))
//                .width(w).pad(5f).get();
//        button.clearChildren();
//        buildServer(host, button);
//    }
//
//
//
//    void safeConnect(String ip, int port, int version){
//        if(version != Version.build && Version.build != -1 && version != -1){
//            ui.showInfo("[scarlet]" + (version > Version.build ? KickReason.clientOutdated : KickReason.serverOutdated).toString() + "\n[]" +
//                    Core.bundle.format("server.versions", Version.build, version));
//        }else{
//            connect(ip, port);
//        }
//    }

    float targetWidth(){
        return Math.min(Core.graphics.getWidth() / Scl.scl() * 0.9f, 500f);//Core.graphics.isPortrait() ? 350f : 500f;
    }

    int i = 0;
    private void connectServer(){
        // initialize progress bar
//        progressBar.start();
//        // disable this screen when attempting to connect
//        setEnabled(false);
        this.hide();
        ui.loadfrag.show();

        StendhalClient client = StendhalClient.get();
        try {
            client.connect(servername, serverport);
        } catch (final Exception ex) {
            // if something goes horribly just cancel the progress bar
            ui.loadfrag.hide();
            ui.showInfoText( "", "Unable to connect to server to create your account. The server may be down or, if you are using a custom server, " +
                    "you may have entered its name and port number incorrectly.");
//            progressBar.cancel();
//            setEnabled(true);
//            JOptionPane.showMessageDialog(
//                    getOwner(),
//                    "Unable to connect to server to create your account. The server may be down or, if you are using a custom server, " +
//                            "you may have entered its name and port number incorrectly.");

            Log.err( ex);
//            this.show();

            return;
        }
//        final Window owner = getOwner();
        try {
            final AccountResult result = client.createAccount(username, password, email);
            if (result.failed()) {
                /*
                 * If the account can't be created, show an error
                 * message and don't continue.
                 */
//                progressBar.cancel();
//                setEnabled(true);
//                JOptionPane.showMessageDialog(owner,
//                        result.getResult().getText(),
//                        "Create account failed",
//                        JOptionPane.ERROR_MESSAGE);
                ui.loadfrag.hide();
                ui.showInfoText("Create account failed", result.getResult().getText());
//                this.show();
            } else {

                /*
                 * Print username returned by server, as server can
                 * modify it at will to match account names rules.
                 */

//                progressBar.step();
//                progressBar.finish();

                client.setAccountUsername(this.username);
                client.setCharacter(this.username);

                /*
                 * Once the account is created, login into server.
                 */
                client.login(this.username, password);
                ui.loadfrag.hide();
//                progressBar.step();
//                progressBar.finish();
//
//                setEnabled(false);
//                if (owner != null) {
//                    owner.setVisible(false);
//                    owner.dispose();
//                }
//
//                stendhal.setDoLogin();
            }
        } catch (final TimeoutException e) {
            ui.loadfrag.hide();
            ui.showInfoText("Error Creating Account",
                    "Unable to connect to server to create your account. The server may be down or, if you are using a custom server, you may have entered its name and port number incorrectly.");
//            this.show();
//            progressBar.cancel();
//            setEnabled(true);
//            JOptionPane.showMessageDialog(
//                    owner,
//                    "Unable to connect to server to create your account. The server may be down or, if you are using a custom server, you may have entered its name and port number incorrectly.",
//                    "Error Creating Account", JOptionPane.ERROR_MESSAGE);
        } catch (final InvalidVersionException e) {
            ui.loadfrag.hide();
            ui.showInfoText("Invalid version", "You are running an incompatible version of Stendhal. Please update");
//            this.show();
//            progressBar.cancel();
//            setEnabled(true);
//            JOptionPane.showMessageDialog(
//                    owner,
//                    "You are running an incompatible version of Stendhal. Please update",
//                    "Invalid version", JOptionPane.ERROR_MESSAGE);
        } catch (final BannedAddressException e) {
            ui.loadfrag.hide();
            ui.showConfirm("IP Banned", "Your IP is banned.", Core.app::exit);
//            this.show();
//            progressBar.cancel();
//            setEnabled(true);
//            JOptionPane.showMessageDialog(
//                    owner,
//                    "Your IP is banned.",
//                    "IP Banned", JOptionPane.ERROR_MESSAGE);
        } catch (final LoginFailedException e) {
            ui.loadfrag.hide();
            ui.showInfoText("Login failed", e.getMessage());
//            progressBar.cancel();
//            setEnabled(true);
//            JOptionPane.showMessageDialog(owner, e.getMessage(),
//                    "Login failed", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Run when the "Create account" button is activated.
     */
    private void onCreateAccount() {
        final String accountUsername = this.username;
        final String password = new String(this.password);

        final boolean ok = checkFields();

        if (!ok) {
            return;
        }

        final String email = this.email;
        final String server = this.servername;
        int port = 32160;

        // port couldn't be accessed from inner class
        final int finalPort = this.serverport;

        // standalone check
        if (StendhalClient.get() == null) {
            ui.showInfoText("Account not created (running standalone)!", "");
//            JOptionPane.showMessageDialog(this,
//                    "Account not created (running standalone)!");
            return;
        }

        /* separate thread for connection process added by TheGeneral */
        // run the connection process in separate thread
        Threads.thread(this::connectServer);
//        Time.run(0, Threads.thread(this::connectServer));
//        Time.runTask(0, Threads.thread(this::connectServer));
//        Time.runTask(0, this::connectServer);
//        Time.runTask(0, this::connectServer);
//        Core.app.post(Threads.thread(this::connectServer));
//        Core.app.post(this::connectServer);

//        final Thread connectionThread = new Thread() {
//
//            @Override
//            public void run() {
//                // initialize progress bar
//                progressBar.start();
//                // disable this screen when attempting to connect
//                setEnabled(false);
//
//
//                try {
//                    client.connect(server, finalPort);
//                    // for each major connection milestone call step()
//                    progressBar.step();
//                } catch (final Exception ex) {
//                    // if something goes horribly just cancel the progress bar
//                    progressBar.cancel();
//                    setEnabled(true);
//                    JOptionPane.showMessageDialog(
//                            getOwner(),
//                            "Unable to connect to server to create your account. The server may be down or, if you are using a custom server, " +
//                                    "you may have entered its name and port number incorrectly.");
//
//                    LOGGER.error(ex, ex);
//
//                    return;
//                }
//                final Window owner = getOwner();
//                try {
//                    final AccountResult result = client.createAccount(
//                            accountUsername, password, email);
//                    if (result.failed()) {
//                        /*
//                         * If the account can't be created, show an error
//                         * message and don't continue.
//                         */
//                        progressBar.cancel();
//                        setEnabled(true);
//                        JOptionPane.showMessageDialog(owner,
//                                result.getResult().getText(),
//                                "Create account failed",
//                                JOptionPane.ERROR_MESSAGE);
//                    } else {
//
//                        /*
//                         * Print username returned by server, as server can
//                         * modify it at will to match account names rules.
//                         */
//
//                        progressBar.step();
//                        progressBar.finish();
//
//                        client.setAccountUsername(accountUsername);
//                        client.setCharacter(accountUsername);
//
//                        /*
//                         * Once the account is created, login into server.
//                         */
//                        client.login(accountUsername, password);
//                        progressBar.step();
//                        progressBar.finish();
//
//                        setEnabled(false);
//                        if (owner != null) {
//                            owner.setVisible(false);
//                            owner.dispose();
//                        }
//
//                        stendhal.setDoLogin();
//                    }
//                } catch (final TimeoutException e) {
//                    progressBar.cancel();
//                    setEnabled(true);
//                    JOptionPane.showMessageDialog(
//                            owner,
//                            "Unable to connect to server to create your account. The server may be down or, if you are using a custom server, you may have entered its name and port number incorrectly.",
//                            "Error Creating Account", JOptionPane.ERROR_MESSAGE);
//                } catch (final InvalidVersionException e) {
//                    progressBar.cancel();
//                    setEnabled(true);
//                    JOptionPane.showMessageDialog(
//                            owner,
//                            "You are running an incompatible version of Stendhal. Please update",
//                            "Invalid version", JOptionPane.ERROR_MESSAGE);
//                } catch (final BannedAddressException e) {
//                    progressBar.cancel();
//                    setEnabled(true);
//                    JOptionPane.showMessageDialog(
//                            owner,
//                            "Your IP is banned.",
//                            "IP Banned", JOptionPane.ERROR_MESSAGE);
//                } catch (final LoginFailedException e) {
//                    progressBar.cancel();
//                    setEnabled(true);
//                    JOptionPane.showMessageDialog(owner, e.getMessage(),
//                            "Login failed", JOptionPane.INFORMATION_MESSAGE);
//                }
//            }
//        };
//        connectionThread.start();
    }

    /**
     * Runs field checks, to, ex. confirm the passwords correct, etc.
     * @return if no error found
     */
    private boolean checkFields() {
        //
        // Check the password
        //
        final String password = this.password;
        final String passwordretype = this.retypePassword;
        if (!password.equals(passwordretype)) {
            ui.showInfoText("Password Mismatch", "The passwords do not match. Please retype both.");
//            JOptionPane.showMessageDialog(owner,
//                    "The passwords do not match. Please retype both.",
//                    "Password Mismatch", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        //
        // Password strength
        //
        final boolean valPass = validatePassword(this.username, password);
        if (!valPass) {
            if (badPasswordReason != null) {
                // didn't like the password for some reason, show a dialog and
                // try again
//                final int i = JOptionPane.showOptionDialog(owner, badPasswordReason,
//                        "Bad Password", JOptionPane.YES_NO_OPTION,
//                        JOptionPane.WARNING_MESSAGE, null, null, 1);
//
//                if (i == JOptionPane.NO_OPTION) {
//                    return false;
//                }
                ui.showInfoText("Bad Password", badPasswordReason);
                return false;
            } else {
                return false;
            }
        }

        //
        // Check the email
        //
        final String email = (this.email).trim();
        if  (!validateEmail(email)) {
            final String warning = badEmailReason + "An email address is the only means for administrators to contact with the legitimate owner of an account.\nIf you don't provide one then you won't be able to get a new password for this account if, for example:\n- You forget your password.\n- Another player somehow gets your password and changes it.\nDo you want to continue anyway?";
//            final int i = JOptionPane.showOptionDialog(owner, warning, badEmailTitle,
//                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE,
//                    null, null, 1);
//            if (i != 0) {
//                // no, let me type a valid email
//                return false;
//            }
            if (continueemail) {
                return continueemail;
            }
            continueemail = false;
            ui.showConfirm(badEmailTitle, warning, ()->{
                continueemail = true;
                onCreateAccount();  //
            });
            return continueemail;
            // yes, continue anyway
        }
        return true;
    }

    /**
     * Do some sanity checks for the password.
     *
     * @param username user name
     * @param password checked password
     * @return <code>true</code> if the password seems reasonable,
     *	<code>false</code> if the password should be rejected
     */
    private boolean validatePassword(final String username, final String password) {
        if (password.length() > 5) {

            // check for all numbers
            boolean allNumbers = true;
            try {
                Integer.parseInt(password);
            } catch (final NumberFormatException e) {
                allNumbers = false;
            }
            if (allNumbers) {
                badPasswordReason = "You have used only numbers in your password. This is not a good security practice.\n"
                        + " Are you sure that you want to use this password?";
            }

            // check for username
            boolean hasUsername = false;
            if (password.contains(username)) {
                hasUsername = true;
            }

            if (!hasUsername) {
                // now we'll do some more checks to see if the password
                // contains more than three letters of the username
                debug("Checking if password contains a derivative of the username, trimming from the back...");
                final int minUserLength = 3;
                for (int i = 1; i < username.length(); i++) {
                    final String subuser = username.substring(0, username.length()
                            - i);
                    debug("\tchecking for \"" + subuser + "\"...");
                    if (subuser.length() <= minUserLength) {
                        break;
                    }

                    if (password.contains(subuser)) {
                        hasUsername = true;
                        debug("Password contains username!");
                        break;
                    }
                }

                if (!hasUsername) {
                    // now from the end of the password..
                    debug("Checking if password contains a derivative of the username, trimming from the front...");
                    for (int i = 0; i < username.length(); i++) {
                        final String subuser = username.substring(i);
                        debug("\tchecking for \"" + subuser + "\"...");
                        if (subuser.length() <= minUserLength) {
                            break;
                        }
                        if (password.contains(subuser)) {
                            hasUsername = true;
                            debug("Password contains username!");
                            break;
                        }
                    }
                }
            }

            if (hasUsername) {
                badPasswordReason = "You have used your username or a derivative of your username in your password. This is a bad security practice.\n"
                        + " Are you sure that you want to use this password?";
                return false;
            }

        } else {
            final String text = "The password you provided is too short. It must be at least 6 characters long.";
            if (isVisible()) {
                ui.showInfoText(text, "");
//                JOptionPane.showMessageDialog(getOwner(), text);
            } else {
//                LOGGER.warn(text);
                debug(text);
            }
            return false;
        }

        return true;
    }

    /**
     * Validate email field format.
     *
     * @param email address to be validate
     * @return <code>true</code> if the email looks good enough, otherwise
     *	<code>false</code>
     */
    private boolean validateEmail(final String email) {
        if  (email.isEmpty()) {
            badEmailTitle = "Email address is empty";
            badEmailReason = "You didn't enter an email address.\n";
            return false;
        } else {
            if (!email.contains("@") || !email.contains(".") || (email.length() <= 5)) {
                badEmailTitle =  "Misspelled email address?";
                badEmailReason = "The email address you entered is probably misspelled.\n";
                return false;
            }
        }
        return true;
    }

    /**
     * Prints text only when running stand-alone.
     * @param text text to be printed
     */
    private void debug(final String text) {
        if (Debug.NOTE2) {
            Log.warn(text);
        }
    }

}