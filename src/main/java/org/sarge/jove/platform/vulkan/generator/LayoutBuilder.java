package org.sarge.jove.platform.vulkan.generator;

import java.lang.foreign.*;
import java.util.*;

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
		// Build field layout
		final var mapper = new LayoutMapper();
		final MemoryLayout[] layouts = fields
				.stream()
				.map(mapper::build)
				.flatMap(List::stream)
				.toArray(MemoryLayout[]::new);

		// Build group layout
		final GroupLayout group = switch(type) {
			case STRUCT -> MemoryLayout.structLayout(layouts);
			case UNION	-> MemoryLayout.unionLayout(layouts);
		};

		// Verify alignment
		assert mapper.count == group.byteSize();
		assert (mapper.count % 4) == 0;						// TODO - is this right?

		return group;
	}

	/**
	 * Maps structure fields to an FFM memory layout and injects alignment padding as required as a side-effect.
	 * TODO - gatherer?
	 */
	private class LayoutMapper {
		private int count;

		public List<MemoryLayout> build(StructureField<NativeType> field) {
			// Build field layout
			final var list = new ArrayList<MemoryLayout>();
			final MemoryLayout layout = layout(field);

			// Inject alignment as required
			// TODO - works (?) but could be tighter => what is the actual alignment rule(s)?
			final long size = layout.byteSize();
			if((size > 4) && !isAligned(count)) {
				list.add(MemoryLayout.paddingLayout(4));
				count += 4;
			}
			count += size;

			// Add field to layout
			final MemoryLayout named = layout.withName(field.name());
			list.add(named);

			return list;
		}
	}

	/**
	 * Builds the memory layout of the given structure field (appending padding as required).
	 */
	private static MemoryLayout layout(StructureField<NativeType> field) {
		final MemoryLayout layout = field.type().layout();
		if(field.length() == 0) {
			return layout;
		}
		else {
			final MemoryLayout aligned = switch(layout) {
				case GroupLayout group when !isAligned(group.byteSize()) -> {
					final var members = new ArrayList<>(group.memberLayouts());
					members.add(MemoryLayout.paddingLayout(4));
					yield MemoryLayout.structLayout(members.toArray(MemoryLayout[]::new));
				}

				default -> layout;
			};
			return MemoryLayout.sequenceLayout(field.length(), aligned);
		}
	}

	/**
	 * @return Whether the given size is word aligned
	 */
	private static boolean isAligned(long size) {
		return (size % 8) == 0;
	}
}
