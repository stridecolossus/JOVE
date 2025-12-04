package org.sarge.jove.platform.vulkan.generator;

import static java.util.Objects.requireNonNull;

import java.lang.foreign.GroupLayout;
import java.util.*;

/**
 * The <i>structure generator</i> builds the template arguments for a structure or union.
 * @author Sarge
 */
class StructureGenerator {
	private final TypeMapper mapper;
	private final LayoutBuilder builder;

	/**
	 * Constructor.
	 * @param mapper		Native type mapper
	 * @param builder		Layout builder
	 */
	public StructureGenerator(TypeMapper mapper, LayoutBuilder builder) {
		this.mapper = requireNonNull(mapper);
		this.builder = requireNonNull(builder);
	}

	/**
	 * Generates the memory layout and template arguments for the given structure.
	 * @param structure Structure metadata
	 * @return Template arguments
	 */
	public Map<String, Object> generate(StructureData structure) {
		// Temporarily register for self-referenced structure fields
		mapper.add(structure.name(), TypeMapper.HANDLE);

		// TODO - populate the sType field

		// Map structure fields to domain types
		final List<StructureField<NativeType>> fields = structure
				.fields()
				.stream()
				.map(this::map)
				.toList();

		// Build field arguments
		final var arguments = fields
				.stream()
				.map(StructureGenerator::arguments)
				.toList();

		// Build memory layout
		final GroupLayout group = builder.layout(structure.name(), structure.group(), fields);

		// Register structure
		mapper.add(new NativeType(structure.name(), group));

		// Generate source code for the layout
		final var writer = new LayoutWriter(3);
		final String layout = writer.write(group);

		// Build template arguments
		return Map.of(
				"name",		structure.name(),
				"fields",	arguments,
				"layout",	layout
		);
	}

	private StructureField<NativeType> map(StructureField<String> field) {
		final NativeType type = mapper.map(field);
		return field.with(type);
	}

	private static Map<String, Object> arguments(StructureField<NativeType> field) {
		return Map.of(
				"name",		field.name(),
				"type",		field.type().name()
		);
	}
}
