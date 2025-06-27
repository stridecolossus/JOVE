package org.sarge.jove.platform.vulkan.pipeline;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.*;
import java.util.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.VkSpecializationInfo;

class SpecialisationConstantsTest {
	private SpecialisationConstants constants;

	@BeforeEach
	void before() {
		final Map<Integer, Object> map = new LinkedHashMap<>();
		map.put(1, 1);
		map.put(2, 2f);
		map.put(3, false);

		constants = new SpecialisationConstants(map);
	}

	@Test
	void type() {
		assertThrows(IllegalArgumentException.class, () -> new SpecialisationConstants(Map.of(1, "invalid")));
	}

	@Test
	void empty() {
		assertEquals(null, new SpecialisationConstants(Map.of()).descriptor());
	}

	@Test
	void descriptor() {
		// Check generated data buffer
		final VkSpecializationInfo info = constants.descriptor();
		final var buffer = ByteBuffer.allocate(12).order(ByteOrder.nativeOrder());
		buffer.putInt(1);
		buffer.putFloat(2);
		buffer.putInt(0);
		buffer.flip();
		assertArrayEquals(buffer.array(), info.pData);
		assertEquals(12, info.dataSize);

		// Check number of entries
		assertEquals(3, info.mapEntryCount);
		assertEquals(3, info.pMapEntries.length);

		// Check integer constant
		final var integer = info.pMapEntries[0];
		assertEquals(1, integer.constantID);
		assertEquals(0, integer.offset);
		assertEquals(4, integer.size);

		// Check floating-point constant
		final var fp = info.pMapEntries[1];
		assertEquals(2, fp.constantID);
		assertEquals(4, fp.offset);
		assertEquals(4, fp.size);

		// Check boolean constant (represented as a 4-byte integer)
		final var bool = info.pMapEntries[2];
		assertEquals(3, bool.constantID);
		assertEquals(8, bool.offset);
		assertEquals(4, bool.size);
	}
}
