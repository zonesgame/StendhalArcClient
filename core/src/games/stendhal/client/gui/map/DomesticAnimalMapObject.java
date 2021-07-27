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
import games.stendhal.client.entity.User;
import temp.java.awt.geom.Rectangle2D;

public class DomesticAnimalMapObject extends MovingMapObject {
	private static final Color COLOR_DOMESTIC_ANIMAL = new Color(255 / 255f, 150 / 255f, 0, 1f);

	private DomesticAnimal domesticanimal;
//	private Color drawColor;

	public DomesticAnimalMapObject(final DomesticAnimal domesticanimal) {
		super(domesticanimal);
		this.domesticanimal = domesticanimal;
		curColor = COLOR_DOMESTIC_ANIMAL;
	}

	@Override
	void draw(final Graphics g, final Rectangle2D drawRect, final float actorx, final float actory, final float scale) {
		// we check this here rather than in the MapPanel so that any changes to the user are refreshed (e.g. disowning pet)
		User user = User.get();
		if ((user != null) && ((user.hasPet() && user.getPetID() == domesticanimal.getObjectID())
				|| (user.hasSheep() && user.getSheepID() == domesticanimal.getObjectID()))) {
			draw(g, drawRect, actorx, actory, scale, curColor);
		}
	}
}
