package org.sarge.jove.platform.vulkan.pipeline;

import static org.sarge.jove.platform.vulkan.core.Vulkan.checkAlignment;
import static org.sarge.lib.Validation.*;

import java.lang.foreign.*;
import java.util.*;

import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.util.EnumMask;

/**
 * A <i>push constant</i> is an alternative mechanism for transferring small amounts of data to shaders.
 * @author Sarge
 */
public class PushConstant {
    /**
     * A <i>push constant range</i> specifies a segment of this push constant.
     */
	public record Range(int offset, int size, Set<VkShaderStage> stages) {
    	/**
    	 * Constructor.
    	 * @param offset		Buffer offset
    	 * @param size			Size of this range (bytes)
    	 * @param stages		Pipeline stages
    	 * @throws IllegalArgumentException if {@link #stages} is empty
    	 */
    	public Range {
    		requireZeroOrMore(offset);
    		requireOneOrMore(size);
    		checkAlignment(offset);
    		checkAlignment(size);
    		requireNotEmpty(stages);
    		stages = Set.copyOf(stages);
    	}

    	/**
    	 * @return Descriptor for this push constant
    	 */
    	VkPushConstantRange populate() {
    		final var range = new VkPushConstantRange();
    		range.stageFlags = new EnumMask<>(stages);
    		range.offset = offset;
    		range.size = size;
    		return range;
    	}
    }

	private final List<Range> ranges;
	private final MemorySegment data;

	/**
	 * Constructor.
	 * @param ranges			Push constant ranges
	 * @param allocator			Off-heap allocator for the backing buffer
	 * @throws IllegalArgumentException if the {@link #constants} have overlapping pipeline stages or do not cover the entire backing buffer
	 */
	public PushConstant(List<Range> ranges, SegmentAllocator allocator) {
		// Order ranges by offset
		this.ranges = ranges
    			.stream()
    			.sorted(Comparator.comparingInt(Range::offset))
    			.toList();

		// Allocate backing buffer
		final Range last = this.ranges.getLast();
		this.data = allocator.allocate(last.offset + last.size);

		// Validate ranges
		coverage();
		stages();
	}

	/**
	 * @return Ranges of this push constant
	 */
	public List<Range> ranges() {
		return ranges;
	}

	/**
	 * @return Mutable backing buffer for this push constant
	 */
	public MemorySegment data() {
		return data;
	}

    /**
     * Checks that the push constants cover the entire backing buffer.
     */
    private void coverage() {
    	int offset = 0;
    	for(var range : ranges) {
    		if(range.offset > offset) {
    			throw new IllegalArgumentException("Unused segment before push constant %s at offset %d".formatted(range, offset));
    		}
    		offset = range.offset;
    	}
    }

    /**
     * Checks that the shader stages are unique for each push constant.
     */
    private void stages() {
    	final Set<VkShaderStage> stages = new HashSet<>();
    	for(var range : ranges) {
    		if(!Collections.disjoint(range.stages, stages)) {
    			throw new IllegalArgumentException("Overlapping shader stages: " + range);
    		}
    		stages.addAll(range.stages);
    	}
    }
}
