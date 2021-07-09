package mindustry.world;

import java.util.Arrays;

import arc.Core;
import arc.Graphics.Cursor;
import arc.Graphics.Cursor.SystemCursor;
import arc.audio.Sound;
import arc.func.Func;
import arc.func.Prov;
import arc.graphics.Color;
import arc.graphics.Pixmap;
import arc.graphics.g2d.BitmapFont;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.GlyphLayout;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.PixmapRegion;
import arc.graphics.g2d.TextureAtlas.AtlasRegion;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.math.geom.Rect;
import arc.math.geom.Vec2;
import arc.scene.ui.layout.Scl;
import arc.scene.ui.layout.Table;
import arc.struct.Array;
import arc.struct.EnumSet;
import arc.util.Align;
import arc.util.ArcAnnotate.Nullable;
import arc.util.Eachable;
import arc.util.Structs;
import arc.util.Time;
import arc.util.pooling.Pools;
import arc.z.util.ISOUtils;
import arc.z.util.ZonesAnnotate;
import arc.z.util.ZonesAnnotate.ZField;
import arc.z.util.ZonesAnnotate.ZMethod;
import mindustry.annotations.Annotations.CallSuper;
import mindustry.ctype.Content;
import mindustry.ctype.ContentType;
import mindustry.ctype.UnlockableContent;
import mindustry.entities.Damage;
import mindustry.entities.TargetPriority;
import mindustry.entities.effect.Puddle;
import mindustry.entities.effect.RubbleDecal;
import mindustry.entities.traits.BuilderTrait.BuildRequest;
import mindustry.entities.type.Bullet;
import mindustry.entities.type.Player;
import mindustry.entities.type.TileEntity;
import mindustry.entities.type.Unit;
import mindustry.gen.Sounds;
import mindustry.graphics.CacheLayer;
import mindustry.graphics.Layer;
import mindustry.graphics.MultiPacker;
import mindustry.graphics.MultiPacker.PageType;
import mindustry.graphics.Pal;
import mindustry.type.Category;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.type.Liquid;
import mindustry.ui.Bar;
import mindustry.ui.Cicon;
import mindustry.ui.ContentDisplay;
import mindustry.ui.Fonts;
import mindustry.world.blocks.Floor;
import mindustry.world.blocks.OverlayFloor;
import mindustry.world.blocks.power.PowerNode;
import mindustry.world.consumers.Consume;
import mindustry.world.consumers.ConsumeLiquid;
import mindustry.world.consumers.ConsumePower;
import mindustry.world.consumers.ConsumeType;
import mindustry.world.consumers.Consumers;
import mindustry.world.meta.Attribute;
import mindustry.world.meta.BlockFlag;
import mindustry.world.meta.BlockGroup;
import mindustry.world.meta.BlockStat;
import mindustry.world.meta.BuildVisibility;
import mindustry.world.meta.StatUnit;
import mindustry.world.meta.values.ItemListValue;
import z.debug.Strs;
import z.debug.assets.PackLoader;
import z.utils.ShapeRenderer;

import static arc.z.util.ISOUtils.TILE_HEIGHT50;
import static mindustry.Vars.content;
import static mindustry.Vars.headless;
import static mindustry.Vars.logic;
import static mindustry.Vars.net;
import static mindustry.Vars.player;
import static mindustry.Vars.renderer;
import static mindustry.Vars.tilesize;
import static mindustry.Vars.tileunit;
import static mindustry.Vars.world;
import static z.debug.ZDebug.disable_buildcost;
import static z.debug.ZDebug.enable_customBar;
import static z.debug.ZDebug.enable_isoInput;

/**
 *  块(解锁内容)
 * */
public class Block extends BlockStorage{
    /** 裂痕纹理数量*/
    public static final int crackRegions = 8, /** 最大裂痕尺寸*/maxCrackSize = 5;

    /** 当块包含瓦砾实体才更新.<p/>whether this block has a tile entity that updates */
    public boolean update;
    /** 块是否有生命, 是否可被摧毁.<p/>whether this block has health and can be destroyed */
    public boolean destructible;
    /** 是否可执行装卸工作.<p/>whether unloaders work on this block*/
    public boolean unloadable = true;
    /** 是否为固体.<p/>whether this is solid */
    public boolean solid;
    /** 是否可成为固体.<p/>whether this block CAN be solid. */
    public boolean solidifes;
    /** 是否可旋转.<p/>whether this is rotateable */
    public boolean rotate;
    /** 是否可右键拆除.<p/>whether you can break this with rightclick */
    public boolean breakable;
    /** 是否为损坏块.<p/>whether to add this block to brokenblocks */
    public boolean rebuildable = true;
    /** 是否可放置地板.<p/>whether this floor can be placed on. */
    public boolean placeableOn = true;
    /** 是否有绝缘性能.<p/>whether this block has insulating properties. */
    public boolean insulated = false;
    /** 瓦砾实体生命.<p/>tile entity health */
    @ZField
    public int[] health = {-1};     // default public int health;
    /** 基础爆炸性.<p/>base block explosiveness */
    public float baseExplosiveness = 0f;
    /** 是否可放在液体边缘.<p/>whether this block can be placed on edges of liquids. */
    public boolean floating = false;
    /** 块尺寸.<p/>multiblock size */
    public int size = 1;
    /** 是否绘制扩展.<p/>Whether to draw this block in the expanded draw range. */
    public boolean expanded = false;
    /** 使用最大定时器.<p/>Max of timers used. */
    public int timers = 0;
    /** 缓存图层, 仅用于绘制缓存图层.<p/>Cache layer. Only used for 'cached' rendering. */
    public CacheLayer cacheLayer = CacheLayer.normal;
    /** 特别标志, 如果是假的, 即使它被缓存, 也会在这个块下绘制.<p/>Special flag; if false, floor will be drawn under this block even if it is cached. */
    public boolean fillsTile = true;
    /** 额外图层1.<p/>Layer to draw extra stuff on. */
    public Layer layer = null;
    /** 额外图层2.<p/>Extra layer to draw extra extra stuff on. */
    public Layer layer2 = null;
    // zones add begon
    /** 背景绘制图层zones add*/
    public Layer layerBg = null;
    // zones add end
    /** 是否可以被替换.<p/>whether this block can be replaced in all cases */
    public boolean alwaysReplace = false;
    /** 块所属UI界面群组.<p/>The block group. Unless {@link #canReplace} is overriden, blocks in the same group can replace each other. */
    public BlockGroup group = BlockGroup.none;
    /** 块标志列表. 用于人工智能路径索引.<p/>List of block flags. Used for AI indexing. */
    public EnumSet<BlockFlag> flags = EnumSet.of();
    /** 目标优先级, 被敌人锁定使用.<p/>Targeting priority of this block, as seen by enemies.*/
    public TargetPriority priority = TargetPriority.base;
    /** 是否可点击配置选择.<p/>Whether the block can be tapped and selected to configure. */
    public boolean configurable;
    /** 是否包含点击事件.<p/>Whether this block consumes touchDown events when tapped. */
    public boolean consumesTap;
    /** 是否绘制液体光源Whether to draw the glow of the liquid for this block, if it has one. */
    public boolean drawLiquidLight = true;
    /** 配置界面所处位置是否需要转移.<p/>Whether the config is positional and needs to be shifted. */
    public boolean posConfig;
    /** 是否网络同步块.<p/>Whether to periodically sync this block across the network.*/
    public boolean sync;
    /** 块是否使用输送机型放置模式.<p/>Whether this block uses conveyor-type placement mode.*/
    public boolean conveyorPlacement;
    /**
     *  在小地图预览中显示的这个块的颜色.
     * 不要手动设置! 在大多数块加载时, 这是溢出的.<p/>
     * The color of this block when displayed on the minimap or map preview.
     * Do not set manually! This is overriden when loading for most blocks.
     */
    public Color color = new Color(0, 0, 0, 1);
    /** 单位是否锁定块.<p/>Whether units target this block. */
    public boolean targetable = true;
    /** 超驱动核心是否对块有影响.<p/>Whether the overdrive core has any effect on this block. */
    public boolean canOverdrive = true;
    /** 边界线图标颜色.<p/>Outlined icon color.*/
    public Color outlineColor = Color.valueOf("404049");
    /** 图标纹理是否添加边界线.<p/>Whether the icon region has an outline added. */
    public boolean outlineIcon = false;
    /** 块是否有阴影.<p/>Whether this block has a shadow under it. */
    public boolean hasShadow = true;
    /** 拆除音频.<p/>Sounds made when this block breaks.*/
    public Sound breakSound = Sounds.boom;

    /** 块活动音频.<p/>The sound that this block makes while active. One sound loop. Do not overuse.*/
    public Sound activeSound = Sounds.none;
    /** 块活动音频音量.<p/>Active sound base volume. */
    public float activeSoundVolume = 0.5f;

    /** 块空闲音频.<p/>The sound that this block makes while idle. Uses one sound loop for all blocks.*/
    public Sound idleSound = Sounds.none;
    /** 块空闲音频音量.<p/>Idle sound base volume. */
    public float idleSoundVolume = 0.5f;

    /** 建造块成本.<p/>Cost of constructing this block. */
    public ItemStack[] requirements = {};           // public ItemStack[] requirements = {};
    /** UI界面分类菜单.<p/>Category in place menu. */
    public Category category = Category.distribution;
    /** 建造这个块的成本, 不要直接修改!<p/>Cost of building this block; do not modify directly! */
    @ZField
    public float[] buildCost = {0};
    /** 这个块是否可见, 目前可是否以构建.<p/>Whether this block is visible and can currently be built. */
    public BuildVisibility buildVisibility = BuildVisibility.hidden;
    /** 建造速度倍数.<p/>Multiplier for speed of building this block. */
    public float buildCostMultiplier = 1f;
    /** 是否可即时传输.<p/>Whether this block has instant transfer.*/
    public boolean instantTransfer = false;
    /** 是否允许解锁*/
    public boolean alwaysUnlocked = false;          // default false

    /** 缓存纹理容器*/
    protected TextureRegion[] cacheRegions = {};
    /** 缓存纹理名称容器*/
    protected Array<String> cacheRegionStrings = new Array<>();
    /** 块提供瓦砾实体*/
    protected Prov<TileEntity> entityType = TileEntity::new;

    /** 临时瓦砾容器*/
    protected Array<Tile> tempTiles = new Array<>();
    /** 生成图标容器*/
    protected TextureRegion[] generatedIcons;
    /** 变体纹理容器*/
    protected TextureRegion[] variantRegions, /** 地图编辑界面变体纹理容器*/editorVariantRegions;
    /** 当前纹理和编辑图标*/
    protected TextureRegion region, editorIcon;

    /** 裂痕纹理容器*/
    protected static TextureRegion[][] cracks;

    /** 倾泻计时器ID.<p/>Dump timer ID.*/
    protected final int timerDump = timers++;
    /** 倾泻时间5 = 12 times/sec.<p/>How often to try dumping items in ticks, e.g. 5 = 12 times/sec*/
    protected /*final*/ int dumpTime = 5;       // zones editor default final

    // zones add begon
    /** 物品状态条是否显示最终控制器, zones扩展*/
    @ZField
    public boolean showItemsBar = true;
    // zones add end

    /**
     *  构建块
     * */
    public Block(String name){
        super(name);
        this.solid = false;
        // zones add begon
        if (buildName == null)
            buildName = name;
        // zones add end
    }

    /** 是否可拆除*/
    public boolean canBreak(Tile tile){
        return true;
    }

    /** 是否可建造*/
    public boolean isBuildable(){
        return buildVisibility != BuildVisibility.hidden && buildVisibility != BuildVisibility.debugOnly;
    }

    /** 是否静态*/
    public boolean isStatic(){
        return cacheLayer == CacheLayer.walls;
    }

    /** 是否临近也要进行移除*/
    public void onProximityRemoved(Tile tile){
        if(tile.entity.power != null){
            tile.block().powerGraphRemoved(tile);
        }
    }

    /** 是否临近也要进行添加*/
    public void onProximityAdded(Tile tile){
        if(tile.block().hasPower) tile.block().updatePowerGraph(tile);
    }

    /** 更新电力图表*/
    protected void updatePowerGraph(Tile tile){
        TileEntity entity = tile.ent();

        for(Tile other : getPowerConnections(tile, tempTiles)){
            if(other.entity.power != null){
                other.entity.power.graph.add(entity.power.graph);
            }
        }
    }

    /** 电力图表移除节点*/
    protected void powerGraphRemoved(Tile tile){
        if(tile.entity == null || tile.entity.power == null){
            return;
        }

        tile.entity.power.graph.remove(tile);
        for(int i = 0; i < tile.entity.power.links.size; i++){
            Tile other = world.tile(tile.entity.power.links.get(i));
            if(other != null && other.entity != null && other.entity.power != null){
                other.entity.power.links.removeValue(tile.pos());
            }
        }
    }

    /** 指定位置电力图表连接节点容器*/
    public Array<Tile> getPowerConnections(Tile tile, Array<Tile> out){
        out.clear();
        if(tile == null || tile.entity == null || tile.entity.power == null) return out;

        for(Tile other : tile.entity.proximity()){
            if(other != null && other.entity != null && other.entity.power != null
            && !(consumesPower && other.block().consumesPower && !outputsPower && !other.block().outputsPower)
            && !tile.entity.power.links.contains(other.pos())){
                out.add(other);
            }
        }

        for(int i = 0; i < tile.entity.power.links.size; i++){
            Tile link = world.tile(tile.entity.power.links.get(i));
            if(link != null && link.entity != null && link.entity.power != null) out.add(link);
        }
        return out;
    }

    /** 获取处理进度百分比*/
    protected float getProgressIncrease(TileEntity entity, float baseTime){
        return 1f / baseTime * entity.delta() * entity.efficiency();
    }

    /** @return 是否激活活动音频. whether this block should play its active sound.*/
    public boolean shouldActiveSound(Tile tile){
        return false;
    }

    /** @return 是否激活空闲音频. whether this block should play its idle sound.*/
    public boolean shouldIdleSound(Tile tile){
        return shouldConsume(tile);
    }

    /** 绘制扩展图层1(layer != null 执行)*/
    public void drawLayer(Tile tile){
    }

    /** 绘制扩展图层2(layer2 != null执行)*/
    public void drawLayer2(Tile tile){
    }

    /** 绘制裂痕*/
    public void drawCracks(Tile tile){
        if(!tile.entity.damaged() || size > maxCrackSize) return;
        int id = tile.pos();
        TextureRegion region = cracks[size - 1][Mathf.clamp((int)((1f - tile.entity.healthf()) * crackRegions), 0, crackRegions-1)];
        Draw.colorl(0.2f, 0.1f + (1f - tile.entity.healthf())* 0.6f);
        Draw.rect(region, tile.drawx(), tile.drawy(), (id%4)*90);
        Draw.color();
    }

    /** 绘制当光标在块上时显示的块覆盖.<p/>Draw the block overlay that is shown when a cursor is over the block. */
    public void drawSelect(Tile tile){
    }

    /** 当你在放置一个块的时候绘制出来.<p/>Drawn when you are placing a block. */
    public void drawPlace(int x, int y, int rotation, boolean valid){
    }

    /** 绘制放置文本*/
    public float drawPlaceText(String text, int x, int y, boolean valid){
        if(renderer.pixelator.enabled()) return 0;

        Color color = valid ? Pal.accent : Pal.remove;
        BitmapFont font = Fonts.outline;
        GlyphLayout layout = Pools.obtain(GlyphLayout.class, GlyphLayout::new);
        boolean ints = font.usesIntegerPositions();
        font.setUseIntegerPositions(false);
        font.getData().setScale(1f / 4f / Scl.scl(1f));
        layout.setText(font, text);

        float width = layout.width;

        font.setColor(color);
        float dx = x * tilesize + offset(), dy = y * tilesize + offset() + size * tilesize / 2f + 3;
        font.draw(text, dx, dy + layout.height + 1, Align.center);
        dy -= 1f;
        Lines.stroke(2f, Color.darkGray);
        Lines.line(dx - layout.width / 2f - 2f, dy, dx + layout.width / 2f + 1.5f, dy);
        Lines.stroke(1f, color);
        Lines.line(dx - layout.width / 2f - 2f, dy, dx + layout.width / 2f + 1.5f, dy);

        font.setUseIntegerPositions(ints);
        font.setColor(Color.white);
        font.getData().setScale(1f);
        Draw.reset();
        Pools.free(layout);
        return width;
    }

    /** 指定瓦砾绘制块*/
    public void draw(Tile tile){
        Draw.rect(region, tile.drawx(), tile.drawy(), rotate ? tile.rotation() * 90 : 0);
    }

    // zones add begon
    /** 绘制背景zones add*/
    public void drawBackground(Tile tile){
    }
    // zones add end

    /** 绘制光源*/
    public void drawLight(Tile tile){
        if(tile.entity != null && hasLiquids && drawLiquidLight && tile.entity.liquids.current().lightColor.a > 0.001f){
            drawLiquidLight(tile, tile.entity.liquids.current(), tile.entity.liquids.smoothAmount());
        }
    }

    /** 绘制流体光源*/
    public void drawLiquidLight(Tile tile, Liquid liquid, float amount){
        if(amount > 0.01f){
            Color color = liquid.lightColor;
            float fract = 1f;
            float opacity = color.a * fract;
            if(opacity > 0.001f){
                renderer.lights.add(tile.drawx(), tile.drawy(), size * 30f * fract, color, opacity);
            }
        }
    }

    /** 绘制队伍颜色标签*/
    public void drawTeam(Tile tile){
        Draw.color(tile.getTeam().color);
        Draw.rect("block-border", tile.drawx() - size * tilesize / 2f + 4, tile.drawy() - size * tilesize / 2f + 4);
        Draw.color();
    }

    /** 块放置调用, 玩家.<p/>Called after the block is placed by this client. */
    @CallSuper
    public void playerPlaced(Tile tile){

    }

    /** 方放置调用, 任何人.<p/>Called after the block is placed by anyone. */
    @CallSuper
    public void placed(Tile tile){
        if(net.client()) return;

        if((consumesPower && !outputsPower) || (!consumesPower && outputsPower)){
            int range = 10;
            tempTiles.clear();
            Geometry.circle(tile.x, tile.y, range, (x, y) -> {
                Tile other = world.ltile(x, y);
                if(other != null && other.block instanceof PowerNode && ((PowerNode)other.block).linkValid(other, tile) && !PowerNode.insulated(other, tile) && !other.entity.proximity().contains(tile) &&
                !(outputsPower && tile.entity.proximity().contains(p -> p.entity != null && p.entity.power != null && p.entity.power.graph == other.entity.power.graph))){
                    tempTiles.add(other);
                }
            });
            tempTiles.sort(Structs.comparingFloat(t -> t.dst2(tile)));
            if(!tempTiles.isEmpty()){
                Tile toLink = tempTiles.first();
                if(!toLink.entity.power.links.contains(tile.pos())){
                    toLink.configureAny(tile.pos());
                }
            }
        }
    }

    /** 指定位置移除块*/
    public void removed(Tile tile){
    }

    /** 有单位在Tile上时没帧调用.<p/>Called every frame a unit is on this tile. */
    public void unitOn(Tile tile, Unit unit){
    }

    /** 当一个在这个Tile上产生的单位被移除时调用.<p/>Called when a unit that spawned at this tile is removed. */
    public void unitRemoved(Tile tile, Unit unit){
    }

    /** 返回是否可以在指定的Tile上放置这个块.<p/>Returns whether ot not this block can be place on the specified tile. */
    public boolean canPlaceOn(Tile tile){
        return true;
    }

    /** 当一些内容被生成时调用. 如果适用, 这将打开内容.<p/>Call when some content is produced. This unlocks the content if it is applicable. */
    public void useContent(Tile tile, UnlockableContent content){
        //only unlocks content in zones
        if(!headless && tile.getTeam() == player.getTeam() && world.isZone()){
            logic.handleContent(content);
        }
    }

    /** 指定属性和*/
    public float sumAttribute(Attribute attr, int x, int y){
        Tile tile = world.tile(x, y);
        if(tile == null) return 0;
        float sum = 0;
        for(Tile other : tile.getLinkedTilesAs(this, tempTiles)){
            sum += other.floor().attributes.get(attr);
        }
        return sum;
    }

    /** 固体百分比*/
    public float percentSolid(int x, int y){
        Tile tile = world.tile(x, y);
        if(tile == null) return 0;
        float sum = 0;
        for(Tile other : tile.getLinkedTilesAs(this, tempTiles)){
            sum += !other.floor.isLiquid ? 1f : 0f;
        }
        return sum / size / size;
    }

    @Override
    public void displayInfo(Table table){
        ContentDisplay.displayBlock(table, this);
    }

    @Override
    public ContentType getContentType(){
        return ContentType.block;
    }

    /** Called after all blocks are created. */
    @Override
    @CallSuper
    public void init(){
        //initialize default health based on size
        if(health[0] == -1){
            health[0] = size * size * 40;
        }

        if ( !disable_buildcost) {
            buildCost[0] = 0f;
            for(ItemStack stack : requirements){
                buildCost[0] += stack.amount * stack.item.cost;
            }
            buildCost[0] *= buildCostMultiplier;
        }

//        if(consumes.has(ConsumeType.power)) hasPower = true;
//        if(consumes.has(ConsumeType.item)) hasItems = true;
//        if(consumes.has(ConsumeType.liquid)) hasLiquids = true;
        for (Consumers cons : consumes) {    // zones editor
            if(cons.has(ConsumeType.power)) hasPower = true;
            if(cons.has(ConsumeType.item)) hasItems = true;
            if(cons.has(ConsumeType.liquid)) hasLiquids = true;
        }

        setStats();
        setBars();

//        consumes.init();
        for (Consumers cons : consumes) {    // zones editor
            cons.init();
        }

//        if(!outputsPower && consumes.hasPower() && consumes.getPower().buffered){
//            throw new IllegalArgumentException("Consumer using buffered power: " + name);
//        }
        if(!outputsPower && consumes[0].hasPower() && consumes[0].getPower().buffered){
            throw new IllegalArgumentException("Consumer using buffered power: " + name);
        }
    }

    @Override
    public void load(){
        region = Core.atlas.find(name);

        cacheRegions = new TextureRegion[cacheRegionStrings.size];
        for(int i = 0; i < cacheRegions.length; i++){
            cacheRegions[i] = Core.atlas.find(cacheRegionStrings.get(i));
        }

        if(cracks == null || (cracks[0][0].getTexture() != null && cracks[0][0].getTexture().isDisposed())){
            cracks = new TextureRegion[maxCrackSize][crackRegions];
            for(int size = 1; size <= maxCrackSize; size++){
                for(int i = 0; i < crackRegions; i++){
                    cracks[size - 1][i] = Core.atlas.find("cracks-" + size + "-" + i);
                }
            }
        }
    }

    /** 添加一个缓存纹理, 获取一个id.<p/>Adds a region by name to be loaded, with the final name "{name}-suffix". Returns an ID to looks this region up by in {@link #reg(int)}. */
    protected int reg(String suffix){
        cacheRegionStrings.add(name + suffix);
        return cacheRegionStrings.size - 1;
    }

    /** 通过id获取缓存纹理.<p/>Returns an internally cached region by ID. */
    protected TextureRegion reg(int id){
        return cacheRegions[id];
    }

    /** 块点击.<p/>Called when the block is tapped. */
    public void tapped(Tile tile, Player player){

    }

    /** 当任意配置被应用到Tile时调用.<p/>Called when arbitrary configuration is applied to a tile. */
    public void configured(Tile tile, @Nullable Player player, int value){

    }

    /** 返回是否应该在这个块上显示光标.<p/>Returns whether or not a hand cursor should be shown over this block. */
    public Cursor getCursor(Tile tile){
        return configurable ? SystemCursor.hand : SystemCursor.arrow;
    }

    /** 构建配置ui<p/>
     * Called when this block is tapped to build a UI on the table.
     * {@link #configurable} must return true for this to be called.
     */
    public void buildConfiguration(Tile tile, Table table){
    }

    /** 更新配置UI位置, 后使table对齐.<p/>Update table alignment after configuring.*/
    public void updateTableAlign(Tile tile, Table table){
        if(enable_isoInput) {
            Vec2 pos = Core.input.mouseScreen(tile.drawxIso(), tile.drawyIso() + tile.block().size * TILE_HEIGHT50  + 8);   //
            table.setPosition(pos.x, pos.y, Align.bottom | Align.center);
        }
        else {
            Vec2 pos = Core.input.mouseScreen(tile.drawx(), tile.drawy() - tile.block().size * tilesize / 2f - 1);
            table.setPosition(pos.x, pos.y, Align.top);
        }
    }

    /** 当选这个Tile时, 调用另一个tile. 返回是否应该取消这个块.<p/>
     * Called when another tile is tapped while this block is selected.
     * Returns whether or not this block should be deselected.
     */
    public boolean onConfigureTileTapped(Tile tile, Tile other){
        return tile != other;
    }

    /** 返回这个配置ui是否应该显示, 是否有指定的玩家点击它.<p/>Returns whether this config menu should show when the specified player taps it. */
    public boolean shouldShowConfigure(Tile tile, Player player){
        return true;
    }

    /** 是否隐藏配置ui.<p/>Whether this configuration should be hidden now. Called every frame the config is open. */
    public boolean shouldHideConfigure(Tile tile, Player player){
        return false;
    }

    public boolean synthetic(){
        return update || destructible;
    }

    /** 绘制块配置状态*/
    public void drawConfigure(Tile tile){
        if(enable_isoInput) {
            Draw.color(Pal.accent);
            Lines.stroke(1f);
            ShapeRenderer.drawDiamond(tile.x, tile.y, tile.block.size);
//            Lines.square(tile.drawx(), tile.drawy(), tile.block().size * tilesize / 2f + 1f);
            Draw.reset();
        }
        else {
            Draw.color(Pal.accent);
            Lines.stroke(1f);
            Lines.square(tile.drawx(), tile.drawy(), tile.block().size * tilesize / 2f + 1f);
            Draw.reset();
        }
    }

    /** 设置统计数据*/
    public void setStats(){
        stats.add(BlockStat.size, "{0}x{0}", size);
        stats.add(BlockStat.health, health[0], StatUnit.none);
        if(isBuildable()){
            stats.add(BlockStat.buildTime, buildCost[0] / 60, StatUnit.seconds);
            stats.add(BlockStat.buildCost, new ItemListValue(false, requirements));
        }

        consumes[0].display(stats);

        // Note: Power stats are added by the consumers.
        if(hasLiquids) stats.add(BlockStat.liquidCapacity, liquidCapacity, StatUnit.liquidUnits);
        if(hasItems) stats.add(BlockStat.itemCapacity, itemCapacity[0], StatUnit.items);
    }

    /** 设置状态条*/
    public void setBars(){
        if(enable_customBar) {
//            String healthName = Core.bundle.get("blocks.health");
            bars.add("health", entity -> new Bar(() -> Core.bundle.format(Strs.str21, (int)entity.health()), () -> Pal.health, entity::healthf).blink(Color.white));
        } else {
            bars.add("health", entity -> new Bar("blocks.health", Pal.health, entity::healthf).blink(Color.white));
        }

        if(hasLiquids){
            Func<TileEntity, Liquid> current;
            if(consumes[0].has(ConsumeType.liquid) && consumes[0].get(ConsumeType.liquid) instanceof ConsumeLiquid){
                Liquid liquid = consumes[0].<ConsumeLiquid>get(ConsumeType.liquid).liquid;
                current = entity -> liquid;
            }else{
                current = entity -> entity.liquids.current();
            }
            bars.add("liquid", entity -> new Bar(() -> entity.liquids.get(current.get(entity)) <= 0.001f ? Core.bundle.get("bar.liquid") : current.get(entity).localizedName,
                    () -> current.get(entity).barColor(), () -> entity.liquids.get(current.get(entity)) / liquidCapacity));
        }

        if(hasPower && consumes[0].hasPower()){
            ConsumePower cons = consumes[0].getPower();
            boolean buffered = cons.buffered;
            float capacity = cons.capacity;

            bars.add("power", entity -> new Bar(() -> buffered ? Core.bundle.format("bar.poweramount", Float.isNaN(entity.power.status * capacity) ? "<ERROR>" : (int)(entity.power.status * capacity)) :
                Core.bundle.get("bar.power"), () -> Pal.powerBar, () -> Mathf.zero(cons.requestedPower(entity)) && entity.power.graph.getPowerProduced() + entity.power.graph.getBatteryStored() > 0f ? 1f : entity.power.status));
        }

        if(hasItems && configurable /*zones extend code*/&& showItemsBar){
            bars.add("items", entity -> new Bar(() -> Core.bundle.format("bar.items", entity.items.total()), () -> Pal.items, () -> (float)entity.items.total() / itemCapacity[entity.level()]));
        }
    }

    /** 链接Tile*/
    public Tile linked(Tile tile){
        return tile;
    }

    /** 指定Tile是否为固体*/
    public boolean isSolidFor(Tile tile){
        return false;
    }

    /** 是否可被指定块替换*/
    public boolean canReplace(Block other){
        return (other != this || rotate) && this.group != BlockGroup.none && other.group == this.group;
    }

    /** @return 当玩家放置在一行的位置时, 可能会替换这个块.<p/>a possible replacement for this block when placed in a line by the player. */
    public Block getReplacement(BuildRequest req, Array<BuildRequest> requests){
        return this;
    }

    /** 处理Tile伤害*/
    public float handleDamage(Tile tile, float amount){
        return amount;
    }

    /** 处理子弹撞击*/
    public void handleBulletHit(TileEntity entity, Bullet bullet){
        entity.damage(bullet.damage());
    }

    /** 更新Tile*/
    public void update(Tile tile){
    }

    /** 是否可接收物品*/
    @Deprecated
    public boolean isAccessible(){
        return (hasItems && itemCapacity[0] > 0);
    }

    /** Tile销毁时执行.<p/>Called when the block is destroyed. */
    public void onDestroyed(Tile tile){
        float x = tile.worldx(), y = tile.worldy();
        float explosiveness = baseExplosiveness;
        float flammability = 0f;
        float power = 0f;

        if(hasItems){
            for(Item item : content.items()){
                int amount = tile.entity.items.get(item);
                explosiveness += item.explosiveness * amount;
                flammability += item.flammability * amount;
            }
        }

        if(hasLiquids){
            flammability += tile.entity.liquids.sum((liquid, amount) -> liquid.explosiveness * amount / 2f);
            explosiveness += tile.entity.liquids.sum((liquid, amount) -> liquid.flammability * amount / 2f);
        }

        if(consumes[tile.entity.level()].hasPower() && consumes[tile.entity.level()].getPower().buffered){
            power += tile.entity.power.status * consumes[tile.entity.level()].getPower().capacity;
        }

        if(hasLiquids){

            tile.entity.liquids.each((liquid, amount) -> {
                float splash = Mathf.clamp(amount / 4f, 0f, 10f);

                for(int i = 0; i < Mathf.clamp(amount / 5, 0, 30); i++){
                    Time.run(i / 2f, () -> {
                        Tile other = world.tile(tile.x + Mathf.range(size / 2), tile.y + Mathf.range(size / 2));
                        if(other != null){
                            Puddle.deposit(other, liquid, splash);
                        }
                    });
                }
            });
        }

        Damage.dynamicExplosion(x, y, flammability, explosiveness * 3.5f, power, tilesize * size / 2f, Pal.darkFlame);
        if(!tile.floor().solid && !tile.floor().isLiquid){
            RubbleDecal.create(tile.drawx(), tile.drawy(), size);
        }
    }

    /**
     *  返回瓷砖的易燃性. 用于火灾计算. 考虑到地板液体的可燃性.<p/>
     * Returns the flammability of the tile. Used for fire calculations.
     * Takes flammability of floor liquid into account.
     */
    public float getFlammability(Tile tile){
        if(!hasItems || tile.entity == null){
            if(tile.floor().isLiquid && !solid){
                return tile.floor().liquidDrop.flammability;
            }
            return 0;
        }else{
            float result = tile.entity.items.sum((item, amount) -> item.flammability * amount);

            if(hasLiquids){
                result += tile.entity.liquids.sum((liquid, amount) -> liquid.flammability * amount / 3f);
            }

            return result;
        }
    }

    /** 显示名称*/
    public String getDisplayName(Tile tile){
        return localizedName;
    }

    /** 显示图标纹理*/
    public TextureRegion getDisplayIcon(Tile tile){
        return icon(Cicon.medium);
    }

    /** 显示配置ui*/
    public void display(Tile tile, Table table){
        TileEntity entity = tile.entity;

        if(entity != null){
            table.table(bars -> {
                bars.defaults().growX().height(18f).pad(4);

                displayBars(tile, bars);
            }).growX();
            table.row();
            table.table(ctable -> {
                displayConsumption(tile, ctable);
            }).growX();

            table.marginBottom(-5);
        }
    }

    /** 显示消耗界面*/
    public void displayConsumption(Tile tile, Table table){
        table.left();
        int level = tile.entity.block.consumes.length == 1 ? 0 : tile.entity.level();
        for(Consume cons : consumes[level].all()){
            if(cons.isOptional() && cons.isBoost()) continue;
            cons.build(tile, table);
        }
    }

    /** 显示状态条*/
    public void displayBars(Tile tile, Table table){
        for(Func<TileEntity, Bar> bar : bars.list()){
            table.add(bar.get(tile.entity)).growX();
            table.row();
        }
    }

    /** 绘制块建造请求状态*/
    public void drawRequest(BuildRequest req, Eachable<BuildRequest> list, boolean valid){
        Draw.reset();
        Draw.mixcol(!valid ? Pal.breakInvalid : Color.white, (!valid ? 0.4f : 0.24f) + Mathf.absin(Time.globalTime(), 6f, 0.28f));
        Draw.alpha(1f);
        drawRequestRegion(req, list);
        Draw.reset();
    }

    /** 绘制块建造请求纹理*/
    public void drawRequestRegion(BuildRequest req, Eachable<BuildRequest> list){
        TextureRegion reg = icon(Cicon.full);
        Draw.rect(icon(Cicon.full), req.drawx(), req.drawy(),
            reg.getWidth() * req.animScale * Draw.scl,
            reg.getHeight() * req.animScale * Draw.scl,
            !rotate ? 0 : req.rotation * 90);

        if(req.hasConfig){
            drawRequestConfig(req, list);
        }
    }

    /** 绘制建造请求配置*/
    public void drawRequestConfig(BuildRequest req, Eachable<BuildRequest> list){

    }

    /** 绘制建造请求配置中心*/
    public void drawRequestConfigCenter(BuildRequest req, Content content, String region){
        Color color = content instanceof Item ? ((Item)content).color : content instanceof Liquid ? ((Liquid)content).color : null;
        if(color == null) return;

        float prev = Draw.scl;

        Draw.color(color);
        Draw.scl *= req.animScale;
        Draw.rect(region, req.drawx(), req.drawy());
        Draw.scl = prev;
        Draw.color();
    }

    /** @return 块小地图颜色, 返回0使用默认值.<p/>a custom minimap color for this tile, or 0 to use default colors. */
    public int minimapColor(Tile tile){
        return 0;
    }

    /** 绘制建造请求顶层*/
    public void drawRequestConfigTop(BuildRequest req, Eachable<BuildRequest> list){

    }

    @Override
    public void createIcons(MultiPacker packer){
        super.createIcons(packer);

        packer.add(PageType.editor, name + "-icon-editor", Core.atlas.getPixmap((AtlasRegion)icon(Cicon.full)));

        if(!synthetic()){
            PixmapRegion image = Core.atlas.getPixmap((AtlasRegion)icon(Cicon.full));
            color.set(image.getPixel(image.width/2, image.height/2));
        }

        getGeneratedIcons();

        Pixmap last = null;

        if(outlineIcon){
            final int radius = 4;
            PixmapRegion region = Core.atlas.getPixmap(getGeneratedIcons()[getGeneratedIcons().length-1]);
            Pixmap out = new Pixmap(region.width, region.height);
            Color color = new Color();
            for(int x = 0; x < region.width; x++){
                for(int y = 0; y < region.height; y++){

                    region.getPixel(x, y, color);
                    out.draw(x, y, color);
                    if(color.a < 1f){
                        boolean found = false;
                        outer:
                        for(int rx = -radius; rx <= radius; rx++){
                            for(int ry = -radius; ry <= radius; ry++){
                                if(Structs.inBounds(rx + x, ry + y, region.width, region.height) && Mathf.dst2(rx, ry) <= radius*radius && color.set(region.getPixel(rx + x, ry + y)).a > 0.01f){
                                    found = true;
                                    break outer;
                                }
                            }
                        }
                        if(found){
                            out.draw(x, y, outlineColor);
                        }
                    }
                }
            }
            last = out;

            packer.add(PageType.main, name, out);
        }

        if(generatedIcons.length > 1){
            Pixmap base = Core.atlas.getPixmap(generatedIcons[0]).crop();
            for(int i = 1; i < generatedIcons.length; i++){
                if(i == generatedIcons.length - 1 && last != null){
                    base.drawPixmap(last);
                }else{
                    base.draw(Core.atlas.getPixmap(generatedIcons[i]));
                }
            }
            packer.add(PageType.main, "block-" + name + "-full", base);
            generatedIcons = null;
            Arrays.fill(cicons, null);
        }
    }

    /** 地图编辑器界面图标.<p/>Never use outside of the editor! */
    public TextureRegion editorIcon(){
        if(editorIcon == null) editorIcon = Core.atlas.find(name + "-icon-editor");
        return editorIcon;
    }

    /** 地图编辑器变体纹理.<p/>Never use outside of the editor! */
    public TextureRegion[] editorVariantRegions(){
        if(editorVariantRegions == null){
            variantRegions();
            editorVariantRegions = new TextureRegion[variantRegions.length];
            for(int i = 0; i < variantRegions.length; i++){
                AtlasRegion region = (AtlasRegion)variantRegions[i];
                editorVariantRegions[i] = Core.atlas.find("editor-" + (region.name.equals("error") && variantRegions.length == 1 ? name : region.name) );
            }
        }
        return editorVariantRegions;
    }

    /** 构建图标容器*/
    protected TextureRegion[] generateIcons(){
        return new TextureRegion[]{Core.atlas.find(name)};
    }

    /** 获取图标容器*/
    public TextureRegion[] getGeneratedIcons(){
        if(generatedIcons == null){
            generatedIcons = generateIcons();
        }
        return generatedIcons;
    }

    /** 变体纹理容器*/
    public TextureRegion[] variantRegions(){
        if(variantRegions == null){
            variantRegions = new TextureRegion[]{icon(Cicon.full)};
        }
        return variantRegions;
    }

    /** 是否包含实体*/
    public boolean hasEntity(){
        return destructible || update;
    }

    /** 获取绑定实体*/
    public final TileEntity newEntity(){
        return entityType.get();
    }

    /** 绘制偏移量.<p/>Offset for placing and drawing multiblocks. */
    public float offset(){
        return ((size + 1) % 2) * tilesize / 2f;
    }

    /** 块范围, 世界坐标*/
    public Rect bounds(int x, int y, Rect rect){
        return rect.setSize(size * tilesize).setCenter(x * tilesize + offset(), y * tilesize + offset());
    }

    /** 是否为多格块*/
    public boolean isMultiblock(){
        return size > 1;
    }

    /** 是否显示*/
    public boolean isVisible(){
        return buildVisibility.visible() && !isHidden();
    }

    /** 是否为地板块*/
    public boolean isFloor(){
        return this instanceof Floor;
    }

    /** 是否为上层覆盖块*/
    public boolean isOverlay(){
        return this instanceof OverlayFloor;
    }

    /** 转化为地板块*/
    public Floor asFloor(){
        return (Floor)this;
    }

    @Override
    public boolean isHidden(){
        return !buildVisibility.visible();
    }

    @Override
    public boolean alwaysUnlocked(){
        return alwaysUnlocked;
    }

    /** 设置块解锁需求*/
    public void requirements(Category cat, ItemStack[] stacks, boolean unlocked){
        requirements(cat, BuildVisibility.shown, stacks);
        this.alwaysUnlocked = unlocked;
    }

    /** 设置块解锁需求*/
    protected void requirements(Category cat, ItemStack[] stacks){
        requirements(cat, BuildVisibility.shown, stacks);
    }

    /** 设置需求. 仅使用此方法设置需求.<p/>Sets up requirements. Use only this method to set up requirements. */
    protected void requirements(Category cat, BuildVisibility visible, ItemStack[] stacks){
        this.category = cat;
        this.requirements = stacks;
        this.buildVisibility = visible;
//        this.setRequirements(stacks);

        Arrays.sort(requirements, Structs.comparingInt(i -> i.item.id));
    }


    // zones add begon
//    /** 获取建造需求*/
//    public ItemStack[] getRequirements() {
//        return _requirements;
//    }
//    /** 设置建造需求*/
//    public void setRequirements(ItemStack[] requirements) {
//        this._requirements = requirements;
//    }

    /** 设置块解锁需求*/
    @ZMethod
    public void requirements(Category cat, ItemStack[][] stacks, Boolean unlocked){
        this.requirements(cat, stacks[0], unlocked);
    }

    /** 纹理中心点, 适配deadzombie资源*/
    @ZonesAnnotate.ZAdd
    public Vec2 regionCenter(int variant) {
        return Vec2.ZERO;
    }

    public void drawRequestIso(BuildRequest req, Eachable<BuildRequest> list, boolean valid){
        Draw.reset();
//        Draw.mixcol(!valid ? Pal.breakInvalid : Color.white, (!valid ? 0.4f : 0.24f) + Mathf.absin(Time.globalTime(), 6f, 0.28f));
        Draw.mixcol(!valid ? Color.red : Color.blue, (!valid ? 0.4f : 0.24f) + Mathf.absin(Time.globalTime(), 6f, 0.28f));
        Draw.alpha(1f);
        drawRequestRegionIso(req, list);
        Draw.reset();
    }

    /** 绘制Iso块建造请求纹理*/
    public void drawRequestRegionIso(BuildRequest req, Eachable<BuildRequest> list){
        ShapeRenderer.drawFillDiamond(req.x , req.y, req.block.size, req.block.size);
        Draw.reset();   // 关闭渲染器颜色

        TextureRegion _regBuild = PackLoader.getInstance().getRegion(buildName, 0, 0);
        Rect _rectBuild = PackLoader.getInstance().getRect(buildName, 0, 0);
        Vec2 pos = Vec2.TEMP2;
        ISOUtils.tileToWorldCoordsCenter(req.x, req.y, size, size, pos);

        float scale = 1;
        float _x = (pos.x - _rectBuild.x) * scale;
        float _y = (pos.y -  _rectBuild.y) * scale;
        float _w = _rectBuild.width * scale;        // _rectBuild.width * scale;
        float _h = _rectBuild.height * scale;
        Draw.rectGdx(_regBuild, _x, _y, _w, _h);

        if(req.hasConfig){
            drawRequestConfig(req, list);   // 绘制建造配置信息
        }
    }

    /** 块范围, Tile坐标*/
    public Rect boundsTile(int x, int y, Rect rect){
        return rect.setSize(size * tileunit).setCenter(x * tileunit + offsetTile(), y * tileunit + offsetTile());
    }

    /** 绘制偏移量, 单位为Tile.*/
    public float offsetTile(){
        return ((size + 1) % 2) * tileunit / 2f;
    }

    /** 建筑的纹理名称, 用于绘制建造图标*/
    protected String buildName = null;
    // zones add end
}
