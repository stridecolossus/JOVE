package org.sarge.jove.foreign;

import static java.lang.foreign.ValueLayout.ADDRESS;

import java.lang.foreign.*;

import org.junit.jupiter.api.BeforeEach;
import org.sarge.jove.platform.vulkan.VkQueueFamilyProperties;

class AddressTest {
	private Arena arena;

	@BeforeEach
	void before() {
		arena = Arena.ofAuto();
	}

//	@Test
	void structure() {
		final var structure = new VkQueueFamilyProperties();
		final var layout = structure.layout();

		final MemorySegment address = arena.allocate(layout, 3);
		System.out.println("address="+address);

		System.out.println("elements...");
		address.elements(layout).forEach(System.out::println);

		System.out.println("slices...");
		final long size = layout.byteSize();
		for(int n = 0; n < 3; ++n) {
			final MemorySegment slice = address.asSlice(n * size, size);
			System.out.println(slice);
		}

//		System.out.println("indexing...");
//		for(int n = 0; n < 3; ++n) {
//			final MemorySegment e = address.getAtIndex(ADDRESS, n);
//			System.out.println(e.reinterpret(ADDRESS.byteSize()));
//		}
	}

//	@Test
	void reference() {
		final MemorySegment address = arena.allocate(ADDRESS, 3);
		System.out.println("address="+address);

		address.setAtIndex(ADDRESS, 1, MemorySegment.ofAddress(42));

		System.out.println("elements...");
		address.elements(ADDRESS).forEach(System.out::println);

		System.out.println("slices...");
		final long size = ADDRESS.byteSize();
		for(int n = 0; n < 3; ++n) {
			final MemorySegment slice = address.asSlice(n * size, size);
			System.out.println(slice);
		}

		System.out.println("indexing...");
		for(int n = 0; n < 3; ++n) {
			final MemorySegment e = address.getAtIndex(ADDRESS, n);
			System.out.println(e.reinterpret(ADDRESS.byteSize()).get(ADDRESS, 0));
		}
	}
}
