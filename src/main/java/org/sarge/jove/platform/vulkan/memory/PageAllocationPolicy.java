package org.sarge.jove.platform.vulkan.memory;

import static org.sarge.lib.util.Check.oneOrMore;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.platform.vulkan.VkPhysicalDeviceLimits;
import org.sarge.jove.platform.vulkan.common.DeviceContext;
import org.sarge.jove.platform.vulkan.util.VulkanProperty;

/**
 * A <i>page allocation policy</i> quantises allocation requests to a given <i>page</i> size.
 * @see #of(DeviceContext)
 * @author Sarge
 */
public class PageAllocationPolicy implements AllocationPolicy {
	/**
	 * Helper - Creates an allocation policy based on the optimal page granularity for the hardware.
	 * @param dev Logical device
	 * @return New page allocation policy
	 * @see VkPhysicalDeviceLimits#bufferImageGranularity
	 */
	public static PageAllocationPolicy of(DeviceContext dev) {
		final VulkanProperty.Provider provider = dev.provider();
		final long granularity = provider.property("bufferImageGranularity").get();
		return new PageAllocationPolicy(granularity);
	}

	private final long page;
	private final long min;

	/**
	 * Constructor.
	 * @param page 		Page size (bytes)
	 * @param min		Minimum number of pages
	 */
	public PageAllocationPolicy(long page, int min) {
		this.page = oneOrMore(page);
		this.min = oneOrMore(min) * page;
	}

	/**
	 * Constructor.
	 * @param page Page size (bytes)
	 */
	public PageAllocationPolicy(long page) {
		this(page, 1);
	}

	@Override
	public long apply(long size, long current) {
		final long num = 1 + ((size - 1) / page);
		return Math.max(min, num * page);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("page", page)
				.append("min", min)
				.build();
	}
}
