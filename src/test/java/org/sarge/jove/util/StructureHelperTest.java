package org.sarge.jove.util;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.sarge.jove.util.TestHelper.assertFloatEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

public class StructureHelperTest {
	@FieldOrder({"value"})
	public static class MockStructure extends Structure {
		public int value;

		public MockStructure(int value) {
			this.value = value;
		}

		public MockStructure() {
			this(0);
		}
	}

	@FieldOrder({"mock", "empty"})
	public class Compound extends Structure {
		public MockStructure mock;
		public MockStructure empty;
	}

	@Test
	public void structures() {
		final MockStructure one = new MockStructure(1);
		final MockStructure two = new MockStructure(2);
		final Memory mem = StructureHelper.structures(List.of(one, two));
		assertNotNull(mem);
		assertEquals(2 * 4, mem.size());
		assertEquals(1, mem.getInt(0));
		assertEquals(2, mem.getInt(4));
	}

	@Test
	public void pointers() {
		final Pointer ptr = new Pointer(42);
		final Memory mem = StructureHelper.pointers(List.of(ptr, ptr));
		assertNotNull(mem);
		assertEquals(2 * 8, mem.size());
		assertEquals(ptr, mem.getPointer(0));
		assertEquals(ptr, mem.getPointer(8));
	}

	@Test
	public void floats() {
		final Memory mem = StructureHelper.floats(new float[]{1, 2});
		assertNotNull(mem);
		assertEquals(2 * 4, mem.size());
		assertFloatEquals(1, mem.getFloat(0));
		assertFloatEquals(2, mem.getFloat(4));
	}

	@Test
	public void integers() {
		final Memory mem = StructureHelper.integers(new int[]{1, 2});
		assertNotNull(mem);
		assertEquals(2 * 4, mem.size());
		assertFloatEquals(1, mem.getInt(0));
		assertFloatEquals(2, mem.getInt(4));
	}

//	@Test
//	public void fields() throws Exception {
//		final MockStructure struct = new MockStructure(1);
//		final Field field = MockStructure.class.getDeclaredField("value");
//		assertArrayEquals(new Field[]{field}, StructureHelper.fields(struct).toArray());
//	}
//
//	@Test
//	public void copy() {
//		final Compound src = new Compound();
//		final Compound dest = new Compound();
//		StructureHelper.copy(src, dest);
//		assertEquals(true, src.dataEquals(dest));
//	}
}
