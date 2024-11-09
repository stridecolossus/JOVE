package org.sarge.jove.lib;

import static java.util.Objects.requireNonNull;

import java.lang.foreign.Arena;

/**
 * Default implementation that allocated new references on-demand within the scope of an {@link Arena}.
 * @author Sarge
 */
public class DefaultReferenceFactory implements ReferenceFactory {
	private final Arena arena;

	/**
	 * Default constructor that uses an automatic arena when allocating references.
	 */
	@SuppressWarnings("resource")
	public DefaultReferenceFactory() {
		this(Arena.ofAuto());
	}

	/**
	 * Constructor.
	 * @param arena Arena when allocating new references
	 */
	public DefaultReferenceFactory(Arena arena) {
		this.arena = requireNonNull(arena);
	}

	@Override
	public PointerReference pointer() {
		return new PointerReference(arena);
	}

	@Override
	public IntegerReference integer() {
		return new IntegerReference(arena);
	}
}
