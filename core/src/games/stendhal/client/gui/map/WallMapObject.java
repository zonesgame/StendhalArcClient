/***************************************************************************
 *                   (C) Copyright 2013 - Faiumoni e. V.                   *
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

import temp.java.awt.Graphics;

import arc.graphics.Color;
import games.stendhal.client.entity.IEntity;
import temp.java.awt.geom.Rectangle2D;

/**
 * representation of a wall entity on the map
 *
 * @author hendrik
 */
public class WallMapObject extends StaticMapObject {
	private static final Color COLOR_BLOCKED = new Color(1.0f, 0.0f, 0.0f, 1f);

	/**
	 * a wall map object
	 *
	 * @param entity Entity
	 */
	public WallMapObject(final IEntity entity) {
		super(entity);
	}

	@Override
	public void draw(final Graphics g, final Rectangle2D drawRect, final float actorx, final float actory, float stagescale, float addy, final float scale) {
		draw(g, drawRect, actorx, actory, stagescale, addy, scale, COLOR_BLOCKED, null);
	}
}
