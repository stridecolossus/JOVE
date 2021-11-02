package org.sarge.jove.platform.vulkan.memory;

import static org.sarge.lib.util.Check.notNull;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.platform.vulkan.VkMemoryRequirements;
import org.sarge.jove.platform.vulkan.memory.Allocator.AllocationException;
import org.sarge.lib.util.Check;

/**
 * The <i>allocation service</i>
 * TODO
 * @author Sarge
 */
public class AllocationService {
	private final MemorySelector selector;
	private final Allocator def;
	private final Map<Object, Allocator> map = new HashMap<>();

	/**
	 * Constructor.
	 * @param selector		Memory selector
	 * @param allocator		Default memory allocator
	 */
	public AllocationService(MemorySelector selector, Allocator allocator) {
		this.selector = notNull(selector);
		this.def = notNull(allocator);
	}

	/**
	 * Overrides the allocator for the given memory properties.
	 * @param props			Memory properties
	 * @param allocator		Allocator
	 */
	public void init(MemoryProperties<?> props, Allocator allocator) {
		Check.notNull(props);
		Check.notNull(allocator);
		map.put(props, allocator);
	}

	/**
	 * Allocates device memory for the given request.
	 * @param reqs			Requirements
	 * @param props			Memory properties
	 * @return New device memory
	 * @throws AllocationException if the memory cannot be allocated
	 */
	public DeviceMemory allocate(VkMemoryRequirements reqs, MemoryProperties<?> props) throws AllocationException {
		final Allocator allocator = map.getOrDefault(props, def);
		final MemoryType type = selector.select(reqs, props);
		return allocator.allocate(type, reqs.size);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("default", def)
				.append("allocators", map)
				.append("selector", selector)
				.build();
	}
}
