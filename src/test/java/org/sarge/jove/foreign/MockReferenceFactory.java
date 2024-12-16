package org.sarge.jove.foreign;

import java.lang.foreign.MemorySegment;

import org.sarge.jove.common.Handle;

public class MockReferenceFactory extends NativeReference.Factory {
//	public IntegerReference integer = new IntegerReference() {
//		@Override
//		public int value() {
//			return 1;
//		}
//	};
//
//	public PointerReference pointer = new PointerReference();
//
//	@Override
//	public IntegerReference integer() {
//		return integer;
//	}
//
//	@Override
//	public PointerReference pointer() {
//		return pointer;
//	}

	@Override
	public NativeReference<Integer> integer() {
		return new NativeReference<>() {
            @Override
            public Integer get() {
            	return null;
            }

			@Override
			protected void update(MemorySegment address) {
			}
		};
//		return integer;
	}

	@Override
	public NativeReference<Handle> pointer() {
		return null;
//		return pointer;
	}
}
