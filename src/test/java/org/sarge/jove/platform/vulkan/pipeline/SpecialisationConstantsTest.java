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
		final Map<Integer, Object> map = Map.of(
				1, 1,
				2, 2f,
				3, false
		);

		constants = new SpecialisationConstants(map);
	}

	@Test
	void type() {
		assertThrows(IllegalArgumentException.class, () -> new SpecialisationConstants(Map.of(1, "invalid")));
	}

	@Test
	void descriptor() {
		// Build descriptor
		final VkSpecializationInfo info = constants.descriptor();
		assertEquals(3, info.mapEntryCount);
		assertEquals(3, info.pMapEntries.length);

		// Check entries
		final Set<Integer> identifiers = new HashSet<>(Set.of(1, 2, 3));
		final ByteBuffer buffer = ByteBuffer.wrap(info.pData).order(ByteOrder.nativeOrder());
		for(int n = 0; n < 3; ++n) {
			// Check entry
			final var entry = info.pMapEntries[n];
			assertEquals(n * 4, entry.offset);
			assertEquals(4, entry.size);

			// Check entries are unique
			assertEquals(true, identifiers.remove(entry.constantID));

			// Check actual constant value
			buffer.position(entry.offset);
			switch(entry.constantID) {
				case 1 -> assertEquals(1, buffer.getInt());
				case 2 -> assertEquals(2f, buffer.getFloat());
				case 3 -> assertEquals(0, buffer.getInt());
				default -> fail();
			}
		}

		// Ensure all constants are present
		assertEquals(true, identifiers.isEmpty());
	}
}
