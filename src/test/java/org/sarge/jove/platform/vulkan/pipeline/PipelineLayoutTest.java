package org.sarge.jove.platform.vulkan.pipeline;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.sarge.jove.platform.vulkan.VkPipelineLayoutCreateInfo;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

public class PipelineLayoutTest extends AbstractVulkanTest {
	private PipelineLayout.Builder builder;

	@BeforeEach
	void before() {
		builder = new PipelineLayout.Builder(dev);
	}

	@Test
	void create() {
		// Create layout
		final PipelineLayout layout = builder
				// TODO
				.result();

		// Check layout
		assertNotNull(layout);
		assertNotNull(layout.handle());

		// Check pipeline allocation
		final ArgumentCaptor<VkPipelineLayoutCreateInfo> captor = ArgumentCaptor.forClass(VkPipelineLayoutCreateInfo.class);
		verify(lib).vkCreatePipelineLayout(eq(dev.handle()), captor.capture(), isNull(), eq(factory.ptr));

		// Check descriptor
		// TODO
	}
}
