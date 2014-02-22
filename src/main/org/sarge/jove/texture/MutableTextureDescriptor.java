package org.sarge.jove.texture;

import org.sarge.lib.util.Check;
import org.sarge.lib.util.ToString;

/**
 * Mutable implementation.
 * @author Sarge
 */
public class MutableTextureDescriptor implements TextureDescriptor {
	private final int w, h;
	private final int dim;

	private boolean alpha = true;
	private WrapPolicy wrap = WrapPolicy.REPEAT;
	private boolean mipmap = true;
	private Filter min = Filter.LINEAR;
	private Filter mag = Filter.LINEAR;

	/**
	 * Constructor for a cube-map.
	 * @param w Width
	 * @param h Height
	 * @param cube Whether the texture is a cube-map
	 */
	public MutableTextureDescriptor( int w, int h, boolean cube ) {
		this.w = w;
		this.h = h;
		this.dim = cube ? 3 : 2;
	}

	/**
	 * Constructor for a 2D texture.
	 * @param w Width
	 * @param h Height
	 */
	public MutableTextureDescriptor( int w, int h ) {
		this( w, h, false );
	}

	/**
	 * Constructor for 1D texture.
	 * @param w width
	 */
	public MutableTextureDescriptor( int w ) {
		this.w = w;
		this.h = 0;
		this.dim = 1;
	}

	@Override
	public int getDimensions() {
		return dim;
	}

	@Override
	public int getWidth() {
		return w;
	}

	@Override
	public int getHeight() {
		return h;
	}

	@Override
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

	@Override
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

	@Override
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

	@Override
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

	@Override
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
