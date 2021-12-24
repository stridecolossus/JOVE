package org.sarge.jove.platform.vulkan.pipeline;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.vulkan.VkPushConstantRange;
import org.sarge.jove.platform.vulkan.VkShaderStage;
import org.sarge.jove.util.IntegerEnumeration;

@Nested
class PushConstantRangeTest {
	private static final Set<VkShaderStage> STAGES = Set.of(VkShaderStage.VERTEX, VkShaderStage.FRAGMENT);

	private PushConstantRange range;

	@BeforeEach
	void before() {
		range = new PushConstantRange(4, 8, STAGES);
	}

	@Test
	void constructor() {
		assertEquals(4, range.offset());
		assertEquals(8, range.size());
		assertEquals(STAGES, range.stages());
		assertEquals(4 + 8, range.length());
	}

	@Test
	void populate() {
		final var info = new VkPushConstantRange();
		range.populate(info);
		assertEquals(4, info.offset);
		assertEquals(8, info.size);
		assertEquals(IntegerEnumeration.mask(STAGES), info.stageFlags);
	}

	@Test
	void constructorInvalidOffsetAlignment() {
		assertThrows(IllegalArgumentException.class, () -> new PushConstantRange(3, 4, Set.of(VkShaderStage.VERTEX)));
	}

	@Test
	void constructorInvalidSizeAlignment() {
		assertThrows(IllegalArgumentException.class, () -> new PushConstantRange(0, 3, Set.of(VkShaderStage.VERTEX)));
	}

	@Test
	void constructorEmptyStages() {
		assertThrows(IllegalArgumentException.class, () -> new PushConstantRange(0, 4, Set.of()));
	}
}
