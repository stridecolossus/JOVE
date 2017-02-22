package org.sarge.jove.model;

import org.sarge.jove.common.Bufferable;
import org.sarge.jove.common.TextureCoordinate;
import org.sarge.lib.util.Check;

/**
 * Texture coordinates data-type.
 * @author Sarge
 */
public class TextureBufferDataType implements BufferDataType {
	private static final TextureBufferDataType[] buffers = new TextureBufferDataType[ 8 ];

	static {
		for( int n = 0; n < buffers.length; ++n ) {
			buffers[ n ] = new TextureBufferDataType( n );
		}
	}

	/**
	 * Retrieves a texture-coords buffer data-type.
	 * @param unit Texture unit
	 * @return Data-type descriptor
	 */
	public static TextureBufferDataType get( int unit ) {
		return buffers[ unit ];
	}

	private final int unit;

	/**
	 * Constructor.
	 * @param unit Texture unit
	 */
	private TextureBufferDataType( int unit ) {
		Check.zeroOrMore( unit );
		this.unit = unit;
	}

	/**
	 * @return Texture unit of this buffer
	 */
	public int getTextureUnit() {
		return unit;
	}

	@Override
	public int getSize() {
		return TextureCoordinate.SIZE;
	}

	@Override
	public Bufferable getData( Vertex vertex ) {
		return vertex.getTextureCoords();
	}

	@Override
	public String toString() {
		return "texture-" + unit;
	}
}
