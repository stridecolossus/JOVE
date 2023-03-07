package org.sarge.jove.platform.vulkan.pipeline;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.*;
import java.util.List;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.*;
import org.sarge.jove.io.*;
import org.sarge.jove.platform.vulkan.VkPipelineCacheCreateInfo;
import org.sarge.jove.platform.vulkan.common.*;
import org.sarge.jove.platform.vulkan.core.VulkanLibrary;
import org.sarge.jove.platform.vulkan.pipeline.PipelineCache.Loader;

import com.sun.jna.ptr.IntByReference;

public class PipelineCacheTest {
	private static final byte[] DATA = new byte[42];

	private PipelineCache cache;
	private DeviceContext dev;
	private VulkanLibrary lib;

	@BeforeEach
	void before() {
		dev = new MockDeviceContext();
		lib = dev.library();
		cache = new PipelineCache(new Handle(1), dev);
	}

	@Test
	void constructor() {
		assertEquals(new Handle(1), cache.handle());
		assertEquals(dev, cache.device());
		assertEquals(false, cache.isDestroyed());
	}

	@Test
	void create() {
		// Create cache
		cache = PipelineCache.create(dev, DATA);
		assertEquals(dev, cache.device());
		assertEquals(false, cache.isDestroyed());

		// Check API
		final var expected = new VkPipelineCacheCreateInfo() {
			@Override
			public boolean equals(Object obj) {
				final var actual = (VkPipelineCacheCreateInfo) obj;
				assertEquals(0, actual.flags);
				assertEquals(DATA.length, actual.initialDataSize);
				assertNotNull(actual.pInitialData);
				return true;
			}
		};
		verify(lib).vkCreatePipelineCache(dev, expected, null, dev.factory().pointer());
	}

	@Test
	void createEmpty() {
		PipelineCache.create(dev, null);
	}

	@Test
	void data() {
		final IntByReference count = dev.factory().integer();
		final ByteBuffer data = cache.data();
		assertEquals(1, data.capacity());
		verify(lib).vkGetPipelineCacheData(dev, cache, count, null);
		verify(lib).vkGetPipelineCacheData(dev, cache, count, data);
	}

	@Test
	void merge() {
		final PipelineCache other = new PipelineCache(new Handle(2), dev);
		final List<PipelineCache> list = List.of(other, other);
		cache.merge(list);
		verify(lib).vkMergePipelineCaches(dev, cache, 2, NativeObject.array(list));
	}

	@Test
	void destroy() {
		cache.destroy();
		assertEquals(true, cache.isDestroyed());
		verify(lib).vkDestroyPipelineCache(dev, cache, null);
	}

	@Nested
	class LoaderTests {
		private Loader loader;

		@BeforeEach
		void before() {
			loader = new Loader(dev);
		}

		@Test
		void load() throws IOException {
			final PipelineCache cache = loader.load(new ByteArrayInputStream(DATA));
			assertNotNull(cache);
		}

		@Test
		void write() throws IOException {
			final ByteArrayOutputStream out = new ByteArrayOutputStream();
			final PipelineCache cache = mock(PipelineCache.class);
			when(cache.data()).thenReturn(BufferHelper.buffer(DATA));
			loader.write(cache, out);
			assertEquals(DATA.length, out.size());
		}

		@Test
		void root() throws IOException {
			final Path root = Files.createTempDirectory("PipelineCacheTest");
			final DataSource src = Loader.source(root);
			assertNotNull(src);
			src.input("name");
			assertEquals(true, Files.exists(root.resolve("name")));
		}
	}
}
