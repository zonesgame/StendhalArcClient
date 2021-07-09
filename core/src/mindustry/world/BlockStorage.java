package mindustry.world;

import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.struct.Array;
import arc.util.Time;
import arc.z.util.ZonesAnnotate.ZField;
import arc.z.util.ZonesAnnotate.ZAdd;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.ctype.UnlockableContent;
import mindustry.entities.Effects;
import mindustry.entities.effect.Puddle;
import mindustry.entities.type.TileEntity;
import mindustry.entities.type.Unit;
import mindustry.type.Item;
import mindustry.type.Liquid;
import mindustry.world.consumers.Consumers;
import mindustry.world.meta.BlockBars;
import mindustry.world.meta.BlockStats;

/**
 *  存储块
 * */
public abstract class BlockStorage extends UnlockableContent{
    /** 是否接收物品*/
    public boolean hasItems;
    /** 是否接收流体*/
    public boolean hasLiquids;
    /** 是否接收电力*/
    public boolean hasPower;

    /** 是否输出流体*/
    public boolean outputsLiquid = false;
    /** 是否消耗电力*/
    public boolean consumesPower = true;
    /** 是否暑促电力*/
    public boolean outputsPower = false;

    /** 物品容量*/
    @ZField
    public int[] itemCapacity = {10};     // default public int itemCapacity = 10;
    /** 流体容量*/
    public float liquidCapacity = 10f;
    /** 流体压力*/
    public float liquidPressure = 1f;

    /** 块统计数据*/
    public final BlockStats stats = new BlockStats();
    /** 块状态条管理器*/
    public final BlockBars bars = new BlockBars();
    /** 块消耗物品*/
//    public final Consumers consumes = new Consumers();
    public Consumers[] consumes = {new Consumers()};

    /**
     *  构建存储块
     * */
    public BlockStorage(String name){
        super(name);
    }

    /** 是否已满*/
    public boolean shouldConsume(Tile tile){
        return true;
    }

    /** 生产是否有效*/
    public boolean productionValid(Tile tile){
        return true;
    }

    /** 生产功率*/
    public float getPowerProduction(Tile tile){
        return 0f;
    }

    /** 返回块可以接收物品数量.<p/>Returns the amount of items this block can accept. */
    public int acceptStack(Item item, int amount, Tile tile, Unit source){
        if(acceptItem(item, tile, tile) && hasItems && (source == null || source.getTeam() == tile.getTeam())){
            return Math.min(getMaximumAccepted(tile, item) - tile.entity.items.get(item), amount);
        }else{
            return 0;
        }
    }

    /** 获取块接收最大物品数量*/
    public int getMaximumAccepted(Tile tile, Item item){
        return itemCapacity[tile.entity.level()];
    }

    /** 从此库存中移除指定数量物品, 并将删除的数量返回.<p/>Remove a stack from this inventory, and return the amount removed. */
    public int removeStack(Tile tile, Item item, int amount){
        if(tile.entity == null || tile.entity.items == null) return 0;
        amount = Math.min(amount, tile.entity.items.get(item));
        tile.entity.noSleep();
        tile.entity.items.remove(item, amount);
        return amount;
    }

    /** 处理物品添加.<p/>Handle a stack input. */
    public void handleStack(Item item, int amount, Tile tile, Unit source){
        tile.entity.noSleep();
        tile.entity.items.add(item, amount);
    }

    /** 是否向外输出物品*/
    public boolean outputsItems(){
        return hasItems;
    }

    /** 返回放置偏移位置.<p/>Returns offset for stack placement. */
    public void getStackOffset(Item item, Tile tile, Vec2 trns){

    }

    /** 直接更新*/
    public void onProximityUpdate(Tile tile){
        if(tile.entity != null) tile.entity.noSleep();
    }

    /** 处理物品添加*/
    public void handleItem(Item item, Tile tile, Tile source){
        tile.entity.items.add(item, 1);
    }

    /** 是否可接收指定物品*/
    public boolean acceptItem(Item item, Tile tile, Tile source){
        return consumes[tile.entity.level()].itemFilters.get(item.id) && tile.entity.items.get(item) < getMaximumAccepted(tile, item);
    }

    /** 是否可接收指定流体*/
    public boolean acceptLiquid(Tile tile, Tile source, Liquid liquid, float amount){
        return hasLiquids && tile.entity.liquids.get(liquid) + amount < liquidCapacity && consumes[tile.entity.level()].liquidfilters.get(liquid.id);
    }

    /** 处理流体添加*/
    public void handleLiquid(Tile tile, Tile source, Liquid liquid, float amount){
        tile.entity.liquids.add(liquid, amount);
    }

    /** 尝试倾泻流体*/
    public void tryDumpLiquid(Tile tile, Liquid liquid){
        Array<Tile> proximity = tile.entity.proximity();
        int dump = tile.rotation();

        for(int i = 0; i < proximity.size; i++){
            incrementDump(tile, proximity.size);
            Tile other = proximity.get((i + dump) % proximity.size);
            Tile in = Edges.getFacingEdge(tile, other);

            other = other.block().getLiquidDestination(other, in, liquid);

            if(other != null && other.getTeam() == tile.getTeam() && other.block().hasLiquids && canDumpLiquid(tile, other, liquid) && other.entity.liquids != null){
                float ofract = other.entity.liquids.get(liquid) / other.block().liquidCapacity;
                float fract = tile.entity.liquids.get(liquid) / liquidCapacity;

                if(ofract < fract) tryMoveLiquid(tile, in, other, (fract - ofract) * liquidCapacity / 2f, liquid);
            }
        }

    }

    /** 是否可倾泻流体*/
    public boolean canDumpLiquid(Tile tile, Tile to, Liquid liquid){
        return true;
    }

    /** 尝试移动流体*/
    public void tryMoveLiquid(Tile tile, Tile tileSource, Tile next, float amount, Liquid liquid){
        float flow = Math.min(next.block().liquidCapacity - next.entity.liquids.get(liquid) - 0.001f, amount);

        if(next.block().acceptLiquid(next, tileSource, liquid, flow)){
            next.block().handleLiquid(next, tileSource, liquid, flow);
            tile.entity.liquids.remove(liquid, flow);
        }
    }

    /** 尝试移动流体*/
    public float tryMoveLiquid(Tile tile, Tile next, boolean leak, Liquid liquid){
        return tryMoveLiquid(tile, next, leak ? 1.5f : 100, liquid);
    }

    /** 尝试移动流体*/
    public float tryMoveLiquid(Tile tile, Tile next, float leakResistance, Liquid liquid){
        if(next == null) return 0;

        next = next.link();
        next = next.block().getLiquidDestination(next, tile, liquid);

        if(next.getTeam() == tile.getTeam() && next.block().hasLiquids && tile.entity.liquids.get(liquid) > 0f){

            if(next.block().acceptLiquid(next, tile, liquid, 0f)){
                float ofract = next.entity.liquids.get(liquid) / next.block().liquidCapacity;
                float fract = tile.entity.liquids.get(liquid) / liquidCapacity * liquidPressure;
                float flow = Math.min(Mathf.clamp((fract - ofract) * (1f)) * (liquidCapacity), tile.entity.liquids.get(liquid));
                flow = Math.min(flow, next.block().liquidCapacity - next.entity.liquids.get(liquid) - 0.001f);

                if(flow > 0f && ofract <= fract && next.block().acceptLiquid(next, tile, liquid, flow)){
                    next.block().handleLiquid(next, tile, liquid, flow);
                    tile.entity.liquids.remove(liquid, flow);
                    return flow;
                }else if(ofract > 0.1f && fract > 0.1f){
                    Liquid other = next.entity.liquids.current();
                    if((other.flammability > 0.3f && liquid.temperature > 0.7f) || (liquid.flammability > 0.3f && other.temperature > 0.7f)){
                        tile.entity.damage(1 * Time.delta());
                        next.entity.damage(1 * Time.delta());
                        if(Mathf.chance(0.1 * Time.delta())){
                            Effects.effect(Fx.fire, (tile.worldx() + next.worldx()) / 2f, (tile.worldy() + next.worldy()) / 2f);
                        }
                    }else if((liquid.temperature > 0.7f && other.temperature < 0.55f) || (other.temperature > 0.7f && liquid.temperature < 0.55f)){
                        tile.entity.liquids.remove(liquid, Math.min(tile.entity.liquids.get(liquid), 0.7f * Time.delta()));
                        if(Mathf.chance(0.2f * Time.delta())){
                            Effects.effect(Fx.steam, (tile.worldx() + next.worldx()) / 2f, (tile.worldy() + next.worldy()) / 2f);
                        }
                    }
                }
            }
        }else if(leakResistance != 100f && !next.block().solid && !next.block().hasLiquids){
            float leakAmount = tile.entity.liquids.get(liquid) / leakResistance;
            Puddle.deposit(next, tile, liquid, leakAmount);
            tile.entity.liquids.remove(liquid, leakAmount);
        }
        return 0;
    }

    /** 流体目的地*/
    public Tile getLiquidDestination(Tile tile, Tile from, Liquid liquid){
        return tile;
    }

    /** 如果没有可用的, 试着把这个物品放到一个附近的容器里. 容器, 它被添加到块的库存中.<p/>
     * Tries to put this item into a nearby container, if there are no available
     * containers, it gets added to the block's inventory.
     */
    public void offloadNear(Tile tile, Item item){
        Array<Tile> proximity = tile.entity.proximity();
        int dump = tile.rotation();

        for(int i = 0; i < proximity.size; i++){
            incrementDump(tile, proximity.size);
            Tile other = proximity.get((i + dump) % proximity.size);
            Tile in = Edges.getFacingEdge(tile, other);
            if(other.getTeam() == tile.getTeam() && other.block().acceptItem(item, other, in) && canDump(tile, other, item)){
                other.block().handleItem(item, other, in);
                return;
            }
        }

        handleItem(item, tile, tile);
    }

    /** 尝试临近瓦砾倾泻物品.<p/>Try dumping any item near the tile. */
    public boolean tryDump(Tile tile){
        return tryDump(tile, null);
    }

    /**
     * 尝试在瓦砾附近倾泻特定物品.<p/>Try dumping a specific item near the tile.
     * @param todump Item to dump. Can be null to dump anything.
     */
    public boolean tryDump(Tile tile, Item todump){
        TileEntity entity = tile.entity;
        if(entity == null || !hasItems || tile.entity.items.total() == 0 || (todump != null && !entity.items.has(todump)))
            return false;

        Array<Tile> proximity = entity.proximity();
        int dump = tile.rotation();

        if(proximity.size == 0) return false;

        for(int i = 0; i < proximity.size; i++){
            Tile other = proximity.get((i + dump) % proximity.size);
            Tile in = Edges.getFacingEdge(tile, other);

            if(todump == null){

                for(int ii = 0; ii < Vars.content.items().size; ii++){
                    Item item = Vars.content.item(ii);

                    if(other.getTeam() == tile.getTeam() && entity.items.has(item) && other.block().acceptItem(item, other, in) && canDump(tile, other, item)){
                        other.block().handleItem(item, other, in);
                        tile.entity.items.remove(item, 1);
                        incrementDump(tile, proximity.size);
                        return true;
                    }
                }
            }else{

                if(other.getTeam() == tile.getTeam() && other.block().acceptItem(todump, other, in) && canDump(tile, other, todump)){
                    other.block().handleItem(todump, other, in);
                    tile.entity.items.remove(todump, 1);
                    incrementDump(tile, proximity.size);
                    return true;
                }
            }

            incrementDump(tile, proximity.size);
        }

        return false;
    }

    /** 增加倾泻*/
    protected void incrementDump(Tile tile, int prox){
        tile.rotation((byte)((tile.rotation() + 1) % prox));
    }

    /** 是否可倾泻指定物品.<p/>Used for dumping items. */
    public boolean canDump(Tile tile, Tile to, Item item){
        return true;
    }

    /** 尝试将物品倾泻到附近容器, 如果成功返回true.<p/>Try offloading an item to a nearby container in its facing direction. Returns true if success. */
    public boolean offloadDir(Tile tile, Item item){
        Tile other = tile.front();
        if(other != null && other.getTeam() == tile.getTeam() && other.block().acceptItem(item, other, tile)){
            other.block().handleItem(item, other, tile);
            return true;
        }
        return false;
    }


    // zones add begon

    /** 根据用户配置检测是否可接收物品*/
    @ZAdd
    public boolean acceptItemCaesar(Item item, Tile tile, Tile source){
        return consumes[tile.entity.level()].itemFilters.get(item.id) && tile.entity.items.get(item) < getMaximumAccepted(tile, item);
    }

    /** 是否获取指定物品*/
    @ZAdd
    public boolean getItemCaesar(Item item, Tile tile, Tile source){
        return consumes[tile.entity.level()].itemFilters.get(item.id) && tile.entity.items.get(item) < getMaximumAccepted(tile, item);
    }

    /**
     * 尝试在瓦砾附近倾泻特定物品.<p/>Try dumping a specific item near the tile.
     * @param todump Item to dump. Can be null to dump anything.
     * @param count to dump item count
     */
//    public boolean tryDump(Tile tile, Item todump, int count){
//        TileEntity entity = tile.entity;
//        if(entity == null || !hasItems || tile.entity.items.total() == 0 || (todump != null && !entity.items.has(todump)))
//            return false;
//
//        Array<Tile> proximity = entity.proximity();
//        int dump = tile.rotation();
//
//        if(proximity.size == 0) return false;
//
//        for(int i = 0; i < proximity.size; i++){
//            Tile other = proximity.get((i + dump) % proximity.size);
//            Tile in = Edges.getFacingEdge(tile, other);
//
//            if(todump == null){
//
//                for(int ii = 0; ii < Vars.content.items().size; ii++){
//                    Item item = Vars.content.item(ii);
//
//                    if(other.getTeam() == tile.getTeam() && entity.items.has(item) && other.block().acceptItem(item, other, in) && canDump(tile, other, item)){
//                        other.block().handleItem(item, other, in);
//                        tile.entity.items.remove(item, 1);
//                        incrementDump(tile, proximity.size);
//                        return true;
//                    }
//                }
//            }else{
//
//                if(other.getTeam() == tile.getTeam() && other.block().acceptItem(todump, other, in) && canDump(tile, other, todump)){
//                    other.block().handleItem(todump, other, in);
//                    tile.entity.items.remove(todump, 1);
//                    incrementDump(tile, proximity.size);
//                    return true;
//                }
//            }
//
//            incrementDump(tile, proximity.size);
//        }
//
//        return false;
//    }

    // zones add end
}
