package org.sarge.jove.platform.vulkan.generator;

import java.util.LinkedHashMap;

/**
 * The <i>enumeration parser</i> extracts the metadata for a native enumeration.
 * @author Sarge
 */
class EnumerationParser {
	/**
	 * Parses an enumeration.
	 * @param tokenizer Input tokenizer
	 * @return Enumeration
	 */
	public EnumerationData parse(Tokenizer tokenizer) {
		// Parse enumeration name
		final String name = tokenizer.next();
		tokenizer.skip("{");

		// Parse enumeration values
		final var values = new LinkedHashMap<String, Integer>();
		while(true) {
			// Extract constant identifier
			final String id = tokenizer.next();

			// Check for end of enumeration
			if(id.equals("}")) {
				break;
			}

			// Extract constant value
			final Integer value = tokenizer.skip("=").integer(values::get);
			tokenizer.peek(",");
			values.put(id, value);
		}

		// Create metadata
		return new EnumerationData(name, values);
	}
}
