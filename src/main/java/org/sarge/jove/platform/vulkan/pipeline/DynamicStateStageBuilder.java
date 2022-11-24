package org.sarge.jove.platform.vulkan.pipeline;

import static org.sarge.lib.util.Check.notNull;

import java.util.HashSet;
import java.util.Set;

import org.sarge.jove.platform.vulkan.VkDynamicState;
import org.sarge.jove.platform.vulkan.VkPipelineDynamicStateCreateInfo;
import org.sarge.jove.util.IntegerArray;

/**
 * Builder for the dynamic state pipeline stage.
 * @see VkPipelineDynamicStateCreateInfo
 * @author Sarge
 */
public class DynamicStateStageBuilder extends AbstractStageBuilder<VkPipelineDynamicStateCreateInfo> {
	private final Set<VkDynamicState> states = new HashSet<>();

	/**
	 * Adds a dynamic state.
	 * @param state Dynamic state
	 */
	public DynamicStateStageBuilder state(VkDynamicState state) {
		states.add(notNull(state));
		return this;
	}

	@Override
	VkPipelineDynamicStateCreateInfo get() {
		// Ignore if no dynamic state
		if(states.isEmpty()) {
			return null;
		}

		// Convert states to integer array
		final int[] array = states.stream().mapToInt(VkDynamicState::value).toArray();

		// Populate descriptor
		final var info = new VkPipelineDynamicStateCreateInfo();
		info.dynamicStateCount = states.size();
		info.pDynamicStates = new IntegerArray(array);

		return info;
	}
}
