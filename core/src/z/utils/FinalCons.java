package z.utils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

import arc.files.Fi;
import arc.math.Angles;
import arc.math.geom.Position;
import arc.math.geom.Vec2;
import arc.struct.Array;
import arc.util.ArcRuntimeException;
import arc.util.io.Streams;
import arc.z.util.ISOUtils;
import mindustry.world.Tile;

import static z.debug.ZDebug.enable_isoInput;

/**
 *
 */
public class FinalCons {
    /** 一秒*/
    public static final float second = 60f;
    /** 一分*/
    public static final float minute = second * 60f;
    /** 一小时*/
    public static final float hour = minute * 60f;

    /**  团队最大队伍数量*/
    public static final int max_squad_count = 16;
    /** 队伍最大成员数量*/
    public static final int max_member_count = 32;

    public static interface SETTING_KEYS {
        public final String lastLogin = "LASTLOGIN";
        public final String savePassword = "SAVEPASSWORD";
    }

    public static interface PreFix {
        public final String pfCharaName = "PREFIX_CHARACTERNAME";
    }


    // function begon

    public static float angleTo(Tile self, Tile other){
        if (enable_isoInput && true) {
            return Angles.angle(self.drawxIso(), self.drawyIso(), other.drawxIso(), other.drawyIso());
        }
        return Angles.angle(self.getX(), self.getY(), other.getX(), other.getY());
    }

    public static float angleTo(float x, float y, Tile other){
        if (enable_isoInput && true) {
            return Angles.angle(x, y, other.drawxIso(), other.drawyIso());
        }
        return Angles.angle(x, y, other.getX(), other.getY());
    }

    public static float angleTo(float x, float y, Position other){
        if (enable_isoInput && true) {
            Vec2 dPos = ISOUtils.tileToWorldCoords(other);
            return Angles.angle(x, y, dPos.x, dPos.y);
        }
        return Angles.angle(x, y, other.getX(), other.getY());
    }

    /** 反转容器内容*/
    public static<T> void sortFlip(Array<T> arr) {
        if (arr.size <= 1)  return;

        for (int i = 0, len = arr.size - 1; i < len; i++, len--) {
            T temp = arr.get(i);
            arr.set(i, arr.get(len));
            arr.set(len, temp);
        }
    }

    // function end


    // debug begon
    public static void createFile(Fi file, String xmlFile) {
        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new OutputStreamWriter(file.write(false)));
            out.write(xmlFile);
//            out.write(new String(xmlFile.toString().getBytes("GBK"), "UTF-8"));
        } catch (ArcRuntimeException ex) {

        } catch (IOException e) {

        } finally {
            Streams.close(out);
        }

//        Writer out = new BufferedWriter(new OutputStreamWriter(file.write(false)));
//        XmlWriter xmlWriter = new XmlWriter(out);
//
//        try {
//            xmlWriter.write(xmlFile.toString());
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            StreamUtils.closeQuietly(xmlWriter);
//        }
    }
    // debug end
}
