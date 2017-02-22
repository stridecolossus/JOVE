package org.sarge.jove.model.obj;

import org.sarge.jove.common.TextureCoordinate;

/**
 * OBJ parser for texture coordinates.
 * @author Sarge
 */
public class TextureCoordinateParser extends ArrayParser<TextureCoordinate> {
	public TextureCoordinateParser() {
		super(TextureCoordinate.SIZE, TextureCoordinate::new);
	}
	
	@Override
	protected void add(TextureCoordinate coords, ObjectModel model) {
		model.add(coords);
	}
}
