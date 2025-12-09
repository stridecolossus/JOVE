package org.sarge.jove.platform.vulkan.generator;

import static org.sarge.jove.platform.vulkan.generator.GeneratorHelper.UNDERSCORE;

import java.util.Map;
import java.util.function.*;

/**
 * The <i>enumeration generator</i> builds the template arguments for an enumeration.
 * @author Sarge
 */
class EnumerationGenerator {
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
				.transform(GeneratorHelper::splitByCase)
				.stream()
				.filter(Predicate.not(EnumerationGenerator::isAllCaps))
				.collect(GeneratorHelper.snake());

		// Truncate enumeration constants and remove duplicates
		final var values = enumeration
				.values()
				.entrySet()
				.stream()
				.map(adapter(prefix + UNDERSCORE))
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
}
