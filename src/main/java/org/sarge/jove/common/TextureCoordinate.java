package org.sarge.jove.common;

import java.nio.FloatBuffer;

import org.sarge.jove.util.MathsUtil;
import org.sarge.lib.util.ToString;

/**
 * Texture coordinates.
 * @author Sarge
 */
public final class TextureCoordinate implements Bufferable {
	public static final TextureCoordinate TOP_LEFT = new TextureCoordinate(0, 1);
	public static final TextureCoordinate BOTTOM_LEFT = new TextureCoordinate(0, 0);
	public static final TextureCoordinate TOP_RIGHT = new TextureCoordinate(1, 1);
	public static final TextureCoordinate BOTTOM_RIGHT = new TextureCoordinate(1, 0);

	public static final int SIZE = 2;

	public final float s, t;

	/**
	 * Origin constructor.
	 */
	public TextureCoordinate() {
		this(0, 0);
	}

	/**
	 * Constructor.
	 * @param s
	 * @param t
	 */
	public TextureCoordinate(float s, float t) {
		this.s = s;
		this.t = t;
	}

	/**
	 * Array constructor.
	 * @param array Texture coordinates as an array
	 */
	public TextureCoordinate(float[] array) {
		if(array.length != 2) throw new IllegalArgumentException("Expected texture coord array");
		this.s = array[0];
		this.t = array[1];
	}

	@Override
	public int getComponentSize() {
		return SIZE;
	}

	@Override
	public void append(FloatBuffer buffer) {
		buffer.put(s);
		buffer.put(t);
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this) return true;
		if(obj == null) return false;
		if(obj instanceof TextureCoordinate) {
			final TextureCoordinate coords = (TextureCoordinate) obj;
			if(!MathsUtil.isEqual(s, coords.s)) return false;
			if(!MathsUtil.isEqual(t, coords.t)) return false;
			return true;
		}
		else {
			return false;
		}
	}

	@Override
	public String toString() {
		return ToString.toString(s, t);
	}
}
