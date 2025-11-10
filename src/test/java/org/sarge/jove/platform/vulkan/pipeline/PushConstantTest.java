package org.sarge.jove.platform.vulkan.pipeline;

import static org.junit.jupiter.api.Assertions.*;
import static org.sarge.jove.platform.vulkan.VkShaderStage.*;

import java.lang.foreign.*;
import java.util.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.VkPushConstantRange;
import org.sarge.jove.platform.vulkan.pipeline.PushConstant.Range;
import org.sarge.jove.util.EnumMask;

public class PushConstantTest {
	private SegmentAllocator allocator;

	@BeforeEach
	void before() {
		allocator = Arena.ofAuto();
	}

	@Test
	void alignment() {
		assertThrows(IllegalArgumentException.class, () -> new Range(1, 4, Set.of(VERTEX)));
		assertThrows(IllegalArgumentException.class, () -> new Range(0, 3, Set.of(VERTEX)));
	}

	@Test
	void empty() {
		assertThrows(IllegalArgumentException.class, () -> new Range(0, 4, Set.of()));
	}

	@Test
	void populate() {
		final Range range = new Range(0, 4, Set.of(VERTEX));
		final VkPushConstantRange descriptor = range.populate();
		assertEquals(new EnumMask<>(VERTEX), descriptor.stageFlags);
		assertEquals(0, descriptor.offset);
		assertEquals(4, descriptor.size);
	}

	@Test
	void constant() {
		final Range range = new Range(0, 4, Set.of(VERTEX));
		final var constant = new PushConstant(List.of(range), allocator);
		assertEquals(4, constant.data().byteSize());
	}

	@Test
	void overlapping() {
		final Range one = new Range(0, 4, Set.of(VERTEX));
		final Range two = new Range(0, 8, Set.of(FRAGMENT));
		final var constant = new PushConstant(List.of(one, two), allocator);
		assertEquals(8, constant.data().byteSize());
	}

	@Test
	void coverage() {
		final Range one = new Range(0, 4, Set.of(VERTEX));
		final Range two = new Range(8, 4, Set.of(FRAGMENT));
		assertThrows(IllegalArgumentException.class, () -> new PushConstant(List.of(one, two), allocator));
	}

	@Test
	void stages() {
		final Range one = new Range(0, 4, Set.of(VERTEX));
		final Range two = new Range(4, 4, Set.of(VERTEX));
		assertThrows(IllegalArgumentException.class, () -> new PushConstant(List.of(one, two), allocator));
	}
}
