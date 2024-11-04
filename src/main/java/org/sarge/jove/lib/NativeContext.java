package org.sarge.jove.lib;

import static java.util.Objects.requireNonNull;

import java.lang.foreign.Linker;

/**
 * The <i>native context</i> is used when building a proxy API for a native library.
 * @author Sarge
 */
public record NativeContext(Linker linker, NativeMapperRegistry registry) {
	/**
	 * Constructor.
	 * @param linker		Native linker
	 * @param registry		Registered native mappers
	 */
	public NativeContext {
		requireNonNull(linker);
		requireNonNull(registry);
	}

	/**
	 * Default constructor.
	 * @see NativeMapperRegistry#create()
	 */
	public NativeContext() {
		this(Linker.nativeLinker(), NativeMapperRegistry.create());
	}
}
