package org.sarge.jove.platform.desktop;

import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.apache.commons.lang3.StringUtils;
import org.sarge.jove.io.ClasspathDataSource;
import org.sarge.jove.io.DataSource;
import org.sarge.jove.io.TextLoader.TextResourceLoader;

/**
 * The <i>key table</i> maps a GLFW keyboard code to the corresponding key name.
 * @author Sarge
 */
public class KeyTable {
	private static final KeyTable INSTANCE = new KeyTable(); // TODO - lazy init?

	/**
	 * @return Singleton key-table instance
	 */
	public static final KeyTable instance() {
		return INSTANCE;
	}

	private final BidiMap<Integer, String> keys;

	private KeyTable() {
		final DataSource src = new ClasspathDataSource();
		this.keys = src.load("key.table.txt", new Loader());
	}

	/**
	 * Looks up a keyboard key name.
	 * @param key GLFW key code
	 * @return Key name or {@code UNKNOWN} if not present
	 */
	public String name(int key) {
		return keys.getOrDefault(key, "UNKNOWN");
	}

	/**
	 * Looks up the key code for the given keyboard key name.
	 * @param name Key name
	 * @return GLFW key code
	 */
	public int code(String name) {
		final Integer code = keys.inverseBidiMap().get(name);
		if(code == null) throw new IllegalArgumentException("Unknown key name: " + name);
		return code;
	}

	/**
	 * Key table loader.
	 */
	private static class Loader extends TextResourceLoader<Entry<Integer, String>, BidiMap<Integer, String>> {
		@Override
		protected Entry<Integer, String> load(String line) {
			// TODO - tokenize/validate/trim helpers
			final String[] tokens = StringUtils.split(line);
			if(tokens.length != 2) throw new IllegalArgumentException("Invalid key table entry");
			final Integer code = Integer.parseInt(tokens[1].trim());
			final String name = tokens[0].trim();
			return Map.entry(code, name);
		}

		@Override
		protected Collector<Entry<Integer, String>, ?, BidiMap<Integer, String>> collector() {
			return Collectors.collectingAndThen(Collectors.toMap(Entry<Integer, String>::getKey, Entry::getValue), DualHashBidiMap::new);
		}
	}
}
