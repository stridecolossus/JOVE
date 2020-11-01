package org.sarge.jove.demo;

import static org.sarge.jove.util.Check.notNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.StringJoiner;

import org.apache.commons.lang.StringUtils;
import org.sarge.jove.util.DataSource;

public class CompileShaderDataSource implements DataSource {
	private final DataSource src;

	public CompileShaderDataSource(DataSource src) {
		this.src = notNull(src);
	}

	@Override
	public InputStream open(String name) throws IOException {
		try {
			System.out.println("Trying " + name);
			return src.open(name);
		}
		catch(IOException e) {
			try {
				System.out.println("Compiling " + name);
				compile(name);
			}
			catch(Exception ex) {
				throw new IOException("Error compiling shader: " + name, ex);
			}

			System.out.println("Re-trying after compile " + name);
			return src.open(name);
		}
	}

	private static void compile(String filename) throws Exception {
		if(!filename.startsWith("spv.")) throw new IllegalArgumentException("Expected SPV prefix: " + filename);
		final String suffix = StringUtils.removeStart(filename, "spv.");

		final StringJoiner cmd = new StringJoiner(" ");
		cmd.add("/VulkanSDK/1.1.101.0/Bin32/glslc");
		cmd.add(suffix);
		cmd.add("-o");
		cmd.add(filename);

		final Process proc = Runtime.getRuntime().exec(cmd.toString());
		final int result = proc.waitFor();
		if(result != 0) {
			throw new IOException(String.format("Error compiling shader: result=%d shader=%s", result, filename));
		}
	}
}


//cd /JOVE/src/test/resources/demo/triangle
//glslc triangle.frag -o spv.triangle.frag
//glslc triangle.vert -o spv.triangle.vert
