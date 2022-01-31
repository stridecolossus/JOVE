package org.sarge.jove.platform.vulkan.pipeline;

import static java.util.stream.Collectors.toList;
import static org.sarge.jove.platform.vulkan.core.VulkanLibrary.check;
import static org.sarge.lib.util.Check.notNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.sarge.jove.io.ResourceLoader;
import org.sarge.jove.platform.vulkan.VkShaderModuleCreateInfo;
import org.sarge.jove.platform.vulkan.VkSpecializationInfo;
import org.sarge.jove.platform.vulkan.VkSpecializationMapEntry;
import org.sarge.jove.platform.vulkan.common.AbstractVulkanObject;
import org.sarge.jove.platform.vulkan.common.DeviceContext;
import org.sarge.jove.platform.vulkan.core.VulkanLibrary;
import org.sarge.jove.platform.vulkan.util.VulkanBoolean;
import org.sarge.jove.util.BufferHelper;
import org.sarge.jove.util.StructureHelper;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * A <i>shader</i> is a Vulkan shader module.
 * @author Sarge
 */
public class Shader extends AbstractVulkanObject {
	/**
	 * Creates a shader module.
	 * @param dev		Parent device
	 * @param code		Shader SPIV code
	 * @return New shader
	 */
	public static Shader create(DeviceContext dev, byte[] code) {
		// Create descriptor
		final var info = new VkShaderModuleCreateInfo();
		info.codeSize = code.length;
		info.pCode = BufferHelper.buffer(code);

		// Allocate shader
		final VulkanLibrary lib = dev.library();
		final PointerByReference shader = dev.factory().pointer();
		check(lib.vkCreateShaderModule(dev, info, null, shader));

		// Create shader
		return new Shader(shader.getValue(), dev);
	}

	/**
	 * Constructor.
	 * @param handle 		Shader module handle
	 * @param dev			Device
	 */
	private Shader(Pointer handle, DeviceContext dev) {
		super(handle, dev);
	}

	@Override
	protected Destructor<Shader> destructor(VulkanLibrary lib) {
		return lib::vkDestroyShaderModule;
	}

	/**
	 * Loader for a shader.
	 */
	public static class Loader implements ResourceLoader<InputStream, Shader> {
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
		public Shader load(InputStream in) throws IOException {
			final byte[] code = in.readAllBytes();
			return create(dev, code);
		}
	}

	/**
	 * Transient specialisation constant record.
	 */
	private static class Constant {
		private final int id;
		private final Object value;
		private final int size;
		private int offset;

		Constant(Entry<Integer, Object> entry) {
			this.id = entry.getKey();
			this.value = entry.getValue();
			this.size = size();
		}

		/**
		 * Populates the descriptor for this constant.
		 */
		void populate(VkSpecializationMapEntry entry) {
			entry.constantID = id;
			entry.offset = offset;
			entry.size = size;
		}

		/**
		 * @return Size of this constant (bytes)
		 */
		private int size() {
			if(value instanceof Integer) {
				return Integer.BYTES;
			}
			else
			if(value instanceof Float) {
				return Float.BYTES;
			}
			else
			if(value instanceof Boolean) {
				return Integer.BYTES;
			}
			else {
				throw new UnsupportedOperationException("Unsupported constant type: " + value.getClass());
			}
		}

		/**
		 * Adds this constant to the data buffer.
		 */
		void append(ByteBuffer buffer) {
			if(value instanceof Integer n) {
				buffer.putInt(n);
			}
			else
			if(value instanceof Float f) {
				buffer.putFloat(f);
			}
			else
			if(value instanceof Boolean b) {
				final VulkanBoolean bool = VulkanBoolean.of(b);
				buffer.putInt(bool.toInteger());
			}
			else {
				assert false;
			}
		}
	}

	/**
	 * Constructs the descriptor for the given specialisation constants.
	 * @param constants Specialisation constants indexed by ID
	 * @return Specialisation constants descriptor or {@code null} if empty
	 */
	public static VkSpecializationInfo constants(Map<Integer, Object> constants) {
		// Skip if empty
		if(constants.isEmpty()) {
			return null;
		}

		// Convert to table of constants
		final List<Constant> table = constants
				.entrySet()
				.stream()
				.map(Constant::new)
				.collect(toList());

		// Patch offsets and calculate total size
		int size = 0;
		for(Constant e : table) {
			e.offset = size;
			size += e.size;
		}

		// Populate map entries
		final var info = new VkSpecializationInfo();
		info.mapEntryCount = table.size();
		info.pMapEntries = StructureHelper.pointer(table, VkSpecializationMapEntry::new, Constant::populate);

		// Populate constant data
		info.dataSize = size;
		info.pData = BufferHelper.allocate(size);
		for(Constant c : table) {
			c.append(info.pData);
		}

		return info;
	}

	/**
	 * Shader module API.
	 */
	interface Library {
		/**
		 * Create a shader.
		 * @param device			Logical device
		 * @param info				Shader descriptor
		 * @param pAllocator		Allocator
		 * @param shader			Returned shader handle
		 * @return Result code
		 */
		int vkCreateShaderModule(DeviceContext device, VkShaderModuleCreateInfo info, Pointer pAllocator, PointerByReference shader);

		/**
		 * Destroys a shader.
		 * @param device			Logical device
		 * @param shader			Shader
		 * @param pAllocator		Allocator
		 */
		void vkDestroyShaderModule(DeviceContext device, Shader shader, Pointer pAllocator);
	}
}
