package org.sarge.jove.texture;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.sarge.jove.texture.TextureDescriptor.Filter;

public class MutableTextureDescriptorTest {
	private MutableTextureDescriptor info;

	@Before
	public void before() {
		info = new MutableTextureDescriptor( 1, 2 );
	}

	@Test
	public void constructor() {
		assertEquals( 1, info.getWidth() );
		assertEquals( 2, info.getHeight() );
		assertEquals( 2, info.getDimensions() );
		assertEquals( true, info.isTranslucent() );
		assertEquals( true, info.isMipMapped() );
		assertEquals( Filter.LINEAR, info.getMinificationFilter() );
		assertEquals( Filter.LINEAR, info.getMagnificationFilter() );
	}

	@Test
	public void setTranslucent() {
		info.setTranslucent( true );
		assertEquals( true, info.isTranslucent() );
	}

	@Test
	public void setMipMapped() {
		info.setMipMapped( false );
		assertEquals( false, info.isMipMapped() );
	}

	@Test
	public void setFilter() {
		info.setMinificationFilter( Filter.NEAREST );
		info.setMagnificationFilter( Filter.NEAREST );
		assertEquals( Filter.NEAREST, info.getMinificationFilter() );
		assertEquals( Filter.NEAREST, info.getMagnificationFilter() );
	}
}
