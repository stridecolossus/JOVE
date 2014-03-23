package org.sarge.jove.terrain;

/**
 * Terrain height-map data.
 * @author Sarge
 */
public interface HeightMap {
	/**
	 * @return Width
	 */
	int getWidth();

	/**
	 * @return Height
	 */
	int getHeight();

	/**
	 * @param x
	 * @param y
	 * @return Height at given coordinate
	 */
	float getHeight( int x, int y );
}
