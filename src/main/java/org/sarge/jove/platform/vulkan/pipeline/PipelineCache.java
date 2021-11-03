package org.sarge.jove.platform.vulkan.pipeline;

import static org.sarge.jove.platform.vulkan.api.VulkanLibrary.check;
import static org.sarge.lib.util.Check.notNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Collection;

import org.sarge.jove.common.Bufferable;
import org.sarge.jove.common.NativeObject;
import org.sarge.jove.platform.vulkan.VkPipelineCacheCreateInfo;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.platform.vulkan.common.AbstractVulkanObject;
import org.sarge.jove.platform.vulkan.common.DeviceContext;
import org.sarge.jove.util.ResourceLoader;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

/**
 * A <i>pipeline cache</i> allows the result of pipeline construction to be reused between pipelines and runs of an application.
 * @author Sarge
 */
public class PipelineCache extends AbstractVulkanObject {
	/**
	 *
	 * @param dev
	 * @param data
	 * @return
	 */
	public static PipelineCache create(DeviceContext dev, byte[] data) {
		// Build create descriptor
		final VkPipelineCacheCreateInfo info = new VkPipelineCacheCreateInfo();
		if(data != null) {
			final ByteBuffer bb = ByteBuffer.allocateDirect(data.length).order(Bufferable.ORDER);
			Bufferable.of(data).buffer(bb);
			info.initialDataSize = data.length;
			info.pInitialData = bb;
		}
		// TODO info.flags = VK_PIPELINE_CACHE_CREATE_EXTERNALLY_SYNCHRONIZED_BIT_EXT

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
	public byte[] data() {
		// TODO - two-step invocation
		// Query size of blob
		final DeviceContext dev = super.device();
		final VulkanLibrary lib = dev.library();
		final IntByReference size = lib.factory().integer();
		check(lib.vkGetPipelineCacheData(dev, this, size, null));

		// Get data
		final byte[] bytes = new byte[size.getValue()];
		check(lib.vkGetPipelineCacheData(dev, this, size, bytes));

		return bytes;
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
	 *
	 */
	public static class Loader implements ResourceLoader<InputStream, PipelineCache> {
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

		// TODO - resource writer abstraction
		public void save(PipelineCache cache, OutputStream out) throws IOException {
			final byte[] data = cache.data();
			out.write(data);
		}
	}
}
