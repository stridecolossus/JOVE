package org.sarge.jove.model;

import org.sarge.jove.common.Colour;
import org.sarge.jove.common.TextureCoordinate;
import org.sarge.jove.geometry.Point;
import org.sarge.lib.util.ToString;

/**
 * Convenience quad class comprised of an array of four vertices.
 * @author Sarge
 */
public class Quad {
	/**
	 * Quad texture coordinates ordered counter-clockwise from top-left vertex.
	 */
	public static final TextureCoordinate[] QUAD_COORDS = new TextureCoordinate[]{
			TextureCoordinate.TOP_LEFT,
			TextureCoordinate.BOTTOM_LEFT,
			TextureCoordinate.TOP_RIGHT,
			TextureCoordinate.BOTTOM_RIGHT,
	};

	private final Vertex[] quad = new Vertex[4];

	/**
	 * Default constructor for unit-size quad centred on the origin.
	 */
	public Quad() {
		this(Point.ORIGIN, 1, 1);
	}

	/**
	 * Constructor.
	 * @param pos	Centre-point
	 * @param w		Width
	 * @param h		Height
	 */
	public Quad(Point pos, float w, float h) {
		this(pos, w, h, true);
	}

	/**
	 * Constructor given top-left corner.
	 * @param pos	Top-left corner
	 * @param w		Width
	 * @param h		Height
	 */
	public Quad(Point pos, float w, float h, boolean centred) {
		// Calc top-left corner
		final float x, y;
		if(centred) {
			x = pos.x - w / 2f;
			y = pos.y + h / 2f;
		}
		else {
			x = pos.x;
			y = pos.y;
		}

		// Calc bottom-right corner
		final float dx = x + w;
		final float dy = y - h;

		// Build vertices
		quad[0] = new Vertex(new Point(x, y, pos.z));
		quad[1] = new Vertex(new Point(x, dy, pos.z));
		quad[2] = new Vertex(new Point(dx, y, pos.z));
		quad[3] = new Vertex(new Point(dx, dy, pos.z));
	}

	/**
	 * Constructor given explicit quad vertices.
	 * @param points Quad vertices
	 */
	public Quad(Point[] quad) {
		if(quad.length != 4) throw new IllegalArgumentException("Invalid number of vertices");
		for(int n = 0; n < 4; ++n) {
			this.quad[n] = new Vertex(quad[n]);
		}
	}

	/**
	 * @return Quad vertices
	 */
	public Vertex[] getVertices() {
		return quad;
	}

	/**
	 * Sets the colour of all vertices in this quad.
	 * @param col Colour
	 */
	public void setColour(Colour col) {
		for(Vertex v : quad) {
			v.setColour(col);
		}
	}

	/**
	 * Adds default texture coordinates to this quad.
	 */
	public void setDefaultTextureCoords() {
		setTextureCoords(QUAD_COORDS);
	}

	/**
	 * Sets the texture coordinates of this quad.
	 * @param coords Texture coordinates in counter-clockwise order starting at top-left
	 */
	public void setTextureCoords(TextureCoordinate[] coords) {
		for(int n = 0; n < quad.length; ++n) {
			quad[n].setTextureCoords(coords[n]);
		}
	}

	@Override
	public String toString() {
		return ToString.toString(this);
	}
}
