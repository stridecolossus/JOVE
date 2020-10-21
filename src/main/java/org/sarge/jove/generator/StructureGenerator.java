package org.sarge.jove.generator;

import static java.util.stream.Collectors.toSet;
import static org.sarge.lib.util.Check.notEmpty;
import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.zeroOrMore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.sarge.lib.util.AbstractEqualsObject;

/**
 * Source code generator for a JNA structure.
 * @author Sarge
 */
public class StructureGenerator {
	private static final Set<String> PRIMITIVES = Util.PRIMITIVES.stream().map(Class::getSimpleName).collect(toSet());

	/**
	 * Structure field descriptor.
	 */
	public class Field extends AbstractEqualsObject {
		private final String name;
		private final String type;
		private final String path;
		private final int len;

		/**
		 * Constructor.
		 * @param name			Field name
		 * @param type			Type
		 * @param len			Array length
		 * @param split			Whether to split the type name
		 */
		protected Field(String name, String type, int len, boolean split) {
			Check.notEmpty(type);
			this.name = notEmpty(name);
			this.len = zeroOrMore(len);

			if(split) {
				final int index = type.lastIndexOf('.');
				if(index == -1) {
					this.type = type;
					path = null;
				}
				else {
					this.type = type.substring(index + 1, type.length());
					path = type;
				}
			}
			else {
				this.type = type;
				path = null;
			}
		}

		/**
		 * @return Field name
		 */
		public String getName() {
			return name;
		}

		/**
		 * @return Type name
		 */
		public String getType() {
			return type;
		}

		/**
		 * @return Import path or <tt>null</tt> if no import is required
		 */
		public String getPath() {
			return path;
		}

		/**
		 * @return Array length
		 */
		public int getLength() {
			return len;
		}
	}

	private final Generator generator;
	private final TypeMapper mapper;

	/**
	 * Constructor.
	 * @param generator 	Source code generator
	 * @param mapper		Field type mapper
	 */
	public StructureGenerator(Generator generator, TypeMapper mapper) {
		this.generator = notNull(generator);
		this.mapper = notNull(mapper);
	}

	/**
	 * Creates a structure field.
	 * @param name			Field name
	 * @param type			Type
	 * @param pointers		Number of pointers 0..2
	 * @param len			Array length
	 * @return Field
	 */
	public Field field(String name, String type, int pointers, int len) {
		// Map type and suffix
		boolean split = true;
		String clazz = mapper.get(type + suffix(pointers, len > 0));

		// Map simple type
		if(clazz == null) {
			if(pointers > 0) {
				clazz = TypeMapper.POINTER_CLASS_NAME;
			}
			else {
				clazz = mapper.get(type);
			}
		}

//		// Map primitive type
//		if((clazz == null) && PRIMITIVES.contains(type)) {
//			if(pointers > 0) {
//				// Pointer to primitives
//				clazz = TypeMapper.POINTER_CLASS_NAME;
//			}
//			else {
//				// Simple primitive
//				clazz = type;
//			}
//		}

		if(clazz == null) {
			if(pointers > 0) {
				// Assume pointer to structure
				clazz = TypeMapper.POINTER_CLASS_NAME;
			}
			else {
				// Embedded structure
				clazz = type;
			}
			split = false;
		}

		// Create field
		return new Field(name, clazz, len, split);
	}

	/**
	 * @return Type suffix
	 */
	private static String suffix(int pointers, boolean array) {
		if(array) {
			return "[]";
		}
		else {
			switch(pointers) {
			case 1: 	return "*";
			case 2: 	return "**";
			default:	return StringUtils.EMPTY;
			}
		}
	}

	/**
	 * Generates a JNA structure.
	 * @param name			Structure name
	 * @param fields		Fields
	 * @return Structure source code
	 */
	public String generate(String name, List<Field> fields) {
		// Build imports list
		final Set<String> imports = fields.stream()
			.map(f -> f.path)
			.filter(Objects::nonNull)
			.collect(toSet());

		// Build template properties
		final Map<String, Object> values = new HashMap<>();
		values.put("imports", imports);
		values.put("fields", fields);

		// Inject the synthetic structure identifier
		if(fields.get(0).getName().equals("sType")) {
			final String[] words = StringUtils.splitByCharacterTypeCamelCase(name.substring(2, name.length()));
			final String suffix = String.join("_", words).toUpperCase();
			values.put("sType", suffix);
		}

		// Generate structure
		return generator.generate(name, values);
	}
}
