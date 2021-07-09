package mindustry.world;

/**
 *  世界内容
 * */
public interface WorldContext{

    /** 返回指定索引瓦砾.<p/>Return a tile in the tile array.*/
    Tile tile(int x, int y);

    /** 创建瓦砾矩阵.<p/>Create the tile array.*/
    void resize(int width, int height);

    /** 创建一个瓦砾并将其放入容器然后返回.<p/>This should create a tile and put it into the tile array, then return it. */
    Tile create(int x, int y, int floorID, int overlayID, int wallID);

    /** 世界是否已经构建完毕.<p/>Returns whether the world is already generating.*/
    boolean isGenerating();

    /** 地图开始构建.<p/>Begins generating.*/
    void begin();

    /** 地图构建完毕.<p/>End generating, prepares tiles.*/
    void end();

}
