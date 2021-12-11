package org.sarge.jove.model;

import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.oneOrMore;

import java.util.List;

import org.sarge.jove.common.Layout;

/**
 * Builder for a grid of vertices in the X-Z plane.
 * @author Sarge
 */
public class GridBuilder {
	/**
	 * A <i>height function</i> is used to set the height of grid vertices (Y axis).
	 */
	public interface HeightFunction {
		/**
		 * Calculates the height at the given coordinates.
		 * @return Height
		 */
		float height(int x, int y);

		/**
		 * Creates a height function that returns a literal height for all tiles.
		 * @param height Height
		 * @return New literal height function
		 */
		static HeightFunction literal(float height) {
			return (x, y) -> height;
		}
	}

	private float width = 1;
	private float breadth = 1;
	private int size = 4;
	private HeightFunction height = HeightFunction.literal(0);

	/**
	 * Sets the grid tile width (in the X axis).
	 * @param w Width
	 */
	public GridBuilder width(float width) {
		this.width = width;
		return this;
	}

	/**
	 * Sets the grid tile breadth (in the Z axis).
	 * @param h Tile breadth
	 */
	public GridBuilder breadth(float breadth) {
		this.breadth = breadth;
		return this;
	}

	/**
	 * Sets the size of the grid (number of tiles in both directions)
	 * @param size Grid size
	 */
	public GridBuilder size(int size) {
		this.size = oneOrMore(size);
		//if(!MathsUtil.isPowerOfTwo(size)) throw new IAE
		return this;
	}

	/**
	 * Sets the function used to set the height of grid vertices (default is zero).
	 * @param height Height function
	 */
	public GridBuilder height(HeightFunction height) {
		this.height = notNull(height);
		return this;
	}

	/**
	 * Constructs this grid.
	 * @param layout Grid vertex layout
	 * @return New grid
	 */
	public Model build(List<Layout> layout) {
		return null;
//		//
//		final ModelBuilder builder = new ModelBuilder(layout);
//		builder.primitive(Primitive.PATCH);
//
//		// Calculate half distance in both directions
//		final float w = width * (size - 1) / 2;
//		final float b = breadth * (size - 1) / 2;
//
//		// Build grid vertices
//		for(int x = 0; x < size; ++x) {
//			for(int y = 0; y < size; ++y) {
//				final float px = x * width - w;
//				final float pz = y * breadth - b;
//				final float h = height.height(x, y);
//
//				final Point pos = new Point(px, h, pz);
//System.out.println(x+","+y+" "+pos);
//
//				final Vertex vertex = Vertex.of(pos);
//				builder.add(vertex);
//			}
//		}
//
//		// Build grid model
//		return builder.build();
	}
}
