package org.sarge.jove.scene;

import static org.sarge.lib.util.Check.notNull;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.Frame;
import org.sarge.jove.material.IntegerPropertyBinder;
import org.sarge.jove.material.Material;

/**
 * The <i>render context</i>
 * TODO
 * @author Sarge
 */
public class RenderContext {
	private final Frame frame;

	/**
	 * Constructor.
	 * @param frame Current frame
	 */
	public RenderContext(Frame frame) {
		this.frame = notNull(frame);
	}

	/**
	 * @return Current frame statistics
	 */
	public Frame frame() {
		return frame;
	}

	/**
	 * Creates a material property for the elapsed frame-time.
	 * @return Elapsed-time material property
	 * @see Frame#elapsed()
	 */
	public Material.Property elapsed() {
		final Property.Binder binder = new IntegerPropertyBinder(() -> (int) frame.elapsed());
		return new Material.Property(binder, Property.Policy.FRAME);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
