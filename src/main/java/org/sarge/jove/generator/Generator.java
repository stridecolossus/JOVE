package org.sarge.jove.generator;

import static org.sarge.lib.util.Check.notEmpty;
import static org.sarge.lib.util.Check.notNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Source code generator.
 * @author Sarge
 */
public class Generator {
	private final TemplateProcessor proc;
	private final String template;
	private final String pack;

	/**
	 * Constructor.
	 * @param proc			Template processor
	 * @param template		Template name
	 * @param pack			Package
	 */
	public Generator(TemplateProcessor proc, String template, String pack) {
		this.proc = notNull(proc);
		this.template = notEmpty(template);
		this.pack = notEmpty(pack);
	}

	/**
	 * @return Package name
	 */
	public String packageName() {
		return pack;
	}

	/**
	 * Generates source code.
	 * @param name			Class name
	 * @param values		Values
	 * @return Source code
	 */
	public String generate(String name, Map<String, Object> values) {
		final Map<String, Object> map = new HashMap<>(values);
		map.put("package", pack);
		map.put("name", name);
		return proc.generate(template, map);
	}
}
