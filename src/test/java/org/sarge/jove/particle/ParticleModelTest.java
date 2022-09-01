package org.sarge.jove.particle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.nio.ByteBuffer;
import java.util.Optional;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Bufferable;
import org.sarge.jove.common.Layout.CompoundLayout;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.model.Primitive;

public class ParticleModelTest {
	private ParticleModel model;
	private ParticleSystem sys;

	@BeforeEach
	void before() {
		sys = new ParticleSystem();
		model = new ParticleModel(sys);
		sys.add(2, 0L);
	}

	@Test
	void header() {
		assertEquals(Primitive.POINTS, model.primitive());
		assertEquals(2, model.count());
		assertEquals(CompoundLayout.of(Point.LAYOUT), model.layout());
		assertEquals(false, model.isIndexed());
	}

	@Test
	void length() {
		final Bufferable vertices = model.vertices();
		assertEquals(2 * Point.LAYOUT.length(), vertices.length());
	}

	@Test
	void buffer() {
		final Bufferable vertices = model.vertices();
		final ByteBuffer bb = mock(ByteBuffer.class);
		when(bb.putFloat(0)).thenReturn(bb);
		vertices.buffer(bb);
		verify(bb, times(2 * 3)).putFloat(0);
		verifyNoMoreInteractions(bb);
	}

	@Test
	void index() {
		assertEquals(Optional.empty(), model.index());
	}
}
