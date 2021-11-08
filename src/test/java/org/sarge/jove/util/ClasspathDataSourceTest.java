package org.sarge.jove.util;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.FileNotFoundException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ClasspathDataSourceTest {
	private static final String PATH = ClasspathDataSource.class.getName().replace('.', '/') + ".class";

	private ClasspathDataSource src;

	@BeforeEach
	void before() {
		src = new ClasspathDataSource();
	}

	@Test
	void input() throws Exception {
		src.input(PATH);
	}

	@Test
	void inputPrepended() throws Exception {
		src.input("/" + PATH);
	}

	@Test
	void inputInvalidPath() throws Exception {
		assertThrows(FileNotFoundException.class, () -> src.input("cobblers"));
	}

	@Test
	void output() {
		assertThrows(UnsupportedOperationException.class, () -> src.output(null));
	}
}
