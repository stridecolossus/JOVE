package org.sarge.jove.particle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.nio.ByteBuffer;
import java.util.Optional;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Bufferable;
import org.sarge.jove.common.Layout.CompoundLayout;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.model.Model.Header;
import org.sarge.jove.model.Primitive;

public class ParticleModelTest {
	private ParticleModel model;
	private ParticleSystem sys;

	@BeforeEach
	void before() {
		sys = new ParticleSystem();
		model = new ParticleModel(sys);
		sys.add(2);
	}

	@Test
	void header() {
		assertEquals(new Header(Primitive.POINTS, 2, CompoundLayout.of(Point.LAYOUT)), model.header());
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
	}

	@Test
	void index() {
		assertEquals(Optional.empty(), model.index());
	}
}
