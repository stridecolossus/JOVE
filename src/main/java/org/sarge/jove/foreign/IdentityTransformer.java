package org.sarge.jove.foreign;

import static java.util.Objects.requireNonNull;

import java.lang.foreign.ValueLayout;

/**
 * The <i>identity transformer</i> marshals an argument as-is, i.e. without any transformation.
 * @author Sarge
 */
public record IdentityTransformer(ValueLayout layout) implements Transformer {
	/**
	 * Constructor.
	 * @param layout Memory layout
	 */
	public IdentityTransformer {
		requireNonNull(layout);
	}
}
