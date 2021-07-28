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

import games.stendhal.client.entity.DomesticAnimal;
import games.stendhal.client.entity.IEntity;
import games.stendhal.client.entity.NPC;
import temp.java.awt.geom.Rectangle2D;

public class RPEntityMapObject extends MovingMapObject {
	private static final Color COLOR_DOMESTIC_ANIMAL = new Color(255 / 255f, 150 / 255f, 0, 1f);
	private static final Color COLOR_CREATURE = Color.yellow;
	private static final Color COLOR_NPC = new Color(0, 150 / 255f, 0, 1);

//	protected Color drawColor;

	public RPEntityMapObject(final IEntity entity) {
		super(entity);
		if (entity instanceof NPC) {
			curColor = COLOR_NPC;
		} else if (entity instanceof DomesticAnimal) {
			curColor = COLOR_DOMESTIC_ANIMAL;
		} else {
			curColor = COLOR_CREATURE;
		}
	}

	@Override
	void draw(final Graphics g, final Rectangle2D drawRect, final float actorx, final float actory, float stagescale, float addy, final float scale) {
		draw(g, drawRect, actorx, actory, stagescale, addy, scale, curColor);
	}
}
