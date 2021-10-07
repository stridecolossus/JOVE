package org.sarge.jove.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

public class NativeObjectTest {
	private NativeObject obj;
	private Pointer ptr;

	@BeforeEach
	void before() {
		ptr = new Pointer(1);
		obj = mock(NativeObject.class);
		when(obj.handle()).thenReturn(new Handle(ptr));
	}

	@Test
	void toArray() {
		final Memory mem = NativeObject.toArray(List.of(obj, obj));
		assertNotNull(mem);
		assertEquals(Native.POINTER_SIZE * 2, mem.size());
		verify(obj, times(2)).handle();
	}

	@Test
	void toArrayEmpty() {
		assertEquals(null, NativeObject.toArray(Set.of()));
	}

	@Nested
	class ConverterTests {
		@Test
		void nativeType() {
			assertEquals(Pointer.class, NativeObject.CONVERTER.nativeType());
		}

		@Test
		void toNative() {
			assertEquals(ptr, NativeObject.CONVERTER.toNative(obj, null));
			assertEquals(null, NativeObject.CONVERTER.toNative(null, null));
		}

		@Test
		void fromNative() {
			assertThrows(UnsupportedOperationException.class, () -> NativeObject.CONVERTER.fromNative(obj, null));
			assertThrows(UnsupportedOperationException.class, () -> NativeObject.CONVERTER.fromNative(null, null));
		}
	}
}
