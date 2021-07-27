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
import temp.Debug;
import temp.java.awt.Graphics;

import games.stendhal.client.entity.EntityChangeListener;
import games.stendhal.client.entity.IEntity;
import games.stendhal.client.entity.Player;
import games.stendhal.client.entity.RPEntity;
import games.stendhal.client.entity.User;
import temp.java.awt.geom.Rectangle2D;

public class PlayerMapObject extends RPEntityMapObject {
	/**The color of the player (blue).*/
	private static final Color COLOR_USER = Color.blue;
	/**The color of other players (white).*/
	private static final Color COLOR_PLAYER = Color.white;
	/**The color of group players, if visible (grayish).*/
	private static final Color COLOR_GROUP = new Color(99 / 255f, 61 / 255f, 139 / 255f, 1);
	/**The color of ghostmode players, if visible (gray).*/
	private static final Color COLOR_GHOST = Color.gray;

	public PlayerMapObject(final IEntity entity) {
		super(entity);

		if (entity instanceof User) {
			curColor = COLOR_USER;
		} else if (entity instanceof Player) {
			final Player player = (Player) entity;

			choosePlayerColor(player);

			// Follow the ghost mode changes of other players
			entity.addChangeListener(new EntityChangeListener<IEntity>() {
				@Override
				public void entityChanged(final IEntity entity, final Object property) {
					if ((property == RPEntity.PROP_GHOSTMODE) || (property == RPEntity.PROP_GROUP_MEMBERSHIP)) {
						choosePlayerColor(player);
					}
				}
			});
		}
	}

	@Override
	void draw(final Graphics g, final Rectangle2D drawRect, final float actorx, final float actory, final float scale) {
		if ((curColor != COLOR_GHOST) || User.isAdmin()) {
			super.draw(g, drawRect, actorx, actory, scale);
		}
	}

	/**Select a color for drawing the player depending on ghostmode status.
	 * @param player
	 */
	private void choosePlayerColor(final Player player) {
		if (player.isGhostMode()) {
			curColor = COLOR_GHOST;
		} else {
			if (User.isPlayerInGroup(player.getName())) {
				curColor = COLOR_GROUP;
			} else {
				curColor = COLOR_PLAYER;
			}
		}
	}

	/**Draws a player using given color.
	 * @param g The graphics context
	 * @param scale Scaling factor
	 * @param color The draw color
	 */
	@Override
	void draw(final Graphics g, final Rectangle2D drawRect, final float actorx, final float actory, final float scale,  final Color color) {
		if (Debug.NOTE1) {
			super.draw(g, drawRect, actorx, actory, scale, color);
			return;
		}
		float mapX = worldToCanvas(x, scale);
		float mapY = worldToCanvas(y, scale);
		final float scale_2 = scale / 2;
		final float size = scale_2 + 2;

		mapX += scale_2;
		mapY += scale_2;

//		g.setColor(color);
//		g.drawLine(mapX - size, mapY, mapX + size, mapY);
//		g.drawLine(mapX, mapY - size, mapX, mapY + size);
	}
}
