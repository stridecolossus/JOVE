package org.sarge.jove.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Iterator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Layout.CompoundLayout;
import org.sarge.jove.common.Layout.MutableCompoundLayout;

public class LayoutTest {
	private Layout layout;

	@BeforeEach
	void before() {
		layout = new Layout(3, Float.class, true);
	}

	@Test
	void constructor() {
		assertEquals(3, layout.size());
		assertEquals(Float.class, layout.type());
		assertEquals(Float.BYTES, layout.bytes());
		assertEquals(true, layout.signed());
	}

	@Test
	void length() {
		assertEquals(3 * Float.BYTES, layout.length());
	}

	@Test
	void of() {
		assertEquals(layout, Layout.of(3));
	}

	@Test
	void equals() {
		assertEquals(true, layout.equals(layout));
		assertEquals(true, layout.equals(Layout.of(3)));
		assertEquals(false, layout.equals(null));
		assertEquals(false, layout.equals(Layout.of(2)));
	}

	@Nested
	class BytesTests {
		@Test
		void floats() {
			assertEquals(Float.BYTES, Layout.bytes(Float.class));
			assertEquals(Float.BYTES, Layout.bytes(float.class));
		}

		@Test
		void integers() {
			assertEquals(Integer.BYTES, Layout.bytes(Integer.class));
			assertEquals(Integer.BYTES, Layout.bytes(int.class));
		}

		@Test
		void shorts() {
			assertEquals(Short.BYTES, Layout.bytes(Short.class));
			assertEquals(Short.BYTES, Layout.bytes(short.class));
		}

		@Test
		void bytes() {
			assertEquals(Byte.BYTES, Layout.bytes(Byte.class));
			assertEquals(Byte.BYTES, Layout.bytes(Byte.class));
		}

		@Test
		void unsupported() {
			assertThrows(IllegalArgumentException.class, () -> Layout.bytes(String.class));
		}
	}

	@Nested
	class CompoundLayoutTests {
		private CompoundLayout compound;
		private Layout other;

		@BeforeEach
		void before() {
			other = Layout.of(3);
			compound = CompoundLayout.of(layout, other);
		}

		@Test
		void constructor() {
			assertEquals(2, compound.size());
		}

		@Test
		void stride() {
			assertEquals((3 + 3) * Float.BYTES, compound.stride());
		}

		@Test
		void contains() {
			assertEquals(true, compound.contains(layout));
			assertEquals(true, compound.contains(other));
		}

		@Test
		void containsIdentity() {
			assertEquals(false, compound.contains(Layout.of(3)));
		}

		@Test
		void iterator() {
			final Iterator<Layout> iterator = compound.iterator();
			assertNotNull(iterator);
			assertEquals(true, iterator.hasNext());
			assertEquals(layout, iterator.next());
			iterator.next();
			assertEquals(false, iterator.hasNext());
		}

		@Test
		void equals() {
			assertEquals(true, compound.equals(compound));
			assertEquals(true, compound.equals(CompoundLayout.of(layout, other)));
			assertEquals(false, compound.equals(null));
			assertEquals(false, compound.equals(CompoundLayout.of(layout, Layout.of(3))));
		}
	}

	@Nested
	class MutableCompoundLayoutTests {
		private MutableCompoundLayout compound;

		@BeforeEach
		void before() {
			compound = new MutableCompoundLayout();
		}

		@Test
		void constructor() {
			assertEquals(0, compound.size());
			assertEquals(0, compound.stride());
			assertEquals(false, compound.iterator().hasNext());
		}

		@Test
		void add() {
			compound.add(layout);
			compound.add(Layout.of(3));
			assertEquals(2, compound.size());
			assertEquals((3 + 3) * Float.BYTES, compound.stride());
			assertEquals(true, compound.contains(layout));
		}

		@Test
		void addDuplicate() {
			compound.add(layout);
			assertThrows(IllegalArgumentException.class, () -> compound.add(layout));
		}
	}
}
