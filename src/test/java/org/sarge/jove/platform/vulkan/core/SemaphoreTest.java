package org.sarge.jove.platform.vulkan.core;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.sarge.jove.platform.vulkan.VkSemaphoreCreateInfo;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

import com.sun.jna.ptr.PointerByReference;

public class SemaphoreTest extends AbstractVulkanTest {
	@Test
	void create() {
		// Create semaphore
		final Semaphore semaphore = Semaphore.create(dev);
		assertNotNull(semaphore);

		// Check API
		final ArgumentCaptor<VkSemaphoreCreateInfo> captor = ArgumentCaptor.forClass(VkSemaphoreCreateInfo.class);
		verify(lib).vkCreateSemaphore(eq(dev), captor.capture(), isNull(), isA(PointerByReference.class));

		// Check create descriptor
		final VkSemaphoreCreateInfo info = captor.getValue();
		assertNotNull(info);
		assertEquals(0, info.flags);
	}

}
