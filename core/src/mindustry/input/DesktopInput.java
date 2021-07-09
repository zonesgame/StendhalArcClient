package mindustry.input;

import arc.Core;
import arc.Events;
import arc.Graphics.Cursor;
import arc.Graphics.Cursor.SystemCursor;
import arc.graphics.Colors;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.input.KeyCode;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Rect;
import arc.math.geom.Vec2;
import arc.scene.Group;
import arc.scene.event.Touchable;
import arc.scene.ui.TextField;
import arc.scene.ui.layout.Table;
import arc.util.ArcAnnotate.Nullable;
import arc.util.Time;
import arc.util.Tmp;
import arc.z.util.ISOUtils;
import mindustry.Vars;
import mindustry.core.GameState.State;
import mindustry.entities.traits.BuilderTrait.BuildRequest;
import mindustry.entities.type.BaseUnit;
import mindustry.game.EventType.LineConfirmEvent;
import mindustry.game.Schematic;
import mindustry.gen.Call;
import mindustry.gen.Icon;
import mindustry.graphics.Pal;
import mindustry.ui.Styles;
import mindustry.world.Tile;
import z.entities.type.base.BaseGroundUnit;
import z.input.unit.UnitInput;

import static arc.Core.scene;
import static mindustry.Vars.maxSchematicSize;
import static mindustry.Vars.net;
import static mindustry.Vars.player;
import static mindustry.Vars.renderer;
import static mindustry.Vars.schematics;
import static mindustry.Vars.state;
import static mindustry.Vars.systemTroops;
import static mindustry.Vars.tilesize;
import static mindustry.Vars.ui;
import static mindustry.Vars.unitGroup;
import static mindustry.Vars.world;
import static mindustry.input.PlaceMode.breaking;
import static mindustry.input.PlaceMode.none;
import static mindustry.input.PlaceMode.placing;
import static z.debug.ZDebug.enable_isoInput;

/**
 *  PC输入处理器
 * */
public class DesktopInput extends InputHandler{
    /** 当前光标类型.<p/>Current cursor type. */
    private Cursor cursorType = SystemCursor.arrow;
    /** 玩家蓝图拖拽的线区域.<p/>Position where the player started dragging a line. */
    private int selectX, selectY, schemX, schemY;
    /** 最后知道的线位置.<p/>Last known line positions.*/
    private int lastLineX, lastLineY, schematicX, schematicY;
    /** 当前的放置类型.<p/>Whether selecting mode is active. */
    private PlaceMode mode;
    /** 线动画缩放值.<p/>Animation scale for line. */
    private float selectScale;
    /** 选择移动的建造请求.<p/>Selected build request for movement. */
    private @Nullable BuildRequest sreq;
    /** 玩家是否拆除状态.<p/>Whether player is currently deleting removal requests. */
    private boolean deleting = false;

    @Override
    public void buildUI(Group group){
        group.fill(t -> {
            t.bottom().update(() -> t.getColor().a = Mathf.lerpDelta(t.getColor().a, player.isBuilding() ? 1f : 0f, 0.15f));
            t.visible(() -> Core.settings.getBool("hints") && selectRequests.isEmpty());
            t.touchable(() -> t.getColor().a < 0.1f ? Touchable.disabled : Touchable.childrenOnly);
            t.table(Styles.black6, b -> {
                b.defaults().left();
                b.label(() -> Core.bundle.format(!player.isBuilding ?  "resumebuilding" : "pausebuilding", Core.keybinds.get(Binding.pause_building).key.toString())).style(Styles.outlineLabel);
                b.row();
                b.label(() -> Core.bundle.format("cancelbuilding", Core.keybinds.get(Binding.clear_building).key.toString())).style(Styles.outlineLabel);
                b.row();
                b.label(() -> Core.bundle.format("selectschematic", Core.keybinds.get(Binding.schematic_select).key.toString())).style(Styles.outlineLabel);
            }).margin(10f);
        });

        group.fill(t -> {
            t.visible(() -> lastSchematic != null && !selectRequests.isEmpty());
            t.bottom();
            t.table(Styles.black6, b -> {
                b.defaults().left();
                b.label( () -> Core.bundle.format("schematic.flip",
                Core.keybinds.get(Binding.schematic_flip_x).key.toString(),
                Core.keybinds.get(Binding.schematic_flip_y).key.toString())).style(Styles.outlineLabel);
                b.row();
                b.table(a -> {
                    a.addImageTextButton("$schematic.add", Icon.save, this::showSchematicSave).colspan(2).size(250f, 50f).disabled(f -> lastSchematic == null || lastSchematic.file != null);
                });
            }).margin(6f);
        });
    }

    @Override
    public void drawTop(){      // 绘制建造操作内容
        Lines.stroke(1f);
        int cursorX = tileX(Core.input.mouseX());
        int cursorY = tileY(Core.input.mouseY());
        if (enable_isoInput) {
            Vec2 worldPos = Core.input.mouseWorld(Core.input.mouseX(), Core.input.mouseY());
//            worldPos.scl( 1f / Draw.scl);
            cursorX = (int) (ISOUtils.worldToTileCenterX(worldPos.x, worldPos.y) + 0.0f);
            cursorY = (int) (ISOUtils.worldToTileCenterY(worldPos.x, worldPos.y) + 0.0f);
            float fx = ISOUtils.worldToTileX(worldPos.x, worldPos.y);
            float fy = ISOUtils.worldToTileY(worldPos.x, worldPos.y);
//            System.out.println(fx + " ++  " + fy);
        }

        //draw selection(s)
        if(mode == placing && block != null){
            for(int i = 0; i < lineRequests.size; i++){
                BuildRequest req = lineRequests.get(i);
                if(i == lineRequests.size - 1 && req.block.rotate){
                    drawArrow(block, req.x, req.y, req.rotation);
                }
                drawRequest(lineRequests.get(i));
            }
        }else if(mode == breaking){     // 绘制拆除选择框
            drawBreakSelection(selectX, selectY, cursorX, cursorY);
        }else if(isPlacing()){
            if (enable_isoInput) {
                if(block.rotate){
                    drawArrow(block, cursorX, cursorY, rotation);
                }
                Draw.color();
                drawRequestIso(cursorX, cursorY, block, rotation);
                block.drawPlace(cursorX, cursorY, rotation, validPlace(cursorX, cursorY, block, rotation));
            }
            else {
                if(block.rotate){
                    drawArrow(block, cursorX, cursorY, rotation);
                }
                Draw.color();
                drawRequest(cursorX, cursorY, block, rotation);
                block.drawPlace(cursorX, cursorY, rotation, validPlace(cursorX, cursorY, block, rotation));
            }
        }

        if(mode == none && !isPlacing()){
            BuildRequest req = getRequest(cursorX, cursorY);
            if(req != null){
                drawSelected(req.x, req.y, req.breaking ? req.tile().block() : req.block, Pal.accent);
            }
        }

        //draw schematic requests
        for(BuildRequest request : selectRequests){
            request.animScale = 1f;
            drawRequest(request);
        }

        if(sreq != null){
            boolean valid = validPlace(sreq.x, sreq.y, sreq.block, sreq.rotation, sreq);
            if(sreq.block.rotate){
                drawArrow(sreq.block, sreq.x, sreq.y, sreq.rotation, valid);
            }

            sreq.block.drawRequest(sreq, allRequests(), valid);

            drawSelected(sreq.x, sreq.y, sreq.block, getRequest(sreq.x, sreq.y, sreq.block.size, sreq) != null ? Pal.remove : Pal.accent);
        }

        if(Core.input.keyDown(Binding.schematic_select) && !Core.scene.hasKeyboard()){      // 蓝图选择
            drawSelection(schemX, schemY, cursorX, cursorY, Vars.maxSchematicSize);
        }

        // zones add begon
        //  绘制单位选择矩阵
        if(Core.input.keyDown(Binding.unit_select) && !Core.scene.hasKeyboard()){
            drawSelection(systemTroops.unitX, systemTroops.unitY, cursorX, cursorY, Vars.maxSchematicSize, Colors.get("BLUE"), Colors.get("GREEN"));
        }
        // zones add end

        Draw.reset();
    }

    @Override
    public void update(){
        super.update();     // zones add code

        if(net.active() && Core.input.keyTap(Binding.player_list)){
            ui.listfrag.toggle();
        }

        if(((player.getClosestCore() == null && player.isDead()) || state.isPaused()) && !ui.chatfrag.shown()){
            //move camera around
            float camSpeed = !Core.input.keyDown(Binding.dash) ? 3f : 8f;
            Core.camera.position.add(Tmp.v1.setZero().add(Core.input.axis(Binding.move_x), Core.input.axis(Binding.move_y)).nor().scl(Time.delta() * camSpeed));

            if(Core.input.keyDown(Binding.mouse_move)){
                Core.camera.position.x += Mathf.clamp((Core.input.mouseX() - Core.graphics.getWidth() / 2f) * 0.005f, -1, 1) * camSpeed;
                Core.camera.position.y += Mathf.clamp((Core.input.mouseY() - Core.graphics.getHeight() / 2f) * 0.005f, -1, 1) * camSpeed;
            }
        }

        if(Core.input.keyRelease(Binding.select)){
            player.isShooting = false;
        }

        if(!state.is(State.menu) && Core.input.keyTap(Binding.minimap) && !scene.hasDialog() && !(scene.getKeyboardFocus() instanceof TextField)){
            ui.minimapfrag.toggle();
        }

        if(state.is(State.menu) || Core.scene.hasDialog()) return;

        //zoom camera
        if((!Core.scene.hasScroll() || Core.input.keyDown(Binding.diagonal_placement)) && !ui.chatfrag.shown() && Math.abs(Core.input.axisTap(Binding.zoom)) > 0 && !Core.input.keyDown(Binding.rotateplaced) && (Core.input.keyDown(Binding.diagonal_placement) || ((!isPlacing() || !block.rotate) && selectRequests.isEmpty()))){
            renderer.scaleCamera(Core.input.axisTap(Binding.zoom));
        }

        if(player.isDead()){
            cursorType = SystemCursor.arrow;
            return;
        }

        pollInput();

        // 取消选择如果不放置. deselect if not placing
        if(!isPlacing() && mode == placing){
            mode = none;
        }

        if(player.isShooting && !canShoot()){
            player.isShooting = false;
        }

        if(isPlacing()){
            cursorType = SystemCursor.hand;
            selectScale = Mathf.lerpDelta(selectScale, 1f, 0.2f);
        }else{
            selectScale = 0f;
        }

        if(!Core.input.keyDown(Binding.diagonal_placement) && Math.abs((int)Core.input.axisTap(Binding.rotate)) > 0){
            rotation = Mathf.mod(rotation + (int)Core.input.axisTap(Binding.rotate), 4);

            if(sreq != null){
                sreq.rotation = Mathf.mod(sreq.rotation + (int)Core.input.axisTap(Binding.rotate), 4);
            }

            if(isPlacing() && mode == placing){
                updateLine(selectX, selectY);
            }else if(!selectRequests.isEmpty()){
                rotateRequests(selectRequests, (int)Core.input.axisTap(Binding.rotate));
            }
        }

        Tile cursor = tileAt(Core.input.mouseX(), Core.input.mouseY());
        if (enable_isoInput) {
            Vec2 worldPos = Core.input.mouseWorld(Core.input.mouseX(), Core.input.mouseY());
            int cursorX = (int) (ISOUtils.worldToTileCenterX(worldPos.x, worldPos.y));
            int cursorY = (int) (ISOUtils.worldToTileCenterY(worldPos.x, worldPos.y));
            cursor = world.tile(cursorX, cursorY);
        }

        if(cursor != null){     // 显示光标悬停瓦砾
            cursor = cursor.link();

            cursorType = cursor.block().getCursor(cursor);

            if(isPlacing() || !selectRequests.isEmpty()){
                cursorType = SystemCursor.hand;
            }

            if(!isPlacing() && canMine(cursor)){
                cursorType = ui.drillCursor;
            }

            if(getRequest(cursor.x, cursor.y) != null && mode == none){
                cursorType = SystemCursor.hand;
            }

            if(canTapPlayer(Core.input.mouseWorld().x, Core.input.mouseWorld().y)){
                cursorType = ui.unloadCursor;
            }

            if(cursor.interactable(player.getTeam()) && !isPlacing() && Math.abs(Core.input.axisTap(Binding.rotate)) > 0 && Core.input.keyDown(Binding.rotateplaced) && cursor.block().rotate){
                Call.rotateBlock(player, cursor, Core.input.axisTap(Binding.rotate) > 0);
            }
        }

        if(!Core.scene.hasMouse()){
            Core.graphics.cursor(cursorType);
        }

        cursorType = SystemCursor.arrow;
    }

    @Override
    public void useSchematic(Schematic schem){
        block = null;
        schematicX = tileX(getMouseX());
        schematicY = tileY(getMouseY());

        selectRequests.clear();
        selectRequests.addAll(schematics.toRequests(schem, schematicX, schematicY));
        mode = none;
    }

    @Override
    public boolean isBreaking(){
        return mode == breaking;
    }

    @Override
    public void buildPlacementUI(Table table){
        table.addImage().color(Pal.gray).height(4f).colspan(4).growX();
        table.row();
        table.left().margin(0f).defaults().size(48f).left();

        table.addImageButton(Icon.paste, Styles.clearPartiali, () -> {
            ui.schematics.show();
        });
    }

    /** 输入处理*/
    void pollInput(){
        if(scene.getKeyboardFocus() instanceof TextField) return;       // 键盘获取输入焦点返回

        Tile selected = tileAt(Core.input.mouseX(), Core.input.mouseY());   // 光标当前选择瓦砾
        int cursorX = tileX(Core.input.mouseX());       // 光标瓦砾X轴位置
        int cursorY = tileY(Core.input.mouseY());       // 光标瓦砾Y轴位置
        int rawCursorX = world.toTile(Core.input.mouseWorld().x), rawCursorY = world.toTile(Core.input.mouseWorld().y);
        if (enable_isoInput) {
            Vec2 worldPos = Core.input.mouseWorld(Core.input.mouseX(), Core.input.mouseY());
//            worldPos.scl( 1f / Draw.scl);
            cursorX = (int) (ISOUtils.worldToTileCenterX(worldPos.x, worldPos.y) + 0.0f);
            cursorY = (int) (ISOUtils.worldToTileCenterY(worldPos.x, worldPos.y) + 0.0f);
            rawCursorX = cursorX;
            rawCursorY = cursorY;
            selected = world.tile(cursorX, cursorY);
        }

        // 如果当前建造队列是空的, 则自动暂停建造. automatically pause building if the current build queue is empty
        if(Core.settings.getBool("buildautopause") && player.isBuilding && !player.isBuilding()){
            player.isBuilding = false;
            player.buildWasAutoPaused = true;
        }

        if(!selectRequests.isEmpty()){
            int shiftX = rawCursorX - schematicX, shiftY = rawCursorY - schematicY;

            selectRequests.each(s -> {
                s.x += shiftX;
                s.y += shiftY;
            });

            schematicX += shiftX;
            schematicY += shiftY;
        }

        if(Core.input.keyTap(Binding.deselect)){        // 玩家停止开采
            player.setMineTile(null);
        }

        if(Core.input.keyTap(Binding.clear_building)){      // 玩家清除建造列表
            player.clearBuilding();
        }

        if(Core.input.keyTap(Binding.schematic_select) && !Core.scene.hasKeyboard()){   // 蓝图起始位置
            schemX = rawCursorX;
            schemY = rawCursorY;
        }

        if(Core.input.keyTap(Binding.schematic_menu) && !Core.scene.hasKeyboard()){
            if(ui.schematics.isShown()){
                ui.schematics.hide();
            }else{
                ui.schematics.show();
            }
        }

        if(Core.input.keyTap(Binding.clear_building) || isPlacing()){
            lastSchematic = null;
            selectRequests.clear();
        }

        // zones add begon
        //  Unit选择添加
        if(Core.input.keyTap(Binding.unit_select) && !Core.scene.hasKeyboard()){
            systemTroops.unitX = rawCursorX;
            systemTroops.unitY = rawCursorY;
        }

        if(Core.input.keyRelease(Binding.unit_select) && !Core.scene.hasKeyboard()){
            for (BaseGroundUnit _unit : systemTroops.selectUnits) {
                _unit.selectState = false;
            }
            systemTroops.selectUnits.clear();

            Placement.NormalizeResult result = Placement.normalizeArea(systemTroops.unitX, systemTroops.unitY, rawCursorX, rawCursorY, 0, false, maxSchematicSize);
            Rect tmpRect = Rect.tmp2.set(result.x, result.y, result.x2 - result.x, result.y2 - result.y);
            for (BaseUnit baseUnit : unitGroup) {
                if (tmpRect.contains(baseUnit.tileX(), baseUnit.tileY()) && baseUnit instanceof BaseGroundUnit) {
                    BaseGroundUnit sgBaseUnit = (BaseGroundUnit) baseUnit;
                    systemTroops.selectUnits.add( sgBaseUnit);
                    sgBaseUnit.selectState = true;
                }
            }

            systemTroops.selectUnitState = systemTroops.selectUnits.size > 0;
//            lastSchematic = schematics.create(schemX, schemY, rawCursorX, rawCursorY);
//            useSchematic(lastSchematic);
//            if(selectRequests.isEmpty()){
//                lastSchematic = null;
//            }
        }

        // 取消单位选择
        if(Core.input.keyRelease(Binding.unit_unselect) && !Core.scene.hasKeyboard()){
            for (BaseGroundUnit _unit : systemTroops.selectUnits) {
                _unit.selectState = false;
            }
            systemTroops.selectUnitState = false;
        }
        // zones add end

        if(Core.input.keyRelease(Binding.schematic_select) && !Core.scene.hasKeyboard()){
            lastSchematic = schematics.create(schemX, schemY, rawCursorX, rawCursorY);
            useSchematic(lastSchematic);
            if(selectRequests.isEmpty()){
                lastSchematic = null;
            }
        }

        if(!selectRequests.isEmpty()){
            if(Core.input.keyTap(Binding.schematic_flip_x)){
                flipRequests(selectRequests, true);
            }

            if(Core.input.keyTap(Binding.schematic_flip_y)){
                flipRequests(selectRequests, false);
            }
        }

        if(sreq != null){
            float offset = ((sreq.block.size + 2) % 2) * tilesize / 2f;
            float x = Core.input.mouseWorld().x + offset;
            float y = Core.input.mouseWorld().y + offset;
            sreq.x = (int)(x / tilesize);
            sreq.y = (int)(y / tilesize);
        }

        if(block == null || mode != placing){
            lineRequests.clear();
        }

        if(Core.input.keyTap(Binding.pause_building)){
            player.isBuilding = !player.isBuilding;
            player.buildWasAutoPaused = false;
        }

        if((cursorX != lastLineX || cursorY != lastLineY) && isPlacing() && mode == placing){
            updateLine(selectX, selectY);
            lastLineX = cursorX;
            lastLineY = cursorY;
        }

        // unit input add begon
        unitInput.pollInput();
        // unit input add end

        if(Core.input.keyTap(Binding.select) && !Core.scene.hasMouse()){
            BuildRequest req = getRequest(cursorX, cursorY);

            if(Core.input.keyDown(Binding.break_block)){
                mode = none;
            }else if(!selectRequests.isEmpty()){
                flushRequests(selectRequests);
            }else if(isPlacing()){
                selectX = cursorX;
                selectY = cursorY;
                lastLineX = cursorX;
                lastLineY = cursorY;
                mode = placing;
                updateLine(selectX, selectY);
            }else if(req != null && !req.breaking && mode == none && !req.initialized){
                sreq = req;
            }else if(req != null && req.breaking){
                deleting = true;
            }
            // zones add begon
            else if ( systemTroops.selectUnitState) {    // 设置队伍移动目标
//                ZDebug.mySquad.setTarget(selected.getX(), selected.getY());
                 {
//                     System.out.println("Mouse1  " + Core.input.mouse().x + "     " + Core.input.mouse().y + "    ---     " + Core.input.mouseX() + "  " + Core.input.mouseY());
//                     System.out.println("Mouse2  " + Core.input.mouseWorld().x + "  " + Core.input.mouseWorld().y);
//                    Vec2 worldPos = Core.input.mouseWorld(Core.input.mouseX(), Core.input.mouseY());
////            worldPos.scl( 1f / Draw.scl);
//                    cursorX = (int) (ISOUtils.worldToTileCenterX(worldPos.x, worldPos.y) + 0.0f);
//                    cursorY = (int) (ISOUtils.worldToTileCenterY(worldPos.x, worldPos.y) + 0.0f);

                     if (selected != null)
                         squadMoveTargetTile.set(selected.getX(), selected.getY());
                     else
                         squadMoveTargetTile.setZero();
                     squadMoveTargetScreen.set(Core.input.mouse());
                     squadMoveTargetWorld.set(Core.input.mouseWorld());
                }
//                squadGroup[player.getTeam().id][0].setTarget(selected.getX(), selected.getY());
//                System.out.println(selected.getX() + "   ----  " + selected.getY());
                //  移动选择单位
//                for (SGGroundUnit unit : selectUnits) {
//                    unit.setMoveTarget(selected);
//                }
                // 更细系统路径算法
//                indexer.moveIndexer = selected;
            }
            // zones add end
            else if(selected != null){
                //only begin shooting if there's no cursor event
                if(!tileTapped(selected) && !tryTapPlayer(Core.input.mouseWorld().x, Core.input.mouseWorld().y) && (player.buildQueue().size == 0 || !player.isBuilding) && !droppingItem &&
                !tryBeginMine(selected) && player.getMineTile() == null && !Core.scene.hasKeyboard()){
                    player.isShooting = true;
                }
            }else if(!Core.scene.hasKeyboard()){ //if it's out of bounds, shooting is just fine
                player.isShooting = true;
            }
        }else if(Core.input.keyTap(Binding.deselect) && isPlacing()){
            block = null;
            mode = none;
        }else if(Core.input.keyTap(Binding.deselect) && !selectRequests.isEmpty()){
            selectRequests.clear();
            lastSchematic = null;
        }else if(Core.input.keyTap(Binding.break_block) && !Core.scene.hasMouse()){
            //is recalculated because setting the mode to breaking removes potential multiblock cursor offset
            deleting = false;
            mode = breaking;
            selectX = tileX(Core.input.mouseX());
            selectY = tileY(Core.input.mouseY());
            if (enable_isoInput) {
                Vec2 worldPos = Core.input.mouseWorld(Core.input.mouseX(), Core.input.mouseY());
//                worldPos.scl( 1f / Draw.scl);
                selectX = (int) (ISOUtils.worldToTileCenterX(worldPos.x, worldPos.y) + 0.0f);
                selectY = (int) (ISOUtils.worldToTileCenterY(worldPos.x, worldPos.y) + 0.0f);
            }
        }

        if(Core.input.keyDown(Binding.select) && mode == none && !isPlacing() && deleting){
            BuildRequest req = getRequest(cursorX, cursorY);
            if(req != null && req.breaking){
                player.buildQueue().remove(req);
            }
        }else{
            deleting = false;
        }

        if(mode == placing && block != null){
            if(!overrideLineRotation && !Core.input.keyDown(Binding.diagonal_placement) && (selectX != cursorX || selectY != cursorY) && ((int)Core.input.axisTap(Binding.rotate) != 0)){
                rotation = ((int)((Angles.angle(selectX, selectY, cursorX, cursorY) + 45) / 90f)) % 4;
                overrideLineRotation = true;
            }
        }else{
            overrideLineRotation = false;
        }

        if(Core.input.keyRelease(Binding.break_block) || Core.input.keyRelease(Binding.select)){

            if(mode == placing && block != null){ //touch up while placing, place everything in selection
                flushRequests(lineRequests);
                lineRequests.clear();
                Events.fire(new LineConfirmEvent());
            }else if(mode == breaking){ //touch up while breaking, break everything in selection
                removeSelection(selectX, selectY, cursorX, cursorY);
            }

            if(selected != null){
                tryDropItems(selected.link(), Core.input.mouseWorld().x, Core.input.mouseWorld().y);
            }

            if(sreq != null){
                if(getRequest(sreq.x, sreq.y, sreq.block.size, sreq) != null){
                    player.buildQueue().remove(sreq, true);
                }
                sreq = null;
            }

            mode = none;
        }

        if(Core.input.keyTap(Binding.toggle_power_lines)){
            if(Core.settings.getInt("lasersopacity") == 0){
                Core.settings.put("lasersopacity", Core.settings.getInt("preferredlaseropacity", 100));
            }else{
                Core.settings.put("preferredlaseropacity", Core.settings.getInt("lasersopacity"));
                Core.settings.put("lasersopacity", 0);
            }
        }

        // zones debug add  begon
        if(Core.input.keyRelease(Binding.select) && !Core.scene.hasMouse()){        // 拖拽设置队伍移动方向
//            System.out.println("get up event....................");
//            Vec2 tmp = Tmp.v11.set(squadMoveTargetTile).sub(selected);
//            System.out.println(squadMoveTargetTile + "     +++     " + selected.getX() + " " + selected.getY() + "   _+_+   " + tmp.len() +
//                    "  $Angle$ " + tmp.angle() + "  " + squadMoveTargetTile.angle(Tmp.v22.set(selected)));
//            System.out.println("Distance World.......   " + squadMoveTargetWorld.sub(Core.input.mouseWorld()).len() + "    Distance Screen.... " + squadMoveTargetScreen.sub(Core.input.mouse()).len());
//            System.out.println("Screen Size " + Core.graphics.getWidth() + "  X  " + Core.graphics.getHeight() + "   |    " + Core.graphics.getBackBufferWidth() + " " + Core.graphics.getBackBufferHeight());
            if ( !squadMoveTargetTile.isZero()) {
                if (Tmp.v22.set(squadMoveTargetScreen).sub(Core.input.mouse()).len() > Math.max(Core.graphics.getWidth(), Core.graphics.getHeight()) * 0.05f) {
                    float angle = Tmp.v22.set(squadMoveTargetScreen).sub(Core.input.mouse()).angle();
                    angle = (angle + 180) % 360;
                    float angleIso = ( ((360 - 45) + 0) % 360 ) + 90;   // iso 坐标
//                    squadGroup[player.getTeam().id][0].setTarget(squadMoveTargetTile.getX(), squadMoveTargetTile.getY(), angleIso);
                    Vars.systemStrategy.getSquad(player.getTeam().id, 0).setTarget(squadMoveTargetTile.getX(), squadMoveTargetTile.getY());
//                    squadGroup[player.getTeam().id][0].setTarget(Core.input.mouseWorld().x, Core.input.mouseWorld().y, angle);
//                    System.out.println("squad angle : " + indexarry[_i%indexarry.length]);
                }
                else {
                    Vars.systemStrategy.getSquad(player.getTeam().id, 0).setTarget(squadMoveTargetTile.getX(), squadMoveTargetTile.getY());
                }
            }
        }
        // zones debug add end
    }
    int _i = 0;
    int indexarry[] = {0, 0, 90, 90, 90, 180, 180, 180, 270, 270, 270, 360, 360};

    @Override
    public boolean selectedBlock(){
        return isPlacing() && mode != breaking;
    }

    @Override
    public float getMouseX(){
        return Core.input.mouseX();
    }

    @Override
    public float getMouseY(){
        return Core.input.mouseY();
    }

    @Override
    public void updateState(){
        if(state.is(State.menu)){
            droppingItem = false;
            mode = none;
            block = null;
            sreq = null;
            selectRequests.clear();
        }
    }


    // zones extend begon

    @Override
    public boolean keyDown(KeyCode keycode) {
        if (unitInput.keyDownEvent(keycode))
            return true;

        return false;
    }

    @Override
    public boolean keyUp(KeyCode keycode) {
        if (unitInput.keyUpEvent(keycode))
            return true;

        return false;
    }

    // zones extend end

    // zones add begon
//    private boolean selectUnitState = false;
//    private Array<SGGroundUnit> selectUnits = new Array<>();
//    /** 单位选择起始位置*/
//    private int unitX, unitY;
    @Deprecated
    private Vec2 squadMoveTargetTile = new Vec2();
    @Deprecated
    private Vec2 squadMoveTargetWorld = new Vec2();
    @Deprecated
    private Vec2 squadMoveTargetScreen = new Vec2();

    private UnitInput unitInput = new UnitInput();
    // zones add end
}
