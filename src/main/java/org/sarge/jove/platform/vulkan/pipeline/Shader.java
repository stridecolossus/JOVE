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
	 * Constructs the descriptor for the given specialisation constants.
	 * <p>
	 * The following types are supported:
	 * <ul>
	 * <li>Integer</li>
	 * <li>Float</li>
	 * <li>Boolean</li>
	 * </ul>
	 * Note that numeric constants must be a wrapper type.
	 * <p>
	 * @param constants Specialisation constants indexed by identifier
	 * @return Specialisation constants descriptor or {@code null} if empty
	 * @throws UnsupportedOperationException for an unsupported constant type
	 */
	public static VkSpecializationInfo constants(Map<Integer, Object> constants) {
		// Skip if empty
		if(constants.isEmpty()) {
			return null;
		}

		/**
		 * Generates the descriptor for each constant and calculates the total length as a side-effect.
		 */
		final var populate = new BiConsumer<Entry<Integer, Object>, VkSpecializationMapEntry>() {
			private int len = 0;

			@Override
			public void accept(Entry<Integer, Object> entry, VkSpecializationMapEntry out) {
				// Determine size of this constant
				final Object value = entry.getValue();
				final int size = switch(value) {
					case Float f -> Float.BYTES;
					case Integer n -> Integer.BYTES;
					case Boolean b -> Integer.BYTES;
					default -> throw new UnsupportedOperationException("Unsupported constant type: " + value.getClass());
				};

				// Init constant
				out.constantID = entry.getKey();
				out.size = size;
				out.offset = len;

				// Update buffer offset
				len += size;
			}
		};

		// Populate map entries
		final var info = new VkSpecializationInfo();
		info.mapEntryCount = constants.size();
		info.pMapEntries = StructureCollector.pointer(constants.entrySet(), new VkSpecializationMapEntry(), populate);

		// Build constants data buffer
		final ByteBuffer buffer = BufferHelper.allocate(populate.len);
		for(var entry : constants.entrySet()) {		// TODO - check same order as above
			switch(entry.getValue()) {
				case Float f -> buffer.putFloat(f);
				case Integer n -> buffer.putInt(n);
				case Boolean b -> {
					final int bool = VulkanBoolean.of(b).toInteger();
					buffer.putInt(bool);
				}
				default -> throw new RuntimeException();
			}
		}
		assert !buffer.hasRemaining();

		// Populate data buffer
		info.dataSize = populate.len;
		info.pData = buffer;

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
