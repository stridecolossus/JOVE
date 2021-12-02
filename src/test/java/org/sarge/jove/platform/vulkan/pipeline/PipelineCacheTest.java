package org.sarge.jove.platform.vulkan.pipeline;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.sarge.jove.common.BufferWrapper;
import org.sarge.jove.common.Handle;
import org.sarge.jove.common.NativeObject;
import org.sarge.jove.io.DataSource;
import org.sarge.jove.platform.vulkan.VkPipelineCacheCreateInfo;
import org.sarge.jove.platform.vulkan.pipeline.PipelineCache.Loader;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

import com.sun.jna.Pointer;

public class PipelineCacheTest extends AbstractVulkanTest {
	private static final byte[] DATA = new byte[42];

	private PipelineCache cache;

	@BeforeEach
	void before() {
		cache = new PipelineCache(new Pointer(1), dev);
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

		// Check cache
		assertNotNull(cache);
		assertEquals(new Handle(POINTER.getValue()), cache.handle());
		assertEquals(dev, cache.device());
		assertEquals(false, cache.isDestroyed());

		// Check API
		final ArgumentCaptor<VkPipelineCacheCreateInfo> captor = ArgumentCaptor.forClass(VkPipelineCacheCreateInfo.class);
		verify(lib).vkCreatePipelineCache(eq(dev), captor.capture(), isNull(), eq(POINTER));

		// Check create descriptor
		final VkPipelineCacheCreateInfo info = captor.getValue();
		assertNotNull(info);
		assertEquals(0, info.flags);
		assertEquals(DATA.length, info.initialDataSize);
		assertNotNull(info.pInitialData);
	}

	@Test
	void createEmpty() {
		PipelineCache.create(dev, null);
	}

	@Test
	void data() {
		final ByteBuffer data = cache.data();
		assertNotNull(data);
		assertEquals(1, data.capacity());
		verify(lib).vkGetPipelineCacheData(dev, cache, INTEGER, null);
		verify(lib).vkGetPipelineCacheData(dev, cache, INTEGER, data);
	}

	@Test
	void merge() {
		final PipelineCache other = new PipelineCache(new Pointer(2), dev);
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
			when(cache.data()).thenReturn(BufferWrapper.buffer(DATA));
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
