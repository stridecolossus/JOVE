package org.sarge.jove.platform.vulkan.generator;

import static java.util.stream.Collectors.joining;

import java.util.*;
import java.util.function.*;

/**
 * The <i>enumeration generator</i> builds the template arguments for an enumeration.
 * @author Sarge
 */
class EnumerationGenerator {
	private static final String UNDERSCORE = "_";

	/**
	 * Generates the template arguments for the given enumeration.
	 * TODO - flags/bits special cases
	 * TODO - truncates constants
	 * @param enumeration Enumeration metadata
	 * @return Enumeration arguments
	 */
	public Map<String, Object> generate(EnumerationData enumeration) {
		// Tokenize enumeration name and convert to snake-case prefix
		final String prefix = enumeration
				.name()
				.replaceFirst("FlagBits", "")
				.transform(EnumerationGenerator::splitByCase)
				.stream()
				.filter(Predicate.not(EnumerationGenerator::isAllCaps))
				.collect(joining(UNDERSCORE, "", UNDERSCORE))
				.toUpperCase();

		// Truncate enumeration constants and remove duplicates
		final var values = enumeration
				.values()
				.entrySet()
				.stream()
				.map(adapter(prefix))
				.distinct()
				.toList();

		// Build template arguments
		final String name = enumeration.name().replaceFirst("FlagBits", "Flags");
		return Map.of(
				"name", name,
				"values", values
		);
	}

	/**
	 * @return Truncate adapter for a enumeration entry
	 */
	private static UnaryOperator<Map.Entry<String, Integer>> adapter(String name) {
		final var truncate = truncate(name);

		return entry -> {
			final String key = entry.getKey();
			final String truncated = truncate.apply(key);
			return Map.entry(truncated, entry.getValue());
		};
	}

	/**
	 * Truncates an enumeration constant identifier.
	 */
	private static UnaryOperator<String> truncate(String prefix) {
		return key -> {
			// Strip bits
			final String cleaned = key
					.replace("_BIT_", "_")
					.replaceFirst("_BIT$", "");

			// Truncate constant name
			final String truncated = cleaned.replace(prefix, "");

			// Walk back one word if ended up with a leading numeric
			if(Character.isDigit(truncated.charAt(0))) {
				final int start = cleaned.indexOf(truncated);
				final int index = cleaned.lastIndexOf('_', start - 2);
				return cleaned.substring(index + 1);
			}
			else {
				return truncated;
			}
		};
	}

	private static boolean isAllCaps(String token) {
		return token.chars().allMatch(Character::isUpperCase);
	}

	/**
	 * Splits a string by character case.
	 */
	private static List<String> splitByCase(String string) {
		// Handle degenerate case of empty string
		if(string.isEmpty()) {
			return List.of();
		}

		// Assume starts in camel-case
		boolean upper = Character.isUpperCase(string.charAt(0));

		// Walk character and check for case transitions
		final List<String> result = new ArrayList<>();
		final int length = string.length();
		int start = 0;
		for(int n = 1; n < length; ++n) {
			if(Character.isUpperCase(string.charAt(n))) {
				if(!upper) {
					// Start new camel-case word
					final String segment = string.substring(start, n);
					result.add(segment);
					upper = true;
					start = n;
				}
			}
			else {
				if(upper) {
					if(n - start > 1) {
						// Handle capitalised word
						final String segment = string.substring(start, n - 1);
						result.add(segment);
						start = n - 1;
					}
					upper = false;
				}
			}
		}

		// Emit final word
		final String last = string.substring(start);
		result.add(last);

		return result;
	}
}
