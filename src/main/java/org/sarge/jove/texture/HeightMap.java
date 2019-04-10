package org.sarge.jove.texture;

import static org.sarge.lib.util.Check.oneOrMore;

import java.nio.ByteBuffer;
import java.util.Arrays;

import org.junit.platform.commons.util.ToStringBuilder;
import org.sarge.jove.util.MathsUtil;

/**
 * A <i>height map</i> is a 2D grid of terrain height values.
 * @author Sarge
 */
public class HeightMap {
	/**
	 * Creates a height-map from the given image.
	 * @param image Image
	 * @return Height-map
	 * @throws IllegalArgumentException if the height-map is not square
	 */
	public static HeightMap of(Image image) {
		if(!image.size().isSquare()) throw new IllegalArgumentException("Height-map image is not square");
		// TODO - this sucks: assumes 4 components, assumes RGBA mapping -> int
		final ByteBuffer buffer = image.buffer();
		final int[] map = new int[buffer.capacity() / 4];
		image.buffer().asIntBuffer().get(map);
		return new HeightMap(map);
	}

	private final int[] map;
	private final int size;

	/**
	 * Constructor.
	 * @param map Height-map array
	 * @throws IllegalArgumentException if the height-map is not square
	 */
	public HeightMap(int[] map) {
		this((int) MathsUtil.sqrt(map.length), map);
	}

	/**
	 * Constructor.
	 * @param size		Size of this height-map
	 * @param map		Height-map
	 * @throws IllegalArgumentException if the height-map is not square
	 */
	protected HeightMap(int size, int[] map) {
		if(size * size != map.length) throw new IllegalArgumentException("Height-map is not square");
		this.size = (int) MathsUtil.sqrt(map.length);
		this.map = Arrays.copyOf(map, map.length);
	}

	/**
	 * @return Size of this height-map
	 */
	public int size() {
		return size;
	}

	/**
	 * Looks up the height at the given coordinates.
	 * @param x
	 * @param y
	 * @return Height
	 * @throws ArrayIndexOutOfBoundsException if the coordinates are not valid for this height-map
	 */
	public int height(int x, int y) {
		return map[x + y * size];
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("size", size).toString();
	}

	/**
	 * Builder for a height-map.
	 */
	public static class Builder {
		private final int[] map;
		private final int size;

		/**
		 * Constructor.
		 * @param size Height-map size
		 */
		public Builder(int size) {
			this.size = oneOrMore(size);
			this.map = new int[size * size];
		}

		/**
		 * Sets the height at the given coordinates.
		 * @param x
		 * @param y
		 * @param h Height
		 */
		public Builder set(int x, int y, int h) {
			map[x + y * size] = h;
			return this;
		}

		/**
		 * Sets the height of the entire height-map.
		 * @param h Height
		 */
		public Builder set(int h) {
			for(int n = 0; n < map.length; ++n) {
				map[n] = h;
			}
			return this;
		}

		/**
		 * Constructs this height-map.
		 * @return New height-map
		 */
		public HeightMap build() {
			return new HeightMap(size, map);
		}
	}
}
