package mindustry.entities;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.math.geom.QuadTree;
import arc.math.geom.Rect;
import arc.math.geom.Vec2;
import arc.struct.Array;
import arc.util.Tmp;
import mindustry.core.Renderer;
import mindustry.entities.traits.Entity;
import mindustry.entities.traits.SolidTrait;
import mindustry.world.Tile;
import z.utils.ShapeRenderer;

import static mindustry.Vars.tilesize;
import static mindustry.Vars.tileunit;
import static mindustry.Vars.world;
import static z.debug.ZDebug.enable_drawDebug;
import static z.debug.ZDebug.enable_isoInput;

/**
 *  实体的碰撞处理
 * */
public class EntityCollisions{
    // 瓦砾碰撞检测范围. range for tile collision scanning
    private static final int r = 1;
    // 移动一单位块. move in 1-unit chunks
    private static final float seg = 1f;

    // 瓦砾碰撞. tile collisions
    private Rect tmp = new Rect();
    private Vec2 vector = new Vec2();
    private Vec2 l1 = new Vec2();
    private Rect r1 = new Rect();
    private Rect r2 = new Rect();

    //实体碰撞. entity collisions
    private Array<SolidTrait> arrOut = new Array<>();

    /** 实体更新步长移动*/
    public void move(SolidTrait entity, float deltax, float deltay){

        boolean movedx = false;

        while(Math.abs(deltax) > 0 || !movedx){
            movedx = true;
            moveDelta(entity, Math.min(Math.abs(deltax), seg) * Mathf.sign(deltax), 0, true);

            if(Math.abs(deltax) >= seg){
                deltax -= seg * Mathf.sign(deltax);
            }else{
                deltax = 0f;
            }
        }

        boolean movedy = false;

        while(Math.abs(deltay) > 0 || !movedy){
            movedy = true;
            moveDelta(entity, 0, Math.min(Math.abs(deltay), seg) * Mathf.sign(deltay), false);

            if(Math.abs(deltay) >= seg){
                deltay -= seg * Mathf.sign(deltay);
            }else{
                deltay = 0f;
            }
        }
    }

    /** 处理瓦砾碰撞*/
    public void moveDelta(SolidTrait entity, float deltax, float deltay, boolean x){

        Rect rect = r1;     // 瓦砾碰撞范围
        entity.hitboxTile(rect);
        entity.hitboxTile(r2);
        rect.x += deltax;
        rect.y += deltay;

        int tilex = Math.round((rect.x + rect.width / 2) / tilesize), tiley = Math.round((rect.y + rect.height / 2) / tilesize);
        if (enable_isoInput) {
            tilex = Math.round(rect.x + rect.width / 2);
            tiley = Math.round(rect.y + rect.height / 2);
        }

        for(int dx = -r; dx <= r; dx++){
            for(int dy = -r; dy <= r; dy++){
                int wx = dx + tilex, wy = dy + tiley;
                if(solid(wx, wy) && entity.collidesGrid(wx, wy)){
                    tmp.setSize(tilesize).setCenter(wx * tilesize, wy * tilesize);
                    if (enable_isoInput) {
                        tmp.setSize(tileunit).setCenter(wx, wy);
                    }

                    if(tmp.overlaps(rect)){
                        if (enable_drawDebug) {
                            Renderer.addDebugDraw(((nulltile) -> {
                                Draw.color(Color.blue);
                                entity.hitbox(Tmp.r2);
                                ShapeRenderer.drawDiamondUnit(Tmp.r2.x + Tmp.r2.width / 2f, Tmp.r2.y + Tmp.r2.height / 2f, Tmp.r2.width, Tmp.r2.height);
//                                ShapeRenderer.drawDiamondUnit(tmp.x + tmp.width / 2f, tmp.y + tmp.height / 2f, tmp.width, tmp.height);
                                Draw.color(Color.red);
                                entity.hitboxTile(Tmp.r3);
                                ShapeRenderer.drawDiamondUnit(Tmp.r3.x + Tmp.r3.width / 2f, Tmp.r3.y + Tmp.r3.height / 2f, Tmp.r3.width, Tmp.r3.height);
//                                ShapeRenderer.drawDiamondUnit(rect.x + rect.width / 2f, rect.y + rect.height / 2f, rect.width, rect.height);
                                Draw.color();
                            }));
                        }
                        Vec2 v = Geometry.overlap(rect, tmp, x);
                        rect.x += v.x;
                        rect.y += v.y;
                    }
                }
            }
        }

        entity.setX(entity.getX() + rect.x - r2.x);
        entity.setY(entity.getY() + rect.y - r2.y);
    }

    public boolean overlapsTile(Rect rect){
        rect.getCenter(vector);
        int r = 1;

        //assumes tiles are centered
        int tilex = Math.round(vector.x / tilesize);
        int tiley = Math.round(vector.y / tilesize);
        if (enable_isoInput) {
            tilex = Math.round(vector.x);
            tiley = Math.round(vector.y);
        }

        for(int dx = -r; dx <= r; dx++){
            for(int dy = -r; dy <= r; dy++){
                int wx = dx + tilex, wy = dy + tiley;
                if(solid(wx, wy)){
                    r2.setSize(tilesize).setCenter(wx * tilesize, wy * tilesize);
                    if (enable_isoInput) {
                        r2.setSize(tileunit).setCenter(wx, wy);
                    }

                    if(r2.overlaps(rect)){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public <T extends Entity> void updatePhysics(EntityGroup<T> group){

        QuadTree tree = group.tree();
        tree.clear();

        for(Entity entity : group.all()){
            if(entity instanceof SolidTrait){
                SolidTrait s = (SolidTrait)entity;
                s.lastPosition().set(s.getX(), s.getY());
                tree.insert(s);
            }
        }
    }

    private static boolean solid(int x, int y){
        Tile tile = world.tile(x, y);
        return tile != null && tile.solid();
    }

    private void checkCollide(Entity entity, Entity other){

        SolidTrait a = (SolidTrait)entity;
        SolidTrait b = (SolidTrait)other;

        a.hitbox(this.r1);
        b.hitbox(this.r2);

        r1.x += (a.lastPosition().x - a.getX());
        r1.y += (a.lastPosition().y - a.getY());
        r2.x += (b.lastPosition().x - b.getX());
        r2.y += (b.lastPosition().y - b.getY());

        float vax = a.getX() - a.lastPosition().x;
        float vay = a.getY() - a.lastPosition().y;
        float vbx = b.getX() - b.lastPosition().x;
        float vby = b.getY() - b.lastPosition().y;

        if(a != b && a.collides(b) && b.collides(a)){
            l1.set(a.getX(), a.getY());
            boolean collide = r1.overlaps(r2) || collide(r1.x, r1.y, r1.width, r1.height, vax, vay,
            r2.x, r2.y, r2.width, r2.height, vbx, vby, l1);
            if(collide){
                a.collision(b, l1.x, l1.y);
                b.collision(a, l1.x, l1.y);
            }
        }
    }

    private boolean collide(float x1, float y1, float w1, float h1, float vx1, float vy1,
                            float x2, float y2, float w2, float h2, float vx2, float vy2, Vec2 out){
        float px = vx1, py = vy1;

        vx1 -= vx2;
        vy1 -= vy2;

        float xInvEntry, yInvEntry;
        float xInvExit, yInvExit;

        if(vx1 > 0.0f){
            xInvEntry = x2 - (x1 + w1);
            xInvExit = (x2 + w2) - x1;
        }else{
            xInvEntry = (x2 + w2) - x1;
            xInvExit = x2 - (x1 + w1);
        }

        if(vy1 > 0.0f){
            yInvEntry = y2 - (y1 + h1);
            yInvExit = (y2 + h2) - y1;
        }else{
            yInvEntry = (y2 + h2) - y1;
            yInvExit = y2 - (y1 + h1);
        }

        float xEntry, yEntry;
        float xExit, yExit;

        xEntry = xInvEntry / vx1;
        xExit = xInvExit / vx1;

        yEntry = yInvEntry / vy1;
        yExit = yInvExit / vy1;

        float entryTime = Math.max(xEntry, yEntry);
        float exitTime = Math.min(xExit, yExit);

        if(entryTime > exitTime || xExit < 0.0f || yExit < 0.0f || xEntry > 1.0f || yEntry > 1.0f){
            return false;
        }else{
            float dx = x1 + w1 / 2f + px * entryTime;
            float dy = y1 + h1 / 2f + py * entryTime;

            out.set(dx, dy);

            return true;
        }
    }

    @SuppressWarnings("unchecked")
    public void collideGroups(EntityGroup<?> groupa, EntityGroup<?> groupb){

        for(Entity entity : groupa.all()){
            if(!(entity instanceof SolidTrait))
                continue;

            SolidTrait solid = (SolidTrait)entity;

            solid.hitbox(r1);
            r1.x += (solid.lastPosition().x - solid.getX());
            r1.y += (solid.lastPosition().y - solid.getY());

            solid.hitbox(r2);
            r2.merge(r1);

            arrOut.clear();
            groupb.tree().getIntersect(arrOut, r2);

            for(SolidTrait sc : arrOut){
                sc.hitbox(r1);
                if(r2.overlaps(r1)){
                    checkCollide(entity, sc);
                }
            }
        }
    }
}
