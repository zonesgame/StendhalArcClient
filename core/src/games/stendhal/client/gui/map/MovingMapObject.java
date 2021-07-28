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
import games.stendhal.client.entity.EntityChangeListener;
import games.stendhal.client.entity.IEntity;
import mindustry.gen.Tex;
import temp.java.awt.geom.Rectangle2D;

public class MovingMapObject extends MapObject implements EntityChangeListener<IEntity> {
	/**
	 * The color of a general entity (pale green).
	 */
	private static final Color COLOR = new Color(200 / 255f, 255 / 255f, 200 / 255f, 1);

	protected Color curColor;
	protected TextureRegion region;

	public MovingMapObject(final IEntity entity) {
		super(entity);
		this.curColor = COLOR;
		this.region = Tex.whiteui.getRegion();

		entity.addChangeListener(this);
	}

	@Override
	void draw(final Graphics g, final Rectangle2D drawRect, final float actorx, float actory, final float stageScale, final float addy, final float scale) {
		draw(g, drawRect, actorx, actory, stageScale, addy, scale, curColor);
	}

	/**
	 * Draw the <code>RPEntity</code> in specified color.
	 * @param g Graphics context
	 * @param scale Scaling factor
	 * @param color Drawing color
	 */
	void draw(final Graphics g, final Rectangle2D drawRect, final float actorx, final float actory, final float stageScale, final float addy, final float scale, final Color color) {
		if ( !drawRect.contains(x, y, width, height)) return;

		final float rx = worldToCanvas(x, scale) * stageScale;
		final float ry = worldToCanvas(y, scale) * stageScale;
		final float rwidth = width * scale * stageScale;
		final float rheight = height * scale * stageScale;

		final float dx = actorx + rx;
		final float dy = actory + (float) ((drawRect.getHeight() + drawRect.getY()) * scale) * stageScale - (ry + rheight);

		Draw.color(color);
		Draw.rectGdx(region, dx, dy - addy, rwidth, rheight);
//		g.setColor(color);
//		g.fillRect(rx, ry, rwidth, rheight);
	}

	@Override
	public void entityChanged(final IEntity entity, final Object property) {
		if (property == IEntity.PROP_POSITION) {
			x = (float) entity.getX();
			y = (float) entity.getY();
		}
	}
}
