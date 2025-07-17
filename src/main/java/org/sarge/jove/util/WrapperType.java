package org.sarge.jove.util;

import static java.util.stream.Collectors.toMap;

import java.util.*;
import java.util.function.Function;

public record WrapperType<T>(Class<T> wrapper, Class<?> primitive, T value) {

	public static final Collection<WrapperType<?>> WRAPPERS = List.of(
			new WrapperType<>(Boolean.class, Boolean.TYPE, Boolean.TRUE),
			new WrapperType<>(Integer.class, Integer.TYPE, Integer.valueOf(0))
	);

	public static final Map<Class<?>, WrapperType> PRIMITIVES = WRAPPERS.stream().collect(toMap(WrapperType::primitive, Function.identity()));
}
