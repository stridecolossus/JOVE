package org.sarge.jove.platform.vulkan.pipeline;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.sarge.jove.common.IntegerEnumeration;
import org.sarge.jove.common.NativeObject.Handle;
import org.sarge.jove.platform.vulkan.VkPipelineLayoutCreateInfo;
import org.sarge.jove.platform.vulkan.VkPipelineStage;
import org.sarge.jove.platform.vulkan.VkPushConstantRange;
import org.sarge.jove.platform.vulkan.pipeline.PipelineLayout.Builder;
import org.sarge.jove.platform.vulkan.pipeline.PipelineLayout.PushConstantRange;
import org.sarge.jove.platform.vulkan.render.DescriptorSet;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

class PipelineLayoutTest extends AbstractVulkanTest {
	private Builder builder;

	@BeforeEach
	void before() {
		builder = new Builder(dev);
	}

	@Test
	void build() {
		// Create descriptor set layout
		final DescriptorSet.Layout set = mock(DescriptorSet.Layout.class);
		when(set.handle()).thenReturn(new Handle(new Pointer(42)));

		// TODO - range

		// Create layout
		final PipelineLayout layout = builder
				.add(set)
				.build();

		// Check layout
		assertNotNull(layout);
		assertNotNull(layout.handle());

		// Check pipeline allocation
		final ArgumentCaptor<VkPipelineLayoutCreateInfo> captor = ArgumentCaptor.forClass(VkPipelineLayoutCreateInfo.class);
		verify(lib).vkCreatePipelineLayout(eq(dev.handle()), captor.capture(), isNull(), isA(PointerByReference.class));

		// Check descriptor
		final VkPipelineLayoutCreateInfo info = captor.getValue();
		assertNotNull(info);

		// Check descriptor-set layouts
		assertEquals(1, info.setLayoutCount);
		assertNotNull(info.pSetLayouts);
	}

	@Test
	void buildEmpty() {
		assertNotNull(builder.build());
	}

	@Test
	void range() {
		final var stages = Set.of(VkPipelineStage.VERTEX_SHADER);
		final PushConstantRange range = new PushConstantRange(stages, 1, 2);
		final VkPushConstantRange struct = new VkPushConstantRange();
		range.populate(struct);
		assertEquals(IntegerEnumeration.mask(stages), struct.stageFlags);
		assertEquals(1, struct.size);
		assertEquals(2, struct.offset);
	}

	@Test
	void destroy() {
		final PipelineLayout layout = builder.build();
		final Handle handle = layout.handle();
		layout.destroy();
		verify(lib).vkDestroyPipelineLayout(dev.handle(), handle, null);
	}
}
