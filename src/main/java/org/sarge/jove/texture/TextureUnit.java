package org.sarge.jove.texture;

import org.sarge.lib.util.Check;
import org.sarge.lib.util.EqualsBuilder;
import org.sarge.lib.util.HashCodeBuilder;
import org.sarge.lib.util.ToString;

/**
 * Encapsulates a texture and associated texture unit.
 * @author Sarge
 */
public final class TextureUnit {
	private final Texture texture;
	private final int unit;

	/**
	 * Constructor.
	 * @param texture	Texture image
	 * @param unit		Texture unit index 0..7
	 */
	public TextureUnit(Texture texture, int unit) {
		Check.notNull(texture);
		Check.range(unit, 0, 7); // TODO - configurable?

		this.texture = texture;
		this.unit = unit;
	}

	/**
	 * @return Texture
	 */
	public Texture getTexture() {
		return texture;
	}

	/**
	 * @return Texture unit
	 */
	public int getTextureUnit() {
		return unit;
	}

	/**
	 * Activates the texture.
	 */
	public void activate() {
		texture.activate(unit);
	}

	/**
	 * Deactivates the texture.
	 */
	public void reset() {
		texture.reset(unit);
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.equals(this, obj);
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.hashCode(this);
	}

	@Override
	public String toString() {
		return ToString.toString(this);
	}
}
