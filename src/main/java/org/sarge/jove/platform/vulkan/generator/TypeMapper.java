package org.sarge.jove.platform.vulkan.generator;

import static java.lang.foreign.ValueLayout.*;
import static java.util.Objects.requireNonNull;
import static org.sarge.jove.util.Validation.requireNotEmpty;

import java.lang.foreign.*;
import java.util.*;

/**
 * The <i>type mapper</i> determines the domain type of a native type.
 * @author Sarge
 */
class TypeMapper {
	/**
	 * Native type mapping for a JOVE handle.
	 */
	public static final NativeType HANDLE = new NativeType("Handle", ADDRESS);

	private final Map<String, NativeType> types = new HashMap<>();

	public TypeMapper() {
		primitives();
		defined();
	}

	/**
	 * Registers primitive types.
	 */
	private void primitives() {
		final ValueLayout[] primitives = {
				JAVA_BYTE,
				JAVA_SHORT,
				JAVA_INT,
				JAVA_LONG,
				JAVA_FLOAT,
				JAVA_DOUBLE
		};

		for(var p : primitives) {
			final String name = p.carrier().getSimpleName();
			add(name, NativeType.of(p));
		}

		add("char", NativeType.of(JAVA_BYTE));
	}

	/**
	 * Registers the standard C types.
	 */
	private void defined() {
		final var custom = Map.of(
				"uint8_t",		"byte",
				"uint16_t",		"short",
				"int32_t",		"int",
				"uint32_t",		"int",
				"int64_t",		"long",
				"uint64_t",		"long",
				"size_t",		"long"
		);

		for(var entry : custom.entrySet()) {
			final NativeType type = types.get(entry.getValue());
			add(entry.getKey(), type);
		}
	}

	/**
	 * Registers a type mapping.
	 * @param name		Native type name
	 * @param type		Mapped type
	 * @throws IllegalArgumentException for a duplicate type name
	 */
	void add(String name, NativeType type) {
		if(types.containsKey(name)) {
			throw new IllegalArgumentException("Duplicate type: " + name);
		}
		addLocal(name, type);
	}

	/**
	 * Registers a type definition for a synonym.
	 * @param type			Type name
	 * @param synonym		Synonym
	 * @throws IllegalArgumentException if {@link #type} has not been added
	 */
	public void typedef(String type, String synonym) {

		type = type.replaceFirst("FlagBits", "Flags");

		// Skip self references
		if(type.equals(synonym)) {
			return;
		}

//		// TODO - fiddle
//		if(synonym.equals("VkBool32")) {
//			return;
//		}

		// Otherwise lookup target type
		final NativeType ref = types.get(type);
		if(ref == null) {
			throw new IllegalArgumentException("Unknown referenced type [%s] for synonym [%s]".formatted(type, synonym));
		}

		if(types.containsKey(synonym) && type.equals("VkFlags")) {
			//System.out.println("*** already defined " + synonym + " -> " + type);
			return;
		}

		// Register synonym
		add(synonym, ref);
	}

	/**
	 * Adds or replaces a native type.
	 * @param type Type
	 */
	public void add(NativeType type) {
		addLocal(type.name(), type);
	}

	private void addLocal(String name, NativeType type) {
		requireNotEmpty(name);
		requireNonNull(type);
		types.put(name, type);
	}

	/**
	 * Maps a structure field to a native type.
	 * @param field Structure field
	 * @return Native type
	 * @throws IllegalArgumentException if the field is unsupported
	 */
	public NativeType map(StructureField<String> field) {
		return switch(field.type()) {
			case "void*"		-> HANDLE;
			case "char*"		-> new NativeType("String", ADDRESS);
			case "char**"		-> new NativeType("String[]", ADDRESS);
			case "VkBool32"		-> new NativeType("boolean", JAVA_INT);
			default				-> special(field);
		};
	}

	private NativeType special(StructureField<String> field) {
		try {
			return mapLocal(field);
		}
		catch(IllegalArgumentException e) {
			throw new IllegalArgumentException("Unsupported field type: " + field, e);
		}
	}

	/**
	 * Maps a structure field.
	 */
	private NativeType mapLocal(StructureField<String> field) {
		final String typename = field.type();
		if(typename.endsWith("*")) {
			return pointer(field);
		}
		else
		if(typename.contains("FlagBits") || typename.contains("Flags")) {
			return enumeration(field);
		}
		else
		if(typename.equals("char") && (field.length() > 0)) {
			return new NativeType("String", JAVA_BYTE);
		}
		else {
			final NativeType type = find(typename);
			if(field.length() == 0) {
				return type;
			}
			else {
				return type.array();
			}
		}
	}

	/**
	 * Looks up the type of structure field.
	 */
	private NativeType find(String typename) {
		final NativeType type = types.get(typename);
		if(type == null) {
			throw new IllegalArgumentException("Unknown field type: " + typename);
		}
		return type;
	}

	/**
	 * Maps an enumeration field or bitfield mask.
	 */
	private NativeType enumeration(StructureField<String> field) {
		// Lookup enumeration
		final String typename = field.type().replaceFirst("FlagBits", "Flags");
		final NativeType type = find(typename);

		// Check for unspecified enumerations (has a typedef to VkFlags but the actual type is undefined and/or unused)
		if(type.name().equals("int")) {
			return type;
		}

		// All enumeration bitfields are represented as a mask
		if(field.type().contains("FlagBits") || field.type().contains("Flags")) {
			if(field.length() > 0) {
				throw new RuntimeException();
			}
			final String mask = String.format("EnumMask<%s>", typename);
			return new NativeType(mask, ValueLayout.JAVA_INT);
		}

		// Otherwise map to enumeration
		if(field.length() > 0) {
			return type.array();
		}
		else {
			return type;
		}
	}

	/**
	 * Maps a pointer field.
	 */
	private NativeType pointer(StructureField<String> field) {
		if(field.length() > 0) {
			throw new RuntimeException("Unexpected pointer array: " + field);
		}

		// Lookup pointer type
		final String typename = field.type();
		final String actual = typename.substring(0, typename.length() - 1);
		final NativeType type = find(actual);

		// Derive actual type depending on plurality
		return switch(type.layout()) {
			case AddressLayout _	-> pluralise(field, HANDLE);
			case GroupLayout _		-> pluralise(field, new NativeType(actual, ADDRESS));
			case ValueLayout _		-> type.array();
			default	-> throw new RuntimeException();
		};
	}

	/**
	 * Adapts a field as an array for pluralised types.
	 */
	private static NativeType pluralise(StructureField<String> field, NativeType type) {
		if(field.name().endsWith("s")) {
			return type.array();
		}
		else {
			return type;
		}
	}
}
