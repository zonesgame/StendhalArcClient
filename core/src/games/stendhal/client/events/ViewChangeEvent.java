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

import javax.swing.SwingUtilities;

//import games.stendhal.client.GameScreen;
import arc.Core;
import games.stendhal.client.entity.Entity;
import mindustry.Vars;
import stendhal.test.T_GameScreen2;
import temp.Debug;

/**
 * View center changing event.
 */
class ViewChangeEvent extends Event<Entity> {
    @Override
    public void execute() {
        final int x = event.getInt("x");
        final int y = event.getInt("y");

        if (Debug.TEMP)
        	;
//        Core.app.post(() -> Vars.gameScreen.positionChanged(x, y));
        Core.app.post(() -> T_GameScreen2.get().positionChanged(x, y));

//        SwingUtilities.invokeLater(new Runnable() {
//            @Override
//            public void run() {
//                GameScreen.get().positionChanged(x, y);
//            }
//        });
    }
}
