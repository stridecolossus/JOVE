package org.sarge.jove.platform.vulkan.memory;

/**
 * An <i>allocator</i> is responsible for allocating device memory.
 * @author Sarge
 */
@FunctionalInterface
public interface Allocator {
	/**
	 * An <i>allocation exception</i> is thrown when this allocator cannot allocate memory.
	 */
	class AllocationException extends RuntimeException {
		/**
		 * Constructor.
		 * @param message Message
		 */
		public AllocationException(String message) {
			super(message);
		}

		protected AllocationException(Throwable cause) {
			super(cause);
		}
	}

	/**
	 * Allocates device memory.
	 * @param type		Type of memory to allocate
	 * @param size		Size of the requested memory (bytes)
	 * @return New device memory
	 * @throws AllocationException if the memory cannot be allocated
	 */
	DeviceMemory allocate(MemoryType type, long size) throws AllocationException;
}
