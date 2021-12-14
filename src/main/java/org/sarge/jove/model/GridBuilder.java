package org.sarge.jove.model;

import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.oneOrMore;

import java.util.List;
import java.util.stream.IntStream;

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
	private Primitive primitive = Primitive.TRIANGLES;
	private IndexFactory index = Triangle.TRIANGLES;

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
	 * Sets the size of the grid (number of vertices in each direction).
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
	 * Sets the drawing primitive (default is {@link Primitive#TRIANGLES}).
	 * @param primitive Drawing primitive
	 */
	public GridBuilder primitive(Primitive primitive) {
		this.primitive = notNull(primitive);
		return this;
	}

	/**
	 * Sets the index factory used to generate indices for the grid (default is {@link Triangle#TRIANGLES}).
	 * @param index Index factory or {@code null} for no index
	 */
	public GridBuilder index(IndexFactory index) {
		this.index = index;
		return this;
	}

	/**
	 * Constructs this grid.
	 * @param layout Grid vertex layout
	 * @return New grid
	 */
	public DefaultModel build() {
		// Init model
		final ModelBuilder model = new ModelBuilder();
		model.primitive(primitive);
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

		// TODO - if index is null then add vertices using index factory OR as above + index

		// Build index for counter-clockwise quads
		if(index != null) {
			IntStream
					.range(0, quads)
					.map(row -> row * size)
					.flatMap(start -> index.strip(quads).map(n -> n + start))
					.forEach(model::add);
		}

		// Build grid model
		return model.build();
	}
}
