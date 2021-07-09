package mindustry.type;

import arc.struct.*;
import arc.graphics.*;
import arc.scene.ui.layout.*;
import mindustry.ctype.*;
import mindustry.ctype.ContentType;
import mindustry.ui.*;
import mindustry.world.blocks.*;

import static mindustry.Vars.content;

/**
 *  游戏内产出的物品
 * */
public class Item extends UnlockableContent{
    public final Color color;

    /** 资源类型, 用于标记是否可被核心接收.<p/>type of the item; used for tabs and core acceptance. default value is {@link ItemType#resource}. */
    public ItemType type = ItemType.resource;
    /** 物品的爆炸属性值.<p/>how explosive this item is. */
    public float explosiveness = 0f;
    /** 物品的可燃性属性值.<p/>flammability above 0.3 makes this eleigible for item burners. */
    public float flammability = 0f;
    /** 物品放射性.<p/>how radioactive this item is. 0=none, 1=chernobyl ground zero */
    public float radioactivity;
    /** 钻探硬度.<p/>drill hardness of the item */
    public int hardness = 0;
    /**
     *  基础物品成本.<p/>
     * base material cost of this item, used for calculating place times
     * 1 cost = 1 tick added to build time
     */
    public float cost = 1f;
    /** 是否默认解锁.<p/>If true, item is always unlocked. */
    public boolean alwaysUnlocked = false;

    public Item(String name, Color color){
        super(name);
        this.color = color;
    }

    public Item(String name){
        this(name, new Color(Color.black));
    }

    @Override
    public boolean alwaysUnlocked(){
        return alwaysUnlocked;
    }

    @Override
    public void displayInfo(Table table){
        ContentDisplay.displayItem(table, this);
    }

    @Override
    public String toString(){
        return localizedName;
    }

    @Override
    public ContentType getContentType(){
        return ContentType.item;
    }

    /** 矿石可生成的所有物品.<p/>Allocates a new array containing all items that generate ores. */
    public static Array<Item> getAllOres(){
        return content.blocks().select(b -> b instanceof OreBlock).map(b -> ((Floor)b).itemDrop);
    }

    // zones add begon
    public int drawid;
    public Item(String name, Color color, int id){
        super(name);
        this.color = color;
        this.drawid = id;
    }
    // zones add end
}
