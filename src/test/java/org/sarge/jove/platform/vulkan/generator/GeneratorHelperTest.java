package org.sarge.jove.platform.vulkan.generator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

class GeneratorHelperTest {
	@Test
	void empty() {
		assertEquals(List.of(), GeneratorHelper.splitByCase(""));
	}

	@Test
	void same() {
		assertEquals(List.of("lower"), GeneratorHelper.splitByCase("lower"));
		assertEquals(List.of("UPPER"), GeneratorHelper.splitByCase("UPPER"));
		assertEquals(List.of("123"), GeneratorHelper.splitByCase("123"));
	}

	@Test
	void capitalised() {
		assertEquals(List.of("One", "Two"), GeneratorHelper.splitByCase("OneTwo"));
	}

	@Test
	void numeric() {
		assertEquals(List.of("Name", "123"), GeneratorHelper.splitByCase("Name123"));
	}

	@Test
	void capitals() {
		assertEquals(List.of("Name", "EXT"), GeneratorHelper.splitByCase("NameEXT"));
		assertEquals(List.of("EXT", "Name"), GeneratorHelper.splitByCase("EXTName"));
	}

	@Test
	void snake() {
		assertEquals("ONE_TWO", Stream.of("One", "Two").collect(GeneratorHelper.snake()));
	}
}
