package org.sarge.jove.foreign;

import static java.lang.foreign.ValueLayout.JAVA_INT;

import java.lang.foreign.MemorySegment;
import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * An <i>integer reference</i> maps to a native integer-by-reference value.
 * @author Sarge
 */
public class IntegerReference {
	private MemorySegment address;
	private int value;

	/**
	 * @return Integer value
	 */
	public int value() {
		return value;
	}

	/**
	 * Sets this integer reference.
	 * @param value Integer reference
	 */
	void set(int value) {
		this.value = value;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(address);
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof IntegerReference that) &&
				Objects.equals(this.address, that.address);
	}

	@Override
	public String toString() {
		return String.format("IntegerReference[%d]", value());
	}

	/**
	 * Native mapper for an integer reference.
	 */
	public static class IntegerReferenceMapper extends AbstractNativeMapper<IntegerReference, MemorySegment> {
		@Override
		public Class<IntegerReference> type() {
			return IntegerReference.class;
		}

		@Override
		public MemorySegment marshal(IntegerReference ref, NativeContext context) {
			if(ref.address == null) {
				ref.address = context.allocator().allocate(JAVA_INT);
			}

			ref.address.set(JAVA_INT, 0, ref.value);

			return ref.address;
		}

		@Override
		public MemorySegment marshalNull(Class<? extends IntegerReference> target) {
			throw new UnsupportedOperationException();
		}

		@Override
		public BiConsumer<MemorySegment, IntegerReference> unmarshal(Class<? extends IntegerReference> target) {
			return (address, ref) -> {
				final int value = address.get(JAVA_INT, 0);
				ref.set(value);
			};
		}
	}
}
