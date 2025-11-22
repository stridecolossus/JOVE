package org.sarge.jove.platform.vulkan.memory;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import org.sarge.jove.platform.vulkan.VkMemoryRequirements;

/**
 * The <i>memory selector</i> is a utility used to select a memory type matching a given allocation.
 * @author Sarge
 */
public class MemorySelector {
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
	 * Falls back to the <b>first</i> type that matches the minimal {@link MemoryProperties#required()} properties (if any).
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


///**
// * Selects the memory type for the given request.
// * @param requirements			Requirements
// * @param properties			Memory properties
// * @return Selected memory type
// * @throws AllocationException if no memory type matches the request
// */
//private MemoryType select(VkMemoryRequirements requirements, MemoryProperties<?> properties) throws AllocationException {
//	final var matcher = new MemoryTypeMatcher(properties);
//
//	return IntStream
//			.range(0, Integer.SIZE)
//			.filter(n -> (requirements.memoryTypeBits & (1 << n)) == n)
//			.mapToObj(n -> types[n])
//			.filter(matcher)
//			.findAny()
//			.or(matcher::fallback)
//			.orElseThrow(() -> new AllocationException("No available memory type: requirements=%s properties=%s".formatted(requirements, properties)));
//
////	System.out.println(Integer.toBinaryString(requirements.memoryTypeBits));
////
////	for(int n = 0; n < 32; ++n) {
////
////		if((requirements.memoryTypeBits & (1 << n)) == 0) {
////			System.out.println("skip "+n);
////			continue;
////		}
////
////		final MemoryType type = types[n];
////
////		System.out.println("test "+n);
////		if(matcher.test(type)) {
////			System.out.println("matched "+n);
////			return type;
////		}
////	}
////
////	System.out.println("fallback");
////	return matcher.fallback().orElseThrow();
//
////	return EnumMask.stream(requirements.memoryTypeBits)
////			.mapToObj(n -> types[n])
////			.filter(matcher)
////			.findAny()
////			.or(matcher::fallback)
////			.orElseThrow(() -> new AllocationException("No available memory type: requirements=%s properties=%s".formatted(requirements, properties)));
//}
//
///**
// * Matches a memory type for the given properties and records the fallback as a side-effect.
// */
//private static class MemoryTypeMatcher implements Predicate<MemoryType> {
//	private final MemoryProperties<?> properties;
//	private MemoryType fallback;
//
//	/**
//	 * Constructor.
//	 * @param properties Memory properties to be matched
//	 */
//	MemoryTypeMatcher(MemoryProperties<?> properties) {
//		this.properties = properties;
//	}
//
//	@Override
//	public boolean test(MemoryType type) {
//		// Test whether matches minimal requirements
//		if(!matches(type, properties.required())) {
//			return false;
//		}
//
//		// Test whether matches optimal requirements
//		if(matches(type, properties.optimal())) {
//			return true;
//		}
//
//		// Record fallback candidate
//		if(fallback == null) {
//			fallback = type;
//		}
//
//		return false;
//	}
//
//	/**
//	 * @return Whether a memory type matches the given memory properties
//	 */
//	private static boolean matches(MemoryType type, Set<VkMemoryProperty> properties) {
//		return type.properties().containsAll(properties);
//	}
//
//	/**
//	 * @return Fallback memory type
//	 */
//	private Optional<MemoryType> fallback() {
//		return Optional.ofNullable(fallback);
//	}
//}
