package org.sarge.jove.model;

import java.util.*;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.lib.util.Check;

/**
 * A <i>glyph</i> defines the layout properties of a character in a {@link GlyphFont}.
 * @author Sarge
 */
public record Glyph(float advance, Map<Character, Float> kerning) {
	/**
	 * Empty kerning pairs.
	 */
	public static final Map<Character, Float> DEFAULT_KERNING = Map.of();

	/**
	 * Constructor.
	 * @param advance Character advance
	 */
	public Glyph {
		Check.zeroOrMore(advance);
		kerning = Map.copyOf(kerning);
	}

	/**
	 * Advance from this glyph to the next character taking into account kerning pairs.
	 * @param next Next character
	 * @return Character advance
	 */
	public float advance(char next) {
		return kerning.getOrDefault(next, advance);
	}

	/**
	 * Writes this glyph as a YAML document.
	 * @return Glyph document
	 */
	public Map<String, Object> write() {
		final var map = new HashMap<String, Object>();
		map.put("advance", advance);
		if(!kerning.isEmpty()) {
			map.put("kerning", kerning);
		}
		return map;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("advance", advance)
				.append("kerning", kerning.size())
				.build();
	}
}
