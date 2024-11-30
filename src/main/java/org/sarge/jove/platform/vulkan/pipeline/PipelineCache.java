package org.sarge.jove.platform.vulkan.pipeline;

import static java.util.Objects.requireNonNull;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.*;
import java.util.Collection;

import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.*;
import org.sarge.jove.io.*;
import org.sarge.jove.platform.vulkan.VkPipelineCacheCreateInfo;
import org.sarge.jove.platform.vulkan.common.*;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.util.VulkanFunction;

/**
 * A <i>pipeline cache</i> allows the result of pipeline construction to be reused between pipelines and runs of an application.
 * TODO - persistent implementation?
 * TODO - doc persistence
 * @author Sarge
 */
public final class PipelineCache extends VulkanObject {
	/**
	 * Creates a pipeline cache with the given data blob.
	 * @param dev		Logical data
	 * @param data		Optional data blob
	 * @return New pipeline cache
	 */
	public static PipelineCache create(DeviceContext dev, byte[] data) {
		// Build create descriptor
		final var info = new VkPipelineCacheCreateInfo();
		if(data != null) {
			info.initialDataSize = data.length;
			info.pInitialData = requireNonNull(data);
		}
		// TODO - info.flags = VK_PIPELINE_CACHE_CREATE_EXTERNALLY_SYNCHRONIZED_BIT_EXT

		// Create cache
		final Vulkan vulkan = dev.vulkan();
		final PointerReference ref = vulkan.factory().pointer();
		vulkan.library().vkCreatePipelineCache(dev, info, null, ref);

		// Create domain object
		return new PipelineCache(ref.handle(), dev);
	}

	/**
	 * Constructor.
	 * @param handle		Cache handle
	 * @param dev			Logical device
	 */
	PipelineCache(Handle handle, DeviceContext dev) {
		super(handle, dev);
	}

	@Override
	protected Destructor<PipelineCache> destructor(VulkanLibrary lib) {
		return lib::vkDestroyPipelineCache;
	}

	/**
	 * Retrieves the data blob for this pipeline cache.
	 * @return Cache data
	 */
	public ByteBuffer data() {
		final DeviceContext dev = super.device();
		final Vulkan vulkan = dev.vulkan();
		final VulkanFunction<ByteBuffer> cache = (count, data) -> vulkan.library().vkGetPipelineCacheData(dev, this, count, null); // TODO data);
		return vulkan.invoke(cache, BufferHelper::allocate);
	}

	/**
	 * Merges the given pipeline caches into this cache.
	 * @param caches Caches to merge
	 */
	public void merge(Collection<PipelineCache> caches) {
		final var array = caches.toArray(PipelineCache[]::new);
		final DeviceContext dev = super.device();
		dev.vulkan().library().vkMergePipelineCaches(dev, this, array.length, array);
	}

	/**
	 * Loader for a pipeline cache.
	 */
	public static class Loader implements ResourceLoader<InputStream, PipelineCache> {
		/**
		 * Helper - Creates a data-source for a persistent pipeline cache.
		 * @param root Data-source root directory
		 * @return New data-source
		 */
		public static FileDataSource source(Path root) {
			return new FileDataSource(root) {
				@Override
				public InputStream input(String name) throws IOException {
					final Path file = root.resolve(name);
					if(!Files.exists(file)) {
						try {
							Files.createFile(file);
						}
						catch(IOException e) {
							throw new RuntimeException("Cannot create pipeline cache file: " + file, e);
						}
					}
					return Files.newInputStream(file);
				}
			};
		}

		private final DeviceContext dev;

		/**
		 * Constructor.
		 * @param dev Logical device
		 */
		public Loader(DeviceContext dev) {
			this.dev = requireNonNull(dev);
		}

		@Override
		public InputStream map(InputStream in) throws IOException {
			return in;
		}

		@Override
		public PipelineCache load(InputStream in) throws IOException {
			final byte[] data = in.readAllBytes();
			return create(dev, data);
		}

		// TODO
		public void write(PipelineCache cache, OutputStream out) throws IOException {
			final byte[] array = BufferHelper.array(cache.data());
			out.write(array);
		}
	}

	/**
	 * Pipeline cache API.
	 */
	interface Library {
		/**
		 * Creates a pipeline cache.
		 * @param device			Logical device
		 * @param pCreateInfo		Pipeline cache descriptor
		 * @param pAllocator		Allocator
		 * @param pPipelineCache	Returned pipeline cache
		 * @return Result
		 */
		int vkCreatePipelineCache(DeviceContext device, VkPipelineCacheCreateInfo pCreateInfo, Handle pAllocator, PointerReference pPipelineCache);

		/**
		 * Merges pipeline caches.
		 * @param device			Logical device
		 * @param dstCache			Target cache
		 * @param srcCacheCount		Number of caches to merge
		 * @param pSrcCaches		Array of caches to merge
		 * @return Result
		 */
		int vkMergePipelineCaches(DeviceContext device, PipelineCache dstCache, int srcCacheCount, PipelineCache[] pSrcCaches);

		/**
		 * Retrieves a pipeline cache.
		 * @param device			Logical device
		 * @param cache				Pipeline cache
		 * @param pDataSize			Cache size
		 * @param pData				Cache data
		 * @return Result
		 */
		int vkGetPipelineCacheData(DeviceContext device, PipelineCache cache, IntegerReference pDataSize, Handle pData); // ByteBuffer pData); // TODO - data blob?

		/**
		 * Destroys a pipeline cache.
		 * @param device			Logical device
		 * @param cache				Pipeline cache to destroy
		 * @param pAllocator		Allocator
		 */
		void vkDestroyPipelineCache(DeviceContext device, PipelineCache cache, Handle pAllocator);
	}
}
