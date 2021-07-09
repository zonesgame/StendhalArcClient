package z.ui;

import arc.Core;
import arc.func.Cons;
import arc.func.Prov;
import arc.graphics.Color;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.scene.Element;
import arc.scene.event.Touchable;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.ButtonGroup;
import arc.scene.ui.Image;
import arc.scene.ui.ImageButton;
import arc.scene.ui.Label;
import arc.scene.ui.ScrollPane;
import arc.scene.ui.layout.Scl;
import arc.scene.ui.layout.Stack;
import arc.scene.ui.layout.Table;
import arc.scene.ui.layout.WidgetGroup;
import arc.struct.Array;
import arc.struct.ObjectMap;
import arc.util.Align;
import arc.util.Scaling;
import mindustry.ctype.UnlockableContent;
import mindustry.entities.type.TileEntity;
import mindustry.gen.Tex;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.ui.Cicon;
import mindustry.ui.Styles;
import mindustry.world.Tile;
import z.debug.Strs;
import z.entities.type.UpgradeTileEntity;
import z.world.blocks.OperationAction;
import z.world.blocks.defense.UpgradeBlock;
import z.world.blocks.storage.GranaryIso;
import z.world.blocks.storage.WarehouseIso;

import static mindustry.Vars.content;
import static mindustry.Vars.control;
import static mindustry.Vars.data;
import static mindustry.Vars.player;
import static mindustry.Vars.systemItems;
import static mindustry.Vars.world;
import static z.utils.FinalCons.hour;
import static z.utils.FinalCons.minute;
import static z.utils.FinalCons.second;

/**
 *  物品选择器
 * */
public class BlockSelection{
    /** 卷轴位置*/
    private static float scrollPos = 0f;

    public static <T extends UnlockableContent> void buildTable(Table table, Array<T> items, Prov<T> holder, Cons<T> consumer){

        ButtonGroup<ImageButton> group = new ButtonGroup<>();
        group.setMinCheckCount(0);
        Table cont = new Table();
        cont.defaults().size(40);

        int i = 0;

        for(T item : items){
            if(!data.isUnlocked(item) && world.isZone()) continue;

            ImageButton button = cont.addImageButton(Tex.whiteui, Styles.clearToggleTransi, 24, () -> control.input.frag.config.hideConfig()).group(group).get();
            button.changed(() -> consumer.get(button.isChecked() ? item : null));
            button.getStyle().imageUp = new TextureRegionDrawable(item.icon(Cicon.small));
            button.update(() -> button.setChecked(holder.get() == item));

            if(i++ % 4 == 3){
                cont.row();
            }
        }

        //add extra blank spaces so it looks nice
        if(i % 4 != 0){
            int remaining = 4 - (i % 4);
            for(int j = 0; j < remaining; j++){
                cont.addImage(Styles.black6);
            }
        }

        ScrollPane pane = new ScrollPane(cont, Styles.smallPane);
        pane.setScrollingDisabled(true, false);
        pane.setScrollYForce(scrollPos);
        pane.update(() -> {
            scrollPos = pane.getScrollY();
        });

        pane.setOverscroll(false, false);
        table.add(pane).maxHeight(Scl.scl(40 * 5));
    }

    /**
     *  zones use
     * */
    public static <T extends OperationAction> void buildTable(Tile tile, Table table, ObjectMap<OperationAction, Cons<T>> actionsMap){
        table.clearChildren();

        WidgetGroup parentGroup = new WidgetGroup();
//        parentGroup.setFillParent(true);
        parentGroup.touchable(Touchable.childrenOnly);
        table.addChild(parentGroup);

        TileEntity entity = tile.ent();
        Array<OperationAction> actions = Array.with(actionsMap.keys().iterator()).sort();
        {   // 添加扩展信息描述
            if (actions.contains(OperationAction.EXTENDEDINFORMATION)) {
                actions.remove(OperationAction.EXTENDEDINFORMATION);

                if( true) {  // 测试显示
                    Table _t = rebuild(tile);
                    Table tableInfo = new Table();
                    tableInfo.touchable(Touchable.disabled);
                    tableInfo.background(Tex.label1);
                    if(_t != null) {
                        tableInfo.add(_t);
                    }
                    tableInfo.pack();
                    tableInfo.setPosition(0, -42, Align.center | Align.top);
                    if(_t != null) {
                        table.addChild(tableInfo);
                    }

//                    Table tableInfo = rebuild(tile);
//                    tableInfo.setPosition(0, 0, Align.center | Align.top);
//                    table.addChild(tableInfo);
                }
                else if (tile.block() instanceof WarehouseIso) {
                    WarehouseIso warehouseBlock = (WarehouseIso) tile.block();
                    WarehouseIso.WarehouseIsoEntity warehouseEntity = (WarehouseIso.WarehouseIsoEntity) entity;
                    int lineHeight = 0;
                    for (ItemStack itemStack : warehouseEntity.warehouseContent) {
                        if (itemStack.amount > 0) lineHeight++;
                    }
                    if (lineHeight > 0) {
                        WidgetGroup extendGroup = new WidgetGroup();
                        extendGroup.touchable(Touchable.disabled);
                        table.addChild(extendGroup);
                        Styles.label1.fontColor = Color.yellow;
                        Label label = new Label("", Styles.label1);
                        label.setSize(300, 50 + lineHeight * 30);
                        label.setPosition(-label.getWidth() / 2, -label.getHeight() - 20);
                        label.setText(Strs.str12);
                        label.setAlignment(Align.top | Align.center);
                        extendGroup.addChild(label);

                        Table ttt = new Table();
                        ttt.setBounds(label.getX(), label.getY() - 5, label.getWidth(), label.getHeight());
                        extendGroup.addChild(ttt);

                        Table nodeTable = new Table();
                        nodeTable.row();
//                    nodeTable.fill();
                        ttt.add(nodeTable);
//                    extendGroup.addChild(nodeTable);
                        for (ItemStack itemStack : warehouseEntity.warehouseContent) {
                            if (itemStack.amount > 0) {
                                nodeTable.table(inset -> {
                                    inset.left();
                                    Item itemNode = itemStack.item;
                                    inset.addImage(SettingDisplay.icon(itemNode)).size(4 * 8).left();
                                    Label label2 = new Label(itemNode.localizedName + "   " + itemStack.amount + "/" + warehouseEntity.getMaximumAcceptedWarehouseSolt());
                                    label2.setAlignment(Align.center);
                                    inset.add(label2).minWidth(120).center();
//                                Slider slider = new Slider(0, ((ItemTurretIso) block).maxAmmo, 1, false);
//                                slider.setValue(tileEntity.acceptAmmo.get(itemNode));
//                                slider.changed(() -> {
//                                    label.setText(String.valueOf((int)slider.getValue()));
//                                    tileEntity.acceptAmmo.put(itemNode, (int)slider.getValue());
//                                });
//                                inset.add(slider).fillX().left();

                                    //map.get(stat).display(inset);
                                }).fillX();
//
                                nodeTable.row();
                            }
                        }
                    }
                }
                else if (tile.block() instanceof GranaryIso) {
                    GranaryIso granaryBlock = (GranaryIso) tile.block();
                    GranaryIso.GranaryIsoEntity granaryEntity = (GranaryIso.GranaryIsoEntity) entity;
                    int lineHeight = 0;
                    for (Item item : systemItems.getGranaryAccept()) {
                        if (granaryEntity.items.get(item) > 0) lineHeight++;
                    }

                    if (lineHeight > 0) {
                        WidgetGroup extendGroup = new WidgetGroup();
                        extendGroup.touchable(Touchable.disabled);
                        table.addChild(extendGroup);
                        Styles.label1.fontColor = Color.yellow;
                        Label label = new Label("", Styles.label1);
                        label.setSize(300, 50 + lineHeight * 30);
                        label.setPosition(-label.getWidth() / 2, -label.getHeight() - 20);
                        label.setText(Strs.str12);
                        label.setAlignment(Align.top | Align.center);
                        extendGroup.addChild(label);

                        Table ttt = new Table();
                        ttt.setBounds(label.getX(), label.getY() - 5, label.getWidth(), label.getHeight());
                        extendGroup.addChild(ttt);

                        Table nodeTable = new Table();
                        nodeTable.row();
//                    nodeTable.fill();
                        ttt.add(nodeTable);
//                    extendGroup.addChild(nodeTable);
                        for (Item item : systemItems.getGranaryAccept()) {
                            if (granaryEntity.items.has(item)) {
                                nodeTable.table(inset -> {
                                    inset.left();
                                    Item itemNode = item;
                                    inset.addImage(SettingDisplay.icon(itemNode)).size(4 * 8).left();
                                    Label label2 = new Label(itemNode.localizedName + "   " + granaryEntity.items.get(itemNode) + "/" + granaryEntity.acceptItemConfig.get(itemNode));
                                    label2.setAlignment(Align.center);
                                    inset.add(label2).minWidth(120).center();
//                                Slider slider = new Slider(0, ((ItemTurretIso) block).maxAmmo, 1, false);
//                                slider.setValue(tileEntity.acceptAmmo.get(itemNode));
//                                slider.changed(() -> {
//                                    label.setText(String.valueOf((int)slider.getValue()));
//                                    tileEntity.acceptAmmo.put(itemNode, (int)slider.getValue());
//                                });
//                                inset.add(slider).fillX().left();

                                    //map.get(stat).display(inset);
                                }).fillX();
//
                                nodeTable.row();
                            }
                        }
                    }
                }
            }
        }

        Image imgBg = new Image(Styles.blockedirotbg);
        imgBg.setScaling(Scaling.fill);
        imgBg.setSize(Styles.blockedirotbg.getRegion().getWidth(), Styles.blockedirotbg.getRegion().getHeight());
        imgBg.touchable(Touchable.disabled);
        imgBg.setAlign(Align.center);
        imgBg.setPosition(-imgBg.getWidth() / 2, 0);
        parentGroup.addChild(imgBg);

        Vec2 startPos = new Vec2(imgBg.getX(), imgBg.getY());
        startPos.set(0, 0);

        // btn  -30, 55, -99, 18, 39, 18            // 基数
        // label   -37, 47, -106, 11, 32, 11
        // btn   -77, 52, 17, 52, -115, -30, 55, -30        // 偶数
        // label   -84, 45, 10, 45, -122, -37, 48, -37
        int[][] btnpos = {
                {-77, 52, 17, 52, -115, -30, 55, -30},     // 偶数
                {-30, 55, -99, 18, 39, 18}, //基数
        };
        int[][] labelpos = {
                {-84, 45, 10, 45, -122, -37, 48, -37},     // 偶数
                {-37, 47, -106, 11, 32, 11},     // 基数
        };

        boolean isAddUnupgrade = false;
        {   // 处理取消升级事件
            if (actions.contains(OperationAction.UNUPGRADE)) {
                actions.remove(OperationAction.UNUPGRADE);
                isAddUnupgrade = true;
            }
        }


        UpgradeTileEntity upgradeEntity = entity instanceof UpgradeTileEntity ? (UpgradeTileEntity) entity : null;
        UpgradeBlock upgradeBlock = entity instanceof UpgradeTileEntity ? (UpgradeBlock) entity.block : null;
//        int curlevel = entity instanceof UpgradeTileEntity ? ((UpgradeTileEntity) entity).curLevel : -1;

        if (true) {
            int i = 0, j = 0;
            int index = actions.size % 2;
            int size = 60;
            int labelWidth = 75;
            int labelHeight = 20;
            Vec2 offset = new Vec2(-size / 2, -size / 2);
            Vec2 labeloffset = new Vec2(-labelWidth / 2, -size * 0.62f);
            for (OperationAction action : actions) {
                ImageButton imgbtn = new ImageButton(Styles.blockedirotico[action.icoindex], Styles.blockeditor1i);
                imgbtn.align(Align.center);
                imgbtn.setSize(size);
                imgbtn.setPosition(btnpos[index][i++], btnpos[index][i++]);
                imgbtn.changed(() -> actionsMap.get(action).get(null));
                parentGroup.addChild(imgbtn);

                Label label = new Label(action.displayName);
                label.getStyle().background = Styles.testNinepatch;
                label.setFontScale(0.8f);
                label.touchable(Touchable.disabled);
                label.setSize(labelWidth, labelHeight);
                label.setPosition(labelpos[index][j++], labelpos[index][j++]);
                label.setAlignment(Align.center);
                parentGroup.addChild(label);

                // debug test
                if (action == OperationAction.UPGRADE) {
                    TileEntity core = player.getClosestCore();

//                    Color color = state.rules.infiniteResources || (core != null && (core.items.has(block.requirements, state.rules.buildCostMultiplier) || state.rules.infiniteResources)) ? Color.white : Color.gray;
                    imgbtn.setDisabled(() -> !upgradeBlock.upgradeCondition(entity));
                    label.update(() -> {
                        Color color = upgradeBlock.upgradeCondition(entity) ? Color.white : Color.gray;
                        label.getStyle().fontColor = color;
                    });
//                    imgbtn.touchable(() -> core.items.has(upgradeBlock.levRequirements[entity.curLevel + 1], state.rules.buildCostMultiplier) ? Touchable.enabled : Touchable.disabled);

                    imgbtn.clicked(() -> buildTable(tile, table, upgradeBlock.getOperationActions(entity)));
                }
                else if(action == OperationAction.MOVE) {
                    imgbtn.clicked(() -> control.input.frag.config.hideConfig());
                }
                else if(action == OperationAction.WORKING || action == OperationAction.REST) {
                    imgbtn.clicked(() -> control.input.frag.config.hideConfig());
                }
            }

            if ( isAddUnupgrade) { // 添加取消事件按钮
                float addy = 15f;

                // 圆形背景事件处理区
                Image img2 = new Image(Styles.block1);
                img2.setScaling(Scaling.fill);
                img2.setSize(Styles.block1.getRegion().getWidth(), Styles.block1.getRegion().getHeight());
//                img2.touchable(Touchable.disabled);
                img2.setAlign(Align.center);
                img2.setPosition(startPos.x - img2.getWidth() / 2, startPos.y - img2.getHeight() / 2 +addy );
                parentGroup.addChild(img2);
                img2.clicked(() ->
                {
                    actionsMap.get(OperationAction.UNUPGRADE).get(null);
                    buildTable(tile, table, upgradeBlock.getOperationActions(entity));
                }); // 添加中断事件

                // 背景
                Image img3 = new Image(Styles.block2);
                img3.setScaling(Scaling.fill);
                img3.setSize(Styles.block2.getRegion().getWidth(), Styles.block2.getRegion().getHeight());
                img3.touchable(Touchable.disabled);
                img3.setAlign(Align.center);
                img3.setPosition(startPos.x - img3.getWidth() / 2, startPos.y - img3.getHeight() / 2 +addy );
                parentGroup.addChild(img3);

                // 背景框
                img3 = new Image(Styles.block3);
                img3.setScaling(Scaling.fill);
                img3.setSize(Styles.block3.getRegion().getWidth(), Styles.block3.getRegion().getHeight());
                img3.touchable(Touchable.disabled);
                img3.setAlign(Align.center);
                img3.setPosition(startPos.x - img3.getWidth() / 2, startPos.y - img3.getHeight() / 2 +addy );
                parentGroup.addChild(img3);

                // 图标
                TextureRegionDrawable drawable = Styles.blockedirotico[OperationAction.UPGRADE.icoindex];
                img3 = new Image(drawable);
                img3.setScaling(Scaling.fill);
                img3.setSize(drawable.getRegion().getWidth(), drawable.getRegion().getHeight());
                img3.touchable(Touchable.disabled);
                img3.setAlign(Align.center);
                img3.setPosition(startPos.x - img3.getWidth() / 2, startPos.y - img3.getHeight() / 2 +addy );
                parentGroup.addChild(img3);

                // 取消图标
                drawable = Styles.block4;
                img3 = new Image(drawable);
                img3.setScaling(Scaling.fill);
                img3.setSize(drawable.getRegion().getWidth(), drawable.getRegion().getHeight());
                img3.touchable(Touchable.disabled);
                img3.setAlign(Align.center);
                img3.setPosition(startPos.x - img3.getWidth() / 2, startPos.y - img3.getHeight() / 2 +addy + 25 );
                parentGroup.addChild(img3);

                // 描述文本
                labelWidth = labelWidth + 12;
                labelHeight = labelHeight + 16;
                Label label2 = new Label(() -> { return getUnupgradeLabel(upgradeBlock, upgradeEntity); });
                label2.getStyle().background = Styles.testNinepatch2;
                label2.setWrap(false);
                label2.setFontScale(0.75f);
                label2.touchable(Touchable.disabled);
                label2.setSize(labelWidth, labelHeight);
                label2.setPosition(startPos.x - labelWidth / 2, startPos.y - labelHeight / 2 +addy - 37);
                label2.setAlignment(Align.center);
                parentGroup.addChild(label2);

                final int curLevel = entity.level();
                // 添加进度完成监听处理
                parentGroup.addChild(new Element() {
                    @Override
                    public void act(float delta) {
                        super.act(delta);
                        if (curLevel != entity.level()) {
                            table.clearChildren();
                            buildTable(tile, table, upgradeBlock.getOperationActions(entity));
                        }
                    }
                });
            }

            return;
        }
    }

    private static String getUnupgradeLabel(UpgradeBlock block, UpgradeTileEntity blockEntity) {
        StringBuilder builder = new StringBuilder();
        if (blockEntity.isUpgrade)
            builder.append(Strs.str3);
        else
            builder.append(Strs.str4);
        builder.append("\n");

        float upgradecost = block.buildCost[blockEntity.copyLevelBy(1)];
        float upgradetime = upgradecost * (1f - blockEntity.progress);

        float value;
        value = upgradetime / hour;
        if ( value >= 1) {      //  时
            builder.append((int)value + Strs.str7);
            upgradetime -= (int)value * hour;
        }
        value = upgradetime / minute;
        if ( value >= 1) {      // 分
            builder.append((int)value + Strs.str6);
            upgradetime -= (int)value * minute;
        }
        value = upgradetime / second;
        if ( value > 0) {      //  秒
            builder.append(Mathf.ceil(value) + Strs.str5);
//            upgradetime -= (int)value * minute;
        }

        return builder.toString();
    }

    /** 构建物品描述子界面*/
    private static Table rebuild(Tile tile){
        Table table = new Table();
//        table.background(Tex.inventory);
//        table.touchable(Touchable.disabled);

        int cols = 4;
        int row = 0;

        table.margin(4f);
        table.defaults().size(8 * 5).pad(4f);

        if(tile.block().hasItems){

            for(int i = 0; i < content.items().size; i++){
                Item item = content.item(i);
                if(!tile.entity.items.has(item)) continue;

                Element image = itemImage(item.icon(Cicon.xlarge), () -> {
                    if(tile == null || tile.entity == null){
                        return "";
                    }
                    return round(tile.entity.items.get(item));
                });

                table.add(image);

                if(row++ % cols == cols - 1) table.row();
            }
        }

        if(row == 0){
            table.setSize(0f, 0f);
        }

        {
//            Vec2 v = Core.input.mouseScreen(tile.drawx() + tile.block().size * tilesize / 2f, tile.drawy() + tile.block().size * tilesize / 2f);
            table.pack();
//            table.setPosition(v.x, v.y, Align.topLeft);
        }

        return row == 0 ? null : table;
    }

    private static String round(float f){
        f = (int)f;
        if(f >= 1000000){
            return (int)(f / 1000000f) + "[gray]" + Core.bundle.getOrNull("unit.millions") + "[]";
        }else if(f >= 1000){
            return (int)(f / 1000) + Core.bundle.getOrNull("unit.thousands");
        }else{
            return (int)f + "";
        }
    }

    private static Element itemImage(TextureRegion region, Prov<CharSequence> text){
        Stack stack = new Stack();

        Table t = new Table().left().bottom();
        t.label(text);

        stack.add(new Image(region));
        stack.add(t);
        return stack;
    }

}
