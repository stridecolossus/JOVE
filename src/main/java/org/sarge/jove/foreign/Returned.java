package org.sarge.jove.foreign;

import java.lang.annotation.*;

/**
 * The <i>returned</i> annotation denotes a method parameter that is passed <i>by reference</i> to the native layer.
 * @author Sarge
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Returned {
	// Marker
}
