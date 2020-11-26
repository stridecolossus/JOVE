package org.sarge.jove.util;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.sarge.jove.util.Check.notEmpty;
import static org.sarge.jove.util.Check.notNull;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * An <i>element</i> is an XML node.
 * @author Sarge
 */
public final class Element {
	/**
	 * TODO
	 */
	public class Attribute {
		private final String name;
		private String value;

		/**
		 * Constructor.
		 * @param name
		 * @param value
		 */
		private Attribute(String name, String value) {
			this.name = notEmpty(name);
			this.value = value;
		}

		/**
		 * @return Whether this attribute is present
		 */
		public boolean isPresent() {
			return value != null;
		}

		public int toInteger() {
			return toInteger(null);
		}

		public int toInteger(Integer def) {
			return toValue(Integer::parseInt, def);
		}

		public <T> T toValue(Function<String, T> converter, T def) {
			if(value == null) {
				if(def == null) throw exception("Attribute not present: " + name);
				return def;
			}
			else {
				// TODO - catch NFE and wrap
				return converter.apply(value);
			}
		}

		@Override
		public String toString() {
			return name + " -> " + String.valueOf(name);
		}
	}

	// Properties
	private final String name;
	private final Map<String, String> attributes;
	private final Optional<String> text;

	// Tree
	private final List<Element> children = new ArrayList<>();
	private Element parent;

	/**
	 * Constructor for a root element.
	 * @param name Element name
	 */
	public Element(String name) {
		this(name, Map.of(), null);
	}

	/**
	 * Constructor.
	 * @param name 				Element name
	 * @param attributes		Attributes
	 * @param text				Optional text content
	 */
	private Element(String name, Map<String, String> attributes, String text) {
		this.name = notEmpty(name);
		this.attributes = Map.copyOf(attributes);
		this.text = Optional.ofNullable(text);
	}

	/**
	 * @return Element name
	 */
	public String name() {
		return name;
	}

	/**
	 * @return Element attributes ordered by name
	 */
	public Map<String, String> attributes() {
		return attributes;
	}

	/**
	 * Looks up the attribute with the given name.
	 * @param name Attribute name
	 * @return Attribute
	 * @throws IllegalArgumentException if the attribute is not present
	 */
	public Attribute attribute(String name) {
		return new Attribute(name, attributes.get(name));
	}

	/**
	 * @return Text content
	 */
	public Optional<String> text() {
		return text;
	}

	/**
	 * @return Parent of this element or {@code null} for a root element
	 */
	public Element parent() {
		return parent;
	}

	/**
	 * @return Whether this is a root element
	 */
	public boolean isRoot() {
		return parent == null;
	}

	/**
	 * @return Sibling index or zero if this element has no siblings
	 */
	public int index() {
		// Check for root or single element
		if((parent == null) || (parent.children.size() == 1)) {
			return 0;
		}

		// Check for single sibling
		final List<Element> siblings = parent.children(name);
		if(siblings.size() == 1) {
			return 0;
		}

		// Otherwise determine sibling index (by reference not equality)
		for(int n = 0; n < siblings.size(); ++n) {
			if(siblings.get(n) == this) {
				return n;
			}
		}
		throw new RuntimeException();
	}

	/**
	 * @return Path from this element to the document root
	 */
	public Stream<Element> path() {
		return Stream.iterate(this, Objects::nonNull, Element::parent);
	}

	/**
	 * @return Child elements
	 */
	public Stream<Element> children() {
		return children.stream();
	}

	/**
	 * Helper - Retrieves the children of this element with the given name.
	 * @param name Name
	 * @return Children
	 */
	public List<Element> children(String name) {
		return children
				.stream()
				.filter(e -> e.name.equals(name))
				.collect(toList());
	}

	/**
	 * Adds a child to this element.
	 * @param child Child element
	 * @return This element
	 * @throws IllegalStateException if the child already has a parent
	 */
	public Element add(Element child) {
		if(child.parent != null) throw new IllegalStateException("Element already has a parent: " + child);
		child.parent = this;
		children.add(child);
		return this;
	}

	/**
	 * An <i>element exception</i> indicates an XML processing exception thrown by the application.
	 * <p>
	 * The exception message is decorated with an XPath-like string representing the location of this element within the document.
	 */
	public class ElementException extends RuntimeException {
		/**
		 * Constructor.
		 * @param message		Message
		 * @param cause			Optional cause
		 */
		private ElementException(String message, Throwable cause) {
			super(message, cause);
		}

		/**
		 * @return Element on which the exception was raised
		 */
		public Element element() {
			return Element.this;
		}

		@Override
		public String getMessage() {
			// Build path
			final var path = path().collect(toList());
			Collections.reverse(path);

			// Convert to indexed path string
			final String str = path.stream().map(this::name).collect(joining("/"));

			// Build message
			return new StringBuilder()
					.append(super.getMessage())
					.append(" at /")
					.append(str)
					.toString();
		}

		/**
		 * @return Name of the given element within the path
		 */
		private String name(Element e) {
			final int index = e.index();
			if(index == 0) {
				return e.name;
			}
			else {
				return String.format("%s[%d]", e.name, index + 1);
			}
		}
	}

	/**
	 * Creates an XML exception at this element.
	 * @param message		Message
	 * @param cause			Optional cause
	 * @return New XML exception
	 */
	public ElementException exception(String message, Throwable cause) {
		return new ElementException(message, cause);
	}

	/**
	 * Creates an XML exception at this element.
	 * @param message Message
~	 * @return New XML exception
	 */
	public ElementException exception(String message) {
		return exception(message, null);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, parent, text, attributes);
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}

		return
				(obj instanceof Element that) &&
				(this.parent == that.parent) &&
				this.name.equals(that.name) &&
				this.text.equals(that.text) &&
				this.attributes.equals(that.attributes);
	}

	@Override
	public String toString() {
		return name;
	}

	/**
	 * Builder for an XML element.
	 */
	public static class Builder {
		private String name;
		private Element parent;
		private final Map<String, String> attributes = new HashMap<>();
		private String text;

		/**
		 * Sets the name of this element.
		 * @param name Element name
		 */
		public Builder name(String name) {
			this.name = notEmpty(name);
			return this;
		}

		/**
		 * Sets the parent of this element.
		 * @param parent Parent element
		 */
		public Builder parent(Element parent) {
			this.parent = notNull(parent);
			return this;
		}

		/**
		 * Adds an attribute.
		 * @param name		Attribute name
		 * @param value		Value
		 */
		public Builder attribute(String name, String value) {
			Check.notEmpty(name);
			Check.notEmpty(value);
			attributes.put(name, value);
			return this;
		}

		/**
		 * Sets the text content of this element.
		 * @param text Text content
		 */
		public Builder text(String text) {
			this.text = notNull(text);
			return this;
		}

		/**
		 * Constructs this element.
		 * @return New element
		 */
		public Element build() {
			// Create element
			final Element e = new Element(name, attributes, text);

			// Attach to parent
			if(parent != null) {
				parent.add(e);
			}

			return e;
		}
	}

	/**
	 * Loader for an XML document.
	 */
	public static class Loader {
		private static final DocumentBuilderFactory FACTORY = DocumentBuilderFactory.newInstance();

		/**
		 * @throws RuntimeException if the underlying XML parser cannot be instantiated
		 */
		public static DocumentBuilder parser() {
			try {
				return FACTORY.newDocumentBuilder();
			}
			catch(Exception e) {
				throw new RuntimeException("Error creating XML document parser", e);
			}
		}

		private final DocumentBuilder parser = parser();

		/**
		 * Loads an XML document.
		 * @param r XML reader
		 * @return Root element
		 * @throws IOException if the XML cannot be loaded
		 */
		public Element load(Reader r) throws IOException {
			// Load document
			Document doc;
			try {
				doc = parser.parse(new InputSource(r));
			}
			catch(SAXException e) {
				throw new IOException("Error parsing XML document", e);
			}

			// Convert to domain
			return load(doc.getDocumentElement());
		}

//		public Stream<Tree> flattened() {
//	        return Stream.concat(
//	                Stream.of(this),
//	                children.stream().flatMap(Tree::flattened));
//	    }

		/**
		 * Recursively loads an XML element.
		 * @param xml XML
		 * @return Element
		 */
		private Element load(org.w3c.dom.Element xml) {
			// Init element
			final Builder builder = new Builder();
			builder.name(xml.getNodeName());

			// Load attributes
			final NamedNodeMap map = xml.getAttributes();
			for(int n = 0; n < map.getLength(); ++n) {
				final Node node = map.item(n);
				builder.attribute(node.getNodeName(), node.getNodeValue());
			}

			// Load contents and children
			final List<Element> children = new ArrayList<>();
			final NodeList nodes = xml.getChildNodes();
			for(int n = 0; n < nodes.getLength(); ++n) {
				final Node node = nodes.item(n);
				switch(node.getNodeType()) {
				case Node.ELEMENT_NODE:
					// Recurse to child element
					// TODO - refactor to tail recursive stream
					final Element child = load((org.w3c.dom.Element) node);
					children.add(child);
					break;

				case Node.TEXT_NODE:
					// Load text content
					final String text = node.getNodeValue().trim();
					if(!text.isEmpty()) {
						builder.text(text);
					}
					break;
				}
			}

			// Construct element
			final Element element = builder.build();
			children.forEach(element::add);

			return element;
		}
	}
}
