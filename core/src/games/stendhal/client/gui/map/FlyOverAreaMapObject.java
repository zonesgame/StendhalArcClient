/***************************************************************************
 *                   (C) Copyright 2018 - Arianne                          *
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

import games.stendhal.client.entity.IEntity;
import temp.java.awt.geom.Rectangle2D;

public class FlyOverAreaMapObject extends StaticMapObject {
	/**The colour of fly over areas (orange).*/
	private static final Color COLOR = new Color(212 / 255f, 158 / 255f, 72 / 255f, 1f);

	public FlyOverAreaMapObject(IEntity entity) {
		super(entity);
	}

	@Override
	void draw(Graphics g, Rectangle2D drawRect, float actorx, float actory, float scale) {
		draw(g, drawRect, actorx, actory, scale, COLOR, null);
	}
}
