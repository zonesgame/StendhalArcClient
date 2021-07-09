package z.ui;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.TextureRegion;
import arc.scene.Element;
import arc.scene.event.Touchable;
import arc.scene.ui.Image;
import arc.scene.ui.Label;
import arc.scene.ui.SettingsDialog;
import arc.scene.ui.Slider;
import arc.scene.ui.TextButton;
import arc.scene.ui.layout.Table;
import arc.struct.Array;
import arc.struct.ObjectMap;
import arc.struct.OrderedMap;
import arc.util.Align;
import arc.util.Strings;
import mindustry.ctype.UnlockableContent;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.type.Item;
import mindustry.type.ItemType;
import mindustry.type.Liquid;
import mindustry.type.Mech;
import mindustry.type.UnitType;
import mindustry.ui.Cicon;
import mindustry.world.Block;
import mindustry.world.meta.BlockStat;
import mindustry.world.meta.BlockStats;
import mindustry.world.meta.StatCategory;
import mindustry.world.meta.StatValue;
import z.debug.Strs;
import z.world.blocks.defense.turrets.ItemTurretIso;
import z.world.blocks.storage.GranaryIso;
import z.world.blocks.storage.WarehouseIso;

import static arc.Core.bundle;
import static mindustry.Vars.systemItems;

public class SettingDisplay{

    public static <T extends UnlockableContent>TextureRegion icon(T t){
        return t.icon(Cicon.medium);
    }

    /** 粮仓显示配置*/
    /** 显示仓库配置数据*/
    public static void displayGranary(Table table, Block block, GranaryIso.GranaryIsoEntity tileEntity){
        table.table(title -> {
            int size = 8 * 6;

            title.addImage(block.icon(Cicon.xlarge)).size(size);
            title.add("[accent]" + block.localizedName).padLeft(5);
        });

        table.row();

        table.addImage().height(3).color(Color.lightGray).pad(8).padLeft(0).padRight(0).fillX();

        table.row();

        if (true) {
            GranaryIso granaryIso = (GranaryIso) block;

            table.add("[LIGHT_GRAY]" +Strs.str13 + ":[] ").left();
            table.row();

            // 临时数据 begon
            Array<Item> acceptType = systemItems.getGranaryAccept();
            // 临时数据 end

            String[] stateMsg = {Strs.str14, Strs.str15, Strs.str16};
            {   // 添加进度条
                for (Item item_ : acceptType) {
                    table.table(inset -> {
                        inset.left();
                        Item itemNode = item_;
                        inset.addImage(icon(itemNode)).size(4 * 8).left();
                        Label label = new Label(itemNode.localizedName);
                        label.setAlignment(Align.left);
                        inset.add(label).minWidth(130).padLeft(5).center();
                        TextButton textButton = new TextButton(stateMsg[ tileEntity.acceptItemConfig.state(item_)]);
//                        Slider slider = new Slider(0, ((ItemTurretIso) block).maxAmmo, 1, false);
//                        slider.setValue(tileEntity.acceptAmmo.get(itemNode));
                        TextButton finalTextButton = textButton;
                        textButton.changed(() -> {
                            finalTextButton.setText(stateMsg[ tileEntity.acceptItemConfig.nextState(item_)]);
                        });
                        inset.add(textButton).minWidth(200).padLeft(0).left();

                        byte acceptUnit = tileEntity.acceptItemConfig.getUnit(item_);
                        final byte maxUnit = tileEntity.acceptItemConfig.getMaxUnit();
                        textButton = new TextButton(acceptUnit == maxUnit ? Strs.str17 : acceptUnit/2 + "/" + maxUnit/2);
//                        Slider slider = new Slider(0, ((ItemTurretIso) block).maxAmmo, 1, false);
//                        slider.setValue(tileEntity.acceptAmmo.get(itemNode));
                        TextButton finalTextButton1 = textButton;
                        textButton.changed(() -> {
//                            byte maxUnit = tileEntity.acceptItemConfig.getMaxUnit();
                            byte curAcceptType = tileEntity.acceptItemConfig.addUnit(item_, (byte)(maxUnit/4));
                            finalTextButton1.setText(curAcceptType == maxUnit ? Strs.str17 : curAcceptType/2 + "/" + maxUnit/2);
                        });
                        inset.add(textButton).minWidth(70).padLeft(10).fillX().left();

                        //map.get(stat).display(inset);
                    }).padTop(10).padLeft(70).padRight(100).fillX();
//
                    table.row();
                }
            }
        }
    }

    /** 显示仓库配置数据*/
    public static void displayWarehouse(Table table, Block block, WarehouseIso.WarehouseIsoEntity tileEntity){
        table.table(title -> {
            int size = 8 * 6;

            title.addImage(block.icon(Cicon.xlarge)).size(size);
            title.add("[accent]" + block.localizedName).padLeft(5);
        });

        table.row();

        table.addImage().height(3).color(Color.lightGray).pad(8).padLeft(0).padRight(0).fillX();

        table.row();

        if (true) {
            WarehouseIso warehouseIso = (WarehouseIso) block;

            table.add("[LIGHT_GRAY]" +Strs.str13 + ":[] ").left();
            table.row();

            // 临时数据 begon
            Array<Item> acceptType = systemItems.getWarehouseAccept();
            // 临时数据 end

            String[] stateMsg = {Strs.str14, Strs.str15, Strs.str16};
            {   // 添加进度条
                for (Item item_ : acceptType) {
                    table.table(inset -> {
                        inset.left();
                        Item itemNode = item_;
                        inset.addImage(icon(itemNode)).size(4 * 8).left();
                        Label label = new Label(itemNode.localizedName);
                        label.setAlignment(Align.left);
                        inset.add(label).minWidth(130).padLeft(5).center();
                        TextButton textButton = new TextButton(stateMsg[ tileEntity.acceptItemConfig.state(item_)]);
//                        Slider slider = new Slider(0, ((ItemTurretIso) block).maxAmmo, 1, false);
//                        slider.setValue(tileEntity.acceptAmmo.get(itemNode));
                        TextButton finalTextButton = textButton;
                        textButton.changed(() -> {
                            finalTextButton.setText(stateMsg[ tileEntity.acceptItemConfig.nextState(item_)]);
                        });
                        inset.add(textButton).minWidth(200).padLeft(0).left();

                        byte acceptUnit = tileEntity.acceptItemConfig.getUnit(item_);
                        final byte maxUnit = tileEntity.acceptItemConfig.getMaxUnit();
                        textButton = new TextButton(acceptUnit == maxUnit ? Strs.str17 : acceptUnit/2 + "/" + maxUnit/2);
//                        Slider slider = new Slider(0, ((ItemTurretIso) block).maxAmmo, 1, false);
//                        slider.setValue(tileEntity.acceptAmmo.get(itemNode));
                        TextButton finalTextButton1 = textButton;
                        textButton.changed(() -> {
//                            byte maxUnit = tileEntity.acceptItemConfig.getMaxUnit();
                            byte curAcceptType = tileEntity.acceptItemConfig.addUnit(item_, (byte)(maxUnit/4));
                            finalTextButton1.setText(curAcceptType == maxUnit ? Strs.str17 : curAcceptType/2 + "/" + maxUnit/2);
                        });
                        inset.add(textButton).minWidth(70).padLeft(10).fillX().left();

                        //map.get(stat).display(inset);
                    }).padTop(10).padLeft(70).padRight(100).fillX();
//
                    table.row();
                }
            }

            table.pack();
            if (true)
                return;


//            table.add(Strs.str10).color(Pal.accent).left();
//            table.row();
//            {
//                table.addImage(icon(item)).size(5 * 8).padRight(4).right().top();
//                table.add(item.localizedName).padRight(10).left().top();
//            }
//            table.row();
        }
    }

    /**
     *  炮台配置数据显示
     * */
    public static void displayTurret(Table table, Block block, ItemTurretIso.ItemTurretIsoEntity tileEntity){

        table.table(title -> {
            int size = 8 * 6;

            title.addImage(block.icon(Cicon.xlarge)).size(size);
            title.add("[accent]" + block.localizedName).padLeft(5);
        });

        table.row();

        table.addImage().height(3).color(Color.lightGray).pad(8).padLeft(0).padRight(0).fillX();

        table.row();

        if (true) {
            ItemTurretIso itemTurretIso = (ItemTurretIso) block;
//            Item item = itemTurretIso.ammo.keys().next();
            Item item = tileEntity.curAmmo;
//            Item item = itemEntry.item;

            table.table(inset -> {
                inset.left();
                inset.add("[LIGHT_GRAY]" +Strs.str10 + ":[] ").left();
                inset.row();
                Image img = inset.addImage(icon(item)).size(5 * 8).padLeft(10).left().get();
                Label label = inset.add(item.localizedName).padLeft(-20).left().fillX().get();
//                Array<StatValue> arr = map.get(stat);
//                for(StatValue value : arr){
//                    System.out.println(value.getClass());
//                    value.display(inset);
//                    inset.add().size(10f);
//                }

                inset.background(Tex.button);
                inset.touchable(Touchable.enabled);
                inset.clicked(() -> {
                    Item newItem = null;
                    ObjectMap.Keys<Item> keys = ((ItemTurretIso) block).ammo[tileEntity.level()].keys();
                    while (keys.hasNext) {
                        Item item2 = keys.next();
                        if (item2 == tileEntity.curAmmo) {
                            newItem = keys.hasNext() ? keys.next() : ((ItemTurretIso) block).ammo[tileEntity.level()].keys().next();
                            break;
                        }
                    }
                    img.setDrawable(icon(newItem));
                    label.setText(newItem.localizedName);

                    // 更新接收物品数量
                    Element actor = table.find(tileEntity.curAmmo.name);
                    if (actor != null && actor instanceof Slider) {
                        ((Slider) actor).setValue(0);
                    }
                    actor = table.find(newItem.name);
                    if (actor != null && actor instanceof Slider) {
                        ((Slider) actor).setValue(((ItemTurretIso) block).maxAmmo[tileEntity.level()]);
                    }

                    tileEntity.curAmmo = newItem;
                });

                //map.get(stat).display(inset);
            }).fillX();
//                    .fillX().padLeft(10);

            table.row();
            table.addImage().height(3).color(Color.lightGray).pad(8).padLeft(0).padRight(0).fillX();
            table.row();
            table.add("[LIGHT_GRAY]" +Strs.str11 + ":[] ").left();
            table.row();

            {   // 添加进度条
                ObjectMap.Keys<Item> keys = tileEntity.acceptAmmo.keys();
                while ( keys.hasNext) {
                    Item itemNode = keys.next();

                    table.table(inset -> {
                        inset.left();
                        inset.addImage(icon(itemNode)).size(3 * 8).left();
                        Label label = new Label(String.valueOf(tileEntity.acceptAmmo.get(itemNode)));
                        label.setAlignment(Align.center);
                        inset.add(label).minWidth(40).center();
                        Slider slider = new Slider(0, ((ItemTurretIso) block).maxAmmo[tileEntity.level()], 1, false);
                        slider.setName(itemNode.name);
                        slider.setValue(tileEntity.acceptAmmo.get(itemNode));
                        slider.changed(() -> {
                            label.setText(String.valueOf((int)slider.getValue()));
                            tileEntity.acceptAmmo.put(itemNode, (int)slider.getValue());
                        });
                        slider.touchable(Touchable.disabled);
                        inset.add(slider).fillX().left();

                        //map.get(stat).display(inset);
                    }).fillX();
//                    .get().setName("SliderTable:" + itemNode.name);   // zones add code
//
                    table.row();
                }
            }

            if (true)
                return;


//            table.add(Strs.str10).color(Pal.accent).left();
//            table.row();
//            {
//                table.addImage(icon(item)).size(5 * 8).padRight(4).right().top();
//                table.add(item.localizedName).padRight(10).left().top();
//            }
//            table.row();
        }

        if (false) {
            SettingsDialog.SettingsTable sound = new SettingsDialog.SettingsTable();
            sound.sliderPref("musicvol", bundle.get("setting.musicvol.name", "Music Volume"), 100, 0, 100, 1, i -> i + "%");
            sound.sliderPref("sfxvol", bundle.get("setting.sfxvol.name", "SFX Volume"), 100, 0, 100, 1, i -> i + "%");
            sound.sliderPref("ambientvol", bundle.get("setting.ambientvol.name", "Ambient Volume"), 100, 0, 100, 1, i -> i + "%");
            table.add(sound);
            return;
        }

        if (true) {
            BlockStats stats = block.stats;

            for(StatCategory cat : stats.toMap().keys()){
                if (cat != StatCategory.shooting)   continue;
                OrderedMap<BlockStat, Array<StatValue>> map = stats.toMap().get(cat);

                if(map.size == 0) continue;
                System.out.println(map.size);

                table.add("$category." + cat.name()).color(Pal.accent).fillX();
                table.row();

                for(BlockStat stat : map.keys()){
                    if (stat != BlockStat.ammo) continue;
                    System.out.println(stat.localized());
                    table.table(inset -> {
                        inset.left();
                        inset.add("[LIGHT_GRAY]" + stat.localized() + ":[] ").left();
                        Array<StatValue> arr = map.get(stat);
                        for(StatValue value : arr){
                            System.out.println(value.getClass());
                            value.display(inset);
                            inset.add().size(10f);
                        }

                        //map.get(stat).display(inset);
                    }).fillX().padLeft(10);
                    table.row();
                }
            }
            return;
        }

        BlockStats stats = block.stats;

        for(StatCategory cat : stats.toMap().keys()){
            OrderedMap<BlockStat, Array<StatValue>> map = stats.toMap().get(cat);

            if(map.size == 0) continue;

            table.add("$category." + cat.name()).color(Pal.accent).fillX();
            table.row();

            for(BlockStat stat : map.keys()){
                table.table(inset -> {
                    inset.left();
                    inset.add("[LIGHT_GRAY]" + stat.localized() + ":[] ").left();
                    Array<StatValue> arr = map.get(stat);
                    for(StatValue value : arr){
                        value.display(inset);
                        inset.add().size(10f);
                    }

                    //map.get(stat).display(inset);
                }).fillX().padLeft(10);
                table.row();
            }
        }
    }

    public static void displayBlock(Table table, Block block){

        table.table(title -> {
            int size = 8 * 6;

            title.addImage(block.icon(Cicon.xlarge)).size(size);
            title.add("[accent]" + block.localizedName).padLeft(5);
        });

        table.row();

        table.addImage().height(3).color(Color.lightGray).pad(8).padLeft(0).padRight(0).fillX();

        table.row();

        if(block.description != null){
            table.add(block.displayDescription()).padLeft(5).padRight(5).width(400f).wrap().fillX();
            table.row();

            table.addImage().height(3).color(Color.lightGray).pad(8).padLeft(0).padRight(0).fillX();
            table.row();
        }

        BlockStats stats = block.stats;

        for(StatCategory cat : stats.toMap().keys()){
            OrderedMap<BlockStat, Array<StatValue>> map = stats.toMap().get(cat);

            if(map.size == 0) continue;

            table.add("$category." + cat.name()).color(Pal.accent).fillX();
            table.row();

            for(BlockStat stat : map.keys()){
                table.table(inset -> {
                    inset.left();
                    inset.add("[LIGHT_GRAY]" + stat.localized() + ":[] ").left();
                    Array<StatValue> arr = map.get(stat);
                    for(StatValue value : arr){
                        value.display(inset);
                        inset.add().size(10f);
                    }

                    //map.get(stat).display(inset);
                }).fillX().padLeft(10);
                table.row();
            }
        }
    }

    public static void displayItem(Table table, Item item){

        table.table(title -> {
            title.addImage(item.icon(Cicon.xlarge)).size(8 * 6);
            title.add("[accent]" + item.localizedName).padLeft(5);
        });

        table.row();

        table.addImage().height(3).color(Color.lightGray).pad(15).padLeft(0).padRight(0).fillX();

        table.row();

        if(item.description != null){
            table.add(item.displayDescription()).padLeft(5).padRight(5).width(400f).wrap().fillX();
            table.row();

            table.addImage().height(3).color(Color.lightGray).pad(15).padLeft(0).padRight(0).fillX();
            table.row();
        }

        table.left().defaults().fillX();

        table.add(Core.bundle.format("item.corestorable", item.type == ItemType.material ? Core.bundle.format("yes") : Core.bundle.format("no")));
        table.row();

        table.add(Core.bundle.format("item.explosiveness", (int)(item.explosiveness * 100)));
        table.row();
        table.add(Core.bundle.format("item.flammability", (int)(item.flammability * 100)));
        table.row();
        table.add(Core.bundle.format("item.radioactivity", (int)(item.radioactivity * 100)));
        table.row();
    }

    public static void displayLiquid(Table table, Liquid liquid){

        table.table(title -> {
            title.addImage(liquid.icon(Cicon.xlarge)).size(8 * 6);
            title.add("[accent]" + liquid.localizedName).padLeft(5);
        });

        table.row();

        table.addImage().height(3).color(Color.lightGray).pad(15).padLeft(0).padRight(0).fillX();

        table.row();

        if(liquid.description != null){
            table.add(liquid.displayDescription()).padLeft(5).padRight(5).width(400f).wrap().fillX();
            table.row();

            table.addImage().height(3).color(Color.lightGray).pad(15).padLeft(0).padRight(0).fillX();
            table.row();
        }

        table.left().defaults().fillX();

        table.add(Core.bundle.format("item.explosiveness", (int)(liquid.explosiveness * 100)));
        table.row();
        table.add(Core.bundle.format("item.flammability", (int)(liquid.flammability * 100)));
        table.row();
        table.add(Core.bundle.format("liquid.heatcapacity", (int)(liquid.heatCapacity * 100)));
        table.row();
        table.add(Core.bundle.format("liquid.temperature", (int)(liquid.temperature * 100)));
        table.row();
        table.add(Core.bundle.format("liquid.viscosity", (int)(liquid.viscosity * 100)));
        table.row();
    }

    public static void displayMech(Table table, Mech mech){
        table.table(title -> {
            title.addImage(mech.icon(Cicon.xlarge)).size(8 * 6);
            title.add("[accent]" + mech.localizedName).padLeft(5);
        });
        table.left().defaults().left();

        table.row();

        table.addImage().height(3).color(Color.lightGray).pad(15).padLeft(0).padRight(0).fillX();

        table.row();

        if(mech.description != null){
            table.add(mech.displayDescription()).padLeft(5).padRight(5).width(400f).wrap().fillX();
            table.row();

            table.addImage().height(3).color(Color.lightGray).pad(15).padLeft(0).padRight(0).fillX();
            table.row();
        }

        table.left().defaults().fillX();

        if(Core.bundle.has("mech." + mech.name + ".weapon")){
            table.add(Core.bundle.format("mech.weapon", Core.bundle.get("mech." + mech.name + ".weapon")));
            table.row();
        }
        if(Core.bundle.has("mech." + mech.name + ".ability")){
            table.add(Core.bundle.format("mech.ability", Core.bundle.get("mech." + mech.name + ".ability")));
            table.row();
        }

        table.add(Core.bundle.format("mech.buildspeed", (int)(mech.buildPower * 100f)));
        table.row();

        table.add(Core.bundle.format("mech.health", (int)mech.health));
        table.row();
        table.add(Core.bundle.format("mech.itemcapacity", mech.itemCapacity));
        table.row();

        if(mech.drillPower > 0){
            table.add(Core.bundle.format("mech.minespeed", (int)(mech.mineSpeed * 100f)));
            table.row();
            table.add(Core.bundle.format("mech.minepower", mech.drillPower));
            table.row();
        }
    }

    public static void displayUnit(Table table, UnitType unit){
        table.table(title -> {
            title.addImage(unit.icon(Cicon.xlarge)).size(8 * 6);
            title.add("[accent]" + unit.localizedName).padLeft(5);
        });

        table.row();

        table.addImage().height(3).color(Color.lightGray).pad(15).padLeft(0).padRight(0).fillX();

        table.row();

        if(unit.description != null){
            table.add(unit.displayDescription()).padLeft(5).padRight(5).width(400f).wrap().fillX();
            table.row();

            table.addImage().height(3).color(Color.lightGray).pad(15).padLeft(0).padRight(0).fillX();
            table.row();
        }

        table.left().defaults().fillX();

        table.add(Core.bundle.format("unit.health", unit.health));
        table.row();
        table.add(Core.bundle.format("unit.speed", Strings.fixed(unit.speed, 1)));
        table.row();
        table.row();
    }
}
