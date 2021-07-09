package mindustry.game;

import arc.util.serialization.Json;
import arc.util.serialization.Json.Serializable;
import arc.util.serialization.JsonValue;
import mindustry.content.*;
import mindustry.ctype.ContentType;
import mindustry.entities.type.BaseUnit;
import mindustry.type.*;

import static mindustry.Vars.content;

/**
 *  一个重生组, 定义了重生信息为特定类型单位, 其中有特定的额外信息如武器, 装备, 弹药, 状态, 和效应.
 *  每个重生组可以在地图的不同区域产生多个子群.<p/>
 * A spawn group defines spawn information for a specific type of unit, with optional extra information like
 * weapon equipped, ammo used, and status effects.
 * Each spawn group can have multiple sub-groups spawned in different areas of the map.
 */
public class SpawnGroup implements Serializable{
    public static final int never = Integer.MAX_VALUE;

    /** 重生点位类型.<p/>The unit type spawned */
    public UnitType type;
    /** 当这个重生结束的时候.<p/>When this spawn should end */
    public int end = never;
    /** 当重生将开始的时候.<p/>When this spawn should start */
    public int begin;
    /** 回合中重生的间隔.<p/>The spacing, in waves, of spawns. For example, 2 = spawns every other wave */
    public int spacing = 1;
    /** 重生点生成的最大单位数量.<p/>Maximum amount of units that spawn */
    public int max = 100;
    /** 在增加单位数量之前, 有多少波需要通过.<p/>How many waves need to pass before the amount of units spawned increases by 1 */
    public float unitScaling = never;
    /** 敌人的数量最初是没有规模的.<p/>Amount of enemies spawned initially, with no scaling */
    public int unitAmount = 1;
    /** 重生点位的状态效应, null禁用.<p/>Status effect applied to the spawned unit. Null to disable. */
    public StatusEffect effect;
    /** 单位重生携带物品.<p/>Items this unit spawns with. Null to disable. */
    public ItemStack items;

    /** 重生组构建*/
    public SpawnGroup(UnitType type){
        this.type = type;
    }

    public SpawnGroup(){
        //serialization use only
    }

    /** 返回回合构建单位数量.<p/>Returns the amount of units spawned on a specific wave. */
    public int getUnitsSpawned(int wave){
        if(wave < begin || wave > end || (wave - begin) % spacing != 0){
            return 0;
        }
        return Math.min(unitAmount + (int)(((wave - begin) / spacing) / unitScaling), max);
    }

    /**
     *  创建单位.<p/>
     * Creates a unit, and assigns correct values based on this group's data.
     * This method does not add() the unit.
     */
    public BaseUnit createUnit(Team team){
        BaseUnit unit = type.create(team);

        if(effect != null){
            unit.applyEffect(effect, 999999f);
        }

        if(items != null){
            unit.addItem(items.item, items.amount);
        }

        return unit;
    }

    @Override
    public void write(Json json){
        json.writeValue("type", type.name);
        if(begin != 0) json.writeValue("begin", begin);
        if(end != never) json.writeValue("end", end);
        if(spacing != 1) json.writeValue("spacing", spacing);
        //if(max != 40) json.writeValue("max", max);
        if(unitScaling != never) json.writeValue("scaling", unitScaling);
        if(unitAmount != 1) json.writeValue("amount", unitAmount);
        if(effect != null) json.writeValue("effect", effect.id);
    }

    @Override
    public void read(Json json, JsonValue data){
        type = content.getByName(ContentType.unit, data.getString("type", "dagger"));
        if(type == null) type = UnitTypes.dagger;
        begin = data.getInt("begin", 0);
        end = data.getInt("end", never);
        spacing = data.getInt("spacing", 1);
        //max = data.getInt("max", 40);
        unitScaling = data.getFloat("scaling", never);
        unitAmount = data.getInt("amount", 1);
        effect = content.getByID(ContentType.status, data.getInt("effect", -1));
    }

    @Override
    public String toString(){
        return "SpawnGroup{" +
        "type=" + type +
        ", end=" + end +
        ", begin=" + begin +
        ", spacing=" + spacing +
        ", max=" + max +
        ", unitScaling=" + unitScaling +
        ", unitAmount=" + unitAmount +
        ", effect=" + effect +
        ", items=" + items +
        '}';
    }
}
