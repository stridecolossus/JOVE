package org.sarge.jove.platform.vulkan.generator;

import java.lang.foreign.*;
import java.util.List;
import java.util.stream.Gatherer;
import java.util.stream.Gatherer.Downstream;

import org.sarge.jove.platform.vulkan.generator.StructureData.GroupType;

/**
 * The <i>layout builder</i> generates the FFM memory layout for a structure.
 * @author Sarge
 */
class LayoutBuilder {
	/**
	 * Builds the memory layout for the given structure
	 * @param name			Structure name
	 * @param type			Group type
	 * @param fields		Fields
	 * @return Memory layout
	 */
	public GroupLayout layout(String name, GroupType type, List<StructureField<NativeType>> fields) {
		// Build field layouts and inject alignment padding as required
		final MemoryLayout[] layouts = fields
				.stream()
				.map(LayoutBuilder::layout)
				.gather(padding())
				.toArray(MemoryLayout[]::new);

		// Build group layout
		final GroupLayout group = switch(type) {
			case STRUCT -> MemoryLayout.structLayout(layouts);
			case UNION	-> MemoryLayout.unionLayout(layouts);
		};

		return group;
	}

	/**
	 * Builds the memory layout of a structure field.
	 */
	private static MemoryLayout layout(StructureField<NativeType> field) {
		final MemoryLayout layout = field.type().layout();
		final MemoryLayout actual = layout(layout, field.length());
		return actual.withName(field.name());
	}

	/**
	 * Wraps array fields as a sequence layout.
	 */
	private static MemoryLayout layout(MemoryLayout layout, int length) {
		if(length == 0) {
			return layout;
		}
		else {
			return MemoryLayout.sequenceLayout(length, layout);
		}
	}

	/**
	 * Creates a stateful gatherer that injects padding into the structure layout.
	 * @return Padding gatherer
	 */
	private static Gatherer<MemoryLayout, FieldAlignment, MemoryLayout> padding() {
		return Gatherer.ofSequential(
				FieldAlignment::new,
				LayoutBuilder::inject,
				LayoutBuilder::append
		);
	}

	/**
	 * Integrator.
	 * Injects padding into the layout as required.
	 */
	private static boolean inject(FieldAlignment alignment, MemoryLayout layout, Downstream<? super MemoryLayout> downstream) {
		// Inject padding as required
		final long padding = alignment.align(layout);
		add(padding, downstream);

		// Append field layout
		downstream.push(layout);

		return true;
	}

	/**
	 * Finisher.
	 * Appends padding to align the structure to the size of the largest member.
	 */
	private static void append(FieldAlignment alignment, Downstream<? super MemoryLayout> downstream) {
		add(alignment.padding(), downstream);
	}

	/**
	 * Adds padding to the structure layout.
	 */
	private static void add(long alignment, Downstream<? super MemoryLayout> downstream) {
		if(alignment > 0) {
			final var padding = MemoryLayout.paddingLayout(alignment);
			downstream.push(padding);
		}
	}
}
