package org.sarge.jove.model;

import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.oneOrMore;

import java.util.List;

import org.sarge.jove.common.Coordinate;
import org.sarge.jove.common.Coordinate.Coordinate2D;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.model.Vertex.Component;

/**
 * The <i>grid builder</i> constructs a grid of vertices in the X-Z plane.
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
	 * Sets the size of the grid (number of tiles in both directions).
	 * @param size Grid size
	 */
	public GridBuilder size(int size) {
		this.size = oneOrMore(size);
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
	public Model build() {
		// Init model
		final ModelBuilder model = new ModelBuilder();
		model.primitive(Primitive.PATCH);		// TODO - optional? e.g. could build triangles?
		model.layout(List.of(Component.POSITION, Component.COORDINATE));

		// Calculate half distance in both directions
		final int quads = size - 1;
		final float w = width * quads / 2;
		final float b = breadth * quads / 2;

		// Build grid vertices
		for(int x = 0; x < size; ++x) {
			for(int y = 0; y < size; ++y) {
				// Determine grid position and height
				final float px = x * width - w;
				final float pz = y * breadth - b;
				final float h = height.height(x, y);
				final Point pos = new Point(px, h, pz);

				// TODO - normals from height function

				// Calculate texture coordinate
				final Coordinate coord = new Coordinate2D((float) x / size, (float) y / size);

				// Add grid vertex
				final Vertex vertex = new Vertex(pos, null, coord, null);
				model.add(vertex);
			}
		}

		// Build index for counter-clockwise quads
		for(int x = 0; x < quads; ++x) {
			for(int y = 0; y < quads; ++y) {
				final int index = x + y * size;
				model.add(index);
				model.add(index + size);
				model.add(index + size + 1);
				model.add(index + 1);
			}
		}
		// TODO - option to index as quads, triangles, strip?
		// TODO - optional flag for whether to create index?

		// Build grid model
		return model.build();
	}
}
