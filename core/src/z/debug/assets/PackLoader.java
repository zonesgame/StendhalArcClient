package z.debug.assets;

import com.badlogic.gdx.graphics.g2d.Gdx2DPixmap;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.zip.GZIPInputStream;

import arc.Core;
import arc.files.Fi;
import arc.graphics.Pixmap;
import arc.graphics.Texture;
import arc.graphics.g2d.TextureRegion;
import arc.math.geom.Rect;
import arc.struct.ObjectMap;
import arc.util.Log;
import arc.util.io.Streams;
import arc.util.serialization.XmlReader;

import static arc.graphics.g2d.Draw.scl;

/**
 *
 */
public class PackLoader {

    private ObjectMap<String, Texture> textures = new ObjectMap<>(128);
    public ObjectMap<String, TextureRegion[][]> packs = new ObjectMap<>(128);
    public ObjectMap<String, Rect[][]> rects = new ObjectMap<>(128);


    private XmlReader reader;

    private PackLoader() {
        reader = new XmlReader();
    }

    private static PackLoader instance = null;
    public static PackLoader getInstance() {
        if (instance == null)
            instance = new PackLoader();
        return instance;
    }

    public void load() {

    }

    public void loadAsync (Fi file) {
        Coding coding = Coding.values()[Integer.parseInt(file.extension().substring(1)) - 1];
        byte[] xmlbytes = ZonesAssetLoader.getZonesAssets(coding, file.readBytes());
        XmlReader.Element root = reader.parse(new String(xmlbytes));

        Fi packfile = Core.files.internal(file.pathWithoutExtension() + ".pack");
        byte[] bytes = packfile.readBytes();
        InputStream inputStream = null;
        try {
            inputStream = new GZIPInputStream(new ByteArrayInputStream(bytes), bytes.length);

            for (int i = 0; i < root.getChildCount(); i++) {
                XmlReader.Element node = root.getChild(i);
                int nodeSize = node.getIntAttribute("size", 0);
                Coding nodeCoding = Coding.valueOf(node.getAttribute("coding").toUpperCase());

                byte[] nodBytes = new byte[nodeSize];
                inputStream.read(nodBytes);
                nodBytes = ZonesAssetLoader.getZonesAssets(nodeCoding, nodBytes);

                String nodeKey = node.getAttribute("path");
                Pixmap pixmap = new Pixmap(new Gdx2DPixmap(nodBytes, 0, nodBytes.length, 0));
                Texture texture = new Texture(pixmap);
                texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
                textures.put(nodeKey, texture);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Streams.close(inputStream);
        }
    }

    /**
     * @param flipDir 5位方向纹理反转补齐为8位方向
     * */
    public void loadRegions(Fi file, boolean flipDir) {
        Coding coding = Coding.ZO;
        byte[] txtbytes = ZonesAssetLoader.getZonesAssets(coding, file.readBytes());
        BufferedReader reader = new BufferedReader(new StringReader(new String(txtbytes)));
        try {
            while (true) {
                String line = reader.readLine();
                if (line == null) break;
                if (line.trim().length() == 0)  continue;

                int indexof = line.indexOf(',');
                String key = line.substring(0, indexof);
                String[] data = line.substring(indexof + 1).split(",");
                int files = Integer.parseInt(data[0]);
                int frames = Integer.parseInt(data[1]);
                int index = 2;  // use for data

                TextureRegion nodeRegions[][] = new TextureRegion[(flipDir && files == 5) ? 8 : files][frames];
                Rect[][] nodeRects = new Rect[(flipDir && files == 5) ? 8 : files][frames];
                for (int i = 0; i < files; i++) {
                    Texture texture = textures.get(key + filesName[i]);
                    int startX = 0;

                    for (int j = 0; j < frames; j++) {
                        int centerX = Integer.parseInt(data[index++]);
                        int centerY = Integer.parseInt(data[index++]);
                        int sizeW = Integer.parseInt(data[index++]);
                        int sizeH = Integer.parseInt(data[index++]);

                        nodeRegions[i][j] = new TextureRegion(texture, startX, 0, sizeW, sizeH);
                        centerY = sizeH - centerY;
                        float scale = 1 / 1f;
                        nodeRects[i][j] = new Rect(centerX * scale, centerY * scale, sizeW * scale, sizeH * scale);

                        startX += sizeW;
                    }
                }

                // 翻转Sprite缺失方向动画
                if (flipDir && files == 5) {
                    int dirCount = 8;
                    for (int d = files; d < dirCount; d++) {
                        int needDirection = dirCount - d;
                        TextureRegion[] needRegions = nodeRegions[needDirection];
                        Rect[] needOffsets = nodeRects[needDirection];

                        for (int f = 0; f < frames; f++) {
                            nodeRects[d][f] = new Rect(needOffsets[f]);
                            nodeRects[d][f].x = (needOffsets[f].getWidth() - needOffsets[f].x);

                            nodeRegions[d][f] = new TextureRegion(needRegions[f]);
                            nodeRegions[d][f].flip(true, false);
                        }
                    }
                }

                packs.put(key, nodeRegions);
                rects.put(key, nodeRects);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            Streams.close(reader);
//            textures.clear();
        }
    }

    public TextureRegion getRegion(String name, int type, int frame) {
        if (packs.get(name) != null)
            return packs.get(name)[type][frame];
        else
            return Core.atlas.find("error");
    }

    public Rect getRect(String name, int type, int frame) {
        if (rects.get(name) != null)
            return rects.get(name)[type][frame];
        else
            return Rect.tmp.set(0, 0, 48 * scl, 48 * scl);
    }

    private final String filesName[] = {
            "A", "B", "C", "D", "E", "F", "G", "H"
    };

//    private final int[] directionIndex = {2, 1, 0, 7, 6, 5, 4, 3};

}
