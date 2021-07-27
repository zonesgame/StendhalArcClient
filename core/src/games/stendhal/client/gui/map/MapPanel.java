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

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import arc.Core;
import arc.Settings;
import arc.graphics.Color;
import arc.graphics.Pixmap;
import arc.graphics.Texture;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.input.KeyCode;
import arc.scene.Element;
import arc.scene.event.ClickListener;
import arc.scene.event.InputEvent;
import arc.util.Disposable;
import games.stendhal.client.StendhalClient;
import games.stendhal.client.entity.IEntity;
import games.stendhal.common.CollisionDetection;
import marauroa.common.game.RPAction;
import temp.java.awt.Rectangle;
import temp.java.awt.geom.Rectangle2D;
import z.utils.FinalCons;

public class MapPanel extends Element implements Disposable {
	/**serial version uid*/
	private static final long serialVersionUID = -6471592733173102868L;

	/**The color of the background (palest grey).*/
	private static final Color COLOR_BACKGROUND = new Color(0.8f, 0.8f, 0.8f, 1f);
	/**The color of blocked areas (red).*/
	public static final Color COLOR_BLOCKED = new Color(1.0f, 0.0f, 0.0f, 1f);
	/**The color of protected areas (palest green).*/
	private static final Color COLOR_PROTECTION = new Color(202 / 255f, 230 / 255f, 202 / 255f, 1f);
	/**The color of other players (white).*/

	/** width of the minimap. */
	private static final int MAP_WIDTH = 128;
	/** height of the minimap. */
	private static final int MAP_HEIGHT = 128;
	/** Minimum scale of the map; the minimum size of one tile in pixels */
	private static final int MINIMUM_SCALE = 2;

//	private final StendhalClient client;
//	private final MapPanelController controller;

	/**The player X coordinate.*/
	private double playerX;
	/**The player Y coordinate.*/
	private double playerY;
	/** X offset of the background image */
	private int xOffset;
	/** Y offset of the background image */
	private int yOffset;

	/**Maximum width of visible part of the map image. This should be accessed only in the event dispatch thread.*/
	private int width;
	/**Maximum height of visible part of the map image. This should be accessed only in the event dispatch thread.*/
	private int height;
	/**Scaling of the map image. Amount of pixels used for each map tile in each dimension. This should be accessed only in the event dispatch thread.*/
	private float scale;		// default int

	final Map<IEntity, MapObject> mapObjects;
	final Rectangle2D drawRect = new Rectangle.Double();

	/**Map background. This should be accessed only in the event dispatch thread.*/
	private TextureRegion mapImage;
	private Texture mapTexture;

	public MapPanel(final Map<IEntity, MapObject> mapObjects) {
		this.mapObjects = mapObjects;
//		client = StendhalClient.get();

		// black area outside the map
//		setBackground(Color.black);
//		updateSize(new Dimension(MAP_WIDTH, MAP_HEIGHT));
//		setOpaque(true);

		// handle clicks for moving.
		// 添加输入事件监听器
		// 如果设置通过支持小地图移动
		this.addListener(new ClickListener() {
			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button) {
				if ( !Core.settings.getBool(FinalCons.SETTING_KEYS.minimapMove, false))   return false;

				final int xpos = (int) x;
				final int ypos = (int) (height - y);
				movePlayer(xpos, ypos, false);
//				return super.touchDown(event, x, y, pointer, button);
				return true;
			}
		});
	}

//	/**Create a new MapPanel.
//	 * @param controller
//	 * @param client
//	 */
//	MapPanel(final MapPanelController controller, final StendhalClient client) {
//		this.client = client;
//		this.controller = controller;
//		// black area outside the map
//		setBackground(Color.black);
//		updateSize(new Dimension(MAP_WIDTH, MAP_HEIGHT));
//		setOpaque(true);
//
//		// handle clicks for moving.
//		this.addMouseListener(new MouseAdapter() {
//			@Override
//			public void mouseClicked(final MouseEvent e) {
//				movePlayer(e.getPoint(), e.getClickCount() > 1);
//			}
//		});
//	}

	public void draw(float x, float y) {
		if (mapImage != null) {
			drawMap( x, y);
			drawEntities( x, y);
			Draw.color();    // 恢复颜色设置
		}
	}

	/**Draw the entities on the map.*/
	private void drawEntities(float actorx, float actory) {
		for (final MapObject object : mapObjects.values()) {
			object.draw(null, drawRect, actorx - xOffset, actory, scale);
		}
	}

	/**Set the dimensions of the component. This must be called from the event dispatch thread.
	 * @param sw the new dimensions
	 */
	private void updateSize(final int sw, final int sh) {
		width = sw;
		height = sh;
		this.setSize(sw, sh);
	}

	/**Draw the map background. This must be called from the event dispatch thread.*/
	private void drawMap(float actorx, float actory) {
		Draw.color();
		Draw.rectGdx(mapImage, actorx, actory);
//		g.drawImage(mapImage, 0, 0, null);
	}

	/**The player's position changed.
	 * @param x The X coordinate (in world units).
	 * @param y The Y coordinate (in world units).
	 */
	public void positionChanged(final double x, final double y) {
		playerX = x;
		playerY = y;

		updateView();
	}

	/**Update the view pan. This should be done when the map size or player
	 * position changes. This must be called from the event dispatch thread.
	 */
	private void updateView() {
		xOffset = 0;
		yOffset = 0;

		if (mapImage == null) {
			return;
		}

		final int imageWidth = mapImage.getWidth();
		final int imageHeight = mapImage.getHeight();

		final int xpos = (int) ((playerX * scale) + 0.5) - width / 2;
		final int ypos = (int) ((playerY * scale) + 0.5) - width / 2;

		if (imageWidth > width) {
			// need to pan width
			if ((xpos + width) > imageWidth) {
				// x is at the screen border
				xOffset = imageWidth - width;
			} else if (xpos > 0) {
				xOffset = xpos;
			}
		}

		if (imageHeight > height) {
			// need to pan height
			if ((ypos + height) > imageHeight) {
				// y is at the screen border
				yOffset = imageHeight - height;
			} else if (ypos > 0) {
				yOffset = ypos;
			}
		}

		if (xOffset != 0 || yOffset != 0 || width != imageWidth || height != imageHeight) {
			mapImage.set(xOffset, yOffset, width, height);
		}

		drawRect.setRect(xOffset / scale, yOffset / scale, width / scale, height / scale);
	}

	/**
	 * Update the map with new data. This method can be called outside the
	 * event dispatch thread.
	 *
	 * @param cd
	 *            The collision map.
	 * @param pd
	 *      	  The protection map.
	 */
	public void update(final CollisionDetection cd, final CollisionDetection pd) {
		// calculate the size and scale of the map
		final int mapWidth = cd.getWidth();
		final int mapHeight = cd.getHeight();
		final int scale = Math.max(MINIMUM_SCALE, Math.min(MAP_HEIGHT / mapHeight, MAP_WIDTH / mapWidth));
		final int width = Math.min(MAP_WIDTH, mapWidth * scale);
		final int height = Math.min(MAP_HEIGHT, mapHeight * scale);

		Pixmap pixmap = new Pixmap(mapWidth * scale, mapHeight * scale);
		pixmap.setColor(COLOR_BACKGROUND);
		pixmap.fillRectangle(0, 0, pixmap.getWidth(), pixmap.getHeight());
		for (int x = 0; x < mapWidth; x++) {
			for (int y = 0; y < mapHeight; y++) {
				if (cd.collides(x, y)) {
					pixmap.setColor(COLOR_BLOCKED);
					pixmap.fillRectangle(x * scale, y * scale, scale, scale);
				} else if (pd != null && pd.collides(x, y)) {
					// draw protection only if there is no collision to draw
					pixmap.setColor(COLOR_PROTECTION);        // COLOR_PROTECTION
					pixmap.fillRectangle(x * scale, y * scale, scale, scale);
				}
			}
		}
		if (mapTexture != null) {
			mapTexture.dispose();
		}
		mapTexture = new Texture(pixmap);
		mapTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
		mapImage = new TextureRegion(mapTexture);
		pixmap.dispose();

		updateSize(MAP_WIDTH, height);
		this.scale = scale;
//		this.setPosition(0, getStage().getHeight() - this.height);

		updateView();
	}

	/**
	 * Tell the player to move to point p
	 * @param posx the point
	 * @param doubleClick <code>true</code> if the movement was requested with a double click, <code>false</code> otherwise
	 */
	private void movePlayer(final int posx, final int posy, boolean doubleClick) {
		// Ignore clicks to the title area
		if (true) {			// p.y <= height
			final RPAction action = new RPAction();
			action.put("type", "moveto");
			action.put("x", (posx + xOffset) / scale);
			action.put("y", (posy + yOffset) / scale);
			if (doubleClick) {
				action.put("double_click", "");
			}
			StendhalClient.get().send(action);
		}
	}

	/**
	 *  Acto绘制方法
	 * */
	@Override
	public void draw() {
		draw( getX(), getY());
	}

	/**
	 *  Actor 宽度
	 * */
	@Override
	public float getWidth() {
		return width;
	}

	/**
	 *  Actor高度
	 * */
	@Override
	public float getHeight() {
		return height;
	}

	@Override
	public void dispose() {
		if (mapTexture != null)
			mapTexture.dispose();
	}
}
