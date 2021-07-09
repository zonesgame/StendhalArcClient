package z.system;

import arc.Core;
import arc.Events;
import arc.math.Mathf;
import arc.struct.ObjectMap;
import arc.util.serialization.XmlReader;
import mindustry.game.EventType;
import mindustry.world.Tile;

import static mindustry.Vars.world;

/**
 *  解决原版Floor不包含TileEntity无法获取纹理变体数据.
 */
public class GroundSystem {
    /** 地表纹理变体索引*/
    private int[][] variants;

    private ObjectMap<String, int[]> regionCenter = new ObjectMap<>();

    public GroundSystem() {
        Events.on(EventType.GroundSystemInitEvent.class, event -> init());

        // 初始化xmlshuju
        XmlReader.Element groundRoot = new XmlReader().parse(Core.files.internal("debug/xml/groundSystem.xml"));
        for (int i = 0; i < groundRoot.getChildCount(); i++) {
            XmlReader.Element node = groundRoot.getChild(i);
            String name = node.getAttribute("name");
            String[] tmp = node.getChildByName("center").getText().split(",");
            int[] offset = new int[tmp.length];
            for (int j = 0; j < offset.length; j++) {
                offset[j] = Integer.parseInt(tmp[j]);
            }
            regionCenter.put(name, offset);
        }
    }

    private void init() {
        variants = new int[world.width()][world.height()];
        for (int y = 0, height = world.height(); y < height; y++) {
            for (int x = 0, width = world.width(); x < width; x++) {
                Tile tile = world.tile(x, y);
                if (tile.floor().variants > 0) {
                    variants[x][y] = Mathf.random(tile.floor().variants - 1);
                }
            }
        }
    }

    public int getVariants(int x, int y) {
//        if (variants == null)   return 0;
        return variants[x][y];
    }

    public int[] getOffset(String blockName) {
        return regionCenter.get(blockName);
    }

}
