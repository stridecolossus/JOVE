package org.sarge.jove.foreign;

import static java.lang.foreign.ValueLayout.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.foreign.ValueLayout;
import java.util.*;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class PrimitiveTest {
	public static Set<Map.Entry<Class<?>, ValueLayout>> primitive() {
		final Map<Class<?>, ValueLayout> primitives = Map.of(
	    		boolean.class,	JAVA_BOOLEAN,
	    		byte.class,		JAVA_BYTE,
	    		char.class,		JAVA_CHAR,
	    		short.class,	JAVA_SHORT,
	    		int.class,		JAVA_INT,
	    		long.class,		JAVA_LONG,
	    		float.class,	JAVA_FLOAT,
	    		double.class,	JAVA_DOUBLE
	    );
		return primitives.entrySet();
	}

	@ParameterizedTest
	@MethodSource
	void primitive(Map.Entry<Class<?>, ValueLayout> entry) {
		final Class<?> type = entry.getKey();
		final ValueLayout layout = entry.getValue();
		final Primitive primitive = Primitive.primitives().get(type);
		assertEquals(layout, primitive.layout());
	}
}
