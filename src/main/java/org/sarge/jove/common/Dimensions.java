package org.sarge.jove.common;

import org.sarge.lib.util.EqualsBuilder;

/**
 * Rectangular dimensions.
 * @author Sarge
 */
public final class Dimensions {
	public final int w, h;

	/**
	 * Constructor.
	 * @param w width
	 * @param h Height
	 */
	public Dimensions( int w, int h ) {
		this.w = w;
		this.h = h;
	}

	public int getWidth() {
		return w;
	}

	public int getHeight() {
		return h;
	}

	@Override
	public boolean equals( Object obj ) {
		return EqualsBuilder.equals( this, obj );
	}

	@Override
	public String toString() {
		return w + "x" + h;
	}
}
