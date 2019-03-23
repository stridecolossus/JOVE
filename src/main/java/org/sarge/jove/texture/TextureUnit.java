package org.sarge.jove.texture;

import static org.sarge.lib.util.Check.zeroOrMore;

import java.util.stream.IntStream;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.lib.util.Check;

/**
 * Texture unit.
 */
public final class TextureUnit {
	/**
	 * Factory for texture units.
	 */
	public static final class Factory {
		private final TextureUnit[] units;

		/**
		 * Constructor.
		 * @param max Maximum number of texture units supported by the graphics system
		 */
		public Factory(int max) {
			Check.oneOrMore(max);
			this.units = IntStream.range(0, max).mapToObj(TextureUnit::new).toArray(TextureUnit[]::new);
		}

		/**
		 * @return Maximum number of texture units
		 */
		public int max() {
			return units.length;
		}

		/**
		 * Retrieves a texture unit.
		 * @param unit Texture unit index
		 * @return Texture unit
		 * @throws ArrayIndexOutOfBoundsException if the unit index is not valid for this factory
		 */
		public TextureUnit unit(int unit) {
			return units[unit];
		}

		@Override
		public String toString() {
			return ToStringBuilder.reflectionToString(this);
		}
	}

	/**
	 * Default texture unit factory.
	 */
	public static final TextureUnit.Factory DEFAULT = new Factory(8);

	private final int unit;

	/**
	 * Constructor.
	 * @param unit Texture unit index
	 */
	private TextureUnit(int unit) {
		this.unit = zeroOrMore(unit);
	}

	/**
	 * @return Texture unit index
	 */
	public int index() {
		return unit;
	}

	@Override
	public boolean equals(Object that) {
		return EqualsBuilder.reflectionEquals(this, that);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}