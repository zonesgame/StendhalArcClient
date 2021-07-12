/* $Id$ */
/***************************************************************************
 *                   (C) Copyright 2003-2010 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.client.sprite;

import arc.graphics.g2d.TextureRegion;
import arc.z.util.ZonesAnnotate;
import temp.java.awt.Graphics;

/**
 * A sprite to be displayed on the screen. Note that a sprite contains no state
 * information, i.e. its just the image and not the location. This allows us to
 * use a single sprite in lots of different places without having to store
 * multiple copies of the image.
 *
 * @author Kevin Glass
 */
public class ImageSprite implements Sprite {
    /**
     * The image to be drawn for this sprite.
     */
    private final TextureRegion image;

    /**
     * The identifier reference.
     */
    private final Object reference;

    /** zones add*/
    @ZonesAnnotate.ZField
    private float scale = 1;
    @ZonesAnnotate.ZField
    private boolean isScale = false;
    @ZonesAnnotate.ZField
    private int width, height;
    // zones add end

    /**
     * Create a new sprite based on an image.
     *
     * @param image The image that is this sprite
     */
    public ImageSprite(final TextureRegion image) {
        this(image, null);
    }

    /**
     * Create a new sprite based on an image.
     *
     * @param image     The image that is this sprite.
     * @param reference The sprite reference, or null.
     */
    public ImageSprite(final TextureRegion image, final Object reference) {
        this.image = image;
        this.reference = reference;
    }

    /**
     * Create an image sprite from another sprite.
     *
     * @param sprite The source sprite.
     */
    public ImageSprite(final Sprite sprite) {
        this(sprite, null);
    }

    /**
     * Create a copy of another sprite.
     *
     * @param sprite    The source sprite.
     * @param reference The sprite reference, or null.
     */
    private ImageSprite(final Sprite sprite, final Object reference) {
        if (sprite == null) {
            this.image = null;
            this.reference = null;
            return;
        }
        if (sprite instanceof ImageSprite) {
            this.image = ((ImageSprite) sprite).image;
            this.reference = reference;
            return;
        }
        if (true)
            throw new RuntimeException(this.getClass().getName());

        this.image = null;
        this.reference = null;
    }

    //
    // ImageSprite
    //

    @ZonesAnnotate.ZMethod
    public Sprite setScale(float scale) {
        this.scale = scale;
        if (scale != 1)
            isScale = true;
        width = (int) (image.getWidth() * scale);
        height = (int) (image.getHeight() * scale);
        return this;
    }

//    protected static GraphicsConfiguration getGC() {
//        return GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
//    }

    /**
     * Get the graphics context of the underlying image.
     *
     * @return The graphics context.
     */
//    public Graphics getGraphics() {
//        return image.getGraphics();
//    }

    //
    // Sprite
    //

    /**
     * Create a sub-region of this sprite. <strong>NOTE: This does not use
     * caching.</strong>
     *
     * @param x      The starting X coordinate.
     * @param y      The starting Y coordinate.
     * @param width  The region width.
     * @param height The region height.
     * @param ref    The sprite reference.
     * @return A new sprite.
     */
    @Override
    public Sprite createRegion(final int x, final int y, int width,
                               int height, final Object ref) {
        final int iwidth = getWidth();
        final int iheight = getHeight();

        if ((x >= iwidth) || (y >= iheight)) {
            /*
             * Outside of image (nothing to draw)
             */
            return new EmptySprite(width, height, ref);
        }
        // Exclude regions outside the original image
        width = Math.min(width, iwidth);
        height = Math.min(height, iheight);

        if ( !(image instanceof TextureRegion)) {
            if (true)
                throw new RuntimeException(this.getClass().getName());
            return null;
        }

        TextureRegion newRegion = new TextureRegion(image.getTexture(), x, y, width, height);
//        newRegion.flip(false, true);
        return new ImageSprite(newRegion, ref);
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
        if ( !isScale)
            g.drawImage(image, x, y, null);
        else
            g.drawImage(image, x, y, width, height, null);
    }

    /**
     * Draws the image.
     *
     * @param g     the graphics context where to draw to
     * @param destx destination x
     * @param desty destination y
     * @param x     the source x
     * @param y     the source y
     * @param w     the width
     * @param h     the height
     */
    @Override
    public void draw(final Graphics g, final int destx, final int desty, final int x, final int y, final int w,
                     final int h) {
        g.drawImage(image, destx, desty, destx + w, desty + h, x, y, x + w, y + h, null);
    }

    /**
     * Get the height of the drawn sprite.
     *
     * @return The height in pixels of this sprite
     */
    @Override
    public int getHeight() {
        if (image == null) {
            return 0;
        }
        return image.getHeight();
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
        if (image == null) {
            return 0;
        }
        return image.getWidth();
    }

    @Override
    public boolean isConstant() {
        return true;
    }

//    /**
//     * Retrieves a single frame from the image.
//     *
//     * @param xIndex Horizontal index.
//     * @param yIndex Vertical index.
//     * @return Cropped Sprite.
//     */
//    public Sprite getFrame(final int xIndex, final int yIndex) {
//        final BufferedImage orig = (BufferedImage) image;
//
//        final int w = getWidth() / 3;
//        final int h = getHeight() / 4;
//        final int x = w * xIndex;
//        final int y = h * yIndex;
//
//        return new ImageSprite(orig.getSubimage(x, y, w, h));
//    }

    @ZonesAnnotate.ZMethod
    public TextureRegion getImage() {
        return image;
    }
}
