package org.sarge.jove.generator;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

/**
 * A <i>template processor</i> generates a string using a Velocity template.
 * @author Sarge
 */
public class TemplateProcessor {
	private final VelocityEngine engine = new VelocityEngine();

	/**
	 * Constructor.
	 * @param prefix Template path prefix
	 */
	public TemplateProcessor(String prefix) {
		final Properties props = new Properties();
		props.setProperty("resource.loader", "file");
		props.setProperty("file.resource.loader.class", "org.apache.velocity.runtime.resource.loader.FileResourceLoader");
		props.setProperty("file.resource.loader.path", prefix);
		props.setProperty("file.resource.loader.cache", "false");
		engine.init(props);
	}

	/**
	 * Populates a template.
	 * @param name		Template name
	 * @param data		Data
	 * @return Results
	 */
	public String generate(String name, Map<String, Object> data) {
		// Load template
		final Template template = engine.getTemplate(name);

		// Init context
		final VelocityContext ctx = new VelocityContext(new HashMap<>(data));

		// Generate source
		final StringWriter out = new StringWriter();
		template.merge(ctx, out);
		return out.toString();
	}
}
