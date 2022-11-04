package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.*;

class VolumeTest {
	private Volume vol;

	@BeforeEach
	void before() {
		vol = spy(Volume.class);
	}

	@Test
	void intersectsDefault() {
		assertThrows(UnsupportedOperationException.class, () -> vol.intersects(mock(Volume.class)));
	}
}
