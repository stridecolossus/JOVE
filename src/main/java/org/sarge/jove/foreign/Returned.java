package org.sarge.jove.foreign;

import java.lang.annotation.*;

/**
 * The <i>returned</i> annotation denotes a parameter that is returned <i>by reference</i> from a native method.
 * @see NativeTransformer#update()
 * @author Sarge
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Returned {
	// Marker
}
