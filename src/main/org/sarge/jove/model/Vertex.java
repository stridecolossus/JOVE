package org.sarge.jove.model;

import org.sarge.jove.common.Colour;
import org.sarge.jove.common.TextureCoord;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;
import org.sarge.lib.util.Check;
import org.sarge.lib.util.EqualsBuilder;
import org.sarge.lib.util.ToString;

/**
 * Mutable vertex data.
 * @author Sarge
 */
public class Vertex {
	protected Point pos;
	protected Vector normal;
	protected Colour col;
	protected TextureCoord coords;

	/**
	 * Constructor.
	 * @param pos Vertex position
	 */
	public Vertex( Point pos ) {
		Check.notNull( pos );
		this.pos = pos;
	}

	/**
	 * TODO - remove
	 * Constructor.
	 * All components are optional except for the vertex position.
	 * @param pos		Vertex position
	 * @param normal	Normal
	 * @param col		Colour
	 * @param coords	Texture coords
	 */
	public Vertex( Point pos, Vector normal, Colour col, TextureCoord coords ) {
		Check.notNull( pos );
		this.pos = pos;
		setNormal( normal );
		setColour( col );
		setTextureCoords( coords );
	}

	/**
	 * @return Vertex position
	 */
	public Point getPosition() {
		return pos;
	}

	/**
	 * @return Vertex normal
	 */
	public Vector getNormal() {
		return normal;
	}

	/**
	 * Sets the vertex normal.
	 * @param normal Normal
	 */
	public void setNormal( Vector normal ) {
		this.normal = normal;
	}

	/**
	 * Adds the given normal to this vertex.
	 * @param n Normal to add
	 * TODO - mutable vector?
	 */
	public void addNormal( Vector n ) {
		this.normal = this.normal.add( n );
	}

	/**
	 * @return Vertex colour
	 */
	public Colour getColour() {
		return col;
	}

	/**
	 * Sets the vertex colour.
	 * @param col Vertex colour
	 */
	public void setColour( Colour col ) {
		this.col = col;
	}

	/**
	 * @return Vertex texture coordinates
	 */
	public TextureCoord getTextureCoords() {
		return coords;
	}

	/**
	 * Sets the vertex texture coordinates.
	 * @param coords Texture coordinates
	 */
	public void setTextureCoords( TextureCoord coords ) {
		this.coords = coords;
	}

	@Override
	public boolean equals( Object obj ) {
		return EqualsBuilder.equals( this, obj );
	}

	@Override
	public String toString() {
		return ToString.toString( this );
	}
}
