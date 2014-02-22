package org.sarge.jove.widget;

import org.sarge.jove.light.Texture;
import org.sarge.jove.model.MeshBuilder;
import org.sarge.jove.model.MeshLayout;
import org.sarge.jove.model.Primitive;

public class TextureDrawable implements Drawable {
	private final Texture tex;
	private final 
	
	@Override
	public Dimensions getDimensions() {
		return null;
	}
	
	@Override
	public void render( Object obj ) {
		final MeshBuilder b = new MeshBuilder( Primitive.TRIANGLES, MeshLayout.create( "V0" ) );
	}
}

