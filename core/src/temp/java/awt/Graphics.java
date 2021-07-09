package temp.java.awt;

import arc.graphics.Color;
import arc.graphics.g2d.TextureRegion;
import temp.java.awt.geom.Rectangle2D;

/**
 *
 */
public interface Graphics {

    public void setComposite(Composite composite);
    public Composite getComposite();

    public void setShapeColor(Color color);

    public void setColor(Color color);

    //    public void drawRect(float x, float y, float w, float h);
    public void fillRect(Color c, Rectangle2D rect2d);
    public void fillRect(Color c, float x, float y, float w, float h);

    public void drawOval(float x, float y, float w, float h);

    public boolean drawImage(TextureRegion img, int x, int y, Object observer);

    public boolean drawImage(TextureRegion img, int x, int y, int width, int height, Object observer);

    public void drawLine(Color color, float startX, float startY, float endX, float endY, float lineWidth) ;

    /**
     * @param dx1 the <i>x</i> coordinate of the first corner of the destination rectangle.
     * @param dy1 the <i>y</i> coordinate of the first corner of the destination rectangle.
     * @param dx2 the <i>x</i> coordinate of the second corner of the destination rectangle.
     * @param dy2 the <i>y</i> coordinate of the second corner of the destination rectangle.
     * @param sx1 the <i>x</i> coordinate of the first corner of the source rectangle.
     * @param sy1 the <i>y</i> coordinate of the first corner of the source rectangle.
     * @param sx2 the <i>x</i> coordinate of the second corner of the source rectangle.
     * @param sy2 the <i>y</i> coordinate of the second corner of the source rectangle.
     */
    public boolean drawImage(TextureRegion img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, Object observer);

//    public Batch getBatch();

    /**
     *  当前绘制的屏幕范围
     * */
    public Rectangle getClipBounds();



    // delete
    public void translate(int x, int y) ;

}
