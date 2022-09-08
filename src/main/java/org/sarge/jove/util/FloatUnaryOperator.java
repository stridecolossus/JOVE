package org.sarge.jove.util;

/**
 *
 * @author Sarge
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
     *
     * nested class?
     *
     * FloatUnaryOperator
     * IntToFloatFunction?
     * FloatFunction<R>
     * FloatSupplier - done
     *
     *
     */

}

//
//    /**
//     * Returns a composed operator that first applies the {@code before}
//     * operator to its input, and then applies this operator to the result.
//     * If evaluation of either operator throws an exception, it is relayed to
//     * the caller of the composed operator.
//     *
//     * @param before the operator to apply before this operator is applied
//     * @return a composed operator that first applies the {@code before}
//     * operator and then applies this operator
//     * @throws NullPointerException if before is null
//     *
//     * @see #andThen(IntUnaryOperator)
//     */
//    default IntUnaryOperator compose(IntUnaryOperator before) {
//        Objects.requireNonNull(before);
//        return (int v) -> applyAsInt(before.applyAsInt(v));
//    }
//
//    /**
//     * Returns a composed operator that first applies this operator to
//     * its input, and then applies the {@code after} operator to the result.
//     * If evaluation of either operator throws an exception, it is relayed to
//     * the caller of the composed operator.
//     *
//     * @param after the operator to apply after this operator is applied
//     * @return a composed operator that first applies this operator and then
//     * applies the {@code after} operator
//     * @throws NullPointerException if after is null
//     *
//     * @see #compose(IntUnaryOperator)
//     */
//    default IntUnaryOperator andThen(IntUnaryOperator after) {
//        Objects.requireNonNull(after);
//        return (int t) -> after.applyAsInt(applyAsInt(t));
//    }
//
//    /**
//     * Returns a unary operator that always returns its input argument.
//     *
//     * @return a unary operator that always returns its input argument
//     */
//    static IntUnaryOperator identity() {
//        return t -> t;
//    }
//}
