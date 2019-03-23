package org.sarge.jove.generator;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.sarge.lib.collection.StrictMap;
import org.sarge.lib.util.AbstractObject;
import org.sarge.lib.util.TextFileLoader;

import com.sun.jna.Pointer;

/**
 * A <i>type mapper</i> maps native types to the Java equivalent, e.g. <tt>char*</tt> -> <tt>String</tt>.
 * <p>
 * Notes:
 * <ul>
 * <li>The resultant name from {@link #map(TypeDefinition)} is always the <i>base</i> native type name if a mapping is not present</li>
 * <li>Duplicate entries are ignored</li>
 * <li>Type mappings can be aliased and are traversed by {@link #get(String)} and {@link #map(TypeDefinition)}>/li>
 * </ul>
 * Example:
 * <pre>
 * // Primitive mapping
 * table.add("uint32_t", "int");
 * table.get();							// Returns "int"
 * table.map(type);						// Returns "int"
 *
 * // Alias
 * table.add("alias", "uint32_t");
 * table.get("alias");					// Returns "int"
 *
 * // Pointers
 * final TypeDefinition type = new TypeDefinition("void", Cardinality.POINTER);
 * table.add("void*", "pointer");
 * table.map(type);						// Returns "pointer", other types return the native type name
 * </pre>
 * @author Sarge
 */
public class TypeMapper extends AbstractObject {
	/**
	 * Generic pointer class-name.
	 */
	public static final String POINTER_CLASS_NAME = Pointer.class.getName();

	private final Map<String, String> table = new StrictMap<>();

	/**
	 * @return Type definition mappings
	 */
	public Map<String, String> mappings() {
		return new HashMap<>(table);
	}

	/**
	 * Adds a type definition mapping.
	 * @param type 		Type name
	 * @param clazz		Java type
	 */
	public void add(String type, String clazz) {
		// TODO - could enforce actual Java class, but makes loader uglier
		if(type.equals(clazz)) throw new IllegalArgumentException("Cannot map to self: " + type);

		// Ignore duplicates
		if(table.containsKey(type)) {
			return;
		}

		// Add mapping
		table.put(type, clazz);
	}

	/**
	 * Looks up a mapping (also traverses aliased mappings).
	 * @param type Type name
	 * @return Mapped Java type or <tt>null</tt> if not found
	 */
	public String get(String type) {
		final String clazz = table.get(type);
		if(clazz == null) {
			// Not found
			return null;
		}
		else {
			// Recurse through alias
			if(table.containsKey(clazz)) {
				return get(clazz);
			}
			else {
				return clazz;
			}
		}
	}

	/**
	 * Loader for type definitions.
	 * <p>
	 * The mappings are a simple text file with each line representing a type definition as follows: <tt>name class</tt>
	 * <p>
	 * The class is one of:
	 * <ul>
	 * <li>primitive 	- Java primitive, e.g. <tt>int<tt>, <tt>boolean</tt>, etc</li>
	 * <li>string		- Special case for strings (essentially treated as a primitive) with name <tt>String</tt></li>
	 * <li>custom		- Java class path, e.g. <tt>com.sun.jna.Pointer</tt></li>
	 * </ul>
	 */
	public static class Loader {
		private final TextFileLoader loader = new TextFileLoader();
		private TypeMapper mapper = new TypeMapper();

		/**
		 * Loads a pre-defined mapping table from the given reader.
		 * @param r Reader
		 * @return Mapping table
		 * @throws IOException if the mappings cannot be loaded
		 * @throws IllegalArgumentException for an invalid mapping or an unknown class
		 */
		public TypeMapper load(Reader r) throws IOException {
			mapper = new TypeMapper();
			loader.load(r, this::load);
			return mapper;
		}

		/**
		 * Loads a mapping.
		 * @param line Line
		 */
		private void load(String line) {
			// Tokenize line
			final String[] tokens = StringUtils.splitByWholeSeparator(line, "->");
			if(tokens.length != 2) throw new IllegalArgumentException("Invalid type definition: " + line);

			// Extract name
			final String name = tokens[0].trim();
			if(name.isEmpty()) throw new IllegalArgumentException("Empty type definition name: " + line);

			// Extract type
			final String typename = tokens[1].trim();
			if(typename.isEmpty()) throw new IllegalArgumentException("Empty type: " + line);

			// Add type definition
			mapper.add(name, typename);
		}
	}
}
