package org.sarge.jove.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class JoveUtilsTest {
	public static final int CONSTANT = 42;

	@Test
	public void mapIntegerConstant() throws Exception {
		assertEquals( "CONSTANT", JoveUtils.mapIntegerConstant( JoveUtilsTest.class, 42 ) );
		assertEquals( null, JoveUtils.mapIntegerConstant( JoveUtilsTest.class, 999 ) );
	}
}
