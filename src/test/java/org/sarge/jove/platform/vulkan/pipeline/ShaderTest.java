package org.sarge.jove.platform.vulkan.pipeline;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

public class ShaderTest extends AbstractVulkanTest {
	private static final byte[] CODE = new byte[]{42};

	private Shader shader;

	@BeforeEach
	void before() {
		shader = Shader.create(dev, CODE);
	}

	@Test
	void create() {
		// Check shader
		assertNotNull(shader);
		assertNotNull(shader.handle());

		// Check API
		final var expected = new VkShaderModuleCreateInfo() {
			@Override
			public boolean equals(Object obj) {
				final var info = (VkShaderModuleCreateInfo) obj;
				assertNotNull(info);
				assertEquals(CODE.length, info.codeSize);
				assertNotNull(info.pCode);
				return true;
			}
		};
		verify(lib).vkCreateShaderModule(dev, expected, null, factory.pointer());
	}

	@DisplayName("A shader can be destroyed")
	@Test
	void destroy() {
		shader.destroy();
		verify(lib).vkDestroyShaderModule(dev, shader, null);
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

	@DisplayName("Shader specialisation constants...")
	@Nested
	class SpecialisationConstantTests {
		@DisplayName("can be constructed from an indexed map of values")
		@Test
		void constants() {
			// Build constants table
			final Map<Integer, Object> map = Map.of(
					1, 1,
					2, 2f,
					3, true
			);

			// Create constants
			final VkSpecializationInfo info = Shader.constants(new LinkedHashMap<>(map));
			assertNotNull(info);
			assertNotNull(info.pMapEntries);
			assertEquals(3, info.mapEntryCount);
			assertEquals(4 + 4 + 4, info.dataSize);

			// Check data buffer
			final ByteBuffer bb = ByteBuffer.allocate(12);
			bb.putInt(1);
			bb.putFloat(2f);
			bb.putInt(1);
			assertEquals(bb, info.pData);
		}

		@DisplayName("can be empty")
		@Test
		void empty() {
			assertEquals(null, Shader.constants(Map.of()));
		}

		@DisplayName("must have a supported data type")
		@Test
		void invalid() {
			assertThrows(UnsupportedOperationException.class, () -> Shader.constants(Map.of(1, "doh")));
		}
	}
}
