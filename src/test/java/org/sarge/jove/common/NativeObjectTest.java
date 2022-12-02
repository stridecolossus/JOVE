package org.sarge.jove.common;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.jupiter.api.*;

import com.sun.jna.*;

class NativeObjectTest {
	private NativeObject obj;

	@BeforeEach
	void before() {
		obj = mock(NativeObject.class);
		when(obj.handle()).thenReturn(new Handle(1));
	}

	@Nested
	class HandleArrayTests {
		private Memory mem;

		@BeforeEach
		void before() {
			mem = NativeObject.array(List.of(obj, obj));
		}

		@DisplayName("An array of native objects can be transformed to a JNA pointer-to-pointer-array")
		@Test
		void array() {
			assertEquals(Native.POINTER_SIZE * 2, mem.size());
			verify(obj, times(2)).handle();
		}

		@DisplayName("An empty array of native objects is transformed to NULL")
		@Test
		void empty() {
			assertEquals(null, NativeObject.array(Set.of()));
		}
	}

	@Nested
	class ConverterTests {
		@DisplayName("A native object is marshalled as a JNA pointer")
		@Test
		void nativeType() {
			assertEquals(Pointer.class, NativeObject.CONVERTER.nativeType());
		}

		@DisplayName("A native object can be marshalled to a JNA pointer")
		@Test
		void toNative() {
			assertEquals(new Pointer(1), NativeObject.CONVERTER.toNative(obj, null));
			assertEquals(null, NativeObject.CONVERTER.toNative(null, null));
		}

		@DisplayName("A native object cannot be marshalled from a JNA pointer")
		@Test
		void fromNative() {
			assertThrows(UnsupportedOperationException.class, () -> NativeObject.CONVERTER.fromNative(obj, null));
			assertThrows(UnsupportedOperationException.class, () -> NativeObject.CONVERTER.fromNative(null, null));
		}
	}
}
