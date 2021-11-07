package org.sarge.jove.platform.vulkan.core;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.sarge.jove.platform.vulkan.VkShaderModuleCreateInfo;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

import com.sun.jna.ptr.PointerByReference;

public class ShaderTest extends AbstractVulkanTest {
	private static final byte[] CODE = new byte[]{42};

	private Shader shader;

	@BeforeEach
	void before() {
		shader = Shader.create(dev, CODE);
	}

	@Test
	void create() {
		// Check API
		final var captor = ArgumentCaptor.forClass(VkShaderModuleCreateInfo.class);
		verify(lib).vkCreateShaderModule(eq(dev), captor.capture(), isNull(), isA(PointerByReference.class));

		// Create shader
		final Shader shader = Shader.create(dev, CODE);
		assertNotNull(shader);
		assertNotNull(shader.handle());

		// Check descriptor
		final var info = captor.getValue();
		assertNotNull(info);
		assertEquals(CODE.length, info.codeSize);

		// Check code buffer
		assertNotNull(info.pCode);
		assertEquals(1, info.pCode.limit());
		assertEquals(1, info.pCode.capacity());
		assertEquals((byte) 42, info.pCode.rewind().get());
	}

	@Test
	void destroy() {
		shader.destroy();
		verify(lib).vkDestroyShaderModule(dev, shader, null);
	}

	@Nested
	class LoaderTests {
		private Shader.Loader loader;

		@BeforeEach
		void before() {
			loader = new Shader.Loader(dev);
		}

		@SuppressWarnings("resource")
		@Test
		void map() throws IOException {
			final InputStream in = mock(InputStream.class);
			assertEquals(in, loader.map(in));
		}

		@Test
		void load() throws IOException {
			final Shader shader = loader.load(new ByteArrayInputStream(CODE));
			assertNotNull(shader);
		}

		@SuppressWarnings("resource")
		@Test
		void loadInvalidFile() throws Exception {
			loader.load(new FileInputStream("./src/test/resources/thiswayup.jpg"));
		}
	}
}
