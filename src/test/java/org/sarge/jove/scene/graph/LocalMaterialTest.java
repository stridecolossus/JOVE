package org.sarge.jove.scene.graph;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.*;
import org.sarge.jove.scene.graph.*;

public class LocalMaterialTest {
	private LocalMaterial local;
	private Material mat;

	@BeforeEach
	void before() {
		local = new LocalMaterial();
		mat = mock(Material.class);
	}

	@DisplayName("A new local material...")
	@Nested
	class Inherited {
		@DisplayName("inherits from its parent by default")
		@Test
		void inherited() {
			assertEquals(true, local.isInherited());
		}

		@DisplayName("is in an undefined state")
		@Test
		void undefined() {
			assertEquals(true, local.isDirty());
			assertThrows(IllegalStateException.class, () -> local.material());
		}

		@DisplayName("can be overridden with a given material")
		@Test
		void set() {
			local.set(mat);
			assertEquals(false, local.isDirty());
			assertEquals(false, local.isInherited());
			assertEquals(mat, local.material());
		}

		@DisplayName("can be updated to inherit from its parent")
		@Test
		void update() {
			final LocalMaterial parent = new LocalMaterial();
			parent.set(mat);
			local.update(parent);
			assertEquals(false, local.isDirty());
			assertEquals(true, local.isInherited());
			assertEquals(mat, local.material());
		}

		@DisplayName("cannot inherit from an empty parent")
		@Test
		void empty() {
			assertThrows(IllegalStateException.class, () -> local.update(null));
		}

		@DisplayName("cannot inherit from an undefined parent")
		@Test
		void invalid() {
			final LocalMaterial parent = new LocalMaterial();
			assertThrows(IllegalStateException.class, () -> local.update(parent));
		}

		@DisplayName("can be cloned")
		@Test
		void copy() {
			final LocalMaterial copy = new LocalMaterial(local);
			assertEquals(true, copy.isDirty());
		}
	}

	@DisplayName("An overridden local material...")
	@Nested
	class Overidden {
		@BeforeEach
		void before() {
			local.set(mat);
		}

		@DisplayName("is in a defined state")
		@Test
		void material() {
			assertEquals(mat, local.material());
		}

		@DisplayName("can be set to a different material")
		@Test
		void change() {
			final Material other = mock(Material.class);
			local.set(other);
			assertEquals(false, local.isDirty());
			assertEquals(false, local.isInherited());
			assertEquals(other, local.material());
		}

		@DisplayName("can be modified to inherit its material")
		@Test
		void inherit() {
			local.inherit();
			assertEquals(true, local.isDirty());
			assertEquals(true, local.isInherited());
		}

		@DisplayName("cannot be updated")
		@Test
		void update() {
			final LocalMaterial parent = new LocalMaterial();
			parent.set(mat);
			assertThrows(AssertionError.class, () -> local.update(parent));
			assertThrows(AssertionError.class, () -> local.update(null));
		}

		@DisplayName("can be cloned")
		@Test
		void copy() {
			final LocalMaterial copy = new LocalMaterial(local);
			assertEquals(false, copy.isDirty());
			assertEquals(mat, copy.material());
		}
	}
}
