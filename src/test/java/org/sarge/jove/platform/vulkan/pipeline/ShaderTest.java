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

	@Nested
	class SpecialisationConstantTests {
		@Test
		void empty() {
			assertEquals(null, Shader.constants(Map.of()));
		}

		@Test
		void invalid() {
			assertThrows(UnsupportedOperationException.class, () -> Shader.constants(Map.of(0, "doh")));
		}

		@Test
		void constants() {
			// Build constants table
			final Map<Integer, Object> map = new LinkedHashMap<>();
			map.put(1, 1);			// Integer
			map.put(2, 2f);			// Float
			map.put(3, true);		// Boolean

			// Create constants
			final VkSpecializationInfo info = Shader.constants(map);
			assertNotNull(info);
			assertEquals(3, info.mapEntryCount);
			assertEquals(4 + 4 + 4, info.dataSize);

			// Check first entry
			final VkSpecializationMapEntry entry = info.pMapEntries;
			assertNotNull(entry);
			assertEquals(1, entry.constantID);
			assertEquals(0, entry.offset);
			assertEquals(4, entry.size);

			// Check data buffer
			final ByteBuffer bb = ByteBuffer.allocate(12);
			bb.putInt(1);
			bb.putFloat(2f);
			bb.putInt(1);
			assertEquals(bb, info.pData);
		}
	}
}
