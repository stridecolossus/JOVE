package org.sarge.jove.model;

import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.lib.util.Check;

/**
 * A <i>glyph</i> defines the layout properties of a character in a {@link GlyphFont}.
 * @author Sarge
 */
public record Glyph(int code, float advance, Map<Integer, Float> kerning) {
	/**
	 * Empty kerning pairs.
	 */
	public static final Map<Integer, Float> DEFAULT_KERNING = Map.of();

	/**
	 * Constructor.
	 * @param code			Code-point
	 * @param advance 		Character advance
	 */
	public Glyph {
		Check.zeroOrMore(code);
		Check.zeroOrMore(advance);
		kerning = Map.copyOf(kerning);
	}

	/**
	 * Constructor for a glyph without kerning metadata.
	 * @param advance Character advance
	 */
	public Glyph(int index, float advance) {
		this(index, advance, DEFAULT_KERNING);
	}

	/**
	 * Advance from this glyph to the next character taking into account kerning pairs.
	 * @param next Next character
	 * @return Character advance
	 */
	public float advance(int next) {
		return kerning.getOrDefault(next, advance);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("code", code)
				.append("advance", advance)
				.append("kerning", kerning.size())
				.build();
	}
}
