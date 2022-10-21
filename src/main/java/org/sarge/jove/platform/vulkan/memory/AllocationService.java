package org.sarge.jove.platform.vulkan.memory;

import static org.sarge.lib.util.Check.notNull;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.platform.vulkan.VkMemoryRequirements;
import org.sarge.jove.platform.vulkan.memory.Allocator.AllocationException;

/**
 * The <i>allocation service</i> is responsible for serving memory requests.
 * <p>
 * The {@link #allocator(MemoryProperties)} provider method can be overridden to implement different strategies for memory allocation.
 * <p>
 * @author Sarge
 */
public class AllocationService {
	private final MemorySelector selector;
	private final Allocator allocator;

	/**
	 * Constructor.
	 * @param selector		Memory selector
	 * @param allocator		Memory allocator
	 */
	public AllocationService(MemorySelector selector, Allocator allocator) {
		this.selector = notNull(selector);
		this.allocator = notNull(allocator);
	}

	/**
	 * Provider for the allocator for the given memory properties.
	 * @param props Memory properties
	 * @return Memory allocator
	 */
	protected Allocator allocator(MemoryProperties<?> props) {
		return allocator;
	}
	// TODO - better if allocator accepted props as well, then can be adapter, no need for this override?

	/**
	 * Allocates device memory for the given request.
	 * <p>
	 * The allocation process is:
	 * <ol>
	 * <li>Select the appropriate memory type for the request via the {@link MemorySelector}</li>
	 * <li>Determine the allocator for the request (see {@link #allocator(MemoryProperties)})</li>
	 * <li>Delegate to the allocator</li>
	 * </ol>
	 * <p>
	 * @param reqs			Memory requirements
	 * @param props			Memory properties
	 * @return New device memory
	 * @throws AllocationException if the memory cannot be allocated
	 */
	public DeviceMemory allocate(VkMemoryRequirements reqs, MemoryProperties<?> props) throws AllocationException {
		final MemoryType type = selector.select(reqs, props);
		final Allocator allocator = allocator(props);
		return allocator.allocate(type, reqs.size);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append(selector)
				.append(allocator)
				.build();
	}
}
