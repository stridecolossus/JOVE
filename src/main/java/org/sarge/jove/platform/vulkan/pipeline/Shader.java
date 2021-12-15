package org.sarge.jove.platform.vulkan.pipeline;

import static org.sarge.jove.platform.vulkan.core.VulkanLibrary.check;
import static org.sarge.lib.util.Check.notNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.sarge.jove.common.BufferWrapper;
import org.sarge.jove.io.ResourceLoader;
import org.sarge.jove.platform.vulkan.VkShaderModuleCreateInfo;
import org.sarge.jove.platform.vulkan.VkSpecializationInfo;
import org.sarge.jove.platform.vulkan.VkSpecializationMapEntry;
import org.sarge.jove.platform.vulkan.common.AbstractVulkanObject;
import org.sarge.jove.platform.vulkan.common.DeviceContext;
import org.sarge.jove.platform.vulkan.core.VulkanLibrary;
import org.sarge.jove.util.StructureHelper;
import org.sarge.lib.util.Check;

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
		info.pCode = BufferWrapper.buffer(code);

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
	 * Builder for shader specialisation constants.
	 */
	public static class ConstantTableBuilder {
		/**
		 * Helper - Creates specialisation constants for the given map.
		 * @param map Constants map indexed by ID
		 * @return Specialisation constants descriptor
		 * @throws IllegalArgumentException for an invalid constant type
		 * @see #add(Map)
		 */
		public static VkSpecializationInfo of(Map<Integer, Object> map) {
			return new ConstantTableBuilder().add(map).build();
		}

		/**
		 * Constant table entry.
		 */
		private record Entry(int id, Object value, int offset) {
			/**
			 * Constructor.
			 * @param id			Constant ID
			 * @param value			Value
			 * @param offset		Offset
			 */
			public Entry {
				Check.zeroOrMore(id);
				Check.notNull(value);
				assert offset >= 0;
			}

			/**
			 * Determines the size of the value.
			 * @param value Value
			 * @return Constant size (bytes)
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
					throw new IllegalArgumentException("Invalid constant type: " + value.getClass().getName());
				}
			}

			/**
			 * Populates a map entry.
			 */
			private void populate(VkSpecializationMapEntry entry) {
				entry.constantID = id;
				entry.offset = offset;
				entry.size = size();
			}

			/**
			 * Populates the data buffer.
			 */
			private void append(ByteBuffer bb) {
				if(value instanceof Integer ) {
					bb.putInt((int) value);
				}
				else
				if(value instanceof Float) {
					bb.putFloat((float) value);
				}
				else
				if(value instanceof Boolean) {
					bb.putInt((boolean) value == true ? 1 : 0);
				}
				else {
					assert false;
				}
			}
		}

		private final Map<Integer, Entry> map = new HashMap<>();
		private int offset;

		/**
		 * Adds a specialisation constant.
		 * @param id		Constant ID
		 * @param value		Value
		 * @throws IllegalArgumentException for a duplicate constant ID
		 * @throws IllegalArgumentException for an invalid constant type
		 */
		public ConstantTableBuilder add(int id, Object value) {
			// Validate
			Check.zeroOrMore(id);
			Check.notNull(value);
			if(map.containsKey(id)) throw new IllegalArgumentException("Duplicate constant: id=" + id);

			// Create transient entry
			final Entry entry = new Entry(id, value, offset);
			map.put(id, entry);

			// Increment offset (also validates constant type)
			final int size = entry.size();
			offset += size;

			return this;
		}

		/**
		 * Adds a map of constants.
		 * @param map Constants map indexed by ID
		 * @see #add(int, Object)
		 */
		public ConstantTableBuilder add(Map<Integer, Object> map) {
			map.entrySet().forEach(e -> add(e.getKey(), e.getValue()));
			return this;
		}

		/**
		 * Constructs this table of constants.
		 * @return Specialisation constants descriptor or {@code null} if empty
		 */
		public VkSpecializationInfo build() {
			// Ignore if no constants
			if(map.isEmpty()) {
				return null;
			}

			// Populate map entries
			final var info = new VkSpecializationInfo();
			final Collection<Entry> values = map.values();
			info.mapEntryCount = map.size();
			info.pMapEntries = StructureHelper.first(values, VkSpecializationMapEntry::new, Entry::populate);

			// Populate constant data
			final int size = values.stream().mapToInt(Entry::size).sum();
			info.dataSize = size;
			info.pData = BufferWrapper.allocate(size);
			values.forEach(e -> e.append(info.pData));

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
