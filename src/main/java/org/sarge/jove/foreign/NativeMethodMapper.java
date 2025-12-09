package org.sarge.jove.foreign;

import static java.util.Objects.requireNonNull;

import java.lang.foreign.*;
import java.lang.reflect.*;
import java.util.*;

/**
 * The <i>native method helper</i> is a helper used to map a method signature to transformers.
 * @author Sarge
 */
class NativeMethodMapper {
	private final Registry registry;

	/**
	 * Constructor.
	 * @param registry Transformer registry
	 */
	public NativeMethodMapper(Registry registry) {
		this.registry = requireNonNull(registry);
	}

	/**
	 * Maps the return type of the given method.
	 * @param method Method
	 * @return Return type transformer or {@code null} for a {@code void} method
	 */
	@SuppressWarnings("rawtypes")
	public Transformer returns(Method method) {
		final Class<?> type = method.getReturnType();

		if(type == void.class) {
			return null;
		}

		return registry
				.transformer(type)
				.orElseThrow(() -> new IllegalArgumentException("Unsupported return type: " + method));
	}

	/**
	 * Maps the parameters of the given method.
	 * @param method Method
	 * @return Parameter transformers
	 * @see #parameter(Parameter)
	 */
	@SuppressWarnings("rawtypes")
	public List<Transformer> parameters(Method method) {
		return Arrays
				.stream(method.getParameters())
				.map(this::parameter)
				.toList();
	}

	/**
	 * Determines the native transformer for the given method parameter.
	 * @param parameter Method parameter
	 * @return Native parameter
	 */
	@SuppressWarnings("rawtypes")
	protected Transformer parameter(Parameter parameter) {
		return registry
				.transformer(parameter.getType())
				.orElseThrow(() -> new IllegalArgumentException("Unsupported parameter type: " + parameter));
	}

	/**
	 * Helper.
	 * Derives the FFM function descriptor from the given native method signature.
	 * @param returns		Optional return value transformer
	 * @param parameters	Parameter transformers
	 * @returns Function descriptor
	 */
	@SuppressWarnings("rawtypes")
	public static FunctionDescriptor descriptor(Transformer returns, List<Transformer> parameters) {
		// Map method signature to FFM layout
		final MemoryLayout[] layouts = parameters
				.stream()
				.map(Transformer::layout)
				.toArray(MemoryLayout[]::new);

		// Append return layout
		if(returns == null) {
			return FunctionDescriptor.ofVoid(layouts);
		}
		else {
			return FunctionDescriptor.of(returns.layout(), layouts);
		}
	}
}
