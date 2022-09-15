package org.sarge.jove.util;

import java.util.Objects;

/**
 * Utility class for floating-point implementations of common function stereotypes.
 * @author Sarge
 */
public final class FloatSupport {
	private FloatSupport() {
	}

	/**
	 * Supplier for a floating-point value.
	 */
	@FunctionalInterface
	public interface FloatSupplier {
	    /**
	     * @return Floating-point value
	     */
		float get();
	}

	/**
	 * Consumer of a floating-point value.
	 */
	@FunctionalInterface
	public interface FloatConsumer {
	    /**
	     * Consumes the given value.
	     * @param f Floating-point value to consume
	     */
		void accept(float f);
	}

	/**
	 * Floating-point function.
	 * @param <R> Result type
	 */
	@FunctionalInterface
	public interface FloatFunction<R> {
		/**
		 * Applies this function to the given floating-point argument.
		 * @param f Argument
		 * @return Result
		 */
		R apply(float f);
	}

	/**
	 * Floating-point unary function.
	 */
	@FunctionalInterface
	public interface FloatUnaryOperator {
		/**
		 * Applies this operator to the given floating-point value.
		 * @param f Value
		 * @return Result
		 */
	    float apply(float f);

	    /**
	     * Identity operator.
	     */
	    FloatUnaryOperator IDENTITY = f -> f;

	    /**
	     * Applies the given operator <i>before</i> this operator.
	     * @param before Operator to apply before
	     * @return Compound operator
	     */
	    default FloatUnaryOperator compose(FloatUnaryOperator before) {
	    	Objects.requireNonNull(before);
	    	return f -> apply(before.apply(f));
	    }

	    /**
	     * Applies the given operator <i>after</i> this operator.
	     * @param after Operator to apply after
	     * @return Compound operator
	     */
	    default FloatUnaryOperator then(FloatUnaryOperator after) {
	    	Objects.requireNonNull(after);
	    	return f -> after.apply(apply(f));
	    }
	}
}
