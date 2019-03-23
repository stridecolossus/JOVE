package org.sarge.jove.platform.vulkan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.Service.ServiceException;
import org.sarge.jove.platform.vulkan.VulkanLibrary.Version;

public class VulkanLibraryTest {
	@Test
	public void check() {
		VulkanLibrary.check(VkResult.VK_SUCCESS.value());
		assertThrows(ServiceException.class, () -> VulkanLibrary.check(VkResult.VK_ERROR_DEVICE_LOST.value()));
	}

	@Test
	public void checkUnknownResultCode() {
		assertThrows(ServiceException.class, () -> VulkanLibrary.check(-1));
	}

	@Nested
	class VersionTests {
		private Version ver;

		@BeforeEach
		public void before() {
			ver = new Version(1, 2, 3);
		}

		@Test
		public void toInteger() {
			assertEquals(4202499, ver.toInteger());
		}

		@Test
		public void compare() {
			final Version other = new Version(1, 2, 4);
			assertEquals(0, ver.compareTo(ver));
			assertEquals(-1, ver.compareTo(other));
			assertEquals(+1, other.compareTo(ver));
		}

		@Test
		public void string() {
			assertEquals("1.2.3", ver.toString());
		}
	}
}
