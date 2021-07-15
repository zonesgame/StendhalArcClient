package stendhal.test;

import arc.*;
import arc.input.*;
import arc.math.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import arc.util.serialization.*;
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

public class T_CreateAccountDialog extends FloatingDialog {
    Dialog add;
    Table local = new Table();
    Table remote = new Table();
    Table global = new Table();
    Table hosts = new Table();
    int totalHosts;

    public T_CreateAccountDialog(){
        super("$joingame");


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

    private String name1 = "NAME1";
    private String name2 = "NAME2";
    private String name3 = "NAME3";
    private String name4;

    void setup(){
        local.clear();
        remote.clear();
        global.clear();
        float w = targetWidth();

        hosts.clear();

        if (true) {
            cont.clear();

            Table groupParent = new Table();
            groupParent.setFillParent(true);

            // 拖动框添加
            ScrollPane pane = new ScrollPane(groupParent);
            pane.setFadeScrollBars(true);
            pane.setScrollingDisabled(false, false);

            // ui组件添加
//            groupParent.table(t -> {
//                t.add("NAME1").padRight(10);        // 添加table
//                t.addField(Core.settings.getString("NAME1"), text -> {      // 添加输入框
//                    para1 = text;
//                    Core.settings.put("NAME1", text);
//                    Core.settings.save();
//                }).grow().pad(8).get().setMaxLength(maxNameLength);
//            }).width(w).height(70f).pad(4);

            for (int i = 6; --i >= 0; ) {
                groupParent.table(t -> {
                    t.add(name1).padRight(10);        // 添加table
                    t.addField(Core.settings.getString(name1), text -> {      // 添加输入框
                        Core.settings.put(name1, text);
                        Core.settings.save();
                    }).grow().pad(8).get().setMaxLength(maxNameLength);
                }).width(w).height(60f).pad(4);

                groupParent.row();
            }

            // 选择框添加
            CheckBox box = new CheckBox(name2);
            box.update(() -> box.setChecked(settings.getBool(name2)));
            box.changed(() -> {
                settings.put(name2, box.isChecked());
                settings.save();
            });
            box.left();
            groupParent.add(box).right().padRight(40).padTop(10);
            groupParent.row();

            // 添加描述文本
            groupParent.table( t -> {
                for (int i = 300; --i > 0; ) {
                    name4 += "A";
                }
                t.add(name4).width(w).growX().padTop(10).padBottom(100).disabled(true).get().setWrap(true);
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
        buttons.addImageTextButton(name3, Icon.left, this::hide).size(210f, 64f);
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


    private void saveServers(){
//        Core.settings.putObject("server-list", servers);
//        Core.settings.save();
    }

}