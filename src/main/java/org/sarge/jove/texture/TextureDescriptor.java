package org.sarge.jove.texture;

import org.sarge.jove.common.Dimensions;
import org.sarge.lib.util.Check;
import org.sarge.lib.util.ToString;

/**
 * Descriptor for texture parameters.
 * @author Sarge
 */
public class TextureDescriptor {
	/**
	 * Texture wrapping policy.
	 */
	public static enum WrapPolicy {
		REPEAT,
		CLAMP,
		CLAMP_TO_EDGE,
	}

	/**
	 * Texture filtering.
	 */
	public static enum Filter {
		LINEAR,
		NEAREST
	}

	private final Dimensions size;
	private final int dim;

	private boolean alpha = true;
	private WrapPolicy wrap = WrapPolicy.REPEAT;
	private boolean mipmap = true;
	private Filter min = Filter.LINEAR;
	private Filter mag = Filter.LINEAR;

	/**
	 * Constructor for a cube-map.
	 * @param size	Image dimensions
	 * @param cube	Whether the texture is a cube-map
	 */
	public TextureDescriptor( Dimensions size, boolean cube ) {
		Check.notNull( size );
		this.size = size;
		this.dim = cube ? 3 : 2;
	}

	/**
	 * Constructor for a 2D texture.
	 * @param size	Image dimensions
	 */
	public TextureDescriptor( Dimensions size ) {
		this( size, false );
	}

	/**
	 * Constructor for 1D texture.
	 * @param w width
	 */
	public TextureDescriptor( int w ) {
		this.size = new Dimensions( w, 0 );
		this.dim = 1;
	}

	/**
	 * @return Number of dimensions 1..3
	 */
	public int getTextureDimension() {
		return dim;
	}

	/**
	 * @return Image dimensions
	 */
	public Dimensions getSize() {
		return size;
	}

	/**
	 * @return Whether the texture has an alpha channel
	 */
	public boolean isTranslucent() {
		return alpha;
	}

	/**
	 * Sets whether the texture has an alpha channel.
	 * @param alpha Whether texture has an alpha channel, default is <tt>true</tt>
	 */
	public void setTranslucent( boolean alpha ) {
		this.alpha = alpha;
	}

	/**
	 * @return Texture wrapping policy
	 */
	public WrapPolicy getWrapPolicy() {
		return wrap;
	}

	/**
	 * Sets the wrapping policy.
	 * @param wrap Wrap policy, default is {@link WrapPolicy#REPEAT}
	 */
	public void setWrapPolicy( WrapPolicy wrap ) {
		Check.notNull( wrap );
		this.wrap = wrap;
	}


	/**
	 * @return Whether the texture is mip-mapped
	 */
	public boolean isMipMapped() {
		return mipmap;
	}

	/**
	 * Sets whether the texture is mip-mapped.
	 * @param mipmap Whether texture is mip-mapped, default is <tt>true</tt>
	 */
	public void setMipMapped( boolean mipmap ) {
		this.mipmap = mipmap;
	}

	/**
	 * @return Minification filter
	 */
	public Filter getMinificationFilter() {
		return min;
	}

	/**
	 * Sets the wrapping policy.
	 * @param min Minification filter, default is {@link Filter#LINEAR}
	 */
	public void setMinificationFilter( Filter min ) {
		Check.notNull( min );
		this.min = min;
	}

	/**
	 * @return Magnification filter
	 */
	public Filter getMagnificationFilter() {
		return mag;
	}

	/**
	 * Sets the magnification filter.
	 * @param mag Magnification filter, default is {@link Filter#LINEAR}
	 */
	public void setMagnificationFilter( Filter mag ) {
		Check.notNull( mag );
		this.mag = mag;
	}

	@Override
	public String toString() {
		return ToString.toString( this );
	}
}
