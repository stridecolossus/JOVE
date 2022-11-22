package org.sarge.jove.platform.vulkan.render;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.DescriptorResource;
import org.sarge.jove.platform.vulkan.core.Command;
import org.sarge.jove.platform.vulkan.pipeline.PipelineLayout;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

public class DescriptorSetTest extends AbstractVulkanTest {
	private Binding binding;
	private DescriptorLayout layout;
	private DescriptorSet descriptor;
	private DescriptorResource res;

	@BeforeEach
	void before() {
		// Create layout with a sampler binding
		binding = new Binding(1, VkDescriptorType.COMBINED_IMAGE_SAMPLER, 1, Set.of(VkShaderStage.FRAGMENT));
		layout = new DescriptorLayout(new Handle(1), dev, List.of(binding));

		// Create sampler resource
		res = mock(DescriptorResource.class);
		when(res.type()).thenReturn(VkDescriptorType.COMBINED_IMAGE_SAMPLER);

		// Create descriptor set
		descriptor = new DescriptorSet(new Handle(2), layout);
	}

	@Test
	void constructor() {
		assertEquals(new Handle(2), descriptor.handle());
		assertEquals(layout, descriptor.layout());
	}

	@Test
	void invalid() {
		final Binding other = new Binding(2, VkDescriptorType.COMBINED_IMAGE_SAMPLER, 1, Set.of(VkShaderStage.FRAGMENT));
		assertThrows(IllegalArgumentException.class, () -> descriptor.set(other, res));
	}

	@Test
	void set() {
		descriptor.set(binding, res);
	}

	@Test
	void setInvalidResource() {
		when(res.type()).thenReturn(VkDescriptorType.STORAGE_BUFFER);
		assertThrows(IllegalArgumentException.class, () -> descriptor.set(binding, res));
	}

	@Test
	void bind() {
		// Create bind command
		final PipelineLayout pipelineLayout = mock(PipelineLayout.class);
		final Command bind = descriptor.bind(pipelineLayout);
		assertNotNull(bind);

		// Check API
		final Command.Buffer cb = mock(Command.Buffer.class);
		bind.record(lib, cb);
		verify(lib).vkCmdBindDescriptorSets(cb, VkPipelineBindPoint.GRAPHICS, pipelineLayout, 0, 1, NativeObject.array(List.of(descriptor)), 0, null);
	}

	@Test
	void update() {
		// Apply update
		descriptor.set(binding, res);
		assertEquals(1, DescriptorSet.update(dev, Set.of(descriptor)));

		// Init expected write descriptor
		final var write = new VkWriteDescriptorSet() {
			@Override
			public boolean equals(Object obj) {
				final var expected = (VkWriteDescriptorSet) obj;
				assertEquals(1, expected.dstBinding);
				assertEquals(VkDescriptorType.COMBINED_IMAGE_SAMPLER, expected.descriptorType);
				assertEquals(descriptor.handle(), expected.dstSet);
				assertEquals(1, expected.descriptorCount);
				assertEquals(0, expected.dstArrayElement);
				return true;
			}
		};

		// Check API
		verify(lib).vkUpdateDescriptorSets(dev, 1, new VkWriteDescriptorSet[]{write}, 0, null);
		verify(res).populate(write);
	}

	@Test
	void updateNotModified() {
		descriptor.set(binding, res);
		DescriptorSet.update(dev, Set.of(descriptor));
		assertEquals(0, DescriptorSet.update(dev, Set.of(descriptor)));
	}

	@Test
	void updateResourceNotPopulated() {
		assertThrows(IllegalStateException.class, () -> DescriptorSet.update(dev, Set.of(descriptor)));
	}
}
