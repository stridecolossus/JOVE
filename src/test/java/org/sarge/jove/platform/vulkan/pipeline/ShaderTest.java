package org.sarge.jove.platform.vulkan.pipeline;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.vulkan.VkShaderModuleCreateInfo;
import org.sarge.jove.platform.vulkan.VkSpecializationInfo;
import org.sarge.jove.platform.vulkan.VkSpecializationMapEntry;
import org.sarge.jove.platform.vulkan.pipeline.Shader.ConstantsTable;
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
		verify(lib).vkCreateShaderModule(dev, expected, null, POINTER);
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
	class ConstantsTableTest {
		private ConstantsTable builder;

		@BeforeEach
		void before() {
			builder = new ConstantsTable();
		}

		@Test
		void buildEmpty() {
			assertNull(builder.build());
		}

		@Test
		void build() {
			// Build constants
			final VkSpecializationInfo info = builder
					.add(1, 1)
					.add(2, 2f)
					.add(3, true)
					.build();

			// Check constants
			assertNotNull(info);
			assertEquals(3, info.mapEntryCount);

			// Check an entry
			final VkSpecializationMapEntry entry = info.pMapEntries;
			assertEquals(1, entry.constantID);
			assertEquals(0, entry.offset);
			assertEquals(Integer.BYTES, entry.size);

			// Check data size
			final int size = 4 + 4 + 4;
			assertEquals(size, info.dataSize);

			// Check data buffer
			final ByteBuffer bb = ByteBuffer.allocate(size);
			bb.putInt(1);
			bb.putFloat(2f);
			bb.putInt(1);
			assertEquals(bb, info.pData);
		}

		@Test
		void buildDuplicateConstant() {
			builder.add(1, 2);
			assertThrows(IllegalArgumentException.class, () -> builder.add(1, 2));
		}

		@Test
		void buildInvalidConstantType() {
			assertThrows(IllegalArgumentException.class, () -> builder.add(1, new Object()));
		}
	}
}
