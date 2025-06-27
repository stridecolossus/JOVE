package org.sarge.jove.platform.vulkan.pipeline;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.VkShaderModuleCreateInfo;
import org.sarge.jove.platform.vulkan.common.*;

class ShaderTest {
	private static final byte[] CODE = new byte[]{42};

	private DeviceContext dev;
	private Shader shader;

	@BeforeEach
	void before() {
		dev = new MockDeviceContext();
		shader = Shader.create(dev, CODE);
	}

	@Test
	void create() {
		final var expected = new VkShaderModuleCreateInfo() {
			@Override
			public boolean equals(Object obj) {
				final var info = (VkShaderModuleCreateInfo) obj;
				assertEquals(CODE.length, info.codeSize);
				assertNotNull(info.pCode);
				return true;
			}
		};
		verify(dev.library()).vkCreateShaderModule(dev, expected, null, dev.factory().pointer());
	}

	@DisplayName("A shader can be destroyed")
	@Test
	void destroy() {
		shader.destroy();
		verify(dev.library()).vkDestroyShaderModule(dev, shader, null);
	}

	@Nested
	class LoaderTest {
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
	}
}
