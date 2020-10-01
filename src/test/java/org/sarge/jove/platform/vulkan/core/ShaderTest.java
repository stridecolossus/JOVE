package org.sarge.jove.platform.vulkan.core;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.function.Function;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.Service.ServiceException;
import org.sarge.jove.platform.vulkan.VkShaderModuleCreateInfo;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

import com.sun.jna.ptr.PointerByReference;

public class ShaderTest extends AbstractVulkanTest {
	private Shader shader;

	@BeforeEach
	void before() {
		shader = Shader.create(dev, new byte[]{42});
	}

	@Test
	void constructor() {
		assertNotNull(shader.handle());
	}

	@Test
	void create() {
		// Check allocation
		final var captor = ArgumentCaptor.forClass(VkShaderModuleCreateInfo.class);
		verify(lib).vkCreateShaderModule(eq(dev.handle()), captor.capture(), isNull(), isA(PointerByReference.class));

		// Check descriptor
		final var info = captor.getValue();
		assertNotNull(info);
		assertEquals(1, info.codeSize);
		assertNotNull(info.pCode);
	}

	@Test
	void destroy() {
		final Handle handle = shader.handle();
		shader.destroy();
		verify(lib).vkDestroyShaderModule(dev.handle(), handle, null);
	}

	@Nested
	class LoaderTests {
		private static final String FILENAME = "filename";

		private Shader.Loader loader;
		private Function<String, byte[]> mapper;

		@SuppressWarnings("unchecked")
		@BeforeEach
		void before() {
			mapper = mock(Function.class);
			when(mapper.apply(FILENAME)).thenReturn(new byte[]{});
			loader = new Shader.Loader(mapper, dev);
		}

		@Test
		void load() {
			shader = loader.load(FILENAME);
			assertNotNull(shader);
			verify(mapper).apply(FILENAME);
		}

		@Test
		void loadFailed() {
			when(mapper.apply(FILENAME)).thenThrow(ServiceException.class);
			assertThrows(ServiceException.class, () -> loader.load(FILENAME));
		}

		@Test
		void create() {
			loader = Shader.Loader.create("./src/test/resources", dev);
			assertNotNull(loader);
			assertNotNull(loader.load("triangle.vert.spv"));
		}

		@Test
		void createInvalidDirectory() {
			assertThrows(IllegalArgumentException.class, () -> Shader.Loader.create("cobblers", dev));
		}
	}
}
