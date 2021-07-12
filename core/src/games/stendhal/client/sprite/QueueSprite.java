package games.stendhal.client.sprite;

import arc.struct.Array;
import temp.java.awt.Graphics;

/**
 *  队列Sprite 执行批量绘制
 */
public class QueueSprite implements Sprite {
    /**
     * The image to be drawn for this sprite.
     */
    private final Sprite[] image;

    /**
     * The identifier reference.
     */
    private final Object reference;

    public QueueSprite(final Array<Sprite> array) {
        Sprite[] sprites = new Sprite[array.size];
        int len = 0;
        for (int i = 0; i < array.size; i++) {
            if (array.get(i) != null) {
                sprites[len++] = array.get(i);
            }
        }
        Sprite[] copySprites = new Sprite[len];
        System.arraycopy(sprites, 0, copySprites, 0, len);

        this.image = copySprites;
        this.reference = null;
    }

    /**
     * Create a new sprite based on an image.
     *
     * @param image The image that is this sprite
     */
    public QueueSprite(final Sprite[] image) {
        this(image, null);
    }

    /**
     * Create a new sprite based on an image.
     *
     * @param image     The image that is this sprite.
     * @param reference The sprite reference, or null.
     */
    public QueueSprite(final Sprite[] image, final Object reference) {
        this.image = image;
        this.reference = reference;
    }

    //
    // ImageSprite
    //

    @Override
    public Sprite createRegion(final int x, final int y, int width, int height, final Object ref) {
        Sprite[] sprites = new Sprite[image.length];
        for (int i = 0; i < sprites.length; i++) {
            sprites[i] = image[i].createRegion(x, y, width, height, ref);
        }

        return new QueueSprite(sprites, ref);
    }

    /**
     * Draw the sprite onto the graphics context provided.
     *
     * @param g The graphics context on which to draw the sprite
     * @param x The x location at which to draw the sprite
     * @param y The y location at which to draw the sprite
     */
    @Override
    public void draw(final Graphics g, final int x, final int y) {
        for (Sprite sprite : image) {
            sprite.draw(g, x, y);
        }
    }


    @Override
    public void draw(final Graphics g, final int destx, final int desty, final int x, final int y, final int w, final int h) {
        for (Sprite s : image) {
            s.draw(g, destx, desty, x, y, w, h);
        }
    }


    @Override
    public int getHeight() {
        return image[0].getHeight();
    }

    /**
     * Get the sprite reference. This identifier is an externally opaque object
     * that implements equals() and hashCode() to uniquely/repeatably reference
     * a keyed sprite.
     *
     * @return The reference identifier, or <code>null</code> if not
     * referencable.
     */
    @Override
    public Object getReference() {
        return reference;
    }

    /**
     * Get the width of the drawn sprite.
     *
     * @return The width in pixels of this sprite
     */
    @Override
    public int getWidth() {
        return image[0].getWidth();
    }

    @Override
    public boolean isConstant() {
        return true;
    }
}
