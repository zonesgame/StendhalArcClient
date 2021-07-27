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

import temp.java.awt.Graphics;

import games.stendhal.client.entity.IEntity;
import temp.java.awt.geom.Rectangle2D;

public abstract class MapObject {
    protected float x;
    protected float y;
    protected float width;
    protected float height;

    MapObject(final IEntity entity) {
        x = (float) entity.getX();
        y = (float) entity.getY();
        width = (int) entity.getWidth();
        height = (int) entity.getHeight();
    }

    /**Draw the entity
     * @param g     Graphics context
     * @param scale Scaling factor
     */
    abstract void draw(Graphics g, Rectangle2D drawRect, float actorx, float actory, float scale);

    /**
     * Scale a world coordinate to canvas coordinates
     * @param crd   World coordinate
     * @param scale Scaling factor
     * @return corresponding canvas coordinate
     */
    float worldToCanvas(final float crd, final float scale) {
        return /*(int)*/ (crd * scale);
    }
}
