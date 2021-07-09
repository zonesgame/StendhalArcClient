package mindustry.ui;

import java.util.*;

/**
 *  定义内容预览图标的大小.<p/>
 * Defines sizes of a content's preview icon.
 * */
public enum Cicon{
    /** 全尺寸.<p/>Full size. */
    full(0),
    /** 微小*/
    tiny(8 * 2),
    /** 小*/
    small(8 * 3),
    /** 中等*/
    medium(8 * 4),
    /** 大*/
    large(8 * 5),
    /** 最大*/
    xlarge(8 * 6);

    public static final Cicon[] all = values();
    public static final Cicon[] scaled = Arrays.copyOfRange(all, 1, all.length);

    /** 尺寸*/
    public final int size;

    Cicon(int size){
        this.size = size;
    }
}
