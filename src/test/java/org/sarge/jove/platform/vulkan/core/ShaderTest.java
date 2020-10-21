package org.sarge.jove.platform.vulkan.core;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.sarge.jove.common.NativeObject.Handle;
import org.sarge.jove.platform.vulkan.VkShaderModuleCreateInfo;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;
import org.sarge.jove.util.Loader;

public class ShaderTest extends AbstractVulkanTest {
	private static final byte[] CODE = new byte[]{42};

	private Shader shader;

	@BeforeEach
	void before() {
		shader = Shader.create(dev, CODE);
	}

	@Test
	void constructor() {
		assertNotNull(shader.handle());
	}

	@Test
	void create() {
		// Check allocation
		final var captor = ArgumentCaptor.forClass(VkShaderModuleCreateInfo.class);
		verify(lib).vkCreateShaderModule(eq(dev.handle()), captor.capture(), isNull(), eq(factory.ptr));

		// Check descriptor
		final var info = captor.getValue();
		assertNotNull(info);
		assertEquals(CODE.length, info.codeSize);

		// Check code buffer
		info.pCode.flip();
		assertNotNull(info.pCode);
		assertEquals(1, info.pCode.capacity());
		assertEquals((byte) 42, info.pCode.get());
	}

	@Test
	void destroy() {
		final Handle handle = shader.handle();
		shader.destroy();
		verify(lib).vkDestroyShaderModule(dev.handle(), handle, null);
	}

	@Nested
	class LoaderTests {
		private Loader<InputStream, Shader> loader;

		@BeforeEach
		void before() {
			loader = Shader.loader(dev);
		}

		@Test
		void load() {
			shader = loader.load(new ByteArrayInputStream(new byte[]{}));
			assertNotNull(shader);
			verify(lib, atLeastOnce()).vkCreateShaderModule(eq(dev.handle()), isA(VkShaderModuleCreateInfo.class), isNull(), eq(factory.ptr));
		}

		@SuppressWarnings("resource")
		@Test
		void loadFile() throws FileNotFoundException {
			loader.load(new FileInputStream("./src/test/resources/thiswayup.jpg"));
		}
	}
}
