package org.sarge.jove.platform.vulkan.pipeline;

import static org.sarge.jove.platform.vulkan.core.VulkanLibrary.check;
import static org.sarge.lib.util.Check.notNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;

import org.sarge.jove.common.NativeObject;
import org.sarge.jove.io.BufferWrapper;
import org.sarge.jove.io.FileDataSource;
import org.sarge.jove.io.ResourceLoader;
import org.sarge.jove.platform.vulkan.VkPipelineCacheCreateInfo;
import org.sarge.jove.platform.vulkan.common.AbstractVulkanObject;
import org.sarge.jove.platform.vulkan.common.DeviceContext;
import org.sarge.jove.platform.vulkan.core.VulkanLibrary;
import org.sarge.jove.platform.vulkan.util.VulkanFunction;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

/**
 * A <i>pipeline cache</i> allows the result of pipeline construction to be reused between pipelines and runs of an application.
 * TODO - persistent implementation?
 * TODO - doc persistence
 * @author Sarge
 */
public class PipelineCache extends AbstractVulkanObject {
	/**
	 * Creates a pipeline cache with the given data blob.
	 * @param dev		Logical data
	 * @param data		Optional data blob
	 * @return New pipeline cache
	 */
	public static PipelineCache create(DeviceContext dev, byte[] data) {
		// Build create descriptor
		final VkPipelineCacheCreateInfo info = new VkPipelineCacheCreateInfo();
		if(data != null) {
			info.initialDataSize = data.length;
			info.pInitialData = BufferWrapper.buffer(data);
		}
		// TODO - info.flags = VK_PIPELINE_CACHE_CREATE_EXTERNALLY_SYNCHRONIZED_BIT_EXT

		// Create cache
		final VulkanLibrary lib = dev.library();
		final PointerByReference ref = dev.factory().pointer();
		check(lib.vkCreatePipelineCache(dev, info, null, ref));

		// Create domain object
		return new PipelineCache(ref.getValue(), dev);
	}

	/**
	 * Constructor.
	 * @param handle		Cache handle
	 * @param dev			Logical device
	 */
	PipelineCache(Pointer handle, DeviceContext dev) {
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
		final VulkanFunction<ByteBuffer> func = (count, data) -> dev.library().vkGetPipelineCacheData(dev, this, count, data);
		final IntByReference count = dev.factory().integer();
		return VulkanFunction.invoke(func, count, BufferWrapper::allocate);
	}

	/**
	 * Merges the given pipeline caches into this cache.
	 * @param caches Caches to merge
	 */
	public void merge(Collection<PipelineCache> caches) {
		final DeviceContext dev = super.device();
		final VulkanLibrary lib = dev.library();
		check(lib.vkMergePipelineCaches(dev, this, caches.size(), NativeObject.toArray(caches)));
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
			this.dev = notNull(dev);
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
			final BufferWrapper buffer = new BufferWrapper(cache.data());
			out.write(buffer.array());
		}
	}

	/**
	 * Pipeline cache API.
	 */
	public interface Library {
		/**
		 * Creates a pipeline cache.
		 * @param device			Logical device
		 * @param pCreateInfo		Pipeline cache descriptor
		 * @param pAllocator		Allocator
		 * @param pPipelineCache	Returned pipeline cache handle
		 * @return Result code
		 */
		int vkCreatePipelineCache(DeviceContext device, VkPipelineCacheCreateInfo pCreateInfo, Pointer pAllocator, PointerByReference pPipelineCache);

		/**
		 * Merges pipeline caches.
		 * @param device			Logical device
		 * @param dstCache			Target cache
		 * @param srcCacheCount		Number of caches to merge
		 * @param pSrcCaches		Array of caches to merge
		 * @return Result code
		 */
		int vkMergePipelineCaches(DeviceContext device, PipelineCache dstCache, int srcCacheCount, Pointer pSrcCaches);

		/**
		 * Retrieves a pipeline cache.
		 * @param device			Logical device
		 * @param cache				Pipeline cache
		 * @param pDataSize			Cache size
		 * @param pData				Cache data
		 * @return Result code
		 */
		int vkGetPipelineCacheData(DeviceContext device, PipelineCache cache, IntByReference pDataSize, ByteBuffer pData);

		/**
		 * Destroys a pipeline cache.
		 * @param device			Logical device
		 * @param cache				Pipeline cache to destroy
		 * @param pAllocator		Allocator
		 */
		void vkDestroyPipelineCache(DeviceContext device, PipelineCache cache, Pointer pAllocator);
	}
}
