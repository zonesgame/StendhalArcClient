package mindustry.entities.traits;


import arc.math.geom.*;
import arc.math.geom.QuadTree.QuadTreeObject;
import mindustry.Vars;

public interface SolidTrait extends QuadTreeObject, MoveTrait, VelocityTrait, Entity, Position{

    /** 获取正常碰撞矩阵*/
    void hitbox(Rect rect);

    /** 获取和瓦砾碰撞矩阵*/
    void hitboxTile(Rect rect);

    Vec2 lastPosition();

    /** 是否检测Tile碰撞, 飞行单位忽略.*/
    default boolean collidesGrid(int x, int y){
        return true;
    }

    default float getDeltaX(){
        return getX() - lastPosition().x;
    }

    default float getDeltaY(){
        return getY() - lastPosition().y;
    }

    default boolean collides(SolidTrait other){
        return true;
    }

    default void collision(SolidTrait other, float x, float y){
    }

    default void move(float x, float y){
        Vars.collisions.move(this, x, y);
    }
}
