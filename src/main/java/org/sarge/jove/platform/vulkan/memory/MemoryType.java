package org.sarge.jove.platform.vulkan.memory;

import java.util.Set;

import org.sarge.jove.platform.vulkan.VkMemoryPropertyFlag;
import org.sarge.lib.util.Check;

/**
 *
 * @author Sarge
 */
public record MemoryType(int index, Set<VkMemoryPropertyFlag> properties) {
	/**
	 * Constructor.
	 * @param index
	 * @param properties
	 */
	public MemoryType {
		Check.zeroOrMore(index);
		Check.notNull(properties);
	}
}
