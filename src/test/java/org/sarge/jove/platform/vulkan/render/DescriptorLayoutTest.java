package org.sarge.jove.platform.vulkan.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.VkDescriptorSetLayoutCreateInfo;
import org.sarge.jove.platform.vulkan.VkDescriptorType;
import org.sarge.jove.platform.vulkan.VkShaderStage;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

import com.sun.jna.Pointer;

public class DescriptorLayoutTest extends AbstractVulkanTest {
	private Binding binding;
	private DescriptorLayout layout;

	@BeforeEach
	void before() {
		binding = new Binding(1, VkDescriptorType.COMBINED_IMAGE_SAMPLER, 1, Set.of(VkShaderStage.FRAGMENT));
		layout = new DescriptorLayout(new Pointer(1), dev, List.of(binding));
	}

	@Test
	void constructor() {
		assertEquals(new Handle(new Pointer(1)), layout.handle());
		assertEquals(binding, layout.binding(1));
	}

	@Test
	void constructorEmptyBindings() {
		assertThrows(IllegalArgumentException.class, () -> new DescriptorLayout(new Pointer(1), dev, List.of()));
	}

	@Test
	void constructorDuplicateBindingIndex() {
		assertThrows(IllegalStateException.class, () -> new DescriptorLayout(new Pointer(1), dev, List.of(binding, binding)));
	}

	@Test
	void destroy() {
		layout.destroy();
		verify(lib).vkDestroyDescriptorSetLayout(dev, layout, null);
	}

	@Test
	void create() {
		// Create layout
		layout = DescriptorLayout.create(dev, List.of(binding));
		assertNotNull(layout);

		// Check API
		final ArgumentCaptor<VkDescriptorSetLayoutCreateInfo> captor = ArgumentCaptor.forClass(VkDescriptorSetLayoutCreateInfo.class);
		verify(lib).vkCreateDescriptorSetLayout(eq(dev), captor.capture(), isNull(), eq(POINTER));

		// Check create descriptor
		final VkDescriptorSetLayoutCreateInfo info = captor.getValue();
		assertNotNull(info);
		assertEquals(1, info.bindingCount);
		assertNotNull(info.pBindings);
	}
}
