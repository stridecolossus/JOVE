package org.sarge.jove.generator;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.sarge.lib.util.Check.notNull;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.GPPLanguage;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.parser.DefaultLogService;
import org.eclipse.cdt.core.parser.FileContent;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IncludeFileContentProvider;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTArrayDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTElaboratedTypeSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTEnumerator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTNamedTypeSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTSimpleDeclSpecifier;
import org.eclipse.cdt.internal.core.index.EmptyCIndex;

public class LibraryParser {
	// Parser support
	private final String[] includePaths = new String[0];
	private final IncludeFileContentProvider emptyIncludes = IncludeFileContentProvider.getEmptyFilesProvider();
	private final IIndex index = EmptyCIndex.INSTANCE;
	private final int options = ILanguage.OPTION_IS_SOURCE_UNIT;
	private final IParserLogService log = new DefaultLogService();

	// Source code generation
	private final Generator generator;
	private final StructureGenerator structure;
	private final SourceWriter writer;
	private final TypeMapper mapper;
	private final Set<String> enumerations = new HashSet<>();

	/**
	 * AST visitor.
	 */
	private class Visitor extends ASTVisitor {
		private int mappingCount;
		private int enumerationCount;
		private int structureCount;

		/**
		 * Constructor.
		 */
		public Visitor() {
			shouldVisitDeclSpecifiers = true;
		}

		@Override
		public int visit(IASTDeclSpecifier spec) {
			if(spec instanceof IASTNamedTypeSpecifier) {
				// Type definition
				map((IASTNamedTypeSpecifier) spec);
				++mappingCount;
			}
			else
			if(spec instanceof IASTEnumerationSpecifier) {
				// Enumeration
				enumeration((IASTEnumerationSpecifier) spec);
				++enumerationCount;
			}
			else
			if(spec instanceof IASTCompositeTypeSpecifier) {
				// Structure
				structure((IASTCompositeTypeSpecifier) spec);
				++structureCount;
			}
			return PROCESS_SKIP;
		}
	}

	/**
	 * Constructor.
	 * @param mapper		Type mapper
	 * @param generator		Default source code generator
	 * @param structure		Structure generator
	 * @param writer		Output writer
	 */
	public LibraryParser(TypeMapper mapper, Generator generator, StructureGenerator structure, SourceWriter writer) {
		this.mapper = notNull(mapper);
		this.generator = notNull(generator);
		this.structure = notNull(structure);
		this.writer = notNull(writer);
	}

	/**
	 * Generates source code for the given file.
	 * @param file			Source file
	 * @param macros		Pre-defined macros
	 * @throws Exception if the file cannot be parsed
	 */
	public void generate(String file, Map<String, String> macros) throws Exception {
		// Load source code
		System.out.println("Loading " + file);
		final FileContent content = FileContent.createForExternalFileLocation(file);
		final IScannerInfo info = new ScannerInfo(macros, includePaths);
		final IASTTranslationUnit unit = GPPLanguage.getDefault().getASTTranslationUnit(content, info, emptyIncludes, index, options, log);

		// Parse tree and generate source files
		System.out.println("Parsing " + file);
		final Visitor visitor = new Visitor();
		unit.accept(visitor);

		// Output statistics
		// TODO
		// - elapsed time
		// - move up
		System.out.println("  type definitions: " + visitor.mappingCount);
		System.out.println("  enumerations: " + visitor.enumerationCount);
		System.out.println("  structures: " + visitor.structureCount);
		System.out.println("  written: " + writer.count());
	}

	/**
	 * Registers a type definition.
	 * @param mapping Type definition node
	 */
	private void map(IASTNamedTypeSpecifier mapping) {
		final String type = mapping.getName().toString();
		final IASTSimpleDeclaration parent = (IASTSimpleDeclaration) mapping.getParent();
		final String name = parent.getDeclarators()[0].getName().toString();

		// Ignore enumerations
		if(enumerations.contains(type)) {
			return;
		}

		// Ignore flags
		if(type.equals("VkFlags")) {
			return;
		}

		// Register mapping
		System.out.println(name+" -> " + type);
		mapper.add(name, type);
	}

	/**
	 * Generates a JNA enumeration.
	 * @param enumeration Enumeration node
	 */
	private void enumeration(IASTEnumerationSpecifier enumeration) {
		// Extract enumeration name
		final String name = StringUtils.removeEnd(enumeration.getName().toString(), "Bits");
		System.out.println("Generating enumeration " + name);

		// Get enumeration values
		final var values = Arrays.stream(enumeration.getEnumerators())
			.map(CPPASTEnumerator.class::cast)
			.collect(toMap(e -> e.getName().toString(), e -> e.getIntegralValue().numericalValue(), Long::sum, LinkedHashMap::new));

		// Generate enumeration
		final String code = generator.generate(name, Map.of("values", values));
		write(name, code);
		enumerations.add(name);
	}

	/**
	 * Generates a JNA structure.
	 * @param spec Structure node
	 */
	private void structure(IASTCompositeTypeSpecifier spec) {
		// Extract structure name
		final String name = spec.getName().toString();
		System.out.println("Generating structure " + name);

		// Get field definitions
		final var fields = Arrays.stream(spec.getChildren())
			.skip(1)					// First is the structure itself
			.map(IASTSimpleDeclaration.class::cast)
			.map(this::field)
			.collect(toList());

		// Generate structure
		final String code = structure.generate(name, fields);
		write(name, code);
	}

	/**
	 * Parses a structure field.
	 * @param node Field node
	 * @return Field descriptor
	 */
	private StructureGenerator.Field field(IASTSimpleDeclaration node) {
		// Get field name
		final CPPASTDeclarator declarator = (CPPASTDeclarator) node.getDeclarators()[0];
		final String name = declarator.getName().toString();

		// Get field type
		final String type;
		final IASTDeclSpecifier spec = node.getDeclSpecifier();
		if(spec instanceof CPPASTNamedTypeSpecifier) {
			final CPPASTNamedTypeSpecifier named = (CPPASTNamedTypeSpecifier) spec;
			type = named.getName().toString();
		}
		else
		if(spec instanceof CPPASTSimpleDeclSpecifier) {
			final CPPASTSimpleDeclSpecifier simple = (CPPASTSimpleDeclSpecifier) spec;
			type = simple.getRawSignature();
		}
		else
		if(spec instanceof CPPASTElaboratedTypeSpecifier) {
			final CPPASTElaboratedTypeSpecifier elaborated = (CPPASTElaboratedTypeSpecifier) spec;
			type = elaborated.getName().toString();
		}
		else {
			throw new UnsupportedOperationException("Unknown field declaration specifier: " + spec.getClass().getName());
		}

		// Determine number of pointers
		final int count = (declarator.getPointerOperators() == null) ? 0 : declarator.getPointerOperators().length;

		// Get array size
		final int len;
		if(declarator instanceof CPPASTArrayDeclarator) {
			final CPPASTArrayDeclarator array = (CPPASTArrayDeclarator) declarator;
			len = Integer.parseInt(array.getArrayModifiers()[0].getConstantExpression().toString());
		}
		else {
			len = 0;
		}

		final String clazz = type.replace("const", "").trim();
		if(name.equals("flags")) {
			return structure.field("flags", "int", 0, 0);
		}
		else {
			return structure.field(name, clazz, count, len);
		}
	}

	/**
	 * Writes a JNA source file.
	 */
	private void write(String name, String code) {
		try {
			writer.write(name, code);
		}
		catch(IOException e) {
			throw new RuntimeException("Error writing enumeration " + name, e);
		}
	}

	public static void main(String[] args) throws Exception {
		// Init type definitions
		final TypeMapper.Loader loader = new TypeMapper.Loader();
		final TypeMapper mapper = loader.load(new InputStreamReader(LibraryParser.class.getResourceAsStream("/typedefs.txt")));

		// Create source code generators
		final TemplateProcessor proc = new TemplateProcessor("src/main/resources");
		final String pack = "org.sarge.jove.platform.vulkan";
		final Generator enumeration = new Generator(proc, "enum.template.txt", pack);
		final StructureGenerator structure = new StructureGenerator(new Generator(proc, "struct.template.txt", pack), mapper);

		// Create source code writer
		final SourceWriter writer = new SourceWriter(Paths.get("src/generated/java/org/sarge/jove/platform/vulkan"));
		// TODO
		// mode
		// - ignore			does not output
		// - replace		only replaces if empty
		// - write			always writes
		//writer.replace(false);

		// Create parser
		final LibraryParser parser = new LibraryParser(mapper, enumeration, structure, writer);

		// Parse main Vulkan header
		final Map<String, String> macros = Map.of(
			"__cplusplus", "1",
			"VK_NO_PROTOTYPES", "1"
		);
		parser.generate("C:/VulkanSDK/1.1.101.0/Include/vulkan/vulkan_core.h", macros);
	}
}
