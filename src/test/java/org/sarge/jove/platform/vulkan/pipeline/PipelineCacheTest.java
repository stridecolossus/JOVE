package org.sarge.jove.platform.vulkan.pipeline;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.*;
import java.util.List;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.*;
import org.sarge.jove.io.DataSource;
import org.sarge.jove.platform.vulkan.VkPipelineCacheCreateInfo;
import org.sarge.jove.platform.vulkan.common.*;
import org.sarge.jove.platform.vulkan.core.VulkanLibrary;
import org.sarge.jove.platform.vulkan.pipeline.PipelineCache.Loader;

import com.sun.jna.ptr.IntByReference;

public class PipelineCacheTest {
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
		cache = PipelineCache.create(dev, new byte[1]);
		assertEquals(dev, cache.device());
		assertEquals(false, cache.isDestroyed());

		// Check API
		final var expected = new VkPipelineCacheCreateInfo() {
			@Override
			public boolean equals(Object obj) {
				final var actual = (VkPipelineCacheCreateInfo) obj;
				assertEquals(0, actual.flags);
				assertEquals(1, actual.initialDataSize);
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
			final PipelineCache cache = loader.load(new ByteArrayInputStream(new byte[0]));
			assertNotNull(cache);
		}

		@Test
		void write() throws IOException {
			final var out = new ByteArrayOutputStream();
			final var cache = new PipelineCache(new Handle(2), dev);
			loader.write(cache, out);
			assertEquals(1, out.size());
		}

		@SuppressWarnings("resource")
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
