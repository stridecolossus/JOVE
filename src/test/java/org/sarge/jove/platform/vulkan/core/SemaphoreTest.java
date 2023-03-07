package org.sarge.jove.platform.vulkan.core;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.vulkan.VkSemaphoreCreateInfo;
import org.sarge.jove.platform.vulkan.common.MockDeviceContext;

public class SemaphoreTest {
	@Test
	void create() {
		final var dev = new MockDeviceContext();
		final Semaphore semaphore = Semaphore.create(dev);
		final var info = new VkSemaphoreCreateInfo() {
			@Override
			public boolean equals(Object obj) {
				return true;
			}
		};
		assertNotNull(semaphore);
		verify(dev.library()).vkCreateSemaphore(dev, info, null, dev.factory().pointer());
	}
}
