package org.sarge.jove.model;

import static org.sarge.lib.util.Check.*;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.regex.*;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.CompoundLayout;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.model.Coordinate.Coordinate2D;
import org.sarge.jove.model.Coordinate.Coordinate2D.Corners;
import org.sarge.jove.model.TextureFont.Metrics;

/**
 * A <i>glyph mesh builder</i> is used to construct the vertex data for multi-line text comprising glyph quads.
 * @author Sarge
 */
public class GlyphMeshBuilder {
	private static final Pattern PATTERN = Pattern.compile("\\S+");

	private final TextureFont font;
	private final MeshBuilder mesh = new MeshBuilder(Primitive.TRIANGLE, new CompoundLayout(Point.LAYOUT, Coordinate2D.LAYOUT));
	private final int tiles;
	private final float size;
	private float scale = 1;
	private float x, y;
	private float margin = Float.MAX_VALUE;

	/**
	 * Constructor.
	 * @param font Texture font for this mesh
	 */
	public GlyphMeshBuilder(TextureFont font) {
		this.font = notNull(font);
		this.tiles = font.tiles();
		this.size = 1f / tiles;
	}

	/**
	 * @return Position of the next character
	 */
	public Point cursor() {
		return new Point(x, y, 0);
	}

	/**
	 * Sets the cursor position.
	 */
	public GlyphMeshBuilder cursor(float x, float y) {
		this.x = x;
		this.y = y;
		return this;
	}

	/**
	 * @return Whether words are line-wrapped by this builder
	 * @see #margin(float)
	 */
	public boolean isLineWrapping() {
		return margin < Float.MAX_VALUE;
	}

	/**
	 * Sets the margin for line wrapping (default is no margin).
	 * @param margin Margin width
	 */
	public GlyphMeshBuilder margin(float margin) {
		this.margin = zeroOrMore(margin);
		return this;
	}

	/**
	 * Sets the glyph size scale for this mesh (default is no scaling).
	 * @param scale Glyph scale
	 */
	public GlyphMeshBuilder scale(float scale) {
		this.scale = scale;
		return this;
	}

	/**
	 * @return Underlying glyph mesh
	 */
	public Mesh mesh() {
		return mesh.mesh();
	}

	/**
	 * Adds texture glyphs for the given text.
	 * <p>
	 * Notes:
	 * <ul>
	 * <li>Text is line-wrapped about the {@link #margin(float)} if configured</li>
	 * <li>Glyphs are not generated for white-space characters</li>
	 * <li>The {@link #cursor()} position is updated to the end of the new text</li>
	 * </ul>
	 * <p>
	 * @param text Text to add
	 * @see TextureFont#metrics(String)
	 */
	public GlyphMeshBuilder add(String text) {
		final boolean wrap = isLineWrapping();
		final Matcher matcher = PATTERN.matcher(text);
		int prev = 0;
		while(matcher.find()) {
			// Determine total advance of the next word
			final int start = matcher.start();
			final int end = matcher.end();
			final String word = text.substring(start, end);
			final List<Metrics> metrics = font.metrics(word);
			final float advance = advance(metrics);

			// Determine total advance of the whitespace before this word
			final String whitespace = text.substring(prev, start);
			final float spacing = advance(font.metrics(whitespace));

			// Line wrap as required
			if(wrap && isMarginExceeded(spacing + advance)) {
				newline();
			}
			else {
				advance(spacing);
			}

			// Build character glyphs
			for(Metrics m : metrics) {
				add(m.coordinates());
				advance(m.advance() * scale);
			}

			// Note start of next whitespace group
			prev = end;
		}

		return this;
	}

	/**
	 * @param metrics Word metrics
	 * @return Total advance of the given glyph metrics
	 */
	private float advance(List<Metrics> metrics) {
		return Metrics.advance(metrics) * scale;
	}

	/**
	 * Determines whether the given advance would exceed the configured margin requiring a new line of text.
	 * @param advance Word advance
	 * @return Whether to start a new line
	 * @see #newline()
	 */
	private boolean isMarginExceeded(float advance) {
		return x + advance > margin;
	}

	/**
	 * Starts a new line of text.
	 */
	public void newline() {
		x = 0;
		y += (font.height() + font.leading()) * scale;
	}

	/**
	 * Advances the cursor position.
	 * @param advance Advance
	 */
	private void advance(float advance) {
		assert advance > 0;
		x += advance;
	}

	/**
	 * Adds a character glyph to this mesh at the current cursor position.
	 * @param corners Glyph texture coordinates
	 */
	private void add(Corners corners) {
		final float w = size * scale;

		final var bl = new Coordinate2D(corners.topLeft().u(), corners.bottomRight().v());
		final var tr = new Coordinate2D(corners.bottomRight().u(), corners.topLeft().v());

		final var bottomLeft = new GlyphVertex(new Point(x, y + w, 0), bl);
		final var topRight = new GlyphVertex(new Point(x + w, y, 0), tr);

		mesh.add(new GlyphVertex(new Point(x, y, 0), corners.topLeft()));
		mesh.add(bottomLeft);
		mesh.add(topRight);

		mesh.add(bottomLeft);
		mesh.add(new GlyphVertex(new Point(x + w, y + w, 0), corners.bottomRight()));
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

		@Override
		public String toString() {
			return new ToStringBuilder(this)
					.appendSuper(super.toString())
					.append("coord", coord)
					.build();
		}
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append(font)
				.append("cursor", this.cursor())
				.append("scale", scale)
				.append("margin", margin)
				.append(mesh)
				.build();
	}
}
