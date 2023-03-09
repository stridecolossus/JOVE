package org.sarge.jove.platform.vulkan.memory;

import static org.sarge.lib.util.Check.*;

import java.util.*;
import java.util.function.Predicate;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.DeviceContext;
import org.sarge.jove.platform.vulkan.core.VulkanLibrary;
import org.sarge.jove.util.BitField;

import com.sun.jna.ptr.PointerByReference;

/**
 * An <i>allocator</i> is responsible for allocating device memory for a given request.
 * @author Sarge
 */
public class Allocator {
	/**
	 * An <i>allocation exception</i> is thrown when this allocator cannot allocate memory.
	 */
	public static class AllocationException extends RuntimeException {
		/**
		 * Constructor.
		 * @param message Message
		 */
		public AllocationException(String message) {
			super(message);
		}

		/**
		 * Constructor.
		 * @param cause Cause
		 */
		protected AllocationException(Throwable cause) {
			super(cause);
		}
	}

	private final DeviceContext dev;
	private final MemoryType[] types;

	/**
	 * Constructor.
	 * @param dev		Logical device
	 * @param types 	Memory types
	 * @see MemoryType#enumerate(VkPhysicalDeviceMemoryProperties)
	 */
	public Allocator(DeviceContext dev, MemoryType[] types) {
		this.dev = notNull(dev);
		this.types = Arrays.copyOf(types, types.length);
	}

	/**
	 * Copy constructor for specialisations.
	 * @param allocator Delegate allocator to copy
	 */
	protected Allocator(Allocator allocator) {
		this(allocator.dev, allocator.types);
	}

	/**
	 * Allocates device memory for the given request.
	 * <p>
	 * The {@link <a href="https://registry.khronos.org/vulkan/specs/1.3-extensions/man/html/VkPhysicalDeviceMemoryProperties.html">Vulkan documentation</a>}
	 * describes the suggested approach to select the appropriate memory type for a request as follows:
	 * <ol>
	 * <li>Filter the candidate memory types by to the {@link VkMemoryRequirements#memoryTypeBits} mask</li>
	 * <li>Find the <i>optimal</i> type matching the given memory properties</li>
	 * <li>Otherwise fallback to an available type matching the minimal <i>required</i> properties</li>
	 * </ol>
	 * <p>
	 * @param reqs			Memory requirements
	 * @param props			Memory properties
	 * @return Allocated memory
	 * @throws IllegalAccessException if {@link #size} is not positive
	 * @throws AllocationException if there is no matching memory type for the request or the memory cannot be allocated by the hardware
	 */
	public DeviceMemory allocate(VkMemoryRequirements reqs, MemoryProperties<?> props) throws AllocationException {
		final MemoryType type = select(reqs, props);
		return allocate(type, reqs.size);
	}

	/**
	 * Selects the memory type for the given request.
	 * @param reqs			Requirements
	 * @param props			Memory properties
	 * @return Selected memory type index
	 * @throws AllocationException if no memory type matches the request
	 */
	private MemoryType select(VkMemoryRequirements reqs, MemoryProperties<?> props) throws AllocationException {
		/**
		 * Matches a memory type for the required properties and also records the fallback as a side-effect.
		 */
		class FallbackMatcher implements Predicate<MemoryType> {
			private MemoryType fallback;

			@Override
			public boolean test(MemoryType type) {
				// Skip if does not satisfy the minimal requirements
				if(!type.matches(props.required())) {
					return false;
				}

				// Check for optimal match
    			if(type.matches(props.optimal())) {
    				return true;
    			}

    			// Record fallback candidate
				if(fallback == null) {
					fallback = type;
				}

				return false;
			}

			private Optional<MemoryType> fallback() {
				return Optional.ofNullable(fallback);
			}
		}

		// Walk candidate memory types and match against the requested properties
		final var matcher = new FallbackMatcher();
		return new BitField(reqs.memoryTypeBits)
				.stream()
				.mapToObj(n -> types[n - 1])
				.filter(matcher)
				.findAny()
				.or(matcher::fallback)
				.orElseThrow(() -> new AllocationException("No available memory type: requirements=%s properties=%s".formatted(reqs, props)));
	}

	/**
	 * Allocates memory of the given type.
	 * @param type		Memory type
	 * @param size		Size (bytes)
	 * @return Allocated memory
	 * @throws IllegalAccessException if {@link #size} is not positive
	 * @throws AllocationException if the memory cannot be allocated
	 */
	protected DeviceMemory allocate(MemoryType type, long size) throws AllocationException {
		// Init memory descriptor
		final var info = new VkMemoryAllocateInfo();
		info.allocationSize = oneOrMore(size);
		info.memoryTypeIndex = type.index();

		// Allocate memory
		final VulkanLibrary lib = dev.library();
		final PointerByReference ref = dev.factory().pointer();
		final int result = lib.vkAllocateMemory(dev, info, null, ref);

		// Check allocated
		if(result != VulkanLibrary.SUCCESS) {
			throw new AllocationException("Cannot allocate memory: type=%s size=%d error=%d".formatted(type, size, result));
		}

		// Create device memory
		return new DefaultDeviceMemory(new Handle(ref), dev, type, size);
	}
}
