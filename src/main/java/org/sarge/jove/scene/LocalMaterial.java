package org.sarge.jove.scene;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.lib.util.Check;

/**
 * A <i>local material</i> specifies the material for a node and its descendants that inherit this property.
 * @see Material#NONE
 * @author Sarge
 */
public class LocalMaterial extends InheritedProperty<LocalMaterial> {
	private Material local;
	private transient Material mat = Material.NONE;

	/**
	 * Constructor.
	 */
	LocalMaterial() {
	}

	/**
	 * Copy constructor.
	 * @param that Local material to copy
	 */
	LocalMaterial(LocalMaterial that) {
		this.local = that.local;
		this.mat = that.mat;
	}

	@Override
	public boolean isDirty() {
		return mat == Material.NONE;
	}

	/**
	 * @return Whether this local material inherits from its ancestors
	 * @see #inherit()
	 */
	public boolean isInherited() {
		return local == null;
	}

	/**
	 * @return Actual material at this node
	 * @throws IllegalStateException if this material is {@link #isInherited()} but has not been updated
	 * @see #update(LocalMaterial)
	 */
	public Material material() {
		if(isDirty()) throw new IllegalStateException("Local material has not been updated: " + this);
		return mat;
	}

	/**
	 * Sets the material at this node.
	 * @param mat Material
	 */
	public void set(Material mat) {
		Check.notNull(mat);
		this.local = mat;
		this.mat = mat;
	}

	/**
	 * Sets this material to inherit from its parent.
	 */
	public void inherit() {
		this.local = null;
		this.mat = Material.NONE;
	}

	/**
	 * Updates this inherited material.
	 * @param parent Parent material to inherit
	 * @throws AssertionError if this material is not {@link #isDirty()} or is not {@link #isInherited()}
	 * @throws IllegalStateException if {@link #parent} is {@code null} or undefined
	 */
	@Override
	void update(LocalMaterial parent) {
		assert isDirty();
		assert isInherited();
		if(parent == null) throw new IllegalStateException("No ancestor to inherit: " + this);
		mat = parent.material();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("local", local)
				.append("mat", mat)
				.build();
	}
}

