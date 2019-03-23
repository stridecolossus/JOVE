package org.sarge.jove.generator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.generator.StructureGenerator.Field;

public class StuctureGeneratorTest {
	private static final String NAME = "name";
	private static final String TYPE = "int";

	private StructureGenerator generator;
	private Generator base;
	private TypeMapper mapper;

	@BeforeEach
	public void before() {
		base = mock(Generator.class);
		mapper = mock(TypeMapper.class);
		generator = new StructureGenerator(base, mapper);
	}

	@Test
	public void generate() {
		when(mapper.get(TYPE)).thenReturn(TypeMapper.POINTER_CLASS_NAME);
		final Field field = generator.field(NAME, TYPE, 0, 0);
		final List<Field> fields = List.of(field);
		final Map<String, Object> expected = Map.of(
			"fields", fields,
			"imports", Set.of(TypeMapper.POINTER_CLASS_NAME)
		);
		generator.generate(NAME, fields);
		verify(base).generate(NAME, expected);
	}

	@Test
	public void path() {
		when(mapper.get(TYPE)).thenReturn(TypeMapper.POINTER_CLASS_NAME);
		final Field field = generator.field(NAME, TYPE, 0, 0);
		assertEquals(NAME, field.getName());
		assertEquals("Pointer", field.getType());
		assertEquals(TypeMapper.POINTER_CLASS_NAME, field.getPath());
		assertEquals(0, field.getLength());
	}

	@Test
	public void mapped() {
		when(mapper.get(TYPE)).thenReturn(TYPE);
		final Field field = generator.field(NAME, TYPE, 0, 0);
		assertEquals(NAME, field.getName());
		assertEquals(TYPE, field.getType());
		assertEquals(null, field.getPath());
		assertEquals(0, field.getLength());
	}

	@Test
	public void mappedPointer() {
		when(mapper.get(TYPE)).thenReturn(TYPE);
		final Field field = generator.field(NAME, TYPE, 1, 0);
		assertEquals(NAME, field.getName());
		assertEquals(TYPE, field.getType());
		assertEquals(null, field.getPath());
		assertEquals(0, field.getLength());
	}

	@Test
	public void mappedArray() {
		when(mapper.get(TYPE)).thenReturn(TYPE);
		final Field field = generator.field(NAME, TYPE, 0, 1);
		assertEquals(NAME, field.getName());
		assertEquals(TYPE, field.getType());
		assertEquals(null, field.getPath());
		assertEquals(1, field.getLength());
	}

	@Test
	public void unmapped() {
		final Field field = generator.field(NAME, TYPE, 0, 0);
		assertEquals(NAME, field.getName());
		assertEquals(TYPE, field.getType());
		assertEquals(null, field.getPath());
		assertEquals(0, field.getLength());
	}

	@Test
	public void unmappedArray() {
		final Field field = generator.field(NAME, TYPE, 0, 1);
		assertEquals(NAME, field.getName());
		assertEquals("int", field.getType());
		assertEquals(null, field.getPath());
		assertEquals(1, field.getLength());
	}

	@Test
	public void umappedStructure() {
		final Field field = generator.field(NAME, "Structure", 0, 0);
		assertEquals(NAME, field.getName());
		assertEquals("Structure", field.getType());
		assertEquals(null, field.getPath());
		assertEquals(0, field.getLength());
	}

	@Test
	public void umappedPointerToStructure() {
		final Field field = generator.field(NAME, "Structure", 1, 0);
		assertEquals(NAME, field.getName());
		assertEquals("Structure.ByReference", field.getType());
		assertEquals(null, field.getPath());
		assertEquals(0, field.getLength());
	}
}
