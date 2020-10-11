package org.sarge.jove.model;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.model.DataBuffer.Layout.Attribute;
import org.sarge.jove.model.DataBuffer.Layout.Rate;
import org.sarge.jove.model.Vertex.Component;

public class DataBufferTest {
	private Attribute attribute;

	@BeforeEach
	public void before() {
		attribute = new Attribute(1, Component.COLOUR, 2);
	}

	@Test
	public void constructor() {
		assertEquals(1, attribute.location());
		assertEquals(Component.COLOUR, attribute.component());
		assertEquals(2, attribute.offset());
	}

	@Test
	public void layout() {
		final DataBuffer.Layout layout = new DataBuffer.Layout(3, Rate.INSTANCE, List.format(attribute), 4);
		assertEquals(3, layout.binding());
		assertEquals(Rate.INSTANCE, layout.rate());
		assertEquals(List.format(attribute), layout.attributes());
		assertEquals(4, layout.stride());
	}

	@Test
	public void builder() {
		// Build layout
		final DataBuffer.Layout layout = new DataBuffer.Layout.Builder()
			.binding(42)
			.add(1, Component.POSITION)
			.add(Component.COLOUR)
			.rate(Rate.INSTANCE)
			.build();

		// Check layout
		assertNotNull(layout);
		assertEquals(42, layout.binding());
		assertEquals(Rate.INSTANCE, layout.rate());
		assertEquals((3 + 4) * Float.BYTES, layout.stride());
		assertEquals(2, layout.attributes().size());

		// Check first attribute
		final Attribute pos = layout.attributes().get(0);
		assertEquals(1, pos.location());
		assertEquals(Component.POSITION, pos.component());
		assertEquals(0, pos.offset());

		// Check second attribute (with auto-allocated location)
		final Attribute col = layout.attributes().get(1);
		assertEquals(2, col.location());
		assertEquals(Component.COLOUR, col.component());
		assertEquals(3 * Float.BYTES, col.offset());
	}

	@Test
	public void of() {
		final DataBuffer.Layout layout = DataBuffer.Layout.create(List.of(Component.POSITION, Component.COLOUR));
		assertNotNull(layout);
		assertEquals(0, layout.binding());
		assertEquals(Rate.VERTEX, layout.rate());
		assertEquals((3 + 4) * Float.BYTES, layout.stride());
		assertEquals(2, layout.attributes().size());
	}
}
