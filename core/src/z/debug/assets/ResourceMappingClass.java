package z.debug.assets;

/**
 *
 */
public enum ResourceMappingClass {
    /*
     * 类型和属性编码
     * */

    PIXMAP,
    TEXTURE,
        /** 纹理扩展， 绘制配置设置*/
        TEXTURE_LINEAR,
    TEXTUREREGION,
    TEXTUREATLAS,
    SOUND,
    MUSIC,
    BITMAPFONT,
    PACK,

    /*
     * 属性编码
     * */

    NULL,
    TEXTURE_ZB,
    TEXTUREATLAS_ZB,
    ;
}
