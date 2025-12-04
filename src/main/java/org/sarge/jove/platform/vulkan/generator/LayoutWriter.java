package org.sarge.jove.platform.vulkan.generator;
import static java.util.stream.Collectors.joining;
import static org.sarge.jove.util.Validation.requireOneOrMore;

import java.lang.foreign.*;
import java.util.*;

/**
 * The <i>layout writer</i> converts a structure layout to source code.
 * @author Sarge
 */
public class LayoutWriter {
	private static final String NEWLINE = "\n";
	private static final String QUOTE = "\"";
	private static final String TAB = "\t";

	private final int indent;

	/**
	 * Constructor.
	 * @param indent Indentation
	 */
	public LayoutWriter(int indent) {
		this.indent = requireOneOrMore(indent);
	}

	/**
	 * Writes a structure layout.
	 */
	public String write(GroupLayout group) {
		final String type = switch(group) {
			case StructLayout _ -> "structLayout";
			case UnionLayout _	-> "unionLayout";
		};

		final List<MemoryLayout> members = group.memberLayouts();

		if((members.size() == 1) && !(members.getFirst() instanceof GroupLayout)) {
			// Inline single layouts
			final String layout = member(members.getFirst());
			return String.format("MemoryLayout.%s(%s)", type, layout);
		}
		else {
			// Build structure fields
			final String suffix = NEWLINE + TAB.repeat(indent - 1);
			final String fields = group
					.memberLayouts()
					.stream()
					.map(this::member)
					.map(this::indent)
					.collect(joining("," + NEWLINE, NEWLINE, suffix));

			// Render layout
			return String.format("MemoryLayout.%s(%s)", type, fields);
		}
	}

	/**
	 * Writes a structure field.
	 */
	private String member(MemoryLayout layout) {
		if(layout instanceof PaddingLayout padding) {
			return write(padding);
		}

		final String name = layout.name().orElseThrow(() -> new NoSuchElementException("Expected named member: " + layout));
		return String.format("%s.withName(%s)", write(layout), quote(name));
	}

	/**
	 * Indents the given line.
	 */
	private String indent(String line) {
		return TAB.repeat(indent).concat(line);
	}

	/**
	 * Writes the given memory layout.
	 * @param layout Memory layout
	 * @return String representation
	 */
	protected String write(MemoryLayout layout) {
		return switch(layout) {
			case AddressLayout _			-> "POINTER";
			case ValueLayout value			-> write(value);
			case SequenceLayout sequence	-> write(sequence);
			case GroupLayout group			-> new LayoutWriter(indent + 1).write(group);
			case PaddingLayout padding		-> write(padding);
		};
	}

	/**
	 * Writes a value layout.
	 */
	private static String write(ValueLayout value) {
		return "JAVA_" + value.carrier().getSimpleName().toUpperCase();
	}

	/**
	 * Writes an array layout.
	 */
	private String write(SequenceLayout sequence) {
		final long length = sequence.elementCount();
		final MemoryLayout component = sequence.elementLayout();
		return String.format("MemoryLayout.sequenceLayout(%d, %s)", length, write(component));
	}

	/**
	 * Writes a padding layout.
	 */
	private static String write(PaddingLayout padding) {
		if(padding.byteSize() == 4) {
			return "PADDING";
		}
		else {
			return String.format("MemoryLayout.paddingLayout(%d)", padding.byteSize());
		}
	}

	/**
	 * Wraps the given string with quotation symbols.
	 */
	private static String quote(String name) {
		final var string = new StringBuilder();
		string.append(QUOTE);
		string.append(name);
		string.append(QUOTE);
		return string.toString();
	}
}
