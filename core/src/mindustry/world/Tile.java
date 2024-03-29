package mindustry.world;

import arc.func.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.ArcAnnotate.*;
import arc.z.util.ISOUtils;
import mindustry.annotations.Annotations.*;
import mindustry.content.*;
import mindustry.entities.traits.*;
import mindustry.entities.type.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.blocks.*;
import mindustry.world.modules.*;

import static mindustry.Vars.*;
import static z.debug.ZDebug.disable_ammo;
import static z.debug.ZDebug.enable_isoInput;

/**
 *  地图瓦砾, 单位长度为1, 坐标系统斜45.
 * */
public class Tile implements Position, TargetTrait{
    /** Tile traversal cost. */
    public byte cost = 1;
    /** 瓦砾绑定实体.<p/>Tile entity, usually null. */
    public TileEntity entity;
    /** 瓦砾位置*/
    public short x, y;
    /** 块*/
    protected Block block;
    /** 地板块*/
    protected Floor floor;
    /** 上层覆盖快*/
    protected Floor overlay;
    /** 块方向(Max 4).<p/>Rotation, 0-3. Also used to store offload location, in which case it can be any number.*/
    protected byte rotation;
    /** 块所属队伍.<p/>Team ordinal. */
    protected byte team;

    public Tile(int x, int y){
        this.x = (short)x;
        this.y = (short)y;
        block = floor = overlay = (Floor)Blocks.air;
    }

    public Tile(int x, int y, int floor, int overlay, int wall){
        this.x = (short)x;
        this.y = (short)y;
        this.floor = (Floor)content.block(floor);
        this.overlay = (Floor)content.block(overlay);
        this.block = content.block(wall);

        //update entity and create it if needed
        changed();
    }

    /** 索引位置.<p/>Returns this tile's position as a {@link Pos}. */
    public int pos(){
        return Pos.get(x, y);
    }

    /** 相对位置*/
    public byte relativeTo(Tile tile){
        return relativeTo(tile.x, tile.y);
    }

    /** 相对位置.<p/>Return relative rotation to a coordinate. Returns -1 if the coordinate is not near this tile. */
    public byte relativeTo(int cx, int cy){
        if(x == cx && y == cy - 1) return 1;
        if(x == cx && y == cy + 1) return 3;
        if(x == cx - 1 && y == cy) return 0;
        if(x == cx + 1 && y == cy) return 2;
        return -1;
    }

    /** 相对位置*/
    public static byte relativeTo(int x, int y, int cx, int cy){
        if(x == cx && y == cy - 1) return 1;
        if(x == cx && y == cy + 1) return 3;
        if(x == cx - 1 && y == cy) return 0;
        if(x == cx + 1 && y == cy) return 2;
        return -1;
    }

    /** 相对位置*/
    public byte absoluteRelativeTo(int cx, int cy){
        if(x == cx && y <= cy - 1) return 1;
        if(x == cx && y >= cy + 1) return 3;
        if(x <= cx - 1 && y == cy) return 0;
        if(x >= cx + 1 && y == cy) return 2;
        return -1;
    }

    /** 相对位置*/
    public static byte absoluteRelativeTo(int x, int y, int cx, int cy){
        if(x == cx && y <= cy - 1) return 1;
        if(x == cx && y >= cy + 1) return 3;
        if(x <= cx - 1 && y == cy) return 0;
        if(x >= cx + 1 && y == cy) return 2;
        return -1;
    }

    /** 使用本地玩家配置一个瓦砾块.<p/>Configure a tile with the current, local player. */
    public void configure(int value){
        Call.onTileConfig(player, this, value);
    }

    /** 配置一个瓦砾块*/
    public void configureAny(int value){
        Call.onTileConfig(null, this, value);
    }

    /** 瓦砾绑定实体*/
    @SuppressWarnings("unchecked")
    public <T extends TileEntity> T ent(){
        return (T)entity;
    }

    /** 瓦砾X世界坐标*/
    public float worldx(){
        if (enable_isoInput)
            return ISOUtils.tileToWorldX(x, y);
        return x * tilesize;
    }

    /** 瓦砾Y世界坐标*/
    public float worldy(){
        if (enable_isoInput)
            return ISOUtils.tileToWorldY(x, y);
        return y * tilesize;
    }

    /** 绘制X轴坐标(世界坐标)*/
    public float drawx(){
        if (enable_isoInput)
            return ISOUtils.tileToWorldX(block().offsetTile() + x, block.offsetTile() + y);
        return block().offset() + worldx();
    }

    /** 绘制Y轴坐标(世界坐标)*/
    public float drawy(){
        if (enable_isoInput)
            return ISOUtils.tileToWorldY(block().offsetTile() + x, block.offsetTile() + y);
        return block().offset() + worldy();
    }

    /** 是否黑色填充瓦砾*/
    public boolean isDarkened(){
        return block().solid && !block().synthetic() && block().fillsTile;
    }

    /** 瓦砾地板块*/
    public @NonNull Floor floor(){
        return floor;
    }

    /** 瓦砾块*/
    public @NonNull Block block(){
        return block;
    }

    /** 瓦砾覆盖块*/
    public @NonNull Floor overlay(){
        return overlay;
    }

    /** 瓦砾块*/
    @SuppressWarnings("unchecked")
    public <T extends Block> T cblock(){
        return (T)block;
    }

    /** 块所属队伍*/
    @Override
    public Team getTeam(){
        return Team.get(link().team);
    }

    /** 设置块所属队伍*/
    public void setTeam(Team team){
        this.team = (byte) team.id;
    }

    public byte getTeamID(){
        return team;
    }

    /** 设置瓦砾块*/
    public void setBlock(@NonNull Block type, Team team, int rotation){
        preChanged();
        this.block = type;
        this.team = (byte) team.id;
        this.rotation = (byte)Mathf.mod(rotation, 4);
        changed();
    }

    /** 设置瓦砾块*/
    public void setBlock(@NonNull Block type, Team team){
        setBlock(type, team, 0);
    }

    /** 设置瓦砾块*/
    public void setBlock(@NonNull Block type){
        if(type == null) throw new IllegalArgumentException("Block cannot be null.");
        preChanged();
        this.block = type;
        this.rotation = 0;
        changed();
    }

    /** 重置瓦砾覆盖层.<p/>This resets the overlay!*/
    public void setFloor(@NonNull Floor type){
        this.floor = type;
        this.overlay = (Floor)Blocks.air;
    }

    /** 设置地板块.<p/>Sets the floor, preserving overlay.*/
    public void setFloorUnder(@NonNull Floor floor){
        Block overlay = this.overlay;
        setFloor(floor);
        setOverlay(overlay);
    }

    /** 移除瓦砾块*/
    public void remove(){
        link().getLinkedTiles(other -> other.setBlock(Blocks.air));
    }

    /** 设置瓦砾块*/
    public void set(Block block, Team team){
        set(block, team, 0);
    }

    /** 设置瓦砾块*/
    public void set(Block block, Team team, int rotation){
        setBlock(block, team, rotation);
        if(block.isMultiblock()){
            int offsetx = -(block.size - 1) / 2;
            int offsety = -(block.size - 1) / 2;

            for(int dx = 0; dx < block.size; dx++){
                for(int dy = 0; dy < block.size; dy++){
                    int worldx = dx + offsetx + x;
                    int worldy = dy + offsety + y;
                    if(!(worldx == x && worldy == y)){
                        Tile toplace = world.tile(worldx, worldy);
                        if(toplace != null){
                            toplace.setBlock(BlockPart.get(dx + offsetx, dy + offsety), team);
                        }
                    }
                }
            }
        }
    }

    /** 移除瓦砾块, 网络同步.<p/>remove()-s this tile, except it's synced across the network */
    public void removeNet(){
        Call.removeTile(this);
    }

    /** 设置瓦砾块, 网络同步.<p/>set()-s this tile, except it's synced across the network */
    public void setNet(Block block, Team team, int rotation){
        Call.setTile(this, block, team, rotation);
    }

    /** 块方向*/
    public byte rotation(){
        return rotation;
    }

    /** 块方向*/
    public void rotation(int rotation){
        this.rotation = (byte)rotation;
    }

    /** 覆盖块ID*/
    public short overlayID(){
        return overlay.id;
    }

    /** 块ID*/
    public short blockID(){
        return block.id;
    }

    /** 地板块ID*/
    public short floorID(){
        return floor.id;
    }

    /** ID设置覆盖块*/
    public void setOverlayID(short ore){
        this.overlay = (Floor)content.block(ore);
    }

    /** 设置覆盖块(矿石Z)*/
    public void setOverlay(Block block){
        this.overlay = (Floor)block;
    }

    /** 清除覆盖块*/
    public void clearOverlay(){
        setOverlayID((short)0);
    }

    /** 瓦砾是否可通行*/
    public boolean passable(){
        return isLinked() || !((floor.solid && (block == Blocks.air || block.solidifes)) || (block.solid && (!block.destructible && !block.update)));
    }

    /** 瓦砾是否被玩家或单位放置块.<p/>Whether this block was placed by a player/unit. */
    public boolean synthetic(){
        return block.update || block.destructible;
    }

    /** 瓦砾块是否为固体*/
    public boolean solid(){
        if (enable_isoInput) {
            return floor.solid || block.solid || block.isSolidFor(this) || (isLinked() && link() != this && link().solid());
        }
        return block.solid || block.isSolidFor(this) || (isLinked() && link() != this && link().solid());
    }

    /** 瓦砾块是否可拆毁*/
    public boolean breakable(){
        return !isLinked() ? (block.destructible || block.breakable || block.update) : link().breakable();
    }

    /** 获取Part块绑定瓦砾*/
    public Tile link(){
        return block.linked(this);
    }

    /** 是否敌人欺诈(无限弹药)*/
    public boolean isEnemyCheat(){
        return (getTeam() == state.rules.waveTeam && state.rules.enemyCheat) || disable_ammo;
    }

    /** 瓦砾块是否为Part块*/
    public boolean isLinked(){
        return block instanceof BlockPart;
    }

    /**
     *  获取块绑定的瓦砾容器<p/>
     * Returns the list of all tiles linked to this multiblock, or an empty array if it's not a multiblock.
     * This array contains all linked tiles, including this tile itself.
     */
    public void getLinkedTiles(Cons<Tile> cons){
        if(block.isMultiblock()){
            int size = block.size;
            int offsetx = -(size - 1) / 2;
            int offsety = -(size - 1) / 2;
            for(int dx = 0; dx < size; dx++){
                for(int dy = 0; dy < size; dy++){
                    Tile other = world.tile(x + dx + offsetx, y + dy + offsety);
                    if(other != null) cons.get(other);
                }
            }
        }else{
            cons.get(this);
        }
    }

    /**
     *  获取块绑定瓦砾容器.<p/>
     * Returns the list of all tiles linked to this multiblock, or an empty array if it's not a multiblock.
     * This array contains all linked tiles, including this tile itself.
     */
    public Array<Tile> getLinkedTiles(Array<Tile> tmpArray){
        tmpArray.clear();
        getLinkedTiles(tmpArray::add);
        return tmpArray;
    }

    /**
     *  获取块绑定瓦砾容器.<p/>
     * Returns the list of all tiles linked to this multiblock if it were this block, or an empty array if it's not a multiblock.
     * This array contains all linked tiles, including this tile itself.
     */
    public Array<Tile> getLinkedTilesAs(Block block, Array<Tile> tmpArray){
        tmpArray.clear();
        if(block.isMultiblock()){
            int offsetx = -(block.size - 1) / 2;
            int offsety = -(block.size - 1) / 2;
            for(int dx = 0; dx < block.size; dx++){
                for(int dy = 0; dy < block.size; dy++){
                    Tile other = world.tile(x + dx + offsetx, y + dy + offsety);
                    if(other != null) tmpArray.add(other);
                }
            }
        }else{
            tmpArray.add(this);
        }
        return tmpArray;
    }

    /** 瓦砾绑定块碰撞范围, 原版返回世界坐标, zones返回tile坐标.*/
    public Rect getHitbox(Rect rect){
        if (enable_isoInput)
            return rect.setSize(block().size).setCenter(getX(), getY());
        return rect.setSize(block().size * tilesize).setCenter(drawx(), drawy());
    }

    /** 获取临近瓦砾*/
    public Tile getNearby(Point2 relative){
        return world.tile(x + relative.x, y + relative.y);
    }

    /** 获取临近瓦砾*/
    public Tile getNearby(int dx, int dy){
        return world.tile(x + dx, y + dy);
    }

    /** 获取临近瓦砾*/
    public Tile getNearby(int rotation){
        if(rotation == 0) return world.tile(x + 1, y);
        if(rotation == 1) return world.tile(x, y + 1);
        if(rotation == 2) return world.tile(x - 1, y);
        if(rotation == 3) return world.tile(x, y - 1);
        return null;
    }

    /** 获取临近瓦砾*/
    public Tile getNearbyLink(int rotation){
        if(rotation == 0) return world.ltile(x + 1, y);
        if(rotation == 1) return world.ltile(x, y + 1);
        if(rotation == 2) return world.ltile(x - 1, y);
        if(rotation == 3) return world.ltile(x, y - 1);
        return null;
    }

    // ▲ ▲ ▼ ▼ ◀ ▶ ◀ ▶ B A
    /** 前方瓦砾*/
    public @Nullable Tile front(){
        return getNearbyLink((rotation + 4) % 4);
    }

    /** 右边瓦砾*/
    public @Nullable Tile right(){
        return getNearbyLink((rotation + 3) % 4);
    }

    /** 后边瓦砾*/
    public @Nullable Tile back(){
        return getNearbyLink((rotation + 2) % 4);
    }

    /** 左边瓦砾*/
    public @Nullable Tile left(){
        return getNearbyLink((rotation + 1) % 4);
    }

    /** 2队伍是否可交互*/
    public boolean interactable(Team team){
        return state.teams.canInteract(team, getTeam());
    }

    /** 瓦砾块产生物品*/
    public @Nullable Item drop(){
        return overlay == Blocks.air || overlay.itemDrop == null ? floor.itemDrop : overlay.itemDrop;
    }

    /** 更新路径消耗值*/
    public void updateOcclusion(){
        cost = 1;
        boolean occluded = false;

        //check for occlusion
        for(int i = 0; i < 8; i++){
            Point2 point = Geometry.d8[i];
            Tile tile = world.tile(x + point.x, y + point.y);
            if(tile != null && tile.floor.isLiquid){
                cost += 4;
            }
            if(tile != null && tile.solid()){
                occluded = true;
                break;
            }
        }

        //+24

        if(occluded){
            cost += 2;
        }

        //+26

        if(link().synthetic() && link().solid()){
            cost += Mathf.clamp(link().block.health[link().entity.level()] / 10f, 0, 20);
        }

        //+46

        if(floor.isLiquid){
            cost += 10;
        }

        //+56

        if(floor.drownTime > 0){
            cost += 70;
        }

        //+126

        if(cost < 0){
            cost = Byte.MAX_VALUE;
        }
    }

    /** 块先前更改*/
    protected void preChanged(){
        block().removed(this);
        if(entity != null){
            entity.removeFromProximity();
        }
        team = 0;
    }

    /** 块更改*/
    protected void changed(){
        if(entity != null){
            entity.remove();
            entity = null;
        }

        Block block = block();

        if(block.hasEntity()){
            entity = block.newEntity().init(this, block.update);
            entity.cons = new ConsumeModule(entity);
            if(block.hasItems) entity.items = new ItemModule();
            if(block.hasLiquids) entity.liquids = new LiquidModule();
            if(block.hasPower){
                entity.power = new PowerModule();
                entity.power.graph.add(this);
            }

            if(!world.isGenerating()){
                entity.updateProximity();
            }
        }else if(!(block instanceof BlockPart) && !world.isGenerating()){
            //since the entity won't update proximity for us, update proximity for all nearby tiles manually
            for(Point2 p : Geometry.d4){
                Tile tile = world.ltile(x + p.x, y + p.y);
                if(tile != null){
                    tile.block().onProximityUpdate(tile);
                }
            }
        }

        updateOcclusion();

        world.notifyChanged(this);
    }

    @Override
    public boolean isDead(){
        return entity == null;
    }

    @Override
    public Vec2 velocity(){
        return Vec2.ZERO;
    }

    /** 瓦砾坐标位置, 矩阵中心点. 原版返回世界坐标.*/
    @Override
    public float getX(){
        if (enable_isoInput)
            return block().offsetTile() + x;
        return drawx();
    }

    @Deprecated
    @Override
    public void setX(float x){
        throw new IllegalArgumentException("Tile position cannot change.");
    }

    /** 瓦砾坐标位置, 矩阵中心点. 原版返回世界坐标*/
    @Override
    public float getY(){
        if (enable_isoInput)
            return block.offsetTile() + y;
        return drawy();
    }

    @Deprecated
    @Override
    public void setY(float y){
        throw new IllegalArgumentException("Tile position cannot change.");
    }

    @Override
    public String toString(){
        return floor.name + ":" + block.name + ":" + overlay + "[" + x + "," + y + "] " + "entity=" + (entity == null ? "null" : (entity.getClass())) + ":" + getTeam();
    }

    //remote utility methods

    /** 网络同步, 移除块*/
    @Remote(called = Loc.server)
    public static void removeTile(Tile tile){
        tile.remove();
    }

    /** 网络同步, 设置块*/
    @Remote(called = Loc.server)
    public static void setTile(Tile tile, Block block, Team team, int rotation){
        tile.set(block, team, rotation);
    }

    // zones add begon
    public float drawxIso(){
        return ISOUtils.tileToWorldX(block().offsetTile() + x, block.offsetTile() + y);
    }

    public float drawyIso(){
        return ISOUtils.tileToWorldY(block().offsetTile() + x, block.offsetTile() + y);
    }
    // zones add end
}
