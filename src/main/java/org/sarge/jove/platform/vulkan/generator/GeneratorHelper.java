package org.sarge.jove.platform.vulkan.generator;

import static java.util.stream.Collectors.*;

import java.util.*;
import java.util.stream.Collector;

final class GeneratorHelper {
	public static final String UNDERSCORE = "_";

	private GeneratorHelper() {
	}

	private enum CharacterType {
		UPPER,
		LOWER,
		DIGIT;

		static CharacterType of(char ch) {
			if(Character.isDigit(ch)) {
				return DIGIT;
			}
			else
			if(Character.isLowerCase(ch)) {
				return LOWER;
			}
			else
			if(Character.isUpperCase(ch)) {
				return UPPER;
			}
			else {
				throw new IllegalArgumentException("Invalid classname character: " + ch);
			}
		}
	}

	/**
	 * Splits a string by character case.
	 */
	public static List<String> splitByCase(String string) {
		// Handle degenerate case of empty string
		if(string.isEmpty()) {
			return List.of();
		}

		// Walk characters and check for type transitions
		final List<String> result = new ArrayList<>();
		final int length = string.length();
		CharacterType type = CharacterType.of(string.charAt(0));
		int start = 0;
		for(int n = 1; n < length; ++n) {
			final CharacterType next = CharacterType.of(string.charAt(n));

			if(next == type) {
				continue;
			}

			if(type == CharacterType.UPPER) {
				if((next == CharacterType.LOWER) && (start == n - 1)) {
					// Found start of a new capitalised word
					type = CharacterType.LOWER;
				}
				else {
					// Found end of a capitalised word
					final String segment = string.substring(start, n - 1);
					result.add(segment);
					type = next;
					start = n - 1;
				}
			}
			else {
				// Otherwise found word boundary
				final String segment = string.substring(start, n);
				result.add(segment);
				type = next;
				start = n;
			}
		}

		// Emit final word
		final String last = string.substring(start);
		result.add(last);

		return result;
	}

	/**
	 * @return Collector that converts strings to snake-case
	 */
	public static Collector<CharSequence, ?, String> snake() {
		return collectingAndThen(joining(UNDERSCORE), String::toUpperCase);
	}
}
