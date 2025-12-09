package org.sarge.jove.model;

import static org.sarge.jove.util.Validation.requireZeroOrMore;

import java.util.Map;

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
		requireZeroOrMore(code);
		requireZeroOrMore(advance);
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
}
