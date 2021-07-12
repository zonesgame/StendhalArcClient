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

import arc.graphics.Color;
import arc.graphics.g2d.BitmapFontCache;
import arc.graphics.g2d.GlyphLayout;
import arc.graphics.g2d.TextureRegion;
import arc.z.util.ZonesAnnotate;
import games.stendhal.client.gui.TransparencyMode;
import mindustry.ui.Fonts;

import temp.java.awt.Graphics;

/**
 * Outlined text representation of a string.
 */
public class TextSprite extends ImageSprite {

    @ZonesAnnotate.ZField
    private final BitmapFontCache fontCache;
    @ZonesAnnotate.ZField
    private final GlyphLayout layout;

    @ZonesAnnotate.ZMethod
    private TextSprite(TextureRegion image) {
        super(image);
        fontCache = null;
        layout = null;
    }

    @ZonesAnnotate.ZMethod
    private TextSprite(TextureRegion image, String text, Color color) {
        super(image);
//        {
//            if (Fonts.def != null || Fonts.chat != null || Fonts.icon != null || Fonts.outline != null) {
//                System.out.println((Fonts.def != null) + "  " + (Fonts.chat != null) + "  " + (Fonts.icon != null) + "  " + (Fonts.outline != null) );
//            }
//        }
        Fonts.outline.getData().setScale(0.6f);
        fontCache = Fonts.outline.getCache();
        fontCache.setColor(color);        // or 	fontCache.setColors(color, 0, text.length());
        layout = fontCache.setText(text, 0, 0);
//		Fonts.resetGameFontScale();
    }

    @ZonesAnnotate.ZMethod
    public static TextSprite createTextSprite(String text, final Color textColor) {
        // textColor.getRed() / 255f, textColor.getGreen() / 255f, textColor.getBlue() / 255f, textColor.getAlpha() / 255f)
        return new TextSprite(null, text, textColor);
    }


    // needed only because there's no other reliable way to calculate
    // string widths other than having a Graphics object
//	private static final Graphics graphics = (new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB)).getGraphics();
//
//	private TextSprite(Image image) {
//		super(image);
//	}
//
//	/**
//	 * Create a new <code>TextSprite</code>
//	 *
//	 * @param text The text to be rendered
//	 * @param textColor Color of the text
//	 * @return TextSprite with the wanted text
//	 */
//	public static TextSprite createTextSprite(String text, final Color textColor) {
//		final GraphicsConfiguration gc = getGC();
//		FontMetrics metrics = graphics.getFontMetrics();
//		LineMetrics lm = metrics.getLineMetrics(text, graphics);
//		final Image image = gc.createCompatibleImage(metrics.stringWidth(text)
//				+ 2, Math.round(lm.getHeight()) + 2, TransparencyMode.TRANSPARENCY);
//
//		drawOutlineString(image, textColor, text, 1, Math.round(lm.getAscent()));
//
//		return new TextSprite(image);
//	}

//    /**
//     * Draw a text string (like <em>Graphics</em><code>.drawString()</code>)
//     * only with an outline border. The area drawn extends 1 pixel out on all
//     * side from what would normal be drawn by drawString().
//     *
//     * @param image     image to draw to
//     * @param textColor The text color.
//     * @param text      The text to draw.
//     * @param x         X position.
//     * @param y         Y position.
//     */
//    private static void drawOutlineString(final Image image, final Color textColor,
//                                          final String text, final int x, final int y) {
//        /*
//         * Use light gray as outline for colors < 25% bright. Luminance = 0.299R +
//         * 0.587G + 0.114B
//         */
//        final int lum = ((textColor.getRed() * 299) + (textColor.getGreen() * 587) + (textColor.getBlue() * 114)) / 1000;
//
//        Color outlineColor;
//        if (lum >= 64) {
//            outlineColor = Color.black;
//        } else {
//            outlineColor = Color.lightGray;
//        }
//        drawOutlineString(image, textColor, outlineColor, text, x, y);
//    }

//    /**
//     * Draw a text string (like <em>Graphics</em><code>.drawString()</code>)
//     * only with an outline border. The area drawn extends 1 pixel out on all
//     * side from what would normal be drawn by drawString().
//     *
//     * @param image        Image to draw to.
//     * @param textColor    The text color.
//     * @param outlineColor The outline color.
//     * @param text         The text to draw.
//     * @param x            The X position.
//     * @param y            The Y position.
//     */
//    private static void drawOutlineString(final Image image, final Color textColor,
//                                          final Color outlineColor, final String text, final int x,
//                                          final int y) {
//        Graphics g = image.getGraphics();
//        g.setColor(outlineColor);
//
//        // The same text will be drawn eight times to create a border
//        // note that this is not a good solution, but re-using the image
//        // to draw it again doesn't work on Mac OSX
//        g.drawString(text, x - 1, y - 1);
//        g.drawString(text, x + 1, y + 1);
//        g.drawString(text, x - 1, y + 1);
//        g.drawString(text, x, y - 1);
//        g.drawString(text, x + 1, y);
//        g.drawString(text, x - 1, y);
//        g.drawString(text, x, y + 1);
//        g.drawString(text, x + 1, y - 1);
//
//        g.setColor(textColor);
//        g.drawString(text, x, y);
//    }

    @ZonesAnnotate.ZAdd
    @Override
    public void draw(Graphics g, int x, int y) {
        if (true) {
//            x += -layout.width / 2;
            y += -layout.height * 0.8f; // default 0.5
            fontCache.setPosition(x, y);

            fontCache.draw();       // g.getBatch()
        } else {
            super.draw(g, x, y);
        }
    }

    @ZonesAnnotate.ZAdd
    @Override
    public void draw(Graphics g, int destx, int desty, int x, int y, int w, int h) {
        if (true) {
            throw new RuntimeException(this.getClass().getName());
        } else {
            super.draw(g, destx, desty, x, y, w, h);
        }
    }

    @ZonesAnnotate.ZAdd
    @Override
    public int getHeight() {
        return (int) layout.height;
    }

    @ZonesAnnotate.ZAdd
    @Override
    public int getWidth() {
        return (int) layout.width;
    }

}
