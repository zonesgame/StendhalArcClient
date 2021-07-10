/***************************************************************************
 *                 (C) Copyright 2003-2014 - Faiumoni e.V.                 *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.client.gui;


import arc.graphics.Color;
import temp.java.awt.Graphics;

/**
 * An effect that flashes the screen for specified duration, using the event
 * strength attribute as the alpha value of the drawn flash.
 */
public class LightningEffect extends EffectLayer {
	final Color c;

	public LightningEffect(int duration, int strength) {
		super(duration);
		c = new Color(1, 1, 1, alpha((int) (255 * (strength / 100.0))) / 255f);
	}

	@Override
	public void drawScreen(Graphics g, int x, int y, int w, int h) {
//		g.setColor(c);
//		g.fillRect(x, y, w, h);
		g.fillRect(c, x, y, w, h);
	}
}
