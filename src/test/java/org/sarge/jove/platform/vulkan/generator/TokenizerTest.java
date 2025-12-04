package org.sarge.jove.platform.vulkan.generator;

import static org.junit.jupiter.api.Assertions.*;

import java.util.NoSuchElementException;

import org.junit.jupiter.api.*;

class TokenizerTest {
	private Tokenizer tokenizer;

	@BeforeEach
	void before() {
		tokenizer = new Tokenizer("key = 42;");
	}

	@Nested
	class Available {
		@Test
		void hasNext() {
			assertEquals(true, tokenizer.hasNext());
		}

		@Test
		void next() {
			assertEquals("key", tokenizer.next());
			assertEquals("=", tokenizer.next());
			assertEquals("42", tokenizer.next());
			assertEquals(";", tokenizer.next());
			assertEquals(false, tokenizer.hasNext());
		}

		@Test
		void peek() {
			assertEquals(false, tokenizer.peek("other"));
			assertEquals(true, tokenizer.peek("key"));
		}

		@Test
		void skip() {
			tokenizer.skip("key");
			assertEquals("=", tokenizer.next());
		}

		@Test
		void mismatch() {
			assertThrows(IllegalArgumentException.class, () -> tokenizer.skip("cobblers"));
		}
	}

	@Nested
	class Exhausted {
		@BeforeEach
		void before() {
			while(tokenizer.hasNext()) {
				tokenizer.next();
			}
		}

		@Test
		void hasNext() {
			assertEquals(false, tokenizer.hasNext());
		}

		@Test
		void next() {
			assertThrows(NoSuchElementException.class, () -> tokenizer.next());
		}

		@Test
		void peek() {
			assertThrows(NoSuchElementException.class, () -> tokenizer.peek("whatever"));
		}

		@Test
		void skip() {
			assertThrows(NoSuchElementException.class, () -> tokenizer.skip("whatever"));
		}
	}

	@Nested
	class Numeric {
		@Test
		void integer() {
			final var integer = new Tokenizer("42");
			assertEquals(42, integer.integer(null));
		}

		@Test
		void negative() {
			final var integer = new Tokenizer("-42");
			assertEquals(-42, integer.integer(null));
		}

		@Test
		void zero() {
			final var integer = new Tokenizer("0");
			assertEquals(0, integer.integer(null));
		}

		@Test
		void hexadecimal() {
			final var hex = new Tokenizer("0x2a");
			assertEquals(42, hex.integer(null));
		}

		@Test
		void octal() {
			final var octal = new Tokenizer("0o52");
			assertEquals(42, octal.integer(null));
		}

		@Test
		void binary() {
			final var binary = new Tokenizer("0b_0010_1010");
			assertEquals(42, binary.integer(null));
		}

		@Test
		void invalid() {
			final var invalid = new Tokenizer("3zzz");
			assertThrows(NumberFormatException.class, () -> invalid.integer(null));
		}

		@Test
		void radix() {
			final var invalid = new Tokenizer("0z42");
			assertThrows(NumberFormatException.class, () -> invalid.integer(null));
		}

		@Test
		void mapper() {
			final var tokenizer = new Tokenizer("whatever");
			assertEquals(42, tokenizer.integer(_ -> 42));
		}

		@Test
		void unknown() {
			final var tokenizer = new Tokenizer("whatever");
			assertThrows(NumberFormatException.class, () -> tokenizer.integer(_ -> null));
		}
	}

	@Nested
	class Filters {
		@Test
		void comments() {
			assertEquals(false, new Tokenizer("//comment").hasNext());
		}

		@Test
		void multiline() {
			final String text = """
					/*
					 * A multiline comment.
					 */
					""";

			assertEquals(false, new Tokenizer(text).hasNext());
		}

		@Test
		void prototypes() {
			final String text = """
					#ifndef VK_NO_PROTOTYPES
						ignored
					#endif
					""";

			assertEquals(false, new Tokenizer(text).hasNext());
		}
	}
}
