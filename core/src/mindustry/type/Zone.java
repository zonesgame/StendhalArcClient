package mindustry.type;

import arc.*;
import arc.struct.*;
import arc.func.*;
import arc.graphics.g2d.*;
import arc.scene.ui.layout.*;
import arc.util.ArcAnnotate.*;
import mindustry.content.*;
import mindustry.ctype.ContentType;
import mindustry.ctype.UnlockableContent;
import mindustry.game.EventType.*;
import mindustry.game.*;
import mindustry.game.Objectives.*;
import mindustry.maps.generators.*;

import static mindustry.Vars.*;

/**
 *  战役地图
 * */
public class Zone extends UnlockableContent{
    /** 地图构建器*/
    public @NonNull Generator generator;
    /** 地图完成目标*/
    public @NonNull Objective configureObjective = new ZoneWave(this, 15);
    /** 地图解锁需要完成的战役目标*/
    public Array<Objective> requirements = new Array<>();
    //TODO autogenerate
    /** 地图包含物品资源*/
    public Array<Item> resources = new Array<>();

    /** 地图规则*/
    public Cons<Rules> rules = rules -> {};
    /** 是否允许默认解锁*/
    public boolean alwaysUnlocked = false;          // default false
    /** 满足条件回合*/
    public int conditionWave = Integer.MAX_VALUE;
    /** 发射周期*/
    public int launchPeriod = 10;
    /** 起始发射装载*/
    public Schematic loadout = Loadouts.basicShard;
    /** 地图图标*/
    public TextureRegion preview;

    /** 基础发射消耗*/
    protected Array<ItemStack> baseLaunchCost = new Array<>();
    /** 起始物品*/
    protected Array<ItemStack> startingItems = new Array<>();
    /** 发射消耗*/
    protected Array<ItemStack> launchCost;

    /** 默认起始物品*/
    private Array<ItemStack> defaultStartingItems = new Array<>();

    /**
     * @param name 战役地图名称
     * @param generator 战役地图构建器
     * */
    public Zone(String name, Generator generator){
        super(name);
        this.generator = generator;
    }

    /**
     * @param name 战役地图名称
     * */
    public Zone(String name){
        this(name, new MapGenerator(name));
    }

    @Override
    public void load(){
        preview = Core.atlas.find("zone-" + name, Core.atlas.find(name + "-zone"));     // 地图图标
    }

    /** 地图规则*/
    public Rules getRules(){
        if(generator instanceof MapGenerator){
            return ((MapGenerator)generator).getMap().rules();
        }else{
            Rules rules = new Rules();
            this.rules.get(rules);
            return rules;
        }
    }

    /** 是否可发射资源物品回合*/
    public boolean isLaunchWave(int wave){
        return metCondition() && wave % launchPeriod == 0;
    }

    /** 地图是否可解锁*/
    public boolean canUnlock(){
        return data.isUnlocked(this) || !requirements.contains(r -> !r.complete());
    }

    /** 地图发射消耗物品*/
    public Array<ItemStack> getLaunchCost(){
        if(launchCost == null){
            updateLaunchCost();
        }
        return launchCost;
    }

    /** 地图发射起始物品*/
    public Array<ItemStack> getStartingItems(){
        return startingItems;
    }

    /** 重置地图发射起始物品*/
    public void resetStartingItems(){
        startingItems.clear();
        defaultStartingItems.each(stack -> startingItems.add(new ItemStack(stack.item, stack.amount)));
    }

    /** 地图是否发射过*/
    public boolean hasLaunched(){
        return Core.settings.getBool(name + "-launched", false);
    }

    /** 设置地图发射记录*/
    public void setLaunched(){
        updateObjectives(() -> {
            Core.settings.put(name + "-launched", true);
            data.modified();
        });
    }

    /** 更新地图当前最高回合记录*/
    public void updateWave(int wave){
        int value = Core.settings.getInt(name + "-wave", 0);

        if(value < wave){
            updateObjectives(() -> {
                Core.settings.put(name + "-wave", wave);
                data.modified();
            });
        }
    }

    /** 更新地图完成目标*/
    public void updateObjectives(Runnable closure){
        Array<ZoneObjective> incomplete = content.zones()
            .map(z -> z.requirements).<Objective>flatten()
            .select(o -> o.zone() == this && !o.complete())
            .as(ZoneObjective.class);

        boolean wasConfig = configureObjective.complete();

        closure.run();
        for(ZoneObjective objective : incomplete){
            if(objective.complete()){
                Events.fire(new ZoneRequireCompleteEvent(objective.zone, content.zones().find(z -> z.requirements.contains(objective)), objective));
            }
        }

        if(!wasConfig && configureObjective.complete()){
            Events.fire(new ZoneConfigureCompleteEvent(this));
        }
    }

    /** 地图最好回合记录*/
    public int bestWave(){
        return Core.settings.getInt(name + "-wave", 0);
    }

    /** @return 是否满足地图发射条件.whether initial conditions to launch are met. */
    public boolean isLaunchMet(){
        return bestWave() >= conditionWave;
    }

    /** 更新发射消耗*/
    public void updateLaunchCost(){
        Array<ItemStack> stacks = new Array<>();

        Cons<ItemStack> adder = stack -> {
            for(ItemStack other : stacks){
                if(other.item == stack.item){
                    other.amount += stack.amount;
                    return;
                }
            }
            stacks.add(new ItemStack(stack.item, stack.amount));
        };

        for(ItemStack stack : baseLaunchCost) adder.get(stack);
        for(ItemStack stack : startingItems) adder.get(stack);

        for(ItemStack stack : stacks){
            if(stack.amount < 0) stack.amount = 0;
        }

        stacks.sort();
        launchCost = stacks;
        Core.settings.putObject(name + "-starting-items", startingItems);
        data.modified();
    }

    /** 玩家是否满足离开战役地图条件.<p/>Whether this zone has met its condition; if true, the player can leave. */
    public boolean metCondition(){
        //players can't leave in attack mode.
        return state.wave >= conditionWave && !state.rules.attackMode;
    }

    /** 是否可配置*/
    public boolean canConfigure(){
        return configureObjective.complete();
    }

    @Override
    public void init(){
        if(generator instanceof MapGenerator && minfo.mod != null){
            ((MapGenerator)generator).removePrefix(minfo.mod.name);
        }

        generator.init(loadout);
        resources.sort();

        for(ItemStack stack : startingItems){
            defaultStartingItems.add(new ItemStack(stack.item, stack.amount));
        }

        @SuppressWarnings("unchecked")
        Array<ItemStack> arr = Core.settings.getObject(name + "-starting-items", Array.class, () -> null);
        if(arr != null){
            startingItems = arr;
        }
    }

    @Override
    public boolean alwaysUnlocked(){
        return alwaysUnlocked;
    }

    @Override
    public boolean isHidden(){
        return true;
    }

    //neither of these are implemented, as zones are not displayed in a normal fashion... yet
    @Override
    public void displayInfo(Table table){
    }

    @Override
    public ContentType getContentType(){
        return ContentType.zone;
    }

}
