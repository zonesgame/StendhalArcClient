package temp.java.awt;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import temp.java.awt.geom.Rectangle2D;

public class Graphics2D implements Graphics {

    @Override
    public void setComposite(Composite composite) {

    }

    @Override
    public Composite getComposite() {
        return null;
    }

    @Override
    public void setShapeColor(Color color) {

    }

    @Override
    public void setColor(Color color) {

    }

    @Override
    public void fillRect(Color c, Rectangle2D rect2d) {

    }

    @Override
    public void fillRect(Color c, float x, float y, float w, float h) {

    }

    @Override
    public void drawOval(float x, float y, float w, float h) {

    }

    @Override
    public boolean drawImage(TextureRegion img, int x, int y, Object observer) {
        Draw.rectGdx(img, x, y);

        return false;
    }

    @Override
    public boolean drawImage(TextureRegion img, int x, int y, int width, int height, Object observer) {
        return false;
    }

    @Override
    public void drawLine(Color color, float startX, float startY, float endX, float endY, float lineWidth) {

    }

    @Override
    public boolean drawImage(TextureRegion img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, Object observer) {
        return false;
    }

    @Override
    public Rectangle getClipBounds() {
        return null;
    }

    @Override
    public void translate(int x, int y) {

    }
}
