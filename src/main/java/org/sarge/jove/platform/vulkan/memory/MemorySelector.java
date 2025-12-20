package org.sarge.jove.platform.vulkan.memory;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import org.sarge.jove.platform.vulkan.VkMemoryRequirements;

/**
 * The <i>memory selector</i> is a utility used to select a memory type matching a given allocation.
 * @author Sarge
 */
class MemorySelector {
	private final MemoryType[] types;

	/**
	 * Constructor.
	 * @param types Memory types
	 */
	public MemorySelector(MemoryType[] types) {
		this.types = types.clone();
	}

	/**
	 * Selects the memory type matching the given allocation.
	 * Falls back to the <b>first</b> type that matches the minimal {@link MemoryProperties#required()} properties (if any).
	 * @param mask				Memory types filter mask
	 * @param properties		Memory properties to be matched
	 * @return Selected memory type
	 * @see VkMemoryRequirements#memoryTypeBits
	 */
	public Optional<MemoryType> select(int mask, MemoryProperties<?> properties) {
		final var matcher = new FallbackMatcher(properties);

		return IntStream
				.range(0, Integer.SIZE - Integer.numberOfLeadingZeros(mask))
        		.filter(n -> (mask & (1 << n)) != 0)
        		.mapToObj(n -> types[n])
        		.filter(matcher)
        		.findAny()
        		.or(matcher::fallback);
	}

	/**
	 * The <i>fallback matcher</i> matches memory types against the specified properties.
	 * Records the <b>first</b> memory type that matched the minimal requirements as a fallback.
	 */
	private static class FallbackMatcher implements Predicate<MemoryType> {
		private final MemoryProperties<?> properties;
		private MemoryType fallback;

		/**
		 * Constructor.
		 * @param properties Memory properties to match
		 */
		public FallbackMatcher(MemoryProperties<?> properties) {
			this.properties = properties;
		}

		@Override
		public boolean test(MemoryType type) {
			// Match against minimal requirements
			if(!type.properties().containsAll(properties.required())) {
				return false;
			}

			// Match against optimal properties
			if(type.properties().containsAll(properties.optimal())) {
				return true;
			}

			// Record fallback
			if(fallback == null) {
				fallback = type;
			}

			// Not matched
			return false;
		}

		/**
		 * @return Fallback type
		 */
		public Optional<MemoryType> fallback() {
			return Optional.ofNullable(fallback);
		}
	}
}
