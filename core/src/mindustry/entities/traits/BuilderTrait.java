package mindustry.entities.traits;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;

import arc.Core;
import arc.Events;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Rect;
import arc.math.geom.Vec2;
import arc.struct.Queue;
import arc.util.ArcAnnotate.Nullable;
import arc.util.Time;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.entities.type.TileEntity;
import mindustry.entities.type.Unit;
import mindustry.game.EventType.BuildSelectEvent;
import mindustry.gen.Call;
import mindustry.graphics.Pal;
import mindustry.world.Block;
import mindustry.world.Build;
import mindustry.world.Pos;
import mindustry.world.Tile;
import mindustry.world.blocks.BuildBlock;
import mindustry.world.blocks.BuildBlock.BuildEntity;

import static mindustry.Vars.content;
import static mindustry.Vars.player;
import static mindustry.Vars.state;
import static mindustry.Vars.tilesize;
import static mindustry.Vars.world;
import static mindustry.entities.traits.BuilderTrait.BuildDataStatic.tmptr;

/**
 * 建造单位属性接口.<p/>
 * Interface for units that build things.
 * */
public interface BuilderTrait extends Entity, TeamTrait{
    //these are not instance variables!
    /** 放置距离*/
    float placeDistance = 220f;
    /** 开采距离*/
    float mineDistance = 70f;

    /** 单位更新建造块.<p/>Updates building mechanism for this unit.*/
    default void updateBuilding(){
        float finalPlaceDst = state.rules.infiniteResources ? Float.MAX_VALUE : placeDistance;  // 工作距离
        Unit unit = (Unit)this;

        Iterator<BuildRequest> it = buildQueue().iterator();
        while(it.hasNext()){
            BuildRequest req = it.next();
            Tile tile = world.tile(req.x, req.y);
            if(tile == null || (req.breaking && tile.block() == Blocks.air) || (!req.breaking && (tile.rotation() == req.rotation || !req.block.rotate) && tile.block() == req.block)){
                it.remove();
            }   // 块拆除完毕
        }

        TileEntity core = unit.getClosestCore();

        //nothing to build.
        if(buildRequest() == null) return;  // 无建造队列

        //find the next build request
        if(buildQueue().size > 1){  // 获取下一个建造块
            int total = 0;
            BuildRequest req;
            while((dst((req = buildRequest()).tile()) > finalPlaceDst || shouldSkip(req, core)) && total < buildQueue().size){
                buildQueue().removeFirst();
                buildQueue().addLast(req);
                total++;
            }
        }

        BuildRequest current = buildRequest();

        if(dst(current.tile()) > finalPlaceDst) return;

        Tile tile = world.tile(current.x, current.y);

        if(!(tile.block() instanceof BuildBlock)){
            if(!current.initialized && canCreateBlocks() && !current.breaking && Build.validPlace(getTeam(), current.x, current.y, current.block, current.rotation)){
                Call.beginPlace(getTeam(), current.x, current.y, current.block, current.rotation);
            }else if(!current.initialized && canCreateBlocks() && current.breaking && Build.validBreak(getTeam(), current.x, current.y)){
                Call.beginBreak(getTeam(), current.x, current.y);
            }else{
                buildQueue().removeFirst();
                return;
            }
        }else if(tile.getTeam() != getTeam()){
            buildQueue().removeFirst();
            return;
        }

        if(tile.entity instanceof BuildEntity && !current.initialized){
            Core.app.post(() -> Events.fire(new BuildSelectEvent(tile, unit.getTeam(), this, current.breaking)));
            current.initialized = true;
        }

        //if there is no core to build with or no build entity, stop building!
        if((core == null && !state.rules.infiniteResources) || !(tile.entity instanceof BuildEntity)){  // 核心和资源不足时终止建设
            return;
        }

        //otherwise, update it.
        BuildEntity entity = tile.ent();

        if(entity == null){
            return;
        }

        if(unit.dst(tile) <= finalPlaceDst){
            unit.rotation = Mathf.slerpDelta(unit.rotation, unit.angleTo(entity), 0.4f);
        }

        if(current.breaking){
            entity.deconstruct(unit, core, 1f / entity.buildCost * Time.delta() * getBuildPower(tile) * state.rules.buildSpeedMultiplier);
        }else{
            if(entity.construct(unit, core, 1f / entity.buildCost * Time.delta() * getBuildPower(tile) * state.rules.buildSpeedMultiplier, current.hasConfig)){
                if(current.hasConfig){
                    Call.onTileConfig(null, tile, current.config);
                }
            }
        }

        current.stuck = Mathf.equal(current.progress, entity.progress);
        current.progress = entity.progress;
    }

    /** @return whether this request should be skipped, in favor of the next one. */
    default boolean shouldSkip(BuildRequest request, @Nullable TileEntity core){
        //requests that you have at least *started* are considered
        if(state.rules.infiniteResources || request.breaking || !request.initialized || core == null) return false;
        return request.stuck && !core.items.has(request.block.requirements);
    }

    default void removeRequest(int x, int y, boolean breaking){
        //remove matching request
        int idx = player.buildQueue().indexOf(req -> req.breaking == breaking && req.x == x && req.y == y);
        if(idx != -1){
            player.buildQueue().removeIndex(idx);
        }
    }

    /** Returns the queue for storing build requests. */
    Queue<BuildRequest> buildQueue();

    /** Build power, can be any float. 1 = builds recipes in normal time, 0 = doesn't build at all. */
    float getBuildPower(Tile tile);

    /** Whether this type of builder can begin creating new blocks. */
    default boolean canCreateBlocks(){
        return true;
    }

    default void writeBuilding(DataOutput output) throws IOException{
        BuildRequest request = buildRequest();

        if(request != null && (request.block != null || request.breaking)){
            output.writeByte(request.breaking ? 1 : 0);
            output.writeInt(Pos.get(request.x, request.y));
            output.writeFloat(request.progress);
            if(!request.breaking){
                output.writeShort(request.block.id);
                output.writeByte(request.rotation);
            }
        }else{
            output.writeByte(-1);
        }
    }

    default void readBuilding(DataInput input) throws IOException{
        readBuilding(input, true);
    }

    default void readBuilding(DataInput input, boolean applyChanges) throws IOException{
        if(applyChanges) buildQueue().clear();

        byte type = input.readByte();
        if(type != -1){
            int position = input.readInt();
            float progress = input.readFloat();
            BuildRequest request;

            if(type == 1){ //remove
                request = new BuildRequest(Pos.x(position), Pos.y(position));
            }else{ //place
                short block = input.readShort();
                byte rotation = input.readByte();
                request = new BuildRequest(Pos.x(position), Pos.y(position), rotation, content.block(block));
            }

            request.progress = progress;

            if(applyChanges){
                buildQueue().addLast(request);
            }else if(isBuilding()){
                BuildRequest last = buildRequest();
                last.progress = progress;
                if(last.tile() != null && last.tile().entity instanceof BuildEntity){
                    ((BuildEntity)last.tile().entity).progress = progress;
                }
            }
        }
    }

    /** Return whether this builder's place queue contains items. */
    default boolean isBuilding(){
        return buildQueue().size != 0;
    }

    /** Clears the placement queue. */
    default void clearBuilding(){
        buildQueue().clear();
    }

    /** Add another build requests to the tail of the queue, if it doesn't exist there yet. */
    default void addBuildRequest(BuildRequest place){
        addBuildRequest(place, true);
    }

    /** Add another build requests to the queue, if it doesn't exist there yet. */
    default void addBuildRequest(BuildRequest place, boolean tail){
        BuildRequest replace = null;
        for(BuildRequest request : buildQueue()){
            if(request.x == place.x && request.y == place.y){
                replace = request;
                break;
            }
        }
        if(replace != null){
            buildQueue().remove(replace);
        }
        Tile tile = world.tile(place.x, place.y);
        if(tile != null && tile.entity instanceof BuildEntity){
            place.progress = tile.<BuildEntity>ent().progress;
        }
        if(tail){
            buildQueue().addLast(place);
        }else{
            buildQueue().addFirst(place);
        }
    }

    /**
     * Return the build requests currently active, or the one at the top of the queue.
     * May return null.
     */
    default @Nullable
    BuildRequest buildRequest(){
        return buildQueue().size == 0 ? null : buildQueue().first();
    }

    //due to iOS weirdness, this is apparently required
    class BuildDataStatic{
        static Vec2[] tmptr = new Vec2[]{new Vec2(), new Vec2(), new Vec2(), new Vec2()};
    }

    /** Draw placement effects for an entity. */
    default void drawBuilding(){
        if(!isBuilding()) return;

        Unit unit = (Unit)this;
        BuildRequest request = buildRequest();
        Tile tile = world.tile(request.x, request.y);

        if(dst(tile) > placeDistance && !state.isEditor()){
            return;
        }

        Lines.stroke(1f, Pal.accent);
        float focusLen = 3.8f + Mathf.absin(Time.time(), 1.1f, 0.6f);
        float px = unit.x + Angles.trnsx(unit.rotation, focusLen);
        float py = unit.y + Angles.trnsy(unit.rotation, focusLen);

        float sz = Vars.tilesize * tile.block().size / 2f;
        float ang = unit.angleTo(tile);

        tmptr[0].set(tile.drawx() - sz, tile.drawy() - sz);
        tmptr[1].set(tile.drawx() + sz, tile.drawy() - sz);
        tmptr[2].set(tile.drawx() - sz, tile.drawy() + sz);
        tmptr[3].set(tile.drawx() + sz, tile.drawy() + sz);

        Arrays.sort(tmptr, (a, b) -> -Float.compare(Angles.angleDist(Angles.angle(unit.x, unit.y, a.x, a.y), ang),
                Angles.angleDist(Angles.angle(unit.x, unit.y, b.x, b.y), ang)));

        float x1 = tmptr[0].x, y1 = tmptr[0].y,
                x3 = tmptr[1].x, y3 = tmptr[1].y;

        Draw.alpha(1f);

        Lines.line(px, py, x1, y1);
        Lines.line(px, py, x3, y3);

        Fill.circle(px, py, 1.6f + Mathf.absin(Time.time(), 0.8f, 1.5f));

        Draw.color();
    }


    /**
     *  用于存储建造请求的类, 可以是一个放置或移除请求.<p/>
     * Class for storing build requests. Can be either a place or remove request. */
    class BuildRequest{
        /** 请求的位置和角度.<p/>Position and rotation of this request. */
        public int x, y, rotation;
        /** 放置的块, 如果为null为拆除请求.<p/>Block being placed. If null, this is a breaking request.*/
        public @Nullable Block block;
        /** 是否为拆除请求.<p/>Whether this is a break request.*/
        public boolean breaking;
        /** 是否带有配置参数.<p/>Whether this request comes with a config int. If yes, any blocks placed with this request will not call playerPlaced.*/
        public boolean hasConfig;
        /** 配置参数, 除非正确否则不使用.<p/>Config int. Not used unless hasConfig is true.*/
        public int config;
        /** 中心点仅用在蓝图.<p/>Original position, only used in schematics.*/
        public int originalX, originalY, originalWidth, originalHeight;

        /** 最后进度.<p/>Last progress.*/
        public float progress;
        /** 是否已经启动了这个请求, 以及其它特殊变量.<p/>Whether construction has started for this request, and other special variables.*/
        public boolean initialized, worldContext = true, stuck;

        /** 显示缩放, 仅用于渲染.<p/>Visual scale. Used only for rendering.*/
        public float animScale = 0f;

        /** 创建一个建造请求.<p/>This creates a build request. */
        public BuildRequest(int x, int y, int rotation, Block block){
            this.x = x;
            this.y = y;
            this.rotation = rotation;
            this.block = block;
            this.breaking = false;
        }

        /** 创建一个移除请求.<p/>This creates a remove request. */
        public BuildRequest(int x, int y){
            this.x = x;
            this.y = y;
            this.rotation = -1;
            this.block = world.tile(x, y).block();
            this.breaking = true;
        }

        public BuildRequest(){

        }

        public BuildRequest copy(){
            BuildRequest copy = new BuildRequest();
            copy.x = x;
            copy.y = y;
            copy.rotation = rotation;
            copy.block = block;
            copy.breaking = breaking;
            copy.hasConfig = hasConfig;
            copy.config = config;
            copy.originalX = originalX;
            copy.originalY = originalY;
            copy.progress = progress;
            copy.initialized = initialized;
            copy.animScale = animScale;
            return copy;
        }

        public BuildRequest original(int x, int y, int originalWidth, int originalHeight){
            originalX = x;
            originalY = y;
            this.originalWidth = originalWidth;
            this.originalHeight = originalHeight;
            return this;
        }

        public Rect bounds(Rect rect){
            if(breaking){
                return rect.set(-100f, -100f, 0f, 0f);
            }else{
                return block.bounds(x, y, rect);
            }
        }

        public BuildRequest set(int x, int y, int rotation, Block block){
            this.x = x;
            this.y = y;
            this.rotation = rotation;
            this.block = block;
            this.breaking = false;
            return this;
        }

        public float drawx(){
            return x*tilesize + block.offset();
        }

        public float drawy(){
            return y*tilesize + block.offset();
        }

        public BuildRequest configure(int config){
            this.config = config;
            this.hasConfig = true;
            return this;
        }

        public @Nullable Tile tile(){
            return world.tile(x, y);
        }

        @Override
        public String toString(){
            return "BuildRequest{" +
            "x=" + x +
            ", y=" + y +
            ", rotation=" + rotation +
            ", recipe=" + block +
            ", breaking=" + breaking +
            ", progress=" + progress +
            ", initialized=" + initialized +
            '}';
        }

        // zones add begon
        public Rect boundsTile(Rect rect){
            if(breaking){
                return rect.set(-100f, -100f, 0f, 0f);
            }else{
                return block.boundsTile(x, y, rect);
            }
        }
        // zones add end
    }
}
