package org.sarge.jove.model;

import static java.util.Objects.requireNonNull;
import static org.sarge.jove.util.Validation.requireZeroOrMore;

import java.nio.ByteBuffer;

import org.sarge.jove.geometry.Point;
import org.sarge.jove.model.Coordinate.Coordinate2D;
import org.sarge.jove.model.Coordinate.Coordinate2D.Corners;

/**
 * A <i>glyph mesh builder</i> is used to construct the vertex data for multi-line text comprising glyph quads.
 * @author Sarge
 */
public class GlyphMeshBuilder {
	private final GlyphFont font;
	private final MutableMesh mesh = new MutableMesh(Primitive.TRIANGLE, Point.LAYOUT, Coordinate2D.LAYOUT);
	private final int tiles;
	private final float size;
	private float scale = 1;
//	private float x, y;
	private Point cursor = Point.ORIGIN;
	private float margin = Float.MAX_VALUE;

	/**
	 * Constructor.
	 * @param atlas Glyph atlas for this mesh
	 */
	public GlyphMeshBuilder(GlyphFont font) {
		this.font = requireNonNull(font);
		this.tiles = font.tiles();
		this.size = 1f / tiles;

// TODO
cursor = new Point(-0.9f, 0, 0);
	}

	/**
	 * @return Position of the next character
	 */
	public Point cursor() {
		return cursor; // new Point(x, y, 0);
	}

	public GlyphMeshBuilder cursor(Point cursor) {
		this.cursor = requireNonNull(cursor);
		return this;
	}

	/**
	 * Sets the margin for line wrapping (default is no margin).
	 * @param margin Margin width
	 */
	public GlyphMeshBuilder margin(float margin) {
		this.margin = requireZeroOrMore(margin);
		return this;
	}

	/**
	 * Sets the glyph size scale for this mesh.
	 * @param scale Glyph scale
	 */
	public GlyphMeshBuilder scale(float scale) {
		this.scale = scale;
		return this;
	}

	/**
	 * @return Glyph mesh
	 */
	public Mesh mesh() {
		return mesh;
	}

	/**
	 * Appends a character to this mesh.
	 * TODO - White-space characters
	 * @param ch Character to add
	 * @see GlyphFont#glyph(char)
	 */
	public GlyphMeshBuilder add(char ch) {
//		return insert(vertices.size(), ch);
		return insert(0, ch); // TODO
	}

	/**
	 * Appends a string to this mesh.
	 * @param str String to append
	 * @see #add(char)
	 */
	public GlyphMeshBuilder add(String str) {
		for(char ch : str.toCharArray()) {
			add(ch);
		}
		return this;
	}

	// TODO

	private Glyph prev;

	public GlyphMeshBuilder insert(int index, char ch) {
		// Lookup glyph for this character
		final Glyph glyph = font.glyph(ch);

		if(prev != null) {
			final float advance = prev.advance(ch) * scale;
			cursor = new Point(cursor.x + advance, cursor.y, cursor.z);
		}
		prev = glyph;

		// Render glyph
		if(!Character.isWhitespace(ch)) {
			render(ch - font.start());
		}

//		// Advance cursor for next character
//		glyph.advance(ch)
//
//		cursor = new Point(cursor.x + glyph.advance() * scale, cursor.y, cursor.z);
////		cursor = new Point(cursor.x + 1/20f, cursor.y, cursor.z);
//		//x += glyph.advance();

		return this;
	}

	/**
	 * Calculates the texture coordinate for the given glyph index.
	 * @param index Glyph index
	 * @return Glyph coordinates
	 */
	private Corners corners(int index) {
		final float x = (index % tiles) * size;
		final float y = (index / tiles) * size;
		final var topLeft = new Coordinate2D(x, y);
		final var bottomRight = new Coordinate2D(x + size, y + size);
		return new Corners(topLeft, bottomRight);
	}

	/**
	 * TODO
	 * x/y = top-left
	 * others calculated from font height & advance
	 *
	 */
	private void render(int index) {

		final Corners corners = corners(index);
		final float w = size * scale;

		final var bl = new Coordinate2D(corners.topLeft().u(), corners.bottomRight().v());
		final var tr = new Coordinate2D(corners.bottomRight().u(), corners.topLeft().v());

		final var bottomLeft = new GlyphVertex(new Point(cursor.x, cursor.y + w, 0), bl);
		final var topRight = new GlyphVertex(new Point(cursor.x + w, cursor.y, 0), tr);

		mesh.add(new GlyphVertex(new Point(cursor.x, cursor.y, 0), corners.topLeft()));
		mesh.add(bottomLeft);
		mesh.add(topRight);

		mesh.add(bottomLeft);
		mesh.add(new GlyphVertex(new Point(cursor.x + w, cursor.y + w, 0), corners.bottomRight()));
		mesh.add(topRight);
	}

	// TODO
	static class GlyphVertex extends Vertex {
		private final Coordinate2D coord;

		public GlyphVertex(Point pos, Coordinate2D coord) {
			super(pos);
			this.coord = coord;
		}

		@Override
		public void buffer(ByteBuffer bb) {
			super.buffer(bb);
			coord.buffer(bb);
		}

		@Override
		public boolean equals(Object obj) {
			return
					(obj == this) ||
					(obj instanceof GlyphVertex that) &&
					super.equals(obj) &&
					this.coord.equals(that.coord);
		}
	}

	/**
	 * Deletes a portion of this mesh.
	 * @param start		Starting index
	 * @param num		Number of glyphs to delete
	 * @throws IndexOutOfBoundsException if the range is out-of-bounds for this mesh
	 */
	public GlyphMeshBuilder delete(int start, int num) {
//		// TODO - this is awful
//		for(int n = start; n < num; ++n) {
//			vertices.remove(start);
//		}
		return this;
	}

	public GlyphMeshBuilder delete(int index) {
		return delete(index, 1);
	}

	/**
	 * Clears this mesh.
	 */
	public GlyphMeshBuilder clear() {
//		x = 0;
//		y = 0;
		cursor = Point.ORIGIN;
//		vertices.clear();
		return this;
	}

//	@Override
//	public int count() {
//		return vertices.size();
//	}
//
//	@Override
//	public ByteSizedBufferable vertices() {
//		return new ByteSizedBufferable() {
//			@Override
//			public int length() {
//				return vertices.size() * LAYOUT.stride();
//			}
//
//			@Override
//			public void buffer(ByteBuffer bb) {
//				for(Vertex v : vertices) {
//					v.buffer(bb);
//				}
//			}
//		};
//	}
//
//	@Override
//	public Optional<ByteSizedBufferable> index() {
//		return Optional.empty();
//	}
}
