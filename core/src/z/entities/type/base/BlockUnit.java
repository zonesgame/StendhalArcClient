package z.entities.type.base;

import java.util.Iterator;

import arc.ai.pfa.Connection;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.java.util.Collections;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.struct.Array;
import arc.util.ArcAnnotate;
import arc.util.Time;
import arc.util.pooling.Pool;
import mindustry.Vars;
import mindustry.ai.Pathfinder.PathTarget;
import mindustry.entities.Predict;
import mindustry.entities.Units;
import mindustry.entities.bullet.BulletType;
import mindustry.entities.type.BaseUnit;
import mindustry.entities.type.TileEntity;
import mindustry.entities.units.UnitCommand;
import mindustry.entities.units.UnitState;
import mindustry.game.Team;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.type.Weapon;
import mindustry.world.Tile;
import mindustry.world.blocks.Floor;
import mindustry.world.modules.ItemModule;
import z.ai.astar.FlatTiledNode;
import z.ai.astar.TiledSmoothableGraphPath;
import z.entities.ani.SpriteAniControl;
import z.entities.type.WorkerTileEntity;
import z.utils.FinalCons;
import z.utils.ShapeRenderer;
import z.world.blocks.caesar.HousingIso;
import z.world.blocks.storage.StorageBlockIso;

import static mindustry.Vars.pathfinder;
import static mindustry.Vars.systemItems;
import static mindustry.Vars.systemWorker;
import static mindustry.Vars.world;
import static z.debug.ZDebug.enable_isoInput;
import static z.entities.type.base.BlockUnit.WorkerType.GETTING;
import static z.entities.type.base.BlockUnit.WorkerType.PATROL;
import static z.entities.type.base.BlockUnit.WorkerType.TRANSPORT;

/**
 *  地面单位实体
 * */
public class BlockUnit<T extends  TileEntity> extends BaseUnit implements Pool.Poolable {
    protected static Vec2 vec = new Vec2();

    /** 行走时间*/
    protected float walkTime;
    /** 被动时间*/
    protected float stuckTime;
    /** 基础角度*/
    protected float baseRotation;

    // zones add begon
    @ArcAnnotate.Nullable
    @Deprecated
    private Tile targetAStar = null;     // or TargetTrait   Tile
    /** 当前移动目标点*/
    private Vec2 curMoveTarget = new Vec2();
    /** 移动路径点*/
    private Iterator<FlatTiledNode> targets = Collections.emptyIterator();
    /** AStar路径数据*/
    protected TiledSmoothableGraphPath<FlatTiledNode> aStarPath = new TiledSmoothableGraphPath<>();
    /** 动画播放管理器*/
    private SpriteAniControl aniControl = new SpriteAniControl().setFrameData(11, 10);
    // zones add end

//    private ItemStack getItem = new ItemStack();
    /** 单位需要的物品, 或运输的物品*/
    private Array<ItemStack> getItems = new Array<>();
    /** 单位自身携带的物品*/
    private ItemModule unitItems = new ItemModule();
    /** 单位执行的AI状态机*/
    private UnitState startState;

    /** 单位状态机状态*/
    public final UnitState

            attack = new UnitState<BlockUnit>() {
        @Override
        public void enter(BlockUnit unit) {
            target = null;
        }

        @Override
        public void update(BlockUnit unit) {
            TileEntity core = getClosestEnemyCore();

            if (core == null) {
                Tile closestSpawn = getClosestSpawner();
                if (closestSpawn == null || !withinDst(closestSpawn, Vars.state.rules.dropZoneRadius + 85f)) {
                    moveToCore(PathTarget.enemyCores);
                }
            } else {
                float dst = dst(core);

                if (dst < getWeapon().bullet.range() / 1.1f) {
                    target = core;
                }

                if (dst > getWeapon().bullet.range() * 0.5f) {
                    moveToCore(PathTarget.enemyCores);
                }
            }
        }
    },
    /**
     * 运输状态
     */
//    transport = new UnitState() {
//        public void update() {
//            if (fail) {     // 完成操作, 回收对象
//                if (spawnEntity != null && spawnEntity instanceof WorkerTileEntity)
//                    ((WorkerTileEntity) spawnEntity).freeWorker(BlockUnit.this);
//                else
//                    remove();
//                return;
//            }
//
//            // 重新获取目标和移动路径
//            if (searchTarget) {
//                searchTarget = false;
//                getPath(systemWorker.getTransportTile(BlockUnit.this, getItems.first()));
//                return;
//            }
//
//            if (!initPath && aStarPath.getCount() > 0) {
//                initPath();
//            }   // 初始化AStar路径
//
//            if (curMoveTarget.isZero()) return;
//
//            velocity.add(vec.trns(angleTo(curMoveTarget), type.speed * Time.delta()));
//            if (true || Units.invalidateTarget(target, BlockUnit.this)) {
//                rotation = Mathf.slerpDelta(rotation, baseRotation, type.rotatespeed);
//                aniControl.update();    // 更新动画
//            }
//
//            if (lastPosition().dst(curMoveTarget) < 0.05f) {
//                if (targets.hasNext()) {
//                    curMoveTarget.set(targets.next().getPosition());
//                } else {
//                    curMoveTarget.set(Vec2.ZERO);
//                    searchTarget = !dumpItem(storageEntity);
//                    if ( !searchTarget)
//                        Time.run(FinalCons.second * 1, () -> fail = true);
//                }   // 完成路径移动
//            }
//        }
//    },

    /**
     *  物品获取和运输状态
     */
    gettingAndTransport = new UnitState<BlockUnit>() {
        @Override
        public void update(BlockUnit unit) {
            // 任务完成或失败, 回收对象
            if (isRemove) {
                if (parentEntity != null && parentEntity instanceof WorkerTileEntity)
                    ((WorkerTileEntity) parentEntity).freeWorker(BlockUnit.this);
                else
                    remove();
                return;
            }

            // 重新检测获取对象目标
            if (searchTarget) {
                searchTarget = false;
                getPath(getStorageTile());
                return;
            }

            // 初始化移动路径
            if (!initPath && aStarPath.getCount() > 0) {
                initPath();
            }

            {  // 到达目标点, 执行完毕, 准备回收对象
                if (curMoveTarget.isZero()) return;
            }

            // 执行移动操作, 和转向
            velocity.add(vec.trns(angleTo(curMoveTarget), type.speed * Time.delta()));
            if (true || Units.invalidateTarget(target, BlockUnit.this)) {
                rotation = Mathf.slerpDelta(rotation, baseRotation, type.rotatespeed);
                aniControl.update();    // 更新动画
            }

            // 到达下一个Tile
            if (lastPosition().dst(curMoveTarget) < 0.05f) {
                if (targets.hasNext()) {    // 执行下一个移动节点
                    curMoveTarget.set(targets.next().getPosition());
                }
                // 到达目标点
                else {
                    curMoveTarget.set(Vec2.ZERO);
                    gettingTargetEvent();
                }   // 完成路径移动
            }
        }
    },

    /**
     * 工作巡逻状态
     */
    patrol = new UnitState<BlockUnit>() {
        private Connection<FlatTiledNode> curConnection = null;
        private int step, maxStep = 36, halfMaxStep = 10;

        @Override
        public boolean equals(Object obj) {
            return false;
        }

        @Override
        public void enter(BlockUnit unit) {
            step = 0;
            target = null;      // not must
            FlatTiledNode graphNode = pathfinder.getGraphNode(x, y);
            if (graphNode != null && (curConnection = graphNode.getConnection(null)) != null) {
                curMoveTarget.set(curConnection.getToNode().getPosition());
            }
            else
                Time.run(FinalCons.second * 2, () -> isRemove = true);
            marketPatrolEvent();
        }

        @Override
        public void exit(BlockUnit unit) {

        }

        @Override
        public void update(BlockUnit unit) {
            if (isRemove) {
                if (parentEntity != null && parentEntity instanceof WorkerTileEntity) {
//                    spawnEntity.items.addAll(marketItems);  // 回收剩余物品
                    ((WorkerTileEntity) parentEntity).freeWorker(BlockUnit.this);
                }
                else
                    remove();
            }   // 对象回收入容器池

            if (curMoveTarget.isZero()) return;

            velocity.add(vec.trns(angleTo(curMoveTarget), type.speed * Time.delta()));
            if (true || Units.invalidateTarget(target, BlockUnit.this)) {
                rotation = Mathf.slerpDelta(rotation, baseRotation, type.rotatespeed);
                aniControl.update();
            }

            if (lastPosition().dst(curMoveTarget) < 0.05f) {
                Connection<FlatTiledNode> targetConnection = curConnection.getToNode().getConnection(curConnection);
                if (targetConnection != null && ++step < maxStep) {
                    curConnection = targetConnection;
                    curMoveTarget.set(curConnection.getToNode().getPosition());
                } else {
                    curMoveTarget.set(Vec2.ZERO);
                    Time.run(FinalCons.second * 1, () -> isRemove = true);
                }
                {   // 事件更新
                    // 商店物品分配事件更新
                    marketPatrolEvent();
                }
            }
        }
    },

    retreat = new UnitState<BlockUnit>() {
        @Override
        public void enter(BlockUnit unit) {
            target = null;
        }

        @Override
        public void update(BlockUnit unit) {
            moveAwayFromCore();
        }
    };

    /**  出生点*/
    private Tile spawnerTile = null;
    @Override
    public void setSpawner(Tile tile) {
        super.setSpawner(tile);
        spawnerTile = tile;
    }

    @Override
    public void onCommand(UnitCommand command){
        state.changeState(command == UnitCommand.retreat ? retreat :
                command == UnitCommand.attack ? attack :
                        command == UnitCommand.rally ? gettingAndTransport :
                                null);
    }

    @Override
    public void interpolate(){
        super.interpolate();

        if(interpolator.values.length > 1){
            baseRotation = interpolator.values[1];
        }
    }

    @Override
    public void move(float x, float y){
        float dst = Mathf.dst(x, y);
        if(dst > 0.01f){
            baseRotation = Mathf.slerp(baseRotation, Mathf.angle(x, y), type.baseRotateSpeed * (dst / type.speed));
        }
        super.move(x, y);
    }

    @Override
    public UnitState getStartState(){
        return startState;
    }

    @Override
    public void update(){
        super.update();

        stuckTime = !vec.set(x, y).sub(lastPosition()).isZero(0.0001f) ? 0f : stuckTime + Time.delta();

        if(!velocity.isZero()){
            baseRotation = Mathf.slerpDelta(baseRotation, velocity.angle(), 0.05f);
        }

        if(stuckTime < 1f){
            walkTime += Time.delta();
        }
    }

    @Override
    public Weapon getWeapon(){
        return type.weapon;
    }

    @Override
    public void draw(){
        ShapeRenderer.drawDiamond(x, y, 1);
        //zones add begon
        float x = this.x;
        float y = this.y;
        float baseRotation = this.baseRotation;
        float rotation = this.rotation;
        if (enable_isoInput) {
            x = wpos.x;
            y = wpos.y;
            baseRotation = 360 - this.baseRotation + 45;
            rotation = 360 - this.rotation + 45;
        }
        // zones add end
        Draw.mixcol(Color.white, hitTime / hitDuration);

        float ft = Mathf.sin(walkTime * type.speed * 5f, 6f, 2f + type.hitsize / 15f);

        Floor floor = getFloorOn();

        if(floor.isLiquid){
            Draw.color(Color.white, floor.color, drownTime * 0.4f);
        }else{
            Draw.color(Color.white);
        }

        {
            int ani = 0;
            int dir = aniControl.getFrameDir(rotation);
            int frame = aniControl.getLoopFrame();

            if (type.name.equals("worker")) {
                Draw.rect(type.getFrameRegion(ani, dir, frame), x, y);
                ani++;
                if (workerType == PATROL)
                    ani++;
                else if (unitItems.total() > 0) {
                    ani += 1 + unitItems.first().drawid;
                }

                Draw.rect(type.getFrameRegion(ani, dir, frame), x, y);
            }
            else if (type.name.equals("marketWorker")) {
                Draw.rect(type.getFrameRegion(0, dir, frame), x, y);
            }
        }

        Draw.mixcol();
    }

    @Override
    public void drawBackItems(float itemtime, boolean number){
    }

    @Override
    public void behavior(){     //  更新攻击行为

        if(!Units.invalidateTarget(target, this)){
            if(dst(target) < getWeapon().bullet.range()){

                rotate(angleTo(target));

                if(Angles.near(angleTo(target), rotation, 13f)){
                    BulletType ammo = getWeapon().bullet;

                    Vec2 to = Predict.intercept(BlockUnit.this, target, ammo.speed);

                    getWeapon().update(BlockUnit.this, to.x, to.y);
                }
            }
        }
    }

    @Override
    public void updateTargeting(){
//        super.updateTargeting();
//
//        if(Units.invalidateTarget(target, team, x, y, Float.MAX_VALUE)){
//            target = null;
//        }
//
//        if(retarget()){
//            targetClosest();
//        }
    }

    private ItemModule marketItems = new ItemModule();
    /** 商场工作人员巡逻事件*/
    private final int[] marketOffsetTile = {1,0, 2,0, -1,0, -2,0,  0,1, 0,2, 0,-1, 0,-2};
    private void marketPatrolEvent() {
        for (int i = marketOffsetTile.length; i >0; ) {
            Tile nodeTile = world.tile(x + marketOffsetTile[--i], y + marketOffsetTile[--i]);
            if (nodeTile != null && nodeTile.block() instanceof HousingIso) {
                ((HousingIso) nodeTile.block()).updatePatrol(nodeTile, this);
            }
        }
    }
    /** 获取市场存储物品*/
    public ItemModule getMarketItems() {
//        return marketItems;
        if(spawnerTile == null || parentEntity.isDead())
            return null;

        return parentEntity.items;
    }

    /** 巡逻*/
    protected void patrol(){
        vec.trns(baseRotation, type.speed * Time.delta());
        velocity.add(vec.x, vec.y);
        vec.trns(baseRotation, type.hitsizeTile * 5);
        Tile tile = world.tileWorld(x + vec.x, y + vec.y);
        if((tile == null || tile.solid() || tile.floor().drownTime > 0 || tile.floor().isLiquid) || stuckTime > 10f){
            baseRotation += Mathf.sign(id % 2 - 0.5f) * Time.delta() * 3f;
        }

        rotation = Mathf.slerpDelta(rotation, velocity.angle(), type.rotatespeed);
    }

    /** 单位圆形旋转*/
    protected void circle(float circleLength){
        if(target == null) return;

        vec.set(target.getX() - x, target.getY() - y);

        if(vec.len() < circleLength){
            vec.rotate((circleLength - vec.len()) / circleLength * 180f);
        }

        vec.setLength(type.speed * Time.delta());

        velocity.add(vec);
    }

    /** 移动到指定目标点*/
    protected void moveToCore(PathTarget path){
        Tile tile = world.tileWorld(x, y);
        if (enable_isoInput) {
            tile = world.tile(x, y);
        }
        if(tile == null) return;
        Tile targetTile = pathfinder.getTargetTile(tile, team, path);

        if(tile == targetTile) return;

        velocity.add(vec.trns(angleTo(targetTile), type.speed * Time.delta()));
        if(Units.invalidateTarget(target, this)){
            rotation = Mathf.slerpDelta(rotation, baseRotation, type.rotatespeed);
        }
    }

    /** 远离核心*/
    protected void moveAwayFromCore(){
        Team enemy = null;
        for(Team team : team.enemies()){
            if(team.active()){
                enemy = team;
                break;
            }
        }

        if(enemy == null){
            for(Team team : team.enemies()){
                enemy = team;
                break;
            }
        }

        if(enemy == null) return;

        Tile tile = world.tileWorld(x, y);
        if (enable_isoInput) {
            tile = world.tile(x, y);
        }
        if(tile == null) return;
        Tile targetTile = pathfinder.getTargetTile(tile, enemy, PathTarget.enemyCores);
        TileEntity core = getClosestCore();

        if(tile == targetTile || core == null || dst(core) < 120f) return;

        velocity.add(vec.trns(angleTo(targetTile), type.speed * Time.delta()));
        rotation = Mathf.slerpDelta(rotation, baseRotation, type.rotatespeed);
    }


    // zones add begon
    /** 单位执行运输的块实体*/
    private T storageEntity;
    /** 单位起始块实体*/
    private T parentEntity;
    /** 单位可执行操作完毕, 成功或失败. */
    protected boolean isOver = false;
    /** 移除状态*/
    private boolean isRemove = false;
    private boolean initPath = false;
    private WorkerType workerType;
    /** 是否重新探测移动目标*/
    private boolean searchTarget = false;
    /** 无效目标寄存器*/
    private Array<Tile> disableTarget = new Array<>(8);
    /** 获取对象池对象, 并初始化*/
    public void obtain(@ArcAnnotate.NonNull WorkerType type, T parententity, Array<ItemStack> needItem) {
        this.parentEntity = parententity;
        this.workerType = type;
        if (needItem != null)
            this.getItems.addAll(needItem);

        Tile storageTile = null;
        switch (type) {
            case TRANSPORT:
                for (ItemStack itemStack : needItem) {
                    unitItems.add(itemStack.item, itemStack.amount);
                }
            case GETTING:
                startState = gettingAndTransport;
                storageTile = getStorageTile();
                break;

            case PATROL:
                startState = patrol;
                break;

            default:
                throw new RuntimeException("unknow type exception!");
        }

        getPath(storageTile);
    }
    public void obtain(WorkerType type) {
        this.obtain(type, null, null);
    }

    private void getPath(Tile targetTile) {
        initPath = false;
        curMoveTarget.set(Vec2.ZERO);
        aStarPath.clear();
        storageEntity = null;
        Time.run(FinalCons.second * 1, () -> {
            searchTarget = (aStarPath.getCount() == 0 && workerType != PATROL);
            if (isOver && searchTarget)
                isRemove = true;

            if (searchTarget) {
                isOver = addDisableTarget(targetTile);
                if (isOver && workerType == TRANSPORT)
                    isRemove = true;
            }

            if (isRemove) searchTarget = false;
        } );      // 目标点无法到达重新探测移动目标
//        Time.run(FinalCons.second * 2, () -> fail = (aStarPath.getCount() == 0 && workerType != PATROL) );

        if (targetTile != null && targetTile.ent() != null && !targetTile.ent().isDead()) {
            storageEntity = targetTile.ent();
            Vars.pathfinder.getPathListNoLastNode(world.tile(x, y), targetTile, aStarPath);
        }
    }

    private void initPath() {
        initPath = true;
        targets = new Array.ArrayIterable<>(aStarPath.nodes).iterator();
        targets.next(); // consume src position
        if (targets.hasNext()) {
            curMoveTarget.set(targets.next().getPosition());
        } else {
            curMoveTarget.set(lastPosition());
        }
    }

    private Tile getStorageTile() {
        if (getItems.size == 0)   // safer code
            return null;

        if (workerType == GETTING) {
            if (isOver)
                return parentEntity.tile;
            else
                return systemWorker.getGettingTile(BlockUnit.this, getItems.first());
        }
        else
            return systemWorker.getTransportTile(BlockUnit.this, getItems.first());
    }

    /** 添加无效目标Tile*/
    private int disableTargetTimer, disableTargetTimerMax = 4;
    private boolean addDisableTarget(Tile targetTile) {
        if (targetTile != null && !disableTarget.contains(targetTile))
            disableTarget.add(targetTile);
        return ++disableTargetTimer > disableTargetTimerMax;
    }

    /** 验证目标是否有效*/
    private boolean validation(Tile target) {
        for (Tile tile : disableTarget) {
            if (tile.x == target.x && tile.y == target.y) {
                return true;
            }
        }
        return false;
    }

    private Array<Tile> returnArray = new Array<>();
    public Array<Tile> getNoDisableTarget(Array<Tile> source) {
        returnArray.clear();
        for (Tile tile : source) {
            if ( !validation(tile))
                returnArray.add(tile);
        }
        return returnArray;
    }

//    /** 仓库获取物品*/
//    private Tile getWarehouse() {
//
//    }
//    /** 粮仓获取物品*/

    /**  向Block倾泻物品
     * @return true 倾泻完所有物品
     * */
    private boolean dumpItem(T blockEntity) {
        for (Item _item : systemItems.getAllAccept()) {
            if (unitItems.get(_item) == 0)  continue;

            int _amount = unitItems.get(_item);
            int acceptCount;
            if (blockEntity instanceof StorageBlockIso.StorageBlockIsoEntity)   // 物品倾泻入仓库
                acceptCount = ((StorageBlockIso.StorageBlockIsoEntity) blockEntity).acceptCount(_item, _amount);
            else {// 强制添加物品到需求对象
//                acceptCount = Math.min(_amount, blockEntity.block.getMaximumAccepted(blockEntity.tile, null) - blockEntity.items.total());
                acceptCount = _amount;
            }

//            blockEntity.items.add(_item, acceptCount);
            blockEntity.block.handleStack(_item, acceptCount, blockEntity.tile, null);
            if(workerType == TRANSPORT)
                parentEntity.items.remove(_item, acceptCount);
            unitItems.add(_item, -acceptCount);
        }

        return unitItems.total() == 0;
    }

    /** 从Block获取物品*/
    private boolean acceptItem(T blockEntity) {
        if ( blockEntity.isDead())   return true;

        for (int i = 0; i < getItems.size; i++) {
            ItemStack itemStack = getItems.get(i);

            if ( blockEntity.items.has(itemStack.item)) {
                int getCount = Math.min(blockEntity.items.get(itemStack.item), itemStack.amount);

//                BlockUnit.this.addItem(itemStack.item, getCount);
                unitItems.add(itemStack.item, getCount);
                blockEntity.items.remove(itemStack.item, getCount);
                itemStack.amount -= getCount;
                if (itemStack.amount <= 0) {
                    getItems.remove(i);
                    i--;
                }
            }
        }

        return getItems.size == 0;
    }

    /** 获取, 到达目标点事件*/
    private void gettingTargetEvent() {
        // 到达获取目标点
        if (storageEntity != parentEntity && workerType == GETTING) {
            final boolean getItemFinish = !acceptItem(storageEntity);
            Time.run(FinalCons.second * 1, () -> {
                searchTarget = getItemFinish;
                if ( !searchTarget)
                    getPath(parentEntity.tile);
            });
        }
        // 到达需要目标点
        else {
            searchTarget = !dumpItem(storageEntity);    // or  spawnEntity
            if ( !searchTarget)
                Time.run(FinalCons.second * 1, () -> isRemove = true);
        }
    }

    /** 对象回收重置*/
    @Override
    public void reset() {
        isOver = isRemove = false;
        searchTarget = false;
        disableTargetTimer = 0;
        disableTarget.clear();
        getItems.clear();
        unitItems.clear();
        // market begon
        marketItems.clear();
        // market end
    }
    // zones add end

    static public enum WorkerType {
        /** 运输物品*/
        TRANSPORT,
        /** 获取物品*/
        GETTING,
        /** 工作巡逻状态*/
        PATROL,
    }
}
