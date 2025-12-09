package org.sarge.jove.foreign;

import java.lang.annotation.*;

/**
 * The <i>updated</i> annotation denotes a <i>by reference</i> parameter that is updated after invocation of a native method.
 * @author Sarge
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Updated {
	// Marker
}
