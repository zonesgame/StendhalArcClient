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

import games.stendhal.client.entity.IEntity;
import temp.java.awt.geom.Rectangle2D;

public class WalkBlockerMapObject extends StaticMapObject {
	/**The colour of walk blockers (dark pink) .*/
    private static final Color COLOR = new Color(209 / 255f, 144 / 255f, 224 / 255f, 1f);

	public WalkBlockerMapObject(final IEntity entity) {
		super(entity);
	}

	@Override
	public void draw(final Graphics g, final Rectangle2D drawRect, final float actorx, final float actory, float stagescale, float addy, final float scale) {
		draw(g, drawRect, actorx, actory, stagescale, addy, scale, COLOR, null);
	}
}
