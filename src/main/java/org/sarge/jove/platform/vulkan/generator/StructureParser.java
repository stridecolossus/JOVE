package org.sarge.jove.platform.vulkan.generator;

import static java.util.Objects.requireNonNull;

import java.util.*;
import java.util.function.Function;

import org.sarge.jove.platform.vulkan.generator.StructureData.GroupType;

/**
 * The <i>structure parser</i> extracts the metadata for a structure or union.
 * @author Sarge
 */
class StructureParser {
	private final Tokenizer tokenizer;
	private final Function<String, Integer> constants;

	/**
	 * Constructor.
	 * @param tokenizer		Source tokenizer
	 * @param constants		Integer constant mapper
	 */
	public StructureParser(Tokenizer tokenizer, Function<String, Integer> constants) {
		this.tokenizer = requireNonNull(tokenizer);
		this.constants = requireNonNull(constants);
	}

	/**
	 * Parses a structure or union.
	 * @param group Group type
	 * @return Structure metadata
	 */
	public StructureData parse(String group) {
		// Parse structure name
		final String name = tokenizer.next();
		tokenizer.skip("{");

		// Parse fields
		final List<StructureField<String>> fields = new ArrayList<>();
		while(true) {
			// Stop at end of structure
			if(tokenizer.peek("}")) {
				break;
			}

			// Parse field
			final String type = typename();
			final String fieldname = name();
			final int length = length();
			final var field = new StructureField<>(fieldname, type, length);
			fields.add(field);
		}

		// Create metadata
		return new StructureData(name, GroupType.parse(group), fields);
	}

	/**
	 * Parses the type name of a structure field.
	 */
	private String typename() {
		// Extract base field type
		final var typename = new StringBuilder();
		tokenizer.peek("const");
		tokenizer.peek("struct");
		typename.append(tokenizer.next());

		// Check for pointer
		if(tokenizer.peek("*")) {
			typename.append("*");
		}

		// Check for pointer-to-pointer
		tokenizer.peek("const");
		if(tokenizer.peek("*")) {
			typename.append("*");
		}

		// Build aggregated type
		final String result = typename.toString();

		// TODO
		if(result.startsWith("PFN_vk")) {
			return "void*";
		}
		else {
			return result;
		}
	}

	/**
	 * Parses a structure field name.
	 */
	private String name() {
		final String name = tokenizer.next();

		// TODO - WTF!
		if(tokenizer.peek(":")) {
			tokenizer.next();
		}

		return name;
	}

	/**
	 * Parses the optional array declaration.
	 * @return Array length or {@code null} if not an array
	 */
	private int length() {
		if(tokenizer.peek("[")) {
			final int length = tokenizer.integer(constants);

			// TODO - WTF!
			if(tokenizer.peek("][")) {
				tokenizer.next();
			}

			tokenizer.skip("];");
			return length;
		}
		else {
			tokenizer.skip(";");
			return 0;
		}
	}
}
