package z.debug.assets;

/**
 * Z起始编码
 * B起始压缩
 * O 无操作
 */
public enum Coding {

    /**
     * null
     */
    OO,
    /**
     * Zip
     */
    BO,
    /**
     * Base64
     */
    ZO,   //
    /**
     * Zip--Base64
     */
    ZB,
    /**
     * Base64--Zip
     */
    BZ,   //
    ;
}
