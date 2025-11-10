package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.model.Coordinate.Coordinate2D;
import org.sarge.jove.model.GlyphMeshBuilder.GlyphVertex;

@Disabled("TODO")
class GlyphMeshBuilderTest {
	private GlyphMeshBuilder builder;
	private GlyphFont font;
	private Glyph glyph;
	private Mesh mesh;

	@BeforeEach
	void before() {
		glyph = new Glyph('A', 3);
		font = new GlyphFont(0, Collections.nCopies(256, glyph), 16);
		builder = new GlyphMeshBuilder(font);
		mesh = builder.mesh();
	}

	@DisplayName("A new glyph mesh...")
	@Nested
	class New {
		@DisplayName("is initially empty")
    	@Test
    	void empty() {
    		assertEquals(0, mesh.count());
    		assertEquals(0, mesh.vertices().limit());
    	}

		@DisplayName("has a cursor at the origin")
    	@Test
    	void cursor() {
    		assertEquals(Point.ORIGIN, builder.cursor());
		}

		@DisplayName("is not indexed")
    	@Test
    	void index() {
    		assertEquals(false, mesh.index().isEmpty());
		}

		@DisplayName("is comprised of triangles with texture coordinates")
    	@Test
    	void mesh() {
    		assertEquals(Primitive.TRIANGLE, mesh.primitive());
    		assertEquals(List.of(Point.LAYOUT, Coordinate2D.LAYOUT), mesh.layout());
		}
    }

	@DisplayName("A glyph mesh with a glyph added...")
	@Nested
	class Added {
		@BeforeEach
		void before() {
			builder.add('A');
		}

		@DisplayName("moves the cursor by the advance of the glyph character")
		@Test
		void advance() {
			assertEquals(new Point(3, 0, 0), builder.cursor());
		}

		@DisplayName("creates a quad comprised of two counter-clockwise triangles at the cursor position")
		@Test
		void vertices() {
			assertEquals(6, mesh.count());

			final float w = 1f / 16;
			final float u = w;
			final float v = 4 * w;

			final var topLeft = new GlyphVertex(new Point(0, 0, 0), new Coordinate2D(u, v));
			final var bottomLeft = new GlyphVertex(new Point(0, w, 0), new Coordinate2D(u, v + w));
			final var topRight = new GlyphVertex(new Point(w, 0, 0), new Coordinate2D(u + w, v));
			final var bottomRight = new GlyphVertex(new Point(w, w, 0), new Coordinate2D(u + w, v + w));

			final Vertex[] expected = {
					topLeft, bottomLeft, topRight,
					bottomLeft, bottomRight, topRight,
			};
//			assertArrayEquals(expected, mesh.vertexData().toArray());
// TODO
		}

		@DisplayName("advances the cursor but does not create a quad for a white-space character")
		@Test
		void whitespace() {
			builder.add(" ");
			assertEquals(6, mesh.count());
			assertEquals(new Point(2 * 3, 0, 0), builder.cursor());
		}
	}

	@DisplayName("A glyph mesh containing some text...")
	@Nested
	class Text {
		@BeforeEach
		void before() {
			builder.add("text");
		}

		@DisplayName("moves the cursor by the total advance of the text")
		@Test
		void advance() {
			assertEquals(new Point(4 * 3, 0, 0), builder.cursor());
		}

		@DisplayName("creates a quad for each character")
		@Test
		void vertices() {
			assertEquals(6 * 4, mesh.count());
		}
	}
}
