package org.sarge.jove.platform.vulkan.memory;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * A <i>memory allocator</i> defines a strategy for allocation of device memory.
 * @see DeviceMemory
 * @author Sarge
 */
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
	 * @param type Type of memory to allocate
	 * @param size Size of the memory (bytes)
	 * @return New device memory
	 * @throws AllocationException if the memory cannot be allocated
	 */
	DeviceMemory allocate(MemoryType type, long size) throws AllocationException;

	/**
	 * Creates an adapter that allocates memory in multiples of the given page size.
	 * @param allocator		Delegate allocator
	 * @param page			Page size (bytes)
	 * @return Paged allocator
	 */
	static Allocator paged(Allocator allocator, long page) {
		return new Allocator() {
			@Override
			public DeviceMemory allocate(MemoryType type, long size) throws AllocationException {
				final long num = 1 + ((size - 1) / page);
				return allocator.allocate(type, num * page);
			}

			@Override
			public String toString() {
				return new ToStringBuilder(this).append(allocator).append("page", page).build();
			}
		};
	}
}
