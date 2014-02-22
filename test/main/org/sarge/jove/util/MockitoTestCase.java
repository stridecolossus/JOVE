package org.sarge.jove.util;

import org.junit.Before;
import org.mockito.MockitoAnnotations;

/**
 * Base-class for Mockito test cases.
 * @author Sarge
 */
public abstract class MockitoTestCase {
	@Before
	public final void initMocks() {
		MockitoAnnotations.initMocks( this );
	}

	/**
	 * Asserts the floating-point value is as expected.
	 * @see MathsUtil#EPSILON
	 * @param expected		Expected value
	 * @param value			Actual value
	 */
	public static void assertFloatEquals( float expected, float value ) {
		org.junit.Assert.assertEquals( expected, value, MathsUtil.EPSILON );
	}
}
