package org.sarge.jove.platform.vulkan.pipeline;

import static org.sarge.jove.platform.vulkan.core.VulkanLibrary.check;
import static org.sarge.lib.util.Check.notNull;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;

import org.sarge.jove.io.ResourceLoader;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.*;
import org.sarge.jove.platform.vulkan.core.VulkanLibrary;
import org.sarge.jove.platform.vulkan.util.VulkanBoolean;
import org.sarge.jove.util.*;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * A <i>shader</i> is a Vulkan shader module.
 * @author Sarge
 */
public class Shader extends AbstractVulkanObject {
	/**
	 * Creates a shader module.
	 * @param dev		Logical device
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
	 * @param dev			Logical device
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
	public static class Constant {
		private final Object value;

		/**
		 * Constructor.
		 * @param value
		 */
		public Constant(Object value) {
			this.value = notNull(value);
			size();
		}

		/**
		 * @return Size of this constant (bytes)
		 */
		int size() {
			return switch(value) {
				case Integer n -> Integer.BYTES;
				case Float f -> Float.BYTES;
				case Boolean b -> Integer.BYTES;
				default -> throw new UnsupportedOperationException("Unsupported constant type: " + value.getClass());
			};
		}

		/**
		 * Adds this constant to the data buffer.
		 */
		void append(ByteBuffer buffer) {
			switch(value) {
				case Integer n -> buffer.putInt(n);
				case Float f -> buffer.putFloat(f);
				case Boolean b -> {
					final VulkanBoolean bool = VulkanBoolean.of(b);
					buffer.putInt(bool.toInteger());
				}
				default -> throw new RuntimeException();
			}
		}

		/**
		 * Constructs the descriptor for the given specialisation constants.
		 * @param constants Specialisation constants indexed by ID
		 * @return Specialisation constants descriptor or {@code null} if empty
		 */
		public static VkSpecializationInfo build(Map<Integer, Constant> constants) {
			// Skip if empty
			if(constants.isEmpty()) {
				return null;
			}

			/**
			 * Generates the descriptor for each constant.
			 * Also initialises the buffer offset of each entry and calculates the total length.
			 */
			final var populate = new BiConsumer<Entry<Integer, Constant>, VkSpecializationMapEntry>() {
				private int len = 0;

				@Override
				public void accept(Entry<Integer, Constant> entry, VkSpecializationMapEntry out) {
					// Init constant
					final int size = entry.getValue().size();
					out.size = size;
					out.constantID = entry.getKey();

					// Update buffer offset
					out.offset = len;
					len += size;
				}
			};

			// Populate map entries
			final var info = new VkSpecializationInfo();
			info.mapEntryCount = constants.size();
			info.pMapEntries = StructureHelper.pointer(constants.entrySet(), VkSpecializationMapEntry::new, populate);

			// Populate constant data
			info.dataSize = populate.len;
			info.pData = BufferHelper.allocate(populate.len);
			for(Constant c : constants.values()) {
				c.append(info.pData);
			}

			return info;
		}
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
		 * @param shader			Returned shader module
		 * @return Result
		 */
		int vkCreateShaderModule(DeviceContext device, VkShaderModuleCreateInfo info, Pointer pAllocator, PointerByReference shader);

		/**
		 * Destroys a shader.
		 * @param device			Logical device
		 * @param shader			Shader module
		 * @param pAllocator		Allocator
		 */
		void vkDestroyShaderModule(DeviceContext device, Shader shader, Pointer pAllocator);
	}
}
