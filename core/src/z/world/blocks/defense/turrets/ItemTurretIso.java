package z.world.blocks.defense.turrets;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import arc.Core;
import arc.Events;
import arc.scene.ui.layout.Table;
import arc.struct.ObjectMap;
import arc.struct.OrderedMap;
import arc.z.util.ZonesAnnotate.ZAdd;
import arc.z.util.ZonesAnnotate.ZField;
import arc.z.util.ZonesAnnotate.ZMethod;
import mindustry.Vars;
import mindustry.content.Items;
import mindustry.entities.bullet.BulletType;
import mindustry.entities.type.TileEntity;
import mindustry.entities.type.Unit;
import mindustry.game.EventType.Trigger;
import mindustry.game.EventType.TurretAmmoDeliverEvent;
import mindustry.graphics.Pal;
import mindustry.type.Item;
import mindustry.ui.Bar;
import mindustry.ui.Cicon;
import mindustry.ui.ItemImage;
import mindustry.ui.MultiReqImage;
import mindustry.ui.ReqImage;
import mindustry.world.Tile;
import mindustry.world.consumers.Consume;
import mindustry.world.consumers.ConsumeItemFilter;
import mindustry.world.meta.BlockStat;
import mindustry.world.meta.BlockStats;
import mindustry.world.meta.values.AmmoListValue;
import z.debug.Strs;
import z.debug.ZDebug;

import static mindustry.Vars.content;
import static mindustry.Vars.data;
import static mindustry.Vars.state;
import static mindustry.Vars.world;

/**
 *  qqtx物品炮塔 冷却炮塔块.
 * */
public class ItemTurretIso extends CooledTurretIso{
    /** 最大弹药数量*/
    @ZField
    public int maxAmmo[] = {30};
    /** 到达供应弹药配置*/
    @ZField
    public int supplyAmmo[] = {20};
    /** 物品弹药容器*/
    public ObjectMap<Item, BulletType>[] ammo = new ObjectMap[]{new ObjectMap<Item, BulletType>()};

    public ItemTurretIso(String name){
        super(name);
        hasItems = true;
        entityType = ItemTurretIsoEntity::new;
    }

    /** 初始化接收弹药类型.<p/>Initializes accepted ammo map. Format: [item1, bullet1, item2, bullet2...] */
    @ZMethod
    public void ammo(Object[]... objects){
//        ammo = OrderedMap.of(objects);
        ammo = new ObjectMap[objects.length];
        for (int i = objects.length; --i >= 0; ) {
            ammo[i] = OrderedMap.of(objects[i]);
        }
    }

    @Override
    public void setStats(){
        super.setStats();

        stats.remove(BlockStat.itemCapacity);
        stats.add(BlockStat.ammo, new AmmoListValue<>(ammo[0]));
        consumes[0].add(new ConsumeItemFilter(i -> ammo[0].containsKey(i)){
//            @Override
//            public void build(Tile tile, Table table){
//                MultiReqImage image = new MultiReqImage();
//                content.items().each(i -> filter.get(i) && (!world.isZone() || data.isUnlocked(i)), item -> image.add(new ReqImage(new ItemImage(item.icon(Cicon.medium)),
//                        () -> tile.entity != null && !((ItemTurretIsoEntity)tile.entity).ammo.isEmpty() && ((ItemEntry)tile.<ItemTurretIsoEntity>ent().ammo.peek()).item == item)));
//
//                table.add(image).size(8 * 4);
//            }

            @Override
            public void build(Tile tile, Table table){
                MultiReqImage image = new MultiReqImage();
                content.items().each(i -> filter.get(i) && (!world.isZone() || data.isUnlocked(i)), item -> image.add(new ReqImage(new ItemImage(item.icon(Cicon.medium)),
                        () -> tile.entity != null && !((ItemTurretIsoEntity)tile.entity).ammo.isEmpty() && (tile.<ItemTurretIsoEntity>ent().curAmmo) == item)));

                table.add(image).size(8 * 4);
            }

            @Override
            public boolean valid(TileEntity entity){
                //valid when there's any ammo in the turret
                return !((ItemTurretIsoEntity)entity).ammo.isEmpty();
            }

            @Override
            public void display(BlockStats stats){
                //don't display
            }
        });
    }

    @Override
    public void onProximityAdded(Tile tile){
        super.onProximityAdded(tile);

        //add first ammo item to cheaty blocks so they can shoot properly
        if(tile.isEnemyCheat() && ammo[tile.entity.level()].size > 0){
            handleItem(ammo[tile.entity.level()].entries().next().key, tile, tile);
        }
    }

    @Override
    public void displayBars(Tile tile, Table bars){
        super.displayBars(tile, bars);

        TurretIsoEntity entity = tile.ent();

//        bars.add(new Bar("blocks.ammo", Pal.ammo, () -> (float)entity.totalAmmo / maxAmmo[entity.level()])).growX();
        bars.add(new Bar(
                () -> Core.bundle.format(Strs.str26, ZDebug.disable_ammo ? Strs.str27 : entity.totalAmmo),
                () -> Pal.ammo,
                () -> ZDebug.disable_ammo ? 1 : (float)entity.totalAmmo / maxAmmo[entity.level()])
        ).growX();
        bars.row();
    }

    @Override
    public int acceptStack(Item item, int amount, Tile tile, Unit source){
        TurretIsoEntity entity = tile.ent();

        BulletType type = ammo[entity.level()].get(item);

        if(type == null) return 0;

        return Math.min((int)((maxAmmo[entity.level()] - entity.totalAmmo) / ammo[entity.level()].get(item).ammoMultiplier), amount);
    }

    @Override
    public void handleStack(Item item, int amount, Tile tile, Unit source){
        for(int i = 0; i < amount; i++){
            handleItem(item, tile, null);
        }
    }

    //currently can't remove items from turrets.
    @Override
    public int removeStack(Tile tile, Item item, int amount){
        return 0;
    }

    @Override
    public void handleItem(Item item, Tile tile, Tile source){
        TurretIsoEntity entity = tile.ent();
        if(entity == null) return;

        if(item == Items.pyratite){
            Events.fire(Trigger.flameAmmo);
        }

        BulletType type = ammo[entity.level()].get(item);
        entity.totalAmmo += type.ammoMultiplier * extendAmmoMultiplier[entity.level()];

        //find ammo entry by type
        for(int i = 0; i < entity.ammo.size; i++){
            ItemEntry entry = (ItemEntry)entity.ammo.get(i);

            //if found, put it to the right
            if(entry.item == item){
                entry.amount += type.ammoMultiplier * extendAmmoMultiplier[entity.level()];
                entity.ammo.swap(i, entity.ammo.size - 1);
                return;
            }
        }

        //must not be found
        entity.ammo.add(new ItemEntry(item, (int)type.ammoMultiplier * extendAmmoMultiplier[entity.level()]));

        //fire events for the tutorial
        if(state.rules.tutorial){
            Events.fire(new TurretAmmoDeliverEvent());
        }
    }

    @Override
    public boolean acceptItem(Item item, Tile tile, Tile source){
        TurretIsoEntity entity = tile.ent();

//        return ammo != null && ammo.get(item) != null && entity.totalAmmo + ammo.get(item).ammoMultiplier <= maxAmmo[entity.level()];   // default
        return ammo != null && ammo[entity.level()].get(item) != null && entity.totalAmmo <= maxAmmo[entity.level()];       // 允许超负荷接收物品
    }

    @ZAdd
    @Override
    public BulletType peekAmmo(Tile tile){
        ItemTurretIsoEntity entity = tile.ent();
        return ammo[entity.level()].get(entity.curAmmo);
    }

    @ZAdd
    public void displayConsumption(Tile tile, Table table){
        table.left();
        int level = tile.entity.block.consumes.length == 1 ? 0 : tile.entity.level();
        for(Consume cons : consumes[level].all()){
            if(cons.isOptional() && cons.isBoost()) continue;
            cons.build(tile, table);
        }
    }


    public class ItemTurretIsoEntity extends TurretIsoEntity{
        // 配置接收弹药数据begon
        /** 当前接收弹药类型*/
        public Item curAmmo = null;
        public ObjectMap<Item, Integer> acceptAmmo = new ObjectMap<>();
        // 配置接收弹药数据end


        @Override
        public void added() {
//            super.added();
            ItemTurretIso block = (ItemTurretIso) this.block;
            ObjectMap.Keys<Item> keys = block.ammo[level()].keys();
            while (keys.hasNext) {
                Item _item = keys.next();
                if (curAmmo == null) {
                    curAmmo = _item;
                    acceptAmmo.put(_item, block.maxAmmo[level()]);
                }
                else {
                    acceptAmmo.put(_item, 0);
                }
            }
        }

        @Override
        public void write(DataOutput stream) throws IOException{
            super.write(stream);
            stream.writeByte(ammo.size);
            for(AmmoEntry entry : ammo){
                ItemEntry i = (ItemEntry)entry;
                stream.writeByte(i.item.id);
                stream.writeShort(i.amount);
            }
        }

        @Override
        public void read(DataInput stream, byte revision) throws IOException{
            super.read(stream, revision);
            byte amount = stream.readByte();
            for(int i = 0; i < amount; i++){
                Item item = Vars.content.item(stream.readByte());
                short a = stream.readShort();
                totalAmmo += a;
                ammo.add(new ItemEntry(item, a));
            }
        }
    }


    /**
     *  物品弹药
     * */
    public class ItemEntry extends AmmoEntry{
        /** 弹药物品*/
        public Item item;

        ItemEntry(Item item, int amount){
            this.item = item;
            this.amount = amount;
        }

        @Override
        public BulletType type(){
            if (true)
                return null;
            else
                return ammo[0].get(item);
        }


        @Override
        public BulletType type(int lev){
            return ammo[lev].get(item);
        }
    }
}
