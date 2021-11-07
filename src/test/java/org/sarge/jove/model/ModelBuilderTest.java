package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.CompoundLayout;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.model.Model.Header;

class ModelBuilderTest {
	private ModelBuilder builder;
	private Vertex vertex;

	@BeforeEach
	void before() {
		builder = new ModelBuilder();
		vertex = Vertex.of(Point.ORIGIN);
	}

	@Test
	void isEmpty() {
		assertEquals(true, builder.isEmpty());
	}

	@Test
	void build() {
		// Build model
		final Model model = builder
				.primitive(Primitive.LINES)
				.layout(Point.LAYOUT)
				.add(vertex)
				.add(vertex)
				.build();

		// Verify model
		assertNotNull(model);
		assertEquals(new Header(new CompoundLayout(List.of(Point.LAYOUT)), Primitive.LINES, 2), model.header());
		assertEquals(false, model.isIndexed());
		assertNotNull(model.vertices());
		assertEquals(Optional.empty(), model.index());
		assertEquals(false, builder.isEmpty());
	}

	@Test
	void buildEmpty() {
		final Model model = builder.build();
		assertNotNull(model);
		assertEquals(new Header(new CompoundLayout(List.of()), Primitive.TRIANGLE_STRIP, 0), model.header());
		assertNotNull(model.vertices());
		assertEquals(Optional.empty(), model.index());
		assertEquals(true, builder.isEmpty());
	}

	@DisplayName("Invalid vertices are trapped if validation is switched on")
	@Test
	void addInvalidVertex() {
		builder.layout(Vector.NORMALS);
		assertThrows(IllegalArgumentException.class, () -> builder.add(vertex));
	}

	@DisplayName("Invalid vertices are ignored by default")
	@Test
	void addValidationIgnored() {
		builder.layout(Vector.NORMALS);
		builder.validate(false);
		builder.add(vertex);
	}

	@DisplayName("Model layout cannot be modified after vertex data has been added")
	@Test
	void buildLayoutModified() {
		builder.layout(Point.LAYOUT);
		builder.add(vertex);
		assertThrows(IllegalStateException.class, () -> builder.layout(Point.LAYOUT));
	}
}
