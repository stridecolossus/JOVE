package org.sarge.jove.model.obj;

import org.sarge.jove.geometry.Vector;

/**
 * OBJ parser for normals.
 * @author Sarge
 */
public class NormalParser extends ArrayParser<Vector> {
	public NormalParser() {
		super(Vector.SIZE, Vector::new);
	}
	
	@Override
	protected void add(Vector normal, ObjectModel model) {
		model.add(normal);
	}
}
