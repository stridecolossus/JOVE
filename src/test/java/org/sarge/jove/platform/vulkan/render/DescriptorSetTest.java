package org.sarge.jove.platform.vulkan.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.sarge.jove.common.Handle;
import org.sarge.jove.common.NativeObject;
import org.sarge.jove.platform.vulkan.VkDescriptorType;
import org.sarge.jove.platform.vulkan.VkPipelineBindPoint;
import org.sarge.jove.platform.vulkan.VkShaderStage;
import org.sarge.jove.platform.vulkan.VkWriteDescriptorSet;
import org.sarge.jove.platform.vulkan.common.DescriptorResource;
import org.sarge.jove.platform.vulkan.core.Command;
import org.sarge.jove.platform.vulkan.pipeline.PipelineLayout;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

import com.sun.jna.Pointer;

public class DescriptorSetTest extends AbstractVulkanTest {
	private Binding binding;
	private DescriptorLayout layout;
	private DescriptorSet descriptor;
	private DescriptorResource res;

	@BeforeEach
	void before() {
		// Create layout with a sampler binding
		binding = new Binding(1, VkDescriptorType.COMBINED_IMAGE_SAMPLER, 1, Set.of(VkShaderStage.FRAGMENT));
		layout = new DescriptorLayout(new Pointer(1), dev, List.of(binding));

		// Create sampler resource
		res = mock(DescriptorResource.class);
		when(res.type()).thenReturn(VkDescriptorType.COMBINED_IMAGE_SAMPLER);

		// Create descriptor set
		descriptor = new DescriptorSet(new Handle(new Pointer(2)), layout);
	}

	@Test
	void constructor() {
		assertEquals(new Handle(new Pointer(2)), descriptor.handle());
		assertEquals(layout, descriptor.layout());
	}

	@Test
	void resource() {
		assertEquals(null, descriptor.resource(binding));
	}

	@Test
	void invalid() {
		final Binding other = new Binding(2, VkDescriptorType.COMBINED_IMAGE_SAMPLER, 1, Set.of(VkShaderStage.FRAGMENT));
		assertThrows(IllegalArgumentException.class, () -> descriptor.resource(other));
		assertThrows(IllegalArgumentException.class, () -> descriptor.set(other, res));
	}

	@Test
	void set() {
		descriptor.set(binding, res);
		assertEquals(res, descriptor.resource(binding));
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
		bind.execute(lib, cb);
		verify(lib).vkCmdBindDescriptorSets(cb, VkPipelineBindPoint.GRAPHICS, pipelineLayout, 0, 1, NativeObject.array(List.of(descriptor)), 0, null);
	}

	@Test
	void update() {
		// Apply update
		descriptor.set(binding, res);
		final int count = DescriptorSet.update(dev, Set.of(descriptor));
		assertEquals(1, count);

		// Check API
		final ArgumentCaptor<VkWriteDescriptorSet[]> captor = ArgumentCaptor.forClass(VkWriteDescriptorSet[].class);
		verify(lib).vkUpdateDescriptorSets(eq(dev), eq(1), captor.capture(), eq(0), isNull());

		// Check updates array
		final VkWriteDescriptorSet[] array = captor.getValue();
		assertNotNull(array);
		assertEquals(1, array.length);

		// Check update descriptor
		final VkWriteDescriptorSet write = array[0];
		assertNotNull(write);
		assertEquals(1, write.dstBinding);
		assertEquals(VkDescriptorType.COMBINED_IMAGE_SAMPLER, write.descriptorType);
		assertEquals(descriptor.handle(), write.dstSet);
		assertEquals(1, write.descriptorCount);
		assertEquals(0, write.dstArrayElement);

		// Check delegated to resource
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
