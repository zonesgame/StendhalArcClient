package z.world.blocks.defense.turrets;

import arc.func.Cons;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Rect;
import arc.math.geom.Vec2;
import arc.scene.ui.layout.Table;
import arc.struct.Array;
import arc.struct.ObjectMap;
import arc.util.Time;
import arc.z.util.ZonesAnnotate.ZAdd;
import mindustry.Vars;
import mindustry.entities.Predict;
import mindustry.entities.bullet.BulletType;
import mindustry.entities.type.Bullet;
import mindustry.entities.type.TileEntity;
import mindustry.graphics.Layer;
import mindustry.type.ItemStack;
import mindustry.world.Tile;
import z.entities.ani.SpriteAniControl;
import z.entities.type.WorkerTileEntity;
import z.tools.serialize.XmlSerialize;
import z.ui.SettingDisplay;
import z.world.blocks.OperationAction;

import static mindustry.Vars.control;
import static mindustry.Vars.tilesize;
import static z.debug.ZDebug.disable_packLoad;
import static z.debug.ZDebug.enable_isoInput;

/**
 *  qqtx箭塔 物品炮塔块.
 * */
public class StoneThrowerTurretIso extends ItemTurretIso{
    public float shotWidth = enable_isoInput ? 2f / tilesize : 2f;

    // zones add begon
    /***/
    private Cons<Tile> drawerEffect = null;
    /***/
    private TextureRegion[][][] regions;         //
    /***/
    private Rect[][][] rects;    //
    /***/
    private Rect rect;
    /** 背景数据*/
    private TextureRegion bgRegion;
    /***/
    private Rect bgRect;
    // zones add end

    public float velocityInaccuracy = 0f;

    public StoneThrowerTurretIso(String name){
        super(name);
        targetAir = false;
//        shots = 2;
        // zones add begon
        this.layer = null;
        this.layerBg = Layer.background;
        // zones add end
    }

    private String configFile;
    public StoneThrowerTurretIso(String name, String configFile){
        this(name);
        this.configFile = configFile;
        Vars.xmlSerialize.loadBlockConfig(configFile, this);
    }

//    @Override
//    public void setStats(){
//        super.setStats();
//
////        stats.remove(BlockStat.reload);
////        stats.add(BlockStat.reload, 60f / reload[0], StatUnit.none);
//    }

    @Override
    protected void shoot(Tile tile, BulletType ammo){
        TurretIsoEntity entity = tile.ent();

        entity.recoil = recoil;
        entity.heat = 1f;

        BulletType type = peekAmmo(tile);

//        tr.trns(entity.rotation, size * tilesize / 2);
        tr.trns(entity.rotation - 90, 1, 1);
//        tr.trns(entity.rotation - 90, shotWidth * i, size * tileunit / 2f);

        Vec2 predict = Predict.intercept(tile, entity.target, type.speed);

        float dst = entity.dst(predict.x, predict.y);
        float maxTraveled = type.lifetime * type.speed;

        for(int i = 0; i < shots[entity.level()]; i++){
//            Bullet.create(ammo, tile.entity, tile.getTeam(), tile.drawx() + tr.x, tile.drawy() + tr.y,
//                    entity.rotation + Mathf.range(inaccuracy[entity.level()] + type.inaccuracy), 1f + Mathf.range(velocityInaccuracy), (dst / maxTraveled));
            Bullet.create(ammo, tile.entity, tile.getTeam(), tile.getX() + tr.x, tile.getY() + tr.y,
                    entity.rotation + Mathf.range(inaccuracy[entity.level()] + type.inaccuracy), 1f + Mathf.range(velocityInaccuracy), (dst / maxTraveled));
        }

        effects(tile);
        useAmmo(tile);
    }

//    @Override
//    protected void shoot(Tile tile, BulletType ammo){
//        TurretIsoEntity entity = tile.ent();
//        entity.shots++;
//
//        int i = Mathf.signs[entity.shots % 2];
//
//        if (enable_isoInput) {
//            tr.trns(entity.rotation - 90, shotWidth * i, size * tileunit / 2f);
//        } else {
//            tr.trns(entity.rotation - 90, shotWidth * i, size * tilesize / 2);
//        }
//        bullet(tile, ammo, entity.rotation + Mathf.range(inaccuracy[entity.level()]));
//
//        effects(tile);
//        useAmmo(tile);
//    }

    // zones add begon
    @Override
    public void load(){
        super.load();
        // zones add begon
        if (disable_packLoad)   return;
        ObjectMap dataPool = Vars.xmlSerialize.loadBlockAnimation(configFile);
        regions = (TextureRegion[][][]) dataPool.get(XmlSerialize.qqtxRegions);
        rects = (Rect[][][]) dataPool.get(XmlSerialize.qqtxRects);
        bgRegion = (TextureRegion) dataPool.get(XmlSerialize.qqtxBG);
        bgRect = (Rect) dataPool.get(XmlSerialize.qqtxBGR);
        dataPool.clear();
        // zones add end
    }

    @Override
    public void update(Tile tile) {
        super.update(tile);

        WorkerTileEntity entity = tile.ent();
        if(!entity.setWorkState || !entity.working)   // 无法执行工作 退出
            return;

        // 更新获取生产物品. zones add begon
        if(tile.entity.timer.get(timerWorker, workerTime) && suppliesAmmo(tile) && entity.offerWorker()){
            tryObtain(tile, getObtainItems(entity));
        }
    }

    /** 是否需要补给弹药*/
    @ZAdd
    private boolean suppliesAmmo(Tile tile){
        ItemTurretIsoEntity entity = tile.ent();
        ItemTurretIso block = (ItemTurretIso) tile.block();
//        return entity.ammo.size == 0 || entity.ammo.peek().amount <= ammoPerShot * storeAmmoMultiplier[tile.entity.level()];
        return entity.totalAmmo < block.supplyAmmo[entity.level()] && entity.totalAmmo < block.maxAmmo[entity.level()];
    }

    @ZAdd
    private Array<ItemStack> getObtainItems(TileEntity entity) {
        Array<ItemStack> obtainItems = super.getDumpItems(entity);

        ItemTurretIsoEntity itemTurretEntity = (ItemTurretIsoEntity) entity;
        int carryAmount = Mathf.ceil((maxAmmo[entity.level()] - itemTurretEntity.totalAmmo) / (float)ammo[entity.level()].get(itemTurretEntity.curAmmo).ammoMultiplier * extendAmmoMultiplier[entity.level()]);
        obtainItems.add(new ItemStack(itemTurretEntity.curAmmo, Math.min(carryItemObtain[entity.level()], carryAmount)));
        return obtainItems;
    }

    @Override
    public void draw(Tile tile){
        Vec2 pos = Vec2.TEMP2;
//        ISOUtils.tileToWorldCoordsCenter(tile.x, tile.y, size, size, pos);
        pos.set(tile.drawxIso(), tile.drawyIso());

//        float scale = 1;
//        float dx = pos.x - bgRect.x;
//        float dy = pos.y - bgRect.y;
//        Draw.rectGdx(bgRegion, dx, dy, bgRect.width, bgRect.height);

        ItemTurretIsoEntity entity = tile.ent();
        if (entity.isDrag)  return;     // 编辑移动状态

        {
            float rotation = entity.rotation;
            rotation = 360 - rotation + 45;

            int stateIndex;
            int dirIndex = SpriteAniControl.getFrameDirQQTX(rotation);
            int frame;

            if (entity.reload == 0) {       // idle state
                stateIndex = entity.level() * 2;
                frame = SpriteAniControl.getFrameIndexFromScale(regions[stateIndex][dirIndex].length, true, Time.globalTime() * 0.03f);
            }
            else {      // attak state
                stateIndex = entity.level() * 2 + 1;
                frame = SpriteAniControl.getFrameIndexFromScale(regions[stateIndex][dirIndex].length, false, entity.reload / reload[entity.level()]);
            }

            TextureRegion region = regions[stateIndex][dirIndex][frame];
            Rect rect = rects[stateIndex][dirIndex][frame];
//                Draw.rectGdxOffset(region, x, y, -regionCenterpoint.x, -regionCenterpoint.y);
            float x = tile.drawxIso();
            float y = tile.drawyIso();
//                Draw.rectGdx(region, dx, dy, rect.width, rect.height);
            Draw.rectGdx(region, x, y, rect);
        }

        if (drawerEffect != null)
            drawerEffect.get(tile);
    }

    @ZAdd
    @Override
    public void drawBackground(Tile tile) {
//        Vec2 pos = Vec2.TEMP2;
////        ISOUtils.tileToWorldCoordsCenter(tile.x, tile.y, size, size, pos);
//        pos.set(tile.drawxIso(), tile.drawyIso());
//
//        float scale = 1;
//        float dx = pos.x - bgRect.x;
//        float dy = pos.y - bgRect.y;
//        Draw.rectGdx(bgRegion, dx, dy, bgRect.width, bgRect.height);


        Vec2 pos = Vec2.TEMP2;
//        ISOUtils.tileToWorldCoordsCenter(tile.x, tile.y, size, size, pos);
        pos.set(tile.drawxIso(), tile.drawyIso());

//        float scale = 1;
//        float dx = pos.x - bgRect.x;
//        float dy = pos.y - bgRect.y;
        Draw.rectGdx(bgRegion, pos.x, pos.y, bgRect);
    }

    /* 添加弹药选择功能*/
    @ZAdd
    @Override
    public <T extends OperationAction> ObjectMap<OperationAction, Cons<T>> getOperationActions(TileEntity tileEntity) {
        ObjectMap<OperationAction, Cons<T>> operationActions = super.getOperationActions(tileEntity);
        // 添加配置功能
        operationActions.put(OperationAction.SETTING, nullValue -> {
            Vars.ui.settingComp.show(this, tileEntity);
            control.input.frag.config.hideConfig();
        });
        return operationActions;
    }

    @Override
    public void displayInfo(Table table){
        SettingDisplay.displayTurret(table, this, (ItemTurretIsoEntity) table.getUserObject());
    }
    // zones add end
}
