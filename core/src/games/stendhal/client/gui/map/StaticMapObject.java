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
package games.stendhal.client.gui.map;

import arc.graphics.Color;
import temp.java.awt.Graphics;

import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import games.stendhal.client.entity.IEntity;
import mindustry.gen.Tex;
import temp.java.awt.geom.Rectangle2D;

abstract class StaticMapObject extends MapObject {

//	protected Color curColor = null;
	protected TextureRegion region;

	StaticMapObject(final IEntity entity) {
		super(entity);
//		this.curColor = Color.white;
		this.region = Tex.whiteui.getRegion();
	}

	/**
	 * Draw the entity
	 *
	 * @param g Graphics context
	 * @param scale Scaling factor
	 * @param color Drawing Color
	 * @param outline Outline color, or <code>null</code> if no outline
	 */
	void draw(final Graphics g, final Rectangle2D drawRect, final float actorx, final float actory, final float scale, final Color color, final Color outline) {
		if ( !drawRect.contains(x, y, width, height)) return;

		final float rx = worldToCanvas(x, scale);
		final float ry = worldToCanvas(y, scale);
		final float rwidth = width * scale;
		final float rheight = height * scale;

		final float dx = actorx + rx;
		final float dy = actory + (float) ((drawRect.getHeight() + drawRect.getY()) * scale) - (ry + rheight);

		if (outline != null) {
			Draw.color(outline);
			Draw.rect(region, dx, dy, rwidth - 1, rheight - 1);
		}

		Draw.color(color);
		Draw.rect(region, dx, dy, rwidth, rheight);
	}
}
