package org.sarge.jove.platform.vulkan.generator;

import java.io.StringWriter;
import java.util.*;

import org.apache.velocity.*;
import org.apache.velocity.app.VelocityEngine;

/**
 * The <i>template processor</i> generates a source file given a template and injected arguments.
 * @author Sarge
 */
class TemplateProcessor {
	private final VelocityEngine engine = new VelocityEngine();

	public TemplateProcessor() {
		final Properties props = new Properties();
		props.setProperty("resource.loader", "file");
		props.setProperty("file.resource.loader.class", "org.apache.velocity.runtime.resource.loader.FileResourceLoader");
		props.setProperty("file.resource.loader.path", "src/main/resources");
		props.setProperty("file.resource.loader.cache", "false");
		engine.init(props);
	}

	/**
	 * Generates a source file.
	 * @param name			Template name
	 * @param arguments		Arguments
	 * @return Source code
	 */
	public String generate(String name, Map<String, ? extends Object> arguments) {
		final Template template = engine.getTemplate(name);
		final var context = new VelocityContext(new HashMap<>(arguments));
		final var out = new StringWriter();
		template.merge(context, out);
		return out.toString().replaceAll("\r", "");
	}
}
