/***************************************************************************
 *                   (C) Copyright 2018 Faiumoni E.v.                      *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.client.sprite;

import java.util.Objects;
import temp.java.awt.Graphics;
import arc.graphics.g2d.TextureRegion;

public class FlippedSprite implements Sprite {
	private static final int FLIP_HORIZONTALLY_FLAG = 0x80000000;
	private static final int FLIP_VERTICALLY_FLAG = 0x40000000;
	private static final int FLIP_DIAGONALLY_FLAG = 0x20000000;

	private final Sprite orig;
	private final Object ref;
//	private final AffineTransform flip;

	public FlippedSprite(Sprite orig, int flags) {
//		this.orig = orig;
		ref = new FlipReference(orig.getReference(), flags);
//		flip = new AffineTransform();

		if (orig instanceof  ImageSprite) {
			TextureRegion region = new TextureRegion(((ImageSprite) orig).getImage());
			boolean flipx = false, flipy = true;
			if ((flags & FLIP_HORIZONTALLY_FLAG) != 0) {
				flipx = true;
			}
			if ((flags & FLIP_VERTICALLY_FLAG) != 0) {
				flipy = false;
			}
			if ((flags & FLIP_DIAGONALLY_FLAG) != 0) {

			}
			region.flip(flipx, flipy);
			this.orig = new ImageSprite(region);
		}
		else {
			throw new RuntimeException();
		}
	}

	@Override
	public Sprite createRegion(int x, int y, int width, int height,
			Object ref) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void draw(Graphics g, int x, int y) {
		orig.draw(g, x, y);
	}

	@Override
	public void draw(Graphics g, int destx, int desty, int x, int y, int w,
			int h) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getHeight() {
		return orig.getHeight();
	}

	@Override
	public Object getReference() {
		return ref;
	}

	@Override
	public int getWidth() {
		return orig.getWidth();
	}

	@Override
	public boolean isConstant() {
		return orig.isConstant();
	}

	private static class FlipReference {
		private final int flags;
		private final Object otherRef;

		FlipReference(Object otherRef, int flags) {
			this.otherRef = otherRef;
			this.flags = flags;
		}

		@Override
		public boolean equals(Object other) {
			if (other == null) {
				return false;
			}
			if (other.getClass() == FlipReference.class) {
				FlipReference flip = (FlipReference) other;
				return flip.flags == flags && flip.otherRef.equals(otherRef);
			}
			return false;
		}

		@Override
		public int hashCode() {
			return Objects.hash(otherRef, flags);
		}
	}
}
