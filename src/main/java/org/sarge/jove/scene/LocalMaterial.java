package org.sarge.jove.scene;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.lib.util.Check;

/**
 *
 * @author Sarge
 */
class LocalMaterial {
	private final Material local;
	private Material mat;

	/**
	 * Default constructor for a node that inherits its material.
	 */
	LocalMaterial() {
		this.local = null;
	}

	/**
	 * Constructor.
	 * @param mat Material
	 */
	LocalMaterial(Material mat) {
		Check.notNull(mat);
		this.local = mat;
		this.mat = mat;
	}

	/**
	 * @return Material
	 */
	public Material material() {
		return mat;
	}

	/**
	 * Updates this material.
	 * @param parent Inherited material
	 */
	private void update(Material parent) {
		if(local == null) {
			mat = parent;
		}
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("local", local)
				.append("mat", mat)
				.build();
	}

	/**
	 *
	 */
	public static class MaterialVisitor {
		private Material parent;

		/**
		 * Updates the given local material.
		 * @param local Local material
		 */
		public void update(LocalMaterial local) {
			local.update(parent);
			parent = local.mat;
		}
	}
}
