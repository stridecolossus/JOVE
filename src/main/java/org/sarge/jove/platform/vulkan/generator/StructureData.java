package org.sarge.jove.platform.vulkan.generator;

import static java.util.Objects.requireNonNull;
import static org.sarge.jove.util.Validation.requireNotEmpty;

import java.util.List;

/**
 * Metadata for a structure or union.
 * @author Sarge
 */
record StructureData(String name, GroupType group, List<StructureField<String>> fields) {
	/**
	 * Group type.
	 */
	public enum GroupType {
		STRUCT,
		UNION;

		/**
		 * Parses a group type.
		 * @param group Group type token
		 * @return Group type
		 * @throws IllegalArgumentException if the group type is invalid
		 */
		public static GroupType parse(String group) {
			return GroupType.valueOf(group.toUpperCase());
		}
	}

	/**
	 * Constructor.
	 * @param name			Structure name
	 * @param group			Group type
	 * @param fields		Fields
	 */
	public StructureData {
		requireNotEmpty(name);
		requireNonNull(group);
		fields = List.copyOf(fields);
	}
}
