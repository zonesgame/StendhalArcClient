package mindustry.input;

import java.util.Iterator;

import arc.Core;
import arc.Events;
import arc.func.Boolf;
import arc.func.Cons;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.input.GestureDetector;
import arc.input.GestureDetector.GestureListener;
import arc.input.InputProcessor;
import arc.input.KeyCode;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.math.geom.Point2;
import arc.math.geom.Rect;
import arc.math.geom.Vec2;
import arc.scene.Group;
import arc.scene.event.Touchable;
import arc.scene.ui.layout.Table;
import arc.scene.ui.layout.WidgetGroup;
import arc.struct.Array;
import arc.util.ArcAnnotate.Nullable;
import arc.util.Eachable;
import arc.util.Time;
import arc.util.Tmp;
import arc.z.util.ISOUtils;
import mindustry.annotations.Annotations.Loc;
import mindustry.annotations.Annotations.Remote;
import mindustry.annotations.Annotations.Variant;
import mindustry.content.Blocks;
import mindustry.content.Fx;
import mindustry.content.UnitTypes;
import mindustry.entities.Effects;
import mindustry.entities.Units;
import mindustry.entities.effect.ItemTransfer;
import mindustry.entities.traits.BuilderTrait.BuildRequest;
import mindustry.entities.type.Player;
import mindustry.game.EventType.DepositEvent;
import mindustry.game.EventType.TapConfigEvent;
import mindustry.game.EventType.TapEvent;
import mindustry.game.Schematic;
import mindustry.game.Teams.BrokenBlock;
import mindustry.gen.Call;
import mindustry.gen.Sounds;
import mindustry.graphics.Pal;
import mindustry.input.Placement.NormalizeDrawResult;
import mindustry.input.Placement.NormalizeResult;
import mindustry.net.Administration.ActionType;
import mindustry.net.ValidateException;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.ui.fragments.OverlayFragment;
import mindustry.world.Block;
import mindustry.world.Build;
import mindustry.world.Pos;
import mindustry.world.Tile;
import mindustry.world.blocks.BuildBlock;
import mindustry.world.blocks.BuildBlock.BuildEntity;
import mindustry.world.blocks.power.PowerNode;
import z.debug.ZDebug;
import z.utils.ShapeRenderer;

import static mindustry.Vars.content;
import static mindustry.Vars.mobile;
import static mindustry.Vars.net;
import static mindustry.Vars.netServer;
import static mindustry.Vars.player;
import static mindustry.Vars.schematics;
import static mindustry.Vars.state;
import static mindustry.Vars.tilesize;
import static mindustry.Vars.tileunit;
import static mindustry.Vars.ui;
import static mindustry.Vars.world;
import static z.debug.ZDebug.d_input;
import static z.debug.ZDebug.enable_isoInput;

/**
 *  输入处理类
 * */
public abstract class InputHandler implements InputProcessor, GestureListener{
    /** 玩家选择范围, 用于丢弃物品.<p/>Used for dropping items. */
    final static float playerSelectRange = mobile ? 17f : 11f;
    /** 最大长度.<p/>Maximum line length. */
    final static int maxLength = 100;
    /***/
    final static Vec2 stackTrns = new Vec2();
    /***/
    final static Rect r1 = new Rect(), r2 = new Rect();
    /** 背部物品丢弃距离.<p/>Distance on the back from where items originate. */
    final static float backTrns = 3f;

    /** ui界面*/
    public final OverlayFragment frag = new OverlayFragment();

    /** 放置中的块*/
    public Block block;
    /** 旋转*/
    public boolean overrideLineRotation;
    /** 方向*/
    public int rotation;
    /** 丢弃物品状态*/
    public boolean droppingItem;
    /** ui群组*/
    public Group uiGroup;

    /***/
    protected @Nullable Schematic lastSchematic;
    /** 手势处理器*/
    protected GestureDetector detector;
    /** 放置块*/
    protected PlaceLine line = new PlaceLine();
    /** 建造请求*/
    protected BuildRequest resultreq;
    /** 光标显示的建造请求*/
    protected BuildRequest brequest = new BuildRequest();
    /** 线建造请求*/
    protected Array<BuildRequest> lineRequests = new Array<>();
    /** 选择建造请求*/
    protected Array<BuildRequest> selectRequests = new Array<>();

    //methods to override

    /** 玩家移除请求块*/
    @Remote(variants = Variant.one)
    public static void removeQueueBlock(int x, int y, boolean breaking){
        player.removeRequest(x, y, breaking);
    }

    /** 玩家丢弃物品*/
    @Remote(targets = Loc.client, called = Loc.server)
    public static void dropItem(Player player, float angle){
        if(net.server() && player.item().amount <= 0){
            throw new ValidateException(player, "Player cannot drop an item.");
        }

        Effects.effect(Fx.dropItem, Color.white, player.x, player.y, angle, player.item().item);
        player.clearItem();
    }

    /** 选择块*/
    @Remote(targets = Loc.both, called = Loc.server, forward = true, unreliable = true)
    public static void rotateBlock(Player player, Tile tile, boolean direction){
        if(net.server() && (!Units.canInteract(player, tile) ||
            !netServer.admins.allowAction(player, ActionType.rotate, tile, action -> action.rotation = Mathf.mod(tile.rotation() + Mathf.sign(direction), 4)))){
            throw new ValidateException(player, "Player cannot rotate a block.");
        }

        tile.rotation(Mathf.mod(tile.rotation() + Mathf.sign(direction), 4));

        if(tile.entity != null){
            tile.entity.updateProximity();
            tile.entity.noSleep();
        }
    }

    /** 转移库存*/
    @Remote(targets = Loc.both, forward = true, called = Loc.server)
    public static void transferInventory(Player player, Tile tile){
        if(player == null || player.timer == null) return;
        if(net.server() && (player.item().amount <= 0 || player.isTransferring|| !Units.canInteract(player, tile) ||
            !netServer.admins.allowAction(player, ActionType.depositItem, tile, action -> {
                action.itemAmount = player.item().amount;
                action.item = player.item().item;
            }))){
            throw new ValidateException(player, "Player cannot transfer an item.");
        }

        if(tile.entity == null) return;

        player.isTransferring = true;

        Item item = player.item().item;
        int amount = player.item().amount;
        int accepted = tile.block().acceptStack(item, amount, tile, player);
        player.item().amount -= accepted;

        int sent = Mathf.clamp(accepted / 4, 1, 8);
        int removed = accepted / sent;
        int[] remaining = {accepted, accepted};
        Block block = tile.block();

        Core.app.post(() -> Events.fire(new DepositEvent(tile, player, item, accepted)));

        for(int i = 0; i < sent; i++){
            boolean end = i == sent - 1;
            Time.run(i * 3, () -> {
                tile.block().getStackOffset(item, tile, stackTrns);

                ItemTransfer.create(item,
                player.x + Angles.trnsx(player.rotation + 180f, backTrns), player.y + Angles.trnsy(player.rotation + 180f, backTrns),
                new Vec2(tile.drawx() + stackTrns.x, tile.drawy() + stackTrns.y), () -> {
                    if(tile.block() != block || tile.entity == null || tile.entity.items == null) return;

                    tile.block().handleStack(item, removed, tile, player);
                    remaining[1] -= removed;

                    if(end && remaining[1] > 0){
                        tile.block().handleStack(item, remaining[1], tile, player);
                    }
                });

                remaining[0] -= removed;

                if(end){
                    player.isTransferring = false;
                }
            });
        }
    }

    /** 点击瓦砾*/
    @Remote(targets = Loc.both, called = Loc.server, forward = true)
    public static void onTileTapped(Player player, Tile tile){
        if(tile == null || player == null) return;
        if(net.server() && (!Units.canInteract(player, tile) ||
            !netServer.admins.allowAction(player, ActionType.tapTile, tile, action -> {}))) throw new ValidateException(player, "Player cannot tap a tile.");
        tile.block().tapped(tile, player);
        Core.app.post(() -> Events.fire(new TapEvent(tile, player)));
    }

    /** 瓦砾设置(通过ui tap)*/
    @Remote(targets = Loc.both, called = Loc.both, forward = true)
    public static void onTileConfig(Player player, Tile tile, int value){
        if(tile == null) return;

        if(net.server() && (!Units.canInteract(player, tile) ||
            !netServer.admins.allowAction(player, ActionType.configure, tile, action -> action.config = value))) throw new ValidateException(player, "Player cannot configure a tile.");
        tile.block().configured(tile, player, value);
        Core.app.post(() -> Events.fire(new TapConfigEvent(tile, player, value)));
    }

    /** 所有建造请求*/
    public Eachable<BuildRequest> allRequests(){
        return cons -> {
            for(BuildRequest request : player.buildQueue()) cons.get(request);
            for(BuildRequest request : selectRequests) cons.get(request);
            for(BuildRequest request : lineRequests) cons.get(request);
        };
    }

    /***/
    public OverlayFragment getFrag(){
        return frag;
    }

    public void update(){
        if (d_input) {
//            Vec2 worldpos = Core.input.mouseWorld(getMouseX(), getMouseY());
//            System.out.println(worldpos.x + "       " + worldpos.y);
            if (Core.input.keyRelease(KeyCode.BACKSPACE) && true) {
                Vec2 worldPos = Core.input.mouseWorld(Core.input.mouseX(), Core.input.mouseY());
//                Vec2 gamepos = Core.camera.unproject(getMouseX(), getMouseY());
                ZDebug.addUnit(worldPos.x, worldPos.y, UnitTypes.testUnit2);
            }
        }
    }

    /** 鼠标X轴位置*/
    public float getMouseX(){
        return Core.input.mouseX();
    }

    /** 鼠标Y轴位置*/
    public float getMouseY(){
        return Core.input.mouseY();
    }

    /** 构建配置界面*/
    public void buildPlacementUI(Table table){

    }

    /** 构建界面*/
    public void buildUI(Group group){

    }

    /** 更新状态*/
    public void updateState(){

    }

    /** 绘制底部*/
    public void drawBottom(){

    }

    /** 绘制顶部*/
    public void drawTop(){

    }

    /** 绘制选择块的效果. 四个箭头*/
    public void drawSelected(int x, int y, Block block, Color color){
        if (enable_isoInput) {
            Draw.color(color);
            for(int i = 0; i < 4; i++){
                Point2 p = Geometry.d8edge[i];
                float offset = -Math.max(block.size - 1, 0) / 2f * tileunit;
                float tx = x*tileunit + block.offsetTile() + offset * p.x;
                float ty = y*tileunit + block.offsetTile() + offset * p.y;
//                ISOUtils.tileToWorldCoords(tx, ty, temp);
//                Draw.rect("block-select-iso1d" + i, temp.x, temp.y);
                ShapeRenderer.drawBlockSelect(tx, ty, block.size, i);
            }
            Draw.reset();
            return;
        }

        Draw.color(color);
        for(int i = 0; i < 4; i++){
            Point2 p = Geometry.d8edge[i];
            float offset = -Math.max(block.size - 1, 0) / 2f * tilesize;
            Draw.rect("block-select",
                x*tilesize + block.offset() + offset * p.x,
                y*tilesize + block.offset() + offset * p.y, i * 90);
        }
        Draw.reset();
    }

    /** 绘制拆除建筑效果*/
    public void drawBreaking(BuildRequest request){
        if(request.breaking){
            drawBreaking(request.x, request.y);
        }else{
            drawSelected(request.x, request.y, request.block, Pal.remove);
        }
    }

    /** 建造请求匹配*/
    public boolean requestMatches(BuildRequest request){
        Tile tile = world.tile(request.x, request.y);
        return tile != null && tile.block() instanceof BuildBlock && tile.<BuildEntity>ent().cblock == request.block;
    }

    /** 绘制拆除块显示的效果图标*/
    public void drawBreaking(int x, int y){
        Tile tile = world.ltile(x, y);
        if(tile == null) return;
        Block block = tile.block();

        drawSelected(x, y, block, Pal.remove);
    }

    /** 使用蓝图*/
    public void useSchematic(Schematic schem){
        selectRequests.addAll(schematics.toRequests(schem, world.toTile(player.x), world.toTile(player.y)));
    }

    /** 显示蓝图保存*/
    protected void showSchematicSave(){
        if(lastSchematic == null) return;

        ui.showTextInput("$schematic.add", "$name", "", text -> {
            Schematic replacement = schematics.all().find(s -> s.name().equals(text));
            if(replacement != null){
                ui.showConfirm("$confirm", "$schematic.replace", () -> {
                    schematics.overwrite(replacement, lastSchematic);
                    ui.showInfoFade("$schematic.saved");
                    ui.schematics.showInfo(replacement);
                });
            }else{
                lastSchematic.tags.put("name", text);
                schematics.add(lastSchematic);
                ui.showInfoFade("$schematic.saved");
                ui.schematics.showInfo(lastSchematic);
            }
        });
    }

    /** 旋转建造请求*/
    public void rotateRequests(Array<BuildRequest> requests, int direction){
        int ox = schemOriginX(), oy = schemOriginY();

        requests.each(req -> {
            //rotate config position
            if(req.block.posConfig){
                int cx = Pos.x(req.config) - req.originalX, cy = Pos.y(req.config) - req.originalY;
                int lx = cx;

                if(direction >= 0){
                    cx = -cy;
                    cy = lx;
                }else{
                    cx = cy;
                    cy = -lx;
                }
                req.config = Pos.get(cx + req.originalX, cy + req.originalY);
            }

            //rotate actual request, centered on its multiblock position
            float wx = (req.x - ox) * tilesize + req.block.offset(), wy = (req.y - oy) * tilesize + req.block.offset();
            float x = wx;
            if(direction >= 0){
                wx = -wy;
                wy = x;
            }else{
                wx = wy;
                wy = -x;
            }
            req.x = world.toTile(wx - req.block.offset()) + ox;
            req.y = world.toTile(wy - req.block.offset()) + oy;
            req.rotation = Mathf.mod(req.rotation + direction, 4);
        });
    }

    /** 翻转建造请求*/
    public void flipRequests(Array<BuildRequest> requests, boolean x){
        int origin = (x ? schemOriginX() : schemOriginY()) * tilesize;

        requests.each(req -> {
            float value = -((x ? req.x : req.y) * tilesize - origin + req.block.offset()) + origin;

            if(x){
                req.x = (int)((value - req.block.offset()) / tilesize);
            }else{
                req.y = (int)((value - req.block.offset()) / tilesize);
            }

            if(req.block.posConfig){
                int corigin = x ? req.originalWidth/2 : req.originalHeight/2;
                int nvalue = -((x ? Pos.x(req.config) : Pos.y(req.config)) - corigin) + corigin;
                if(x){
                    req.originalX = -(req.originalX - corigin) + corigin;
                    req.config = Pos.get(nvalue, Pos.y(req.config));
                }else{
                    req.originalY = -(req.originalY - corigin) + corigin;
                    req.config = Pos.get(Pos.x(req.config), nvalue);
                }
            }

            //flip rotation
            if(x == (req.rotation % 2 == 0)){
                req.rotation = Mathf.mod(req.rotation + 2, 4);
            }
        });
    }

    /** 蓝图X中心点*/
    protected int schemOriginX(){
        return rawTileX();
    }

    /** 蓝图Y中心点*/
    protected int schemOriginY(){
        return rawTileY();
    }

    /** 返回重叠此位置或null的建造请求.<p/>Returns the selection request that overlaps this position, or null. */
    protected BuildRequest getRequest(int x, int y){
        return getRequest(x, y, 1, null);
    }

    /** 返回重叠此位置或null的建造请求.<p/>Returns the selection request that overlaps this position, or null. */
    protected BuildRequest getRequest(int x, int y, int size, BuildRequest skip){
        float offset = ((size + 1) % 2) * tilesize / 2f;
        r2.setSize(tilesize * size);
        r2.setCenter(x * tilesize + offset, y * tilesize + offset);
        resultreq = null;

        Boolf<BuildRequest> test = req -> {
            if(req == skip) return false;
            Tile other = req.tile();

            if(other == null) return false;

            if(!req.breaking){
                r1.setSize(req.block.size * tilesize);
                r1.setCenter(other.worldx() + req.block.offset(), other.worldy() + req.block.offset());
            }else{
                r1.setSize(other.block().size * tilesize);
                r1.setCenter(other.worldx() + other.block().offset(), other.worldy() + other.block().offset());
            }

            return r2.overlaps(r1);
        };

        for(BuildRequest req : player.buildQueue()){
            if(test.get(req)) return req;
        }

        for(BuildRequest req : selectRequests){
            if(test.get(req)) return req;
        }

        return null;
    }

    /** 绘制拆除的选择范围框*/
    protected void drawBreakSelection(int x1, int y1, int x2, int y2){
        if (enable_isoInput) {
            NormalizeDrawResult result = Placement.normalizeDrawAreaIso(Blocks.air, x1, y1, x2, y2, false, maxLength, 1f);
            NormalizeResult dresult = Placement.normalizeArea(x1, y1, x2, y2, rotation, false, maxLength);

            for(int x = dresult.x; x <= dresult.x2; x++){           // 拆除块执行标记
                for(int y = dresult.y; y <= dresult.y2; y++){
                    Tile tile = world.ltile(x, y);
                    if(tile == null || !validBreak(tile.x, tile.y)) continue;

                    drawBreaking(tile.x, tile.y);
                }
            }

            Tmp.r1.set(result.x, result.y, result.x2 - result.x, result.y2 - result.y);

            Draw.color(Pal.remove);
            Lines.stroke(1f);

            for(BuildRequest req : player.buildQueue()){        // 拆除建造中的块标记
                if(req.breaking) continue;
                if(req.boundsTile(Tmp.r2).overlaps(Tmp.r1)){
                    drawBreaking(req);
                }
            }

            for(BuildRequest req : selectRequests){
                if(req.breaking) continue;
                if(req.boundsTile(Tmp.r2).overlaps(Tmp.r1)){
                    drawBreaking(req);
                }
            }

            for(BrokenBlock req : player.getTeam().data().brokenBlocks){
                Block block = content.block(req.block);
                if(block.boundsTile(req.x, req.y, Tmp.r2).overlaps(Tmp.r1)){
                    drawSelected(req.x, req.y, content.block(req.block), Pal.remove);
                }
            }

            Lines.stroke(2f);

            Draw.color(Pal.removeBack);
            ShapeRenderer.drawDiamondPoint(result.x + 0.1f, result.y + 0.1f, result.x2 - 0.1f, result.y2 - 0.1f);
//            Lines.rect(result.x, result.y - 1, result.x2 - result.x, result.y2 - result.y);
            Draw.color(Pal.remove);
            ShapeRenderer.drawDiamondPoint(result.x, result.y, result.x2, result.y2);
//            Lines.rect(result.x, result.y, result.x2 - result.x, result.y2 - result.y);
            return;
        }

        NormalizeDrawResult result = Placement.normalizeDrawArea(Blocks.air, x1, y1, x2, y2, false, maxLength, 1f);
        NormalizeResult dresult = Placement.normalizeArea(x1, y1, x2, y2, rotation, false, maxLength);

        for(int x = dresult.x; x <= dresult.x2; x++){
            for(int y = dresult.y; y <= dresult.y2; y++){
                Tile tile = world.ltile(x, y);
                if(tile == null || !validBreak(tile.x, tile.y)) continue;

                drawBreaking(tile.x, tile.y);
            }
        }

        Tmp.r1.set(result.x, result.y, result.x2 - result.x, result.y2 - result.y);

        Draw.color(Pal.remove);
        Lines.stroke(1f);

        for(BuildRequest req : player.buildQueue()){
            if(req.breaking) continue;
            if(req.bounds(Tmp.r2).overlaps(Tmp.r1)){
                drawBreaking(req);
            }
        }

        for(BuildRequest req : selectRequests){
            if(req.breaking) continue;
            if(req.bounds(Tmp.r2).overlaps(Tmp.r1)){
                drawBreaking(req);
            }
        }

        for(BrokenBlock req : player.getTeam().data().brokenBlocks){
            Block block = content.block(req.block);
            if(block.bounds(req.x, req.y, Tmp.r2).overlaps(Tmp.r1)){
                drawSelected(req.x, req.y, content.block(req.block), Pal.remove);
            }
        }

        Lines.stroke(2f);

        Draw.color(Pal.removeBack);
        Lines.rect(result.x, result.y - 1, result.x2 - result.x, result.y2 - result.y);
        Draw.color(Pal.remove);
        Lines.rect(result.x, result.y, result.x2 - result.x, result.y2 - result.y);
    }

    /** 绘制范围选择*/
    protected void drawSelection(int x1, int y1, int x2, int y2, int maxLength){
        NormalizeDrawResult result = Placement.normalizeDrawArea(Blocks.air, x1, y1, x2, y2, false, maxLength, 1f);

        Lines.stroke(2f);

        Draw.color(Pal.accentBack);
        Lines.rect(result.x, result.y - 1, result.x2 - result.x, result.y2 - result.y);
        Draw.color(Pal.accent);
        Lines.rect(result.x, result.y, result.x2 - result.x, result.y2 - result.y);
    }

    /** 刷新选择建造请求*/
    protected void flushSelectRequests(Array<BuildRequest> requests){
        for(BuildRequest req : requests){
            if(req.block != null && validPlace(req.x, req.y, req.block, req.rotation)){
                BuildRequest other = getRequest(req.x, req.y, req.block.size, null);
                if(other == null){
                    selectRequests.add(req.copy());
                }else if(!other.breaking && other.x == req.x && other.y == req.y && other.block.size == req.block.size){
                    selectRequests.remove(other);
                    selectRequests.add(req.copy());
                }
            }
        }
    }

    /** 刷新建造请求*/
    protected void flushRequests(Array<BuildRequest> requests){
        for(BuildRequest req : requests){
            if(req.block != null && validPlace(req.x, req.y, req.block, req.rotation)){
                BuildRequest copy = req.copy();
                if(copy.hasConfig && copy.block.posConfig){
                    copy.config = Pos.get(Pos.x(copy.config) + copy.x - copy.originalX, Pos.y(copy.config) + copy.y - copy.originalY);
                }
                player.addBuildRequest(copy);
            }
        }
    }

    /** 绘制建造请求*/
    protected void drawRequest(BuildRequest request){
        request.block.drawRequest(request, allRequests(), validPlace(request.x, request.y, request.block, request.rotation));
    }

    /** 为特定的块绘制一个放置图标.<p/>Draws a placement icon for a specific block. */
    protected void drawRequest(int x, int y, Block block, int rotation){
        brequest.set(x, y, rotation, block);
        brequest.animScale = 1f;
        block.drawRequest(brequest, allRequests(), validPlace(x, y, block, rotation));
    }

    /** 移除选择的所有.<p/>Remove everything from the queue in a selection. */
    protected void removeSelection(int x1, int y1, int x2, int y2){
        removeSelection(x1, y1, x2, y2, false);
    }

    /** 移除选择的所有.<p/>Remove everything from the queue in a selection. */
    protected void removeSelection(int x1, int y1, int x2, int y2, boolean flush){
        NormalizeResult result = Placement.normalizeArea(x1, y1, x2, y2, rotation, false, maxLength);
        for(int x = 0; x <= Math.abs(result.x2 - result.x); x++){
            for(int y = 0; y <= Math.abs(result.y2 - result.y); y++){
                int wx = x1 + x * Mathf.sign(x2 - x1);
                int wy = y1 + y * Mathf.sign(y2 - y1);

                Tile tile = world.ltile(wx, wy);

                if(tile == null) continue;

                if(!flush){
                    tryBreakBlock(wx, wy);
                }else if(validBreak(tile.x, tile.y) && !selectRequests.contains(r -> r.tile() != null && r.tile().link() == tile)){
                    selectRequests.add(new BuildRequest(tile.x, tile.y));
                }
            }
        }

        //remove build requests
        Tmp.r1.set(result.x * tilesize, result.y * tilesize, (result.x2 - result.x) * tilesize, (result.y2 - result.y) * tilesize);

        Iterator<BuildRequest> it = player.buildQueue().iterator();
        while(it.hasNext()){
            BuildRequest req = it.next();
            if(!req.breaking && req.bounds(Tmp.r2).overlaps(Tmp.r1)){
                it.remove();
            }
        }

        it = selectRequests.iterator();
        while(it.hasNext()){
            BuildRequest req = it.next();
            if(!req.breaking && req.bounds(Tmp.r2).overlaps(Tmp.r1)){
                it.remove();
            }
        }

        //remove blocks to rebuild
        Iterator<BrokenBlock> broken = state.teams.get(player.getTeam()).brokenBlocks.iterator();
        while(broken.hasNext()){
            BrokenBlock req = broken.next();
            Block block = content.block(req.block);
            if(block.bounds(req.x, req.y, Tmp.r2).overlaps(Tmp.r1)){
                broken.remove();
            }
        }
    }

    /** 更新线建造请求*/
    protected void updateLine(int x1, int y1, int x2, int y2){
        lineRequests.clear();
        iterateLine(x1, y1, x2, y2, l -> {
            rotation = l.rotation;
            BuildRequest req = new BuildRequest(l.x, l.y, l.rotation, block);
            req.animScale = 1f;
            lineRequests.add(req);
        });

        if(Core.settings.getBool("blockreplace")){
            lineRequests.each(req -> {
                Block replace = req.block.getReplacement(req, lineRequests);
                if(replace.unlockedCur()){
                    req.block = replace;
                }
            });
        }
    }

    /** 更新线建造请求*/
    protected void updateLine(int x1, int y1){
        if (enable_isoInput) {
            Vec2 worldPos = Core.input.mouseWorld(Core.input.mouseX(), Core.input.mouseY());
//            worldPos.scl( 1f / Draw.scl);
            int cursorX = (int) ISOUtils.worldToTileX(worldPos.x, worldPos.y);
            int cursorY = (int) ISOUtils.worldToTileY(worldPos.x, worldPos.y);
            updateLine(x1, y1, cursorX, cursorY);
        } else {
            updateLine(x1, y1, tileX(getMouseX()), tileY(getMouseY()));
        }
    }

    /** 处理瓦砾的点击事件.<p/>Handles tile tap events that are not platform specific. */
    boolean tileTapped(Tile tile){
        tile = tile.link();

        boolean consumed = false, showedInventory = false;

        //check if tapped block is configurable
        if(tile.block().configurable && tile.interactable(player.getTeam())){
            consumed = true;
            if(((!frag.config.isShown() && tile.block().shouldShowConfigure(tile, player)) //if the config fragment is hidden, show
            //alternatively, the current selected block can 'agree' to switch config tiles
            || (frag.config.isShown() && frag.config.getSelectedTile().block().onConfigureTileTapped(frag.config.getSelectedTile(), tile)))){
                Sounds.click.at(tile);
                frag.config.showConfig(tile);
            }
            //otherwise...
        }else if(!frag.config.hasConfigMouse()){ //make sure a configuration fragment isn't on the cursor
            //then, if it's shown and the current block 'agrees' to hide, hide it.
            if(frag.config.isShown() && frag.config.getSelectedTile().block().onConfigureTileTapped(frag.config.getSelectedTile(), tile)){
                consumed = true;
                frag.config.hideConfig();
            }

            if(frag.config.isShown()){
                consumed = true;
            }
        }

        //call tapped event
        if(!consumed && tile.interactable(player.getTeam())){
            Call.onTileTapped(player, tile);
        }

        //consume tap event if necessary
        if(tile.interactable(player.getTeam()) && tile.block().consumesTap){
            consumed = true;
        }else if(tile.interactable(player.getTeam()) && tile.block().synthetic() && !consumed){
            if(tile.block().hasItems && tile.entity.items.total() > 0){
                frag.inv.showFor(tile);
                consumed = true;
                showedInventory = true;
            }
        }

        if(!showedInventory){
            frag.inv.hide();
        }

        return consumed;
    }

    /** 试着选择玩家丢弃的物品, 如果成功返回true.<p/>Tries to select the player to drop off items, returns true if successful. */
    boolean tryTapPlayer(float x, float y){
        if(canTapPlayer(x, y)){
            droppingItem = true;
            return true;
        }
        return false;
    }

    /** 玩家是否可点击*/
    boolean canTapPlayer(float x, float y){
        return Mathf.dst(x, y, player.x, player.y) <= playerSelectRange && player.item().amount > 0;
    }

    /** 尝试开始开采瓦砾, 如果成功就返回true.<p/>Tries to begin mining a tile, returns true if successful. */
    boolean tryBeginMine(Tile tile){
        if(canMine(tile)){
            //if a block is clicked twice, reset it
            player.setMineTile(player.getMineTile() == tile ? null : tile);
            return true;
        }
        return false;
    }

    /** 指定瓦砾是否可开采*/
    boolean canMine(Tile tile){
        return !Core.scene.hasMouse()
        && tile.drop() != null && tile.drop().hardness <= player.mech.drillPower
        && !(tile.floor().playerUnmineable && tile.overlay().itemDrop == null)
        && player.acceptsItem(tile.drop())
        && tile.block() == Blocks.air && player.dst(tile.worldx(), tile.worldy()) <= Player.mineDistance;
    }

    /** 返回鼠标所在的瓦砾.<p/>Returns the tile at the specified MOUSE coordinates. */
    Tile tileAt(float x, float y){
        return world.tile(tileX(x), tileY(y));
    }

    /** 鼠标所在的瓦砾X轴位置*/
    int rawTileX(){
        return world.toTile(Core.input.mouseWorld().x);
    }

    /** 鼠标所在的瓦砾Y轴位置*/
    int rawTileY(){
        return world.toTile(Core.input.mouseWorld().y);
    }

    /** 屏幕坐标转换为瓦砾坐标*/
    int tileX(float cursorX){
        Vec2 vec = Core.input.mouseWorld(cursorX, 0);
        if(selectedBlock()){
            vec.sub(block.offset(), block.offset());
        }
        return world.toTile(vec.x);
    }

    /** 屏幕坐标转化为瓦砾坐标*/
    int tileY(float cursorY){
        Vec2 vec = Core.input.mouseWorld(0, cursorY);
        if(selectedBlock()){
            vec.sub(block.offset(), block.offset());
        }
        return world.toTile(vec.y);
    }

    /** 是否有选择块*/
    public boolean selectedBlock(){
        return isPlacing();
    }

    /** 是否放置状态中*/
    public boolean isPlacing(){
        return block != null;
    }

    /** 是否拆除状态中*/
    public boolean isBreaking(){
        return false;
    }

    /** 鼠标角度*/
    public float mouseAngle(float x, float y){
        if (enable_isoInput && true) {
            float angle = Core.input.mouseWorld(getMouseX(), getMouseY()).sub(x, y).angle();
            return (360 - angle + 45);      // 转化为斜45角度
        }
        return Core.input.mouseWorld(getMouseX(), getMouseY()).sub(x, y).angle();
    }

    /** 从监听池移除该对象监听器*/
    public void remove(){
        Core.input.removeProcessor(this);
        frag.remove();
        if(Core.scene != null){
            Table table = (Table)Core.scene.find("inputTable");
            if(table != null){
                table.clear();
            }
        }
        if(detector != null){
            Core.input.removeProcessor(detector);
        }
        if(uiGroup != null){
            uiGroup.remove();
            uiGroup = null;
        }
    }

    /** 添加该监听对象到容器池*/
    public void add(){
        Core.input.getInputProcessors().remove(i -> i instanceof InputHandler || (i instanceof GestureDetector && ((GestureDetector)i).getListener() instanceof InputHandler));
        Core.input.addProcessor(detector = new GestureDetector(20, 0.5f, 0.3f, 0.15f, this));
        Core.input.addProcessor(this);
        if(Core.scene != null){
            Table table = (Table)Core.scene.find("inputTable");
            if(table != null){
                table.clear();
                buildPlacementUI(table);
            }

            uiGroup = new WidgetGroup();
            uiGroup.touchable(Touchable.childrenOnly);
            uiGroup.setFillParent(true);
            ui.hudGroup.addChild(uiGroup);
            buildUI(uiGroup);

            frag.add();
        }

        if(player != null){
            player.isBuilding = true;
        }
    }

    /** 是否可以射击*/
    public boolean canShoot(){
        return block == null && !Core.scene.hasMouse() && !onConfigurable() && !isDroppingItem();
    }

    /** 是否可配置*/
    public boolean onConfigurable(){
        return false;
    }

    /** 是否倾泻物品中*/
    public boolean isDroppingItem(){
        return droppingItem;
    }

    /** 尝试向指定块倾泻物品*/
    public void tryDropItems(Tile tile, float x, float y){
        if(!droppingItem || player.item().amount <= 0 || canTapPlayer(x, y) || state.isPaused() ){
            droppingItem = false;
            return;
        }

        droppingItem = false;

        ItemStack stack = player.item();

        if(tile.block().acceptStack(stack.item, stack.amount, tile, player) > 0 && tile.interactable(player.getTeam()) && tile.block().hasItems && player.item().amount > 0 && !player.isTransferring && tile.interactable(player.getTeam())){
            Call.transferInventory(player, tile);
        }else{
            Call.dropItem(player.angleTo(x, y));
        }
    }

    /** 尝试指定位置放置块*/
    public void tryPlaceBlock(int x, int y){
        if(block != null && validPlace(x, y, block, rotation)){
            placeBlock(x, y, block, rotation);
        }
    }

    /** 尝试指定位置拆除块*/
    public void tryBreakBlock(int x, int y){
        if(validBreak(x, y)){
            breakBlock(x, y);
        }
    }

    /** 是否有效位置*/
    public boolean validPlace(int x, int y, Block type, int rotation){
        return validPlace(x, y, type, rotation, null);
    }

    /** 是否有效位置*/
    public boolean validPlace(int x, int y, Block type, int rotation, BuildRequest ignore){
        for(BuildRequest req : player.buildQueue()){
            if(req != ignore
                    && !req.breaking
                    && req.block.bounds(req.x, req.y, Tmp.r1).overlaps(type.bounds(x, y, Tmp.r2))
                    && !(type.canReplace(req.block) && Tmp.r1.equals(Tmp.r2))){
                return false;
            }
        }
        return Build.validPlace(player.getTeam(), x, y, type, rotation);
    }

    /** 是否有效拆除*/
    public boolean validBreak(int x, int y){
        return Build.validBreak(player.getTeam(), x, y);
    }

    /** 指定位置建造块*/
    public void placeBlock(int x, int y, Block block, int rotation){
        BuildRequest req = getRequest(x, y);
        if(req != null){
            player.buildQueue().remove(req);
        }
        player.addBuildRequest(new BuildRequest(x, y, rotation, block));
    }

    /** 指定位置拆除块*/
    public void breakBlock(int x, int y){
        Tile tile = world.ltile(x, y);
        player.addBuildRequest(new BuildRequest(tile.x, tile.y));
    }

    /** 绘制建造方向箭头*/
    public void drawArrow(Block block, int x, int y, int rotation){
        drawArrow(block, x, y, rotation, validPlace(x, y, block, rotation));
    }

    /** 绘制建造方向箭头*/
    public void drawArrow(Block block, int x, int y, int rotation, boolean valid){
        Draw.color(!valid ? Pal.removeBack : Pal.accentBack);
        Draw.rect(Core.atlas.find("place-arrow"),
        x * tilesize + block.offset(),
        y * tilesize + block.offset() - 1,
        Core.atlas.find("place-arrow").getWidth() * Draw.scl,
        Core.atlas.find("place-arrow").getHeight() * Draw.scl, rotation * 90 - 90);

        Draw.color(!valid ? Pal.remove : Pal.accent);
        Draw.rect(Core.atlas.find("place-arrow"),
        x * tilesize + block.offset(),
        y * tilesize + block.offset(),
        Core.atlas.find("place-arrow").getWidth() * Draw.scl,
        Core.atlas.find("place-arrow").getHeight() * Draw.scl, rotation * 90 - 90);
    }

    /** 迭代线建造请求*/
    void iterateLine(int startX, int startY, int endX, int endY, Cons<PlaceLine> cons){
        Array<Point2> points;
        boolean diagonal = Core.input.keyDown(Binding.diagonal_placement);

        if(Core.settings.getBool("swapdiagonal") && mobile){
            diagonal = !diagonal;
        }

        if(block instanceof PowerNode){
            diagonal = !diagonal;
        }

        if(diagonal){
            points = Placement.pathfindLine(block != null && block.conveyorPlacement, startX, startY, endX, endY);
        }else{
            points = Placement.normalizeLine(startX, startY, endX, endY);
        }

        if(block instanceof PowerNode){
            Array<Point2> skip = new Array<>();
            
            for(int i = 1; i < points.size; i++){
                int overlaps = 0;
                Point2 point = points.get(i);

                //check with how many powernodes the *next* tile will overlap
                for(int j = 0; j < i; j++){
                    if(!skip.contains(points.get(j)) && ((PowerNode)block).overlaps(world.ltile(point.x, point.y), world.ltile(points.get(j).x, points.get(j).y))){
                        overlaps++;
                    }
                }

                //if it's more than one, it can bridge the gap
                if(overlaps > 1){
                    skip.add(points.get(i-1));
                }
            }
            //remove skipped points
            points.removeAll(skip);
        }

        float angle = Angles.angle(startX, startY, endX, endY);
        int baseRotation = rotation;
        if(!overrideLineRotation || diagonal){
            baseRotation = (startX == endX && startY == endY) ? rotation : ((int)((angle + 45) / 90f)) % 4;
        }

        Tmp.r3.set(-1, -1, 0, 0);

        for(int i = 0; i < points.size; i++){
            Point2 point = points.get(i);

            if(block != null && Tmp.r2.setSize(block.size * tilesize).setCenter(point.x * tilesize + block.offset(), point.y * tilesize + block.offset()).overlaps(Tmp.r3)){
                continue;
            }

            Point2 next = i == points.size - 1 ? null : points.get(i + 1);
            line.x = point.x;
            line.y = point.y;
            if(!overrideLineRotation || diagonal){
                line.rotation = next != null ? Tile.relativeTo(point.x, point.y, next.x, next.y) : baseRotation;
            }else{
                line.rotation = rotation;
            }
            line.last = next == null;
            cons.get(line);

            Tmp.r3.setSize(block.size * tilesize).setCenter(point.x * tilesize + block.offset(), point.y * tilesize + block.offset());
        }
    }

    /**
     *  放置块
     * */
    class PlaceLine{
        public int x, y, rotation;
        public boolean last;
    }


    // zones add begon
    /** 是否为编辑事件块移动*/
    public boolean operationActionMove = false;

    /**  zones add function<p> 重新上色选择区域*/
    protected void drawSelection(int x1, int y1, int x2, int y2, int maxLength, Color rectColor, Color outLineColor){
        if (enable_isoInput) {
            NormalizeDrawResult result = Placement.normalizeDrawAreaIso(Blocks.air, x1, y1, x2, y2, false, maxLength, 1f);
            Lines.stroke(2f);

            Draw.color(rectColor);
            ShapeRenderer.drawDiamondPoint(result.x + 0.1f, result.y + 0.1f, result.x2 - 0.1f, result.y2 - 0.1f);
//            Lines.rect(result.x, result.y - 1, result.x2 - result.x, result.y2 - result.y);
            Draw.color(outLineColor);
            ShapeRenderer.drawDiamondPoint(result.x, result.y, result.x2, result.y2);
//            Lines.rect(result.x, result.y, result.x2 - result.x, result.y2 - result.y);
            return;
        }
        NormalizeDrawResult result = Placement.normalizeDrawArea(Blocks.air, x1, y1, x2, y2, false, maxLength, 1f);

        Lines.stroke(2f);

        Draw.color(rectColor);
        Lines.rect(result.x, result.y - 1, result.x2 - result.x, result.y2 - result.y);
        Draw.color(outLineColor);
        Lines.rect(result.x, result.y, result.x2 - result.x, result.y2 - result.y);
    }

    /** 为特定的块绘制一个放置图标.<p/>Draws a placement icon for a specific block. */
    protected void drawRequestIso(int x, int y, Block block, int rotation){
        brequest.set(x, y, rotation, block);
        brequest.animScale = 1f;
        block.drawRequestIso(brequest, allRequests(), validPlace(x, y, block, rotation));
    }

    // zones add end
}
