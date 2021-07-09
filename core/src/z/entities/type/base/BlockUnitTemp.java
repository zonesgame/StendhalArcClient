//package z.entities.type.base;
//
//import java.util.Iterator;
//
//import arc.graphics.Color;
//import arc.graphics.g2d.Draw;
//import arc.graphics.g2d.Fill;
//import arc.java.util.Collections;
//import arc.math.Angles;
//import arc.math.Mathf;
//import arc.math.geom.Vec2;
//import arc.struct.Array;
//import arc.util.ArcAnnotate.Nullable;
//import arc.util.Time;
//import mindustry.Vars;
//import mindustry.ai.Pathfinder;
//import z.ai.astar.FlatTiledNode;
//import z.ai.astar.TiledSmoothableGraphPath;
//import mindustry.entities.Predict;
//import mindustry.entities.Units;
//import mindustry.entities.bullet.BulletType;
//import mindustry.entities.type.BaseUnit;
//import mindustry.entities.type.TileEntity;
//import mindustry.entities.units.UnitCommand;
//import mindustry.entities.units.UnitState;
//import mindustry.game.Team;
//import mindustry.type.Weapon;
//import mindustry.world.Tile;
//import mindustry.world.blocks.Floor;
//import mindustry.world.meta.BlockFlag;
//
//import static mindustry.Vars.indexer;
//import static mindustry.Vars.pathfinder;
//import static mindustry.Vars.tilesize;
//import static mindustry.Vars.world;
//
///**
// *  三国对象地面单位处理类
// * */
//public class BlockUnitTemp extends BaseUnit {
//    private static final Vec2 temp2 = new Vec2();
//    protected static Vec2 vec = new Vec2();
//
//    /** 行走时间*/
//    protected float walkTime;
//    /** 受困时间*/
//    protected float stuckTime;
//    /** 基础角度*/
//    protected float baseRotation;
//
//    // zones add begon
//    /** 单位选择状态*/
//    private boolean selectState = false;
//    /** AStar算法移动目标点*/
//    @Nullable
//    @Deprecated
//    private Tile targetAStar = null;     // or TargetTrait   Tile
//    /** 当前移动目标点*/
//    private Vec2 curMoveTarget = new Vec2();
//    /** 移动路径点*/
//    private Iterator<FlatTiledNode> targets = Collections.emptyIterator();
//    /** 是否开始线程路径计算*/
//    private boolean searchPath = false;
//    private int moveStepIndex = 0;
//    /** AStar路径数据*/
//    protected TiledSmoothableGraphPath<FlatTiledNode> aStarPath = new TiledSmoothableGraphPath<>();
//    protected boolean executeAStarMove = false;
//
//    // zones add end
//
//    /** 攻击状态*/
//    public final UnitState
//
//    /** 测试状态*/
//    testState = new UnitState(){
//        @Override
//        public void entered(){
////            target = null;
//        }
//
//        @Override
//        public void exited() {
//        }
//
//        @Override
//        public void update(){
//
//
//            //  检测线程路径计算是否完成
//            if (searchPath && aStarPath.getCount() > 0) {
//                searchPath = false;
//
//                targets = new Array.ArrayIterable<>(aStarPath.nodes).iterator();
//                targets.next(); // consume src position
//                if (targets.hasNext()) {
//                    curMoveTarget.set(targets.next().getPosition()).scl(tilesize);
//                } else {
//                    curMoveTarget.set(lastPosition()).scl(tilesize);
//                }
//
//                System.out.println("起始点:   " + SGGroundUnit.this.tileX() + "  " + SGGroundUnit.this.tileY());
//                System.out.println("目标点:  " + curMoveTarget.x + " " + curMoveTarget.y);
//                System.out.println("路径初始化完毕     " + aStarPath.getCount());
//                for (FlatTiledNode node : aStarPath.nodes) {
//                    System.out.println(node.x + " " + node.y);
//                }
//                System.out.println("____________________________________end");
//            }
//            // 线程路径数据初始化 end
//
//            if (curMoveTarget.isZero()) return;
//            if (lastPosition().epsilonEquals(curMoveTarget)) {      // 到达目标点
//                return;
////                if (!targets.hasNext()) {
////                    aStarPath.clear();
//////                    if (isMoving(mode)) setMode(getNeutralMode());
////                    return;
////                }
//            }
//
//            velocity.add(vec.trns(angleTo(curMoveTarget), type.speed * Time.delta()));
//            if(Units.invalidateTarget(target, SGGroundUnit.this) || true){
//                rotation = Mathf.slerpDelta(rotation, baseRotation, type.rotatespeed);
//            }
//
//            if (lastPosition().dst(curMoveTarget) < 1) {
//                if (targets.hasNext()) {
//                    curMoveTarget.set(targets.next().getPosition()).scl(tilesize);
//                }
//            }
//
//        }
//    },
//
//    /** 团结状态*/
//    rally = new UnitState(){
//        public void update(){
//            Tile target = getClosest(BlockFlag.rally);
//
//            if(target != null && dst(target) > 80f){
//                moveToCore(Pathfinder.PathTarget.rallyPoints);
//            }
//        }
//    },
//    /** 撤退状态*/
//    retreat = new UnitState(){
//        public void entered(){
//            target = null;
//        }
//
//        public void update(){
//            moveAwayFromCore();
//        }
//    };
//
//    @Override
//    public void onCommand(UnitCommand command){
//        state.set(command == UnitCommand.retreat ? retreat :
//                command == UnitCommand.attack ? testState :
//                        command == UnitCommand.rally ? rally :
//                                null);
//    }
//
//    @Override
//    public void interpolate(){
//        super.interpolate();
//
//        if(interpolator.values.length > 1){
//            baseRotation = interpolator.values[1];
//        }
//    }
//
//    @Override
//    public void move(float x, float y){
//        float dst = Mathf.dst(x, y);
//        if(dst > 0.01f){
//            baseRotation = Mathf.slerp(baseRotation, Mathf.angle(x, y), type.baseRotateSpeed * (dst / type.speed));
//        }
//        super.move(x, y);
//    }
//
//    @Override
//    public UnitState getStartState(){
////        return attack;
//        return testState;
//    }
//
//    @Override
//    public void update(){
//        super.update();
//
//        stuckTime = !vec.set(x, y).sub(lastPosition()).isZero(0.0001f) ? 0f : stuckTime + Time.delta();
//
//        if(!velocity.isZero()){
//            baseRotation = Mathf.slerpDelta(baseRotation, velocity.angle(), 0.05f);
//        }
//
//        if(stuckTime < 1f){
//            walkTime += Time.delta();
//        }
//    }
//
//    @Override
//    public Weapon getWeapon(){
//        return type.weapon;
//    }
//
//    @Override
//    public void draw(){
//        Draw.mixcol(Color.white, hitTime / hitDuration);
//
//        float ft = Mathf.sin(walkTime * type.speed * 5f, 6f, 2f + type.hitsize / 15f);
//
//        Floor floor = getFloorOn();
//
//        if(floor.isLiquid){
//            Draw.color(Color.white, floor.color, 0.5f);
//        }
//
//        for(int i : Mathf.signs){
//            Draw.rect(type.legRegion,
//                    x + Angles.trnsx(baseRotation, ft * i),
//                    y + Angles.trnsy(baseRotation, ft * i),
//                    type.legRegion.getWidth() * i * Draw.scl, type.legRegion.getHeight() * Draw.scl - Mathf.clamp(ft * i, 0, 2), baseRotation - 90);
//        }
//
//        if(floor.isLiquid){
//            Draw.color(Color.white, floor.color, drownTime * 0.4f);
//        }else{
//            Draw.color(Color.white);
//        }
//
//        Draw.rect(type.baseRegion, x, y, baseRotation - 90);
//
//        Draw.rect(type.region, x, y, rotation - 90);
//
//        for(int i : Mathf.signs){
//            float tra = rotation - 90, trY = -type.weapon.getRecoil(this, i > 0) + type.weaponOffsetY;
//            float w = -i * type.weapon.region.getWidth() * Draw.scl;
//            Draw.rect(type.weapon.region,
//                    x + Angles.trnsx(tra, getWeapon().width * i, trY),
//                    y + Angles.trnsy(tra, getWeapon().width * i, trY), w, type.weapon.region.getHeight() * Draw.scl, rotation - 90);
//        }
//
//        // zones add begon
//        if (selectState) {
//            Draw.color(Color.darkGray);
//            Fill.rect(x, y, drawSize() * Draw.scl * 0.7f, 8 * Draw.scl);
//            Draw.color(Color.scarlet);
//            Fill.rect(x, y, drawSize() * Draw.scl * 0.7f, 8 * Draw.scl);
//        }
//        // zones add end
//
//        Draw.mixcol();
//    }
//
//    @Override
//    public void behavior(){
//
//        if(!Units.invalidateTarget(target, this)){
//            if(dst(target) < getWeapon().bullet.range()){
//
//                rotate(angleTo(target));
//
//                if(Angles.near(angleTo(target), rotation, 13f)){
//                    BulletType ammo = getWeapon().bullet;
//
//                    Vec2 to = Predict.intercept(BlockUnit.this, target, ammo.speed);
//
//                    getWeapon().update(BlockUnit.this, to.x, to.y);
//                }
//            }
//        }
//    }
//
//    @Override
//    public void updateTargeting(){
//        super.updateTargeting();
//
//        if(Units.invalidateTarget(target, team, x, y, Float.MAX_VALUE)){
//            target = null;
//        }
//
//        if(retarget()){
//            targetClosest();
//        }
//    }
//
//    // zones add begon
//    /** AStar算法移动到指定目标点.要求必须忽略Unit之间的碰撞.*/
//    protected void moveTo(Tile tile){
//        if (targetAStar != tile && aStarPath.getCount() == 0) {
//            Tile posTile = world.tileWorld(x, y);
//            if (posTile == null || tile == null) return;
//
//            targetAStar = tile;
//            pathfinder.getPathListNoLastNode(posTile, targetAStar, aStarPath);
//            searchPath = true;
//            System.out.println("start path search ++++++++++++    " + tile.x + "  " + tile.y);
//        }
//
//        //  检测线程路径计算是否完成
//        if (searchPath && aStarPath.getCount() > 0) {
//            System.out.println("路径探测成功------------------------" + tile.x + " " + tile.y + "   path node count " + aStarPath.getCount());
//            {
//                for (int _i = 0; _i < aStarPath.getCount(); _i++) {
//                    FlatTiledNode _node = aStarPath.get(_i);
//                    System.out.println(_node.x + "   ---    " + _node.y);
//                }
//                System.out.println("Path print end ___+_+_+_1");
//            }
//            searchPath = false;
//
//            targets = new Array.ArrayIterable<>(aStarPath.nodes).iterator();
//            targets.next(); // consume src position
//            if (targets.hasNext()) {
//                curMoveTarget.set(targets.next().getPosition());//.scl(tilesize);
//            } else {
//                curMoveTarget.set(lastPosition());//.scl(tilesize);
//            }
//            System.out.println("move node   " + curMoveTarget.x + " +++ " + curMoveTarget.y);
//        }
//        // 线程路径数据初始化 end
//
//        if (true) {     // 改进方法
//            Tile curTile = world.tileWorld(x, y);
//            if(curTile == null) return;
//            if (curMoveTarget.x == curTile.x && curMoveTarget.y == curTile.y && targets.hasNext()) {
//                Vec2 nextNode = targets.next().getPosition();
//                curMoveTarget.set(nextNode);
//                System.out.println("move node   " + curMoveTarget.x + " +++ " + curMoveTarget.y);
//                if ( !nextNode.isZero()) {
//
//                }
//            }
//
//            if ( !targets.hasNext())
//                aStarPath.clear();
//
//            Tile targetTile = world.tile((int)curMoveTarget.x, (int)curMoveTarget.y);
//            if(curTile == targetTile) return;
//
//            velocity.add(vec.trns(angleTo(targetTile), type.speed * Time.delta()));
//            if(Units.invalidateTarget(target, this) || true){
//                rotation = Mathf.slerpDelta(rotation, baseRotation, type.rotatespeed);
//            }
//
//            return;
//        }
//
//        if (curMoveTarget.isZero()) return;
//        if (lastPosition().epsilonEquals(curMoveTarget)) {      // 到达目标点
//            return;
////                if (!targets.hasNext()) {
////                    aStarPath.clear();
//////                    if (isMoving(mode)) setMode(getNeutralMode());
////                    return;
////                }
//        }
//
//        velocity.add(vec.trns(angleTo(curMoveTarget), type.speed * Time.delta()));
//        if(Units.invalidateTarget(target, BlockUnit.this) || true){
//            rotation = Mathf.slerpDelta(rotation, baseRotation, type.rotatespeed);
//        }
//
//        if (lastPosition().dst(curMoveTarget) < 1) {
//            if (targets.hasNext()) {
//                curMoveTarget.set(targets.next().getPosition()).scl(tilesize);
//            }
//        }
//    }
//    // zones add end
//
//    /** 巡逻*/
//    protected void patrol(){
//        vec.trns(baseRotation, type.speed * Time.delta());
//        velocity.add(vec.x, vec.y);
//        vec.trns(baseRotation, type.hitsizeTile * 5);
//        Tile tile = world.tileWorld(x + vec.x, y + vec.y);
//        if((tile == null || tile.solid() || tile.floor().drownTime > 0 || tile.floor().isLiquid) || stuckTime > 10f){
//            baseRotation += Mathf.sign(id % 2 - 0.5f) * Time.delta() * 3f;
//        }
//
//        rotation = Mathf.slerpDelta(rotation, velocity.angle(), type.rotatespeed);
//    }
//
//    /** 循环*/
//    protected void circle(float circleLength){
//        if(target == null) return;
//
//        vec.set(target.getX() - x, target.getY() - y);
//
//        if(vec.len() < circleLength){
//            vec.rotate((circleLength - vec.len()) / circleLength * 180f);
//        }
//
//        vec.setLength(type.speed * Time.delta());
//
//        velocity.add(vec);
//    }
//
//    /** 移动到核心*/
//    protected void moveToCore(Pathfinder.PathTarget path){
//        Tile tile = world.tileWorld(x, y);
//        if(tile == null) return;
//        Tile targetTile = pathfinder.getTargetTile(tile, team, path);
//
//        if(tile == targetTile) return;
//
//        velocity.add(vec.trns(angleTo(targetTile), type.speed * Time.delta()));
//        if(Units.invalidateTarget(target, this)){
//            rotation = Mathf.slerpDelta(rotation, baseRotation, type.rotatespeed);
//        }
//        // zones add begon
//        // zones add end
//    }
//
//    /** 远离核心*/
//    protected void moveAwayFromCore(){
//        Team enemy = null;
//        for(Team team : team.enemies()){
//            if(team.active()){
//                enemy = team;
//                break;
//            }
//        }
//
//        if(enemy == null){
//            for(Team team : team.enemies()){
//                enemy = team;
//                break;
//            }
//        }
//
//        if(enemy == null) return;
//
//        Tile tile = world.tileWorld(x, y);
//        if(tile == null) return;
//        Tile targetTile = pathfinder.getTargetTile(tile, enemy, Pathfinder.PathTarget.enemyCores);
//        TileEntity core = getClosestCore();
//
//        if(tile == targetTile || core == null || dst(core) < 120f) return;
//
//        velocity.add(vec.trns(angleTo(targetTile), type.speed * Time.delta()));
//        rotation = Mathf.slerpDelta(rotation, baseRotation, type.rotatespeed);
//    }
//
//    // zones add function begon
//    @Override
//    public void select(boolean select) {
//        this.selectState = select;
//    }
//
//    @Override
//    public void setMoveTarget(Tile targetTile) {
////        if (true) {
////            indexer.moveIndexer = targetTile;
////            return;
////        }
//
//        targetAStar = targetTile;
//        Tile tile = world.tileWorld(x, y);
//        if (targetAStar == null || tile == null) return;
//
//        pathfinder.getPathList(tile, targetAStar, aStarPath);
//        searchPath = true;
//    }
//
//    //    @Override
////    public void unSelect() {
////        System.out.println("unit unSelect,,,,,,");
////    }
//    // zones add function end
//}
