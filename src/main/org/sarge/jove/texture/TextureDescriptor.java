package org.sarge.jove.texture;

/**
 * Descriptor for texture parameters.
 * @author Sarge
 */
public interface TextureDescriptor {
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

	/**
	 * @return Number of dimensions 1..3
	 */
	int getDimensions();

	/**
	 * @return Width
	 */
	int getWidth();

	/**
	 * @return Height
	 */
	int getHeight();

	/**
	 * @return Whether the texture has an alpha channel
	 */
	boolean isTranslucent();

	/**
	 * @return Texture wrapping policy
	 */
	WrapPolicy getWrapPolicy();

	/**
	 * @return Whether the texture is mip-mapped
	 */
	boolean isMipMapped();

	/**
	 * @return Minification filter
	 */
	Filter getMinificationFilter();

	/**
	 * @return Magnification filter
	 */
	Filter getMagnificationFilter();
}
