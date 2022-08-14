package org.sarge.jove.platform.vulkan.pipeline;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.pipeline.Shader.Constant;
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

	@DisplayName("Shader specialisation constants...")
	@Nested
	class SpecialisationConstantTests {
		@DisplayName("can be floating-point values")
		@Test
		void floats() {
			final Constant constant = new Constant(42f);
			final ByteBuffer bb = mock(ByteBuffer.class);
			constant.append(bb);
			verify(bb).putFloat(42f);
			assertEquals(Float.BYTES, constant.size());
		}

		@DisplayName("can be integer values")
		@Test
		void integer() {
			final Constant constant = new Constant(42);
			final ByteBuffer bb = mock(ByteBuffer.class);
			constant.append(bb);
			verify(bb).putInt(42);
			assertEquals(Integer.BYTES, constant.size());
		}

		@DisplayName("can be boolean values")
		@Test
		void bool() {
			final Constant constant = new Constant(true);
			final ByteBuffer bb = mock(ByteBuffer.class);
			constant.append(bb);
			verify(bb).putInt(1);
			assertEquals(Integer.BYTES, constant.size());
		}

		@DisplayName("must be a supported type")
		@Test
		void invalid() {
			assertThrows(UnsupportedOperationException.class, () -> new Shader.Constant("doh"));
		}

		@DisplayName("can be empty")
		@Test
		void empty() {
			assertEquals(null, Shader.Constant.build(Map.of()));
		}

		@DisplayName("can be converted to a data buffer of the constant values")
		@Test
		void constants() {
			// Build constants table
			final Map<Integer, Constant> map = Map.of(
					1, new Constant(1),
					2, new Constant(2f),
					3, new Constant(true)
			);

			// Create constants
			final VkSpecializationInfo info = Constant.build(new LinkedHashMap<>(map));
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
	}
}
