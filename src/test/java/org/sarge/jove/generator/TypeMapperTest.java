package org.sarge.jove.generator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.StringReader;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.generator.TypeMapper.Loader;

public class TypeMapperTest {
	private static final String NAME = "name";
	private static final String TARGET = "String";

	private TypeMapper mapper;

	@BeforeEach
	public void before() {
		mapper = new TypeMapper();
	}

	@Test
	public void get() {
		assertEquals(null, mapper.get(NAME));
	}

	@Test
	public void add() {
		mapper.add(NAME, TARGET);
		assertEquals(TARGET, mapper.get(NAME));
	}

	@Test
	public void alias() {
		mapper.add(NAME, TARGET);
		mapper.add(NAME, "ignored");
		assertEquals(TARGET, mapper.get(NAME));
	}

	@Nested
	class LoaderTests {
		private Loader loader;

		@BeforeEach
		public void before() {
			loader = new Loader();
		}

		@Test
		public void load() throws IOException {
			mapper = loader.load(new StringReader("void* -> " + TypeMapper.POINTER_CLASS_NAME));
			assertEquals(TypeMapper.POINTER_CLASS_NAME, mapper.get("void*"));
		}

		@Test
		public void loadMissingType() throws IOException {
			assertThrows(IllegalArgumentException.class, () -> loader.load(new StringReader("-> type")));
		}

		@Test
		public void loadMissingMapping() throws IOException {
			assertThrows(IllegalArgumentException.class, () -> loader.load(new StringReader("name ->")));
		}

		@Test
		public void loadInvalidMapping() throws IOException {
			assertThrows(IllegalArgumentException.class, () -> loader.load(new StringReader("name type")));
		}
	}
}
