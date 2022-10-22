package org.sarge.jove.platform.vulkan.pipeline;

import java.util.Set;

import org.sarge.jove.platform.util.IntegerEnumeration;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.lib.util.Check;

/**
 * A <i>push constant range</i> specifies a segment of the push constant buffer and where that data can be used in the pipeline.
 * @author Sarge
 */
public record PushConstantRange(int offset, int size, Set<VkShaderStage> stages) {
	/**
	 * Constructor.
	 * @param offset		Offset (bytes)
	 * @param size			Size (bytes)
	 * @param stages		Pipeline shader stage(s) that can access this range
	 * @throws IllegalArgumentException if {@link #offset} or {@link #size} are not correctly aligned
	 * @see TODO
	 */
	public PushConstantRange {
		Check.zeroOrMore(offset);
		Check.oneOrMore(size);
		Check.notEmpty(stages);
		stages = Set.copyOf(stages);
		validate(offset);
		validate(size);
	}

	/**
	 * Tests that the given offset/size satisfies the alignment rules for push constants.
	 * TODO - link
	 * @param size Size/offset
	 */
	static void validate(int size) {
		if((size % 4) != 0) throw new IllegalArgumentException("Push constants offset/size must be multiples of four");
	}

	/**
	 * @return Length of this push constant range, i.e. {@code offset + size}
	 */
	public int length() {
		return offset + size;
	}

	/**
	 * Populates a push constant range descriptor.
	 */
	void populate(VkPushConstantRange range) {
		range.stageFlags = IntegerEnumeration.reduce(stages);
		range.size = size;
		range.offset = offset;
	}
}
