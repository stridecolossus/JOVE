package org.sarge.jove.platform.vulkan.generator;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.lang.foreign.ValueLayout;
import java.nio.file.*;
import java.util.*;

/**
 * The <i>Vulkan generator</i> builds the source code for Vulkan enumerations and structures.
 * @author Sarge
 */
class VulkanGenerator {
	private final Tokenizer tokenizer;
	private final TypeMapper mapper = new TypeMapper();
	private final TemplateProcessor template = new TemplateProcessor();
	private final Map<String, Integer> constants = new HashMap<>();
	private final FilePrinter printer;

	private int enumerations;
	private int structures;

	/**
	 * Constructor.
	 * @param file			Vulkan header
	 * @param printer		Source file printer
	 */
	public VulkanGenerator(String file, FilePrinter printer) {
		this.tokenizer = new Tokenizer(file);
		this.printer = requireNonNull(printer);
	}

	/**
	 *
	 */
	public void generate() {
		while(tokenizer.hasNext()) {
			switch(tokenizer.next()) {
				case "#" -> define();
				case "typedef" -> typedef();
				case "VK_DEFINE_NON_DISPATCHABLE_HANDLE", "VK_DEFINE_HANDLE" -> handle();
			}
		}
	}

	/**
	 * Parses and registers a macro definition for integer constants.
	 */
	private void define() {
		// Check whether this line is a macro
		if(!tokenizer.peek("define")) {
			return;
		}

		// Extract key-value
		final String name = tokenizer.next();
		final String token = tokenizer.next();

		// Ignore string declarations
		if(token.equals("\"")) {
			return;
		}

		// Parse macro constant
		final Integer value = parse(token);
		if(value != null) {
			constants.put(name, value);
		}
	}

	/**
	 * Parses a macro constant.
	 */
	private static Integer parse(String token) {
		if(token.startsWith("(~")) {
			return -1;
		}

		try {
			return Integer.parseInt(token);
		}
		catch(NumberFormatException _) {
			return null;
		}
	}

	/**
	 * Parses a handle definition.
	 */
	private void handle() {
		// Extract handle identifier
		final String name;
		tokenizer.skip("(");
		name = tokenizer.next();
		tokenizer.skip(")");

		// Register handle
		mapper.add(name, TypeMapper.HANDLE);
	}

	/**
	 * Parses a type definition.
	 */
	private void typedef() {
		final String type = tokenizer.next();
		switch(type) {
			case "enum" -> enumeration();
			case "struct", "union" -> structure(type);
			default -> synonym(type);
		}
	}

	/**
	 * Parses a type definition for a synonym.
	 * @param type Type name
	 */
	private void synonym(String type) {
		// Extract synonym identifier
		final String synonym = tokenizer.next();
		if(!tokenizer.peek(";")) {
			tokenizer.next();
			return;
		}

		// Register synonym
		mapper.typedef(type, synonym);
	}

	/**
	 * Parses and generates an enumeration.
	 */
	private void enumeration() {
		// Extract enumeration metadata
		final var parser = new EnumerationParser();
		final EnumerationData enumeration = parser.parse(tokenizer);

		// Build enumeration template arguments
		final var generator = new EnumerationGenerator();
		final Map<String, Object> arguments = generator.generate(enumeration);

		// Generate source code
		final String source = template.generate("enumeration-template.txt", arguments);

		// Write source code
		final String name = (String) arguments.get("name");
		printer.print(name, source);

		// Register enumeration (replacing existing type definitions)
		mapper.add(new NativeType(name, ValueLayout.JAVA_INT));
		++enumerations;
	}

	/**
	 * Parses and generates a structure or union.
	 * @param type Group type
	 */
	private void structure(String type) {
		// TODO...
		if(tokenizer.peek("VkAllocationCallbacks")) {
			return;
		}
		if(tokenizer.peek("VkAccelerationStructureInfoNV")) {
			return;
		}
		if(tokenizer.peek("VkAccelerationStructureCreateInfoNV")) {
			return;
		}
		// ...TODO

		// Extract structure metadata
		final var parser = new StructureParser(tokenizer, constants::get);
		final StructureData structure = parser.parse(type);

		// Generate template arguments
		final var generator = new StructureGenerator(mapper);
		final Map<String, Object> arguments = generator.generate(structure);

		// Generate source code
		final String source = template.generate("structure-template.txt", arguments);
		printer.print(structure.name(), source);
		++structures;
	}

	/**
	 *
	 */
	public static void main(String[] args) throws IOException {

		final Path directory = Paths.get("./src/generated/java/org/sarge/jove/platform/vulkan");

		final Path path = Paths.get("/VulkanSDK/1.2.154.1/Include/vulkan/vulkan_core.h");
		final String file = Files.readString(path);
//		final var printer = FilePrinter.IGNORE;
//		final var printer = FilePrinter.of(new PrintWriter(System.out));
//		final var printer = FilePrinter.of(directory, "java", true);
		final var printer = new CompareFilePrinter();
		final var generator = new VulkanGenerator(file, printer);
		generator.generate();

		System.out.println("constants=" + generator.constants.size());
		System.out.println("enumerations=" + generator.enumerations);
		System.out.println("structures=" + generator.structures);

		/**
		 *
		 * header file
		 * output directory
		 * output to file or console
		 * whether to compare?
		 * skip enums/structures, skip all *except* specified?
		 * filter enumerations(s), structure(s)
		 *
		 *
		 */
	}
}
