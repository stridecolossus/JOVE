package org.sarge.jove.platform.vulkan.pipeline;

import static org.sarge.jove.platform.vulkan.api.VulkanLibrary.check;
import static org.sarge.lib.util.Check.notNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;

import org.sarge.jove.common.NativeObject;
import org.sarge.jove.platform.vulkan.VkPipelineCacheCreateInfo;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.platform.vulkan.common.AbstractVulkanObject;
import org.sarge.jove.platform.vulkan.common.DeviceContext;
import org.sarge.jove.platform.vulkan.util.VulkanFunction;
import org.sarge.jove.util.BufferHelper;
import org.sarge.jove.util.DataSource;
import org.sarge.jove.util.ResourceLoaderWriter;

import com.sun.jna.Pointer;
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
			info.pInitialData = BufferHelper.buffer(data);
		}
		// TODO - info.flags = VK_PIPELINE_CACHE_CREATE_EXTERNALLY_SYNCHRONIZED_BIT_EXT

		// Create cache
		final VulkanLibrary lib = dev.library();
		final PointerByReference ref = lib.factory().pointer();
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
		final VulkanFunction<ByteBuffer> func = (api, count, data) -> api.vkGetPipelineCacheData(dev, this, count, data);
		return VulkanFunction.invoke(func, dev.library(), BufferHelper::allocate);
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
	public static class Loader implements ResourceLoaderWriter<InputStream, OutputStream, PipelineCache> {
		/**
		 * Helper - Creates a data-source for a persistent pipeline cache.
		 * @param root Data-source root directory
		 * @return New data-source
		 */
		public static DataSource source(Path root) {
			return new DataSource(root) {
				@Override
				protected Path resolve(String name) {
					final Path file = super.resolve(name);
					if(!Files.exists(file)) {
						try {
							Files.createFile(file);
						}
						catch(IOException e) {
							throw new RuntimeException("Cannot create pipeline cache file: " + file, e);
						}
					}
					return file;
				}

				@Override
				public DataSource resolve(Path path) {
					throw new UnsupportedOperationException();
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

		@Override
		public OutputStream map(OutputStream out) throws IOException {
			return out;
		}

		@Override
		public void write(PipelineCache cache, OutputStream out) throws IOException {
			final ByteBuffer bb = cache.data();
			final byte[] data = BufferHelper.toArray(bb);
			out.write(data);
		}
	}
}
