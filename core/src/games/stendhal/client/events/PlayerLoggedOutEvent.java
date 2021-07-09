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
package games.stendhal.client.events;

import games.stendhal.client.World;
import games.stendhal.client.entity.Entity;
import marauroa.common.Logger;

class PlayerLoggedOutEvent extends Event<Entity> {
	private static final Logger logger = Logger.getLogger(PlayerLoggedOutEvent.class);

	@Override
	public void execute() {
		String playerName = event.get("name");
		logger.debug("Executing logout event for "+playerName);
		World.get().removePlayerLoggingOut(playerName);
	}
}
