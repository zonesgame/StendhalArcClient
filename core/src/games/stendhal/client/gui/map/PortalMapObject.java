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

public class PortalMapObject extends StaticMapObject {
	public PortalMapObject(final IEntity entity) {
		super(entity);
	}

	@Override
	void draw(final Graphics g, final Rectangle2D drawRect, final float actorx, final float actory, final float scale) {
		draw(g, drawRect, actorx, actory, scale, Color.white, Color.black);
	}
}
