package org.sarge.jove.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.sarge.jove.util.TestHelper.assertThrows;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.util.Element.ElementException;

public class ElementTest {
	private static final String PARENT = "parent";
	private static final String CHILD = "child";

	private Element parent, child;

	@BeforeEach
	void before() {
		parent = new Element(PARENT);
		child = new Element(CHILD);
	}

	@Test
	void constructor() {
		// Check element properties
		assertEquals(PARENT, parent.name());
		assertEquals(null, parent.parent());
		assertEquals(Optional.empty(), parent.text());
		assertEquals(Map.of(), parent.attributes());
		assertEquals(true, parent.isRoot());
		assertEquals(0, parent.index());

		// Check children
		assertNotNull(parent.children());
		assertEquals(0, parent.children().count());

		// Check path
		assertNotNull(parent.path());
		assertArrayEquals(new Element[]{parent}, parent.path().toArray());
	}

	@Test
	void attribute() {
		final Element e = new Element.Builder()
				.name(PARENT)
				.attribute("attr", "42")
				.build();

		final Element.Attribute attr = e.attribute("attr");
		assertNotNull(attr);
		assertEquals(true, attr.isPresent());
		assertEquals(42, attr.toInteger());
	}

	@Test
	void attributeNotPresent() {
		final Element.Attribute attr = parent.attribute("cobblers");
		assertNotNull(attr);
		assertEquals(false, attr.isPresent());
		assertEquals(42, attr.toInteger(42));
		assertThrows(ElementException.class, "Attribute not present", () -> attr.toInteger());
	}

	@Test
	void add() {
		parent.add(child);
		assertEquals(0, child.children().count());
		assertEquals(parent, child.parent());
		assertEquals(false, child.isRoot());
		assertEquals(0, child.index());
		assertArrayEquals(new Element[]{child}, parent.children().toArray());
		assertArrayEquals(new Element[]{child, parent}, child.path().toArray());
	}

	@Test
	void siblings() {
		final Element sibling = new Element(CHILD);
		parent.add(child);
		parent.add(sibling);
		assertEquals(2, parent.children().count());
		assertEquals(0, child.index());
		assertEquals(1, sibling.index());
		assertEquals(List.of(child, sibling), parent.children(CHILD));
	}

	@Test
	void addAlreadyAdded() {
		parent.add(child);
		assertThrows(IllegalStateException.class, () -> parent.add(child));
	}

	@Test
	void hash() {
		parent.hashCode();
	}

	@Test
	void equals() {
		assertEquals(true, parent.equals(parent));
		assertEquals(true, parent.equals(new Element(PARENT)));
		assertEquals(false, parent.equals(null));
		assertEquals(false, parent.equals(child));
	}

	@Nested
	class ExceptionTests {
		private static final String MESSAGE = "message";

		@Test
		void root() {
			final var cause = new IllegalArgumentException();
			final ElementException e = parent.exception(MESSAGE, cause);
			assertNotNull(e);
			assertEquals(parent, e.element());
			assertEquals("message at /parent", e.getMessage());
			assertEquals(cause, e.getCause());
		}

		@Test
		void child() {
			parent.add(child);
			assertEquals("message at /parent/child", child.exception(MESSAGE).getMessage());
		}

		@Test
		void sibling() {
			parent.add(new Element(CHILD)).add(child);
			assertEquals("message at /parent/child[2]", child.exception(MESSAGE).getMessage());
		}
	}

	@Nested
	class BuilderTests {
		private Element.Builder builder;

		@BeforeEach
		void before() {
			builder = new Element.Builder();
		}

		@Test
		void build() {
			// Construct element
			final Element child = builder
					.name(CHILD)
					.attribute("attr", "value")
					.text("text")
					.parent(parent)
					.build();

			// Check child
			assertNotNull(child);
			assertEquals(CHILD, child.name());
			assertEquals(Optional.of("text"), child.text());
			assertEquals(true, child.equals(child));

			// Check attributes
			assertEquals(Map.of("attr", "value"), child.attributes());

			// Check children
			assertEquals(parent, child.parent());
			assertEquals(0, child.children().count());

			// Check added to parent
			assertArrayEquals(new Element[]{child}, parent.children().toArray());
		}
	}

	@Nested
	class LoaderTests {
		private Element.Loader loader;

		@BeforeEach
		void before() {
			loader = new Element.Loader();
		}

		@Test
		void load() throws IOException {
			// Create XML
			final String xml =
					"""
					<parent one="1" two="2">
						<child>
							text
						</child>

						<child>
						</child>
					</parent>
					""";

			// Load XML
			final Element root = loader.load(new StringReader(xml));
			assertNotNull(root);

			// Check parent element
			assertEquals(PARENT, root.name());
			assertEquals(Optional.empty(), root.text());
			assertEquals(Map.of("one", "1", "two", "2"), root.attributes());
			assertEquals(2, root.children().count());

			// Check child
			final Element child = root.children().iterator().next();
			assertEquals(CHILD, child.name());
			assertEquals(Optional.of("text"), child.text());
		}
	}
}
