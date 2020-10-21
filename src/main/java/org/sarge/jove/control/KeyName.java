package org.sarge.jove.control;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * The <i>key name</i> is used to lookup up the name of a <i>standard</i> keyboard key.
 * @author Sarge
 */
public final class KeyName {
	private static final String[] NAMES;

	/**
	 * Loads the key name file.
	 */
	static {
		// Open key names file
		final InputStream in = KeyName.class.getResourceAsStream("/keynames.txt");
		if(in == null) throw new RuntimeException("Key name file not found");

		// Load key names
		try {
			try(final BufferedReader r = new BufferedReader(new InputStreamReader(in))) {
				NAMES = r.lines()
					.map(KeyName::name)
					.toArray(String[]::new);
			}
		}
		catch(IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Extracts the key name.
	 */
	private static String name(String line) {
		final int index = line.indexOf('-');
		final String name = line.substring(index + 2);
		if(name.startsWith("Unknown")) {
			return null;
		}
		else {
			return name;
		}
	}

	/**
	 * Creates a reverse lookup table for key name to index.
	 * @return Key indices indexed by key names
	 */
	public static Map<String, Integer> reverse() {
		final Map<String, Integer> map = new HashMap<>();
		for(int n = 0; n < NAMES.length; ++n) {
			if(NAMES[n] != null) {
				map.put(NAMES[n], n);
			}
		}
		return map;
	}

	/**
	 * Looks up the name of the given key.
	 * @param key Key index
	 * @return Key name or <tt>null</tt> if undefined
	 */
	public static String name(int key) {
		Check.zeroOrMore(key);
		if(key >= NAMES.length) {
			return null;
		}
		else {
			return NAMES[key];
		}
	}

	private KeyName() {
	}
}
