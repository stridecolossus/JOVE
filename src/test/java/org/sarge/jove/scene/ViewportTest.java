package org.sarge.jove.scene;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sarge.jove.util.TestHelper.assertFloatEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Coordinate;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.common.Rectangle;

public class ViewportTest {
	private Viewport viewport;
	private Rectangle rect;
	private Projection projection;

	@BeforeEach
	public void before() {
		rect = new Rectangle(new Coordinate(0, 0), new Dimensions(640, 480));
		projection = mock(Projection.class);
		when(projection.height(rect.size())).thenReturn(100f);
		viewport = new Viewport(rect, 1, 2, projection);
	}

	@Test
	public void constructor() {
		assertEquals(rect, viewport.rectangle());
		assertEquals(1, viewport.near());
		assertEquals(2, viewport.far());
		assertEquals(projection, viewport.projection());
		assertFloatEquals(100f, viewport.height());
		assertFloatEquals(100f * 640 / 480, viewport.width());
	}

	@Test
	public void matrix() {
		viewport.matrix();
		verify(projection).matrix(1, 2, rect.size());
	}
}
