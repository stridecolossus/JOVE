package org.sarge.jove.model.obj;

import org.sarge.jove.geometry.Point;

/**
 * OBJ parser for vertices.
 * @author Sarge
 */
public class VertexParser extends ArrayParser<Point> {
	public VertexParser() {
		super(Point.SIZE, Point::new);
	}
	
	@Override
	protected void add(Point pt, ObjectModel model) {
		model.add(pt);
	}
}
