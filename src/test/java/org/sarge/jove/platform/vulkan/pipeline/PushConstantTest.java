package org.sarge.jove.platform.vulkan.pipeline;

import static org.junit.jupiter.api.Assertions.*;
import static org.sarge.jove.platform.vulkan.VkShaderStageFlags.*;

import java.lang.foreign.*;
import java.util.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.core.Command.Buffer;
import org.sarge.jove.platform.vulkan.pipeline.PushConstant.Range;
import org.sarge.jove.util.*;

public class PushConstantTest {
	private PushConstant constant;
	private Range one, two;
	private SegmentAllocator allocator;

	@BeforeEach
	void before() {
		allocator = Arena.ofAuto();
		one = new Range(0, 4, Set.of(VERTEX));
		two = new Range(4, 4, Set.of(FRAGMENT));
		constant = new PushConstant(List.of(one, two), allocator);
	}

	@DisplayName("A push constant range...")
	@Nested
	class RangeTest {
		@DisplayName("must be aligned to a multiple of 4 bytes")
    	@Test
    	void alignment() {
    		assertThrows(IllegalArgumentException.class, () -> new Range(1, 4, Set.of(VERTEX)));
    		assertThrows(IllegalArgumentException.class, () -> new Range(0, 3, Set.of(VERTEX)));
    	}

		@DisplayName("must apply to at least one pipeline stage")
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
	}

	@DisplayName("The backing buffer for a push constant...")
	@Nested
	class DataTest {
		@DisplayName("can be retrieved")
		@Test
		void all() {
			assertEquals(8, constant.data().byteSize());
		}

		@DisplayName("can be sliced for a range of the constant")
    	@Test
    	void data() {
    		assertEquals(4, constant.data(one).byteSize());
    		assertEquals(4, constant.data(two).byteSize());
    	}

		@DisplayName("cannot be sliced for an unknown range")
    	@Test
    	void invalid() {
    		final Range other = new Range(0, 4, Set.of(GEOMETRY));
    		assertThrows(IllegalArgumentException.class, () -> constant.data(other));
    	}
	}

	@DisplayName("The ranges of a push constant...")
	@Nested
	class RangeCoverageTest {
    	@DisplayName("can overlap")
    	@Test
    	void overlapping() {
    		final Range one = new Range(0, 4, Set.of(VERTEX));
    		final Range two = new Range(0, 8, Set.of(FRAGMENT));
    		final var constant = new PushConstant(List.of(one, two), allocator);
    		assertEquals(8, constant.data().byteSize());
    	}

    	@DisplayName("must be contiguous")
    	@Test
    	void coverage() {
    		final Range one = new Range(0, 4, Set.of(VERTEX));
    		final Range two = new Range(8, 4, Set.of(FRAGMENT));
    		assertThrows(IllegalArgumentException.class, () -> new PushConstant(List.of(one, two), allocator));
    	}

    	@DisplayName("cannot have overlapping pipeline stages")
    	@Test
    	void stages() {
    		final Range one = new Range(0, 4, Set.of(VERTEX));
    		final Range two = new Range(4, 4, Set.of(VERTEX));
    		assertThrows(IllegalArgumentException.class, () -> new PushConstant(List.of(one, two), allocator));
    	}

//    	// TODO
//    	@Test
//    	void limit() {
//    		constant.validate(new MockLogicalDevice());
//    	}
//
//    	// TODO
//    	@DisplayName("cannot exceed the hardware limit")
//    	@Test
//    	void exceeds() {
//    		final var device = new MockLogicalDevice() {
//    			@Override
//    			public DeviceLimits limits() {
//    				final var limits = new VkPhysicalDeviceLimits();
//    				limits.maxPushConstantsSize = 1;
//    				return new DeviceLimits(limits);
//    			}
//    		};
//    		assertThrows(IllegalArgumentException.class, () -> constant.validate(device));
//    	}
	}

	@DisplayName("A push constant update...")
	@Nested
	class UpdatedCommandTest {
		@SuppressWarnings("unused")
		private static class MockUpdateLibrary {
			public void vkCmdPushConstants(Buffer commandBuffer, PipelineLayout layout, EnumMask<VkShaderStageFlags> stageFlags, int offset, int size, Handle pValues) {
				assertNotNull(layout);
				assertNotEquals(0, stageFlags.bits());
				assertEquals(0, offset);
				assertTrue(offset + size <= pValues.address().byteSize());
				assertEquals(0, size % 4);
			}
		}

		private PipelineLayout layout;
		private LogicalDevice device;

		@BeforeEach
		void before() {
			final var mockery  = new Mockery(new MockUpdateLibrary(), PipelineLayout.Library.class);
			device = new MockLogicalDevice(mockery.proxy());
			layout = new PipelineLayout(new Handle(2), device, constant);
		}

    	@DisplayName("can write a range of the constant")
		@Test
		void update() {
			final var update = constant.update(one, layout);
			update.execute(null);
			//assertEquals(4, library.size);
		}

    	@DisplayName("can write the entire backing buffer")
		@Test
		void all() {
			final var update = constant.update(layout);
			update.execute(null);
			//assertEquals(8, library.size);
		}

    	@DisplayName("can write the entire backing buffer for a constant with a single range")
		@Test
		void single() {
			final var single = new PushConstant(List.of(one), allocator);
			final var layout = new PipelineLayout(new Handle(2), device, single);
			final var update = single.update(layout);
			update.execute(null);
			//assertEquals(4, library.size);
		}

    	@DisplayName("cannot be configured for an invalid range")
		@Test
		void invalid() {
			final var other = new Range(0, 4, Set.of(VkShaderStageFlags.GEOMETRY));
			assertThrows(IllegalArgumentException.class, () -> constant.update(other, layout));
		}

    	@DisplayName("cannot be configured for a different pipeline")
		@Test
		void layout() {
			final var other = new PipelineLayout(new Handle(2), device, null);
			assertThrows(IllegalArgumentException.class, () -> constant.update(one, other));
		}
	}
}

