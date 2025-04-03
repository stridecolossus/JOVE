package org.sarge.jove.foreign;

import static java.util.Objects.requireNonNull;

import java.util.*;
import java.util.logging.Logger;

import org.sarge.jove.common.*;
import org.sarge.jove.common.Handle.HandleTransformer;
import org.sarge.jove.common.NativeObject.NativeObjectTransformer;
import org.sarge.jove.foreign.NativeReference.NativeReferenceTransformer;
import org.sarge.jove.foreign.NativeStructure.StructureTransformer;
import org.sarge.jove.util.*;
import org.sarge.jove.util.EnumMask.EnumMaskTransformer;
import org.sarge.jove.util.IntEnum.IntEnumTransformer;

/**
 * A <i>registry</i> maps Java types to native transformers.
 * @author Sarge
 */
public class Registry {
	private static final Logger LOG = Logger.getLogger(Registry.class.getName());

	private final Map<Class<?>, Transformer> registry = new HashMap<>();

	/**
	 * Looks up the native transformer for the given Java type.
	 * @param type Java type
	 * @return Native transformer
	 */
	public Transformer get(Class<?> type) {
		final Transformer transformer = registry.get(type);
		if(transformer == null) {
			return create(type);
		}
		else {
			return transformer;
		}
	}

	/**
	 * Determines the native transformer for the given specialised type.
	 * @param type Java type
	 * @return Transformer
	 * @throws IllegalArgumentException if the type is unsupported
	 */
	@SuppressWarnings("unchecked")
	private Transformer create(Class<?> type) {
		// TODO - some sort of factory/matcher approach
		if(type.isArray()) {
			final Class<?> component = type.getComponentType();
			return new ArrayTransformer(get(component));
		}
		else
		if(IntEnum.class.isAssignableFrom(type)) {
			final var actual = (Class<? extends IntEnum>) type;
			return new IntEnumTransformer(actual);
		}
		else
		if(NativeStructure.class.isAssignableFrom(type)) {
			final var builder = new StructureTransformer.Builder(this);
			return builder.build((Class<? extends NativeStructure>) type);
		}
		else {
			return find(type);
		}
	}

	/**
	 * Finds a registered supertype transformer for the given type.
	 * @param type Java type
	 * @return Supertype transformer
	 */
	private Transformer find(Class<?> type) {
		return registry
				.keySet()
				.stream()
				.filter(e -> e.isAssignableFrom(type))
				.findAny()
				.map(registry::get)
				.orElseThrow(() -> new IllegalArgumentException("Unsupported reference type: " + type));	}

	/**
	 * Registers a native transformer for the given type.
	 * @param type				Java or domain type
	 * @param transformer		Native transformer
	 */
	public void add(Class<?> type, Transformer transformer) {
		requireNonNull(type);
		requireNonNull(transformer);
		registry.put(type, transformer);
		LOG.info("Register native transformer: type=%s transformer=%s".formatted(type, transformer));
	}

	/**
	 * Creates a registry pre-populated with transformers for all Java primitives and supporting JOVE types.
	 * @return Default registry
	 */
	public static Registry create() {
		final var registry = new Registry();
		Primitive.primitives().forEach(registry::add);
		registry.init();
		return registry;
	}

	/**
	 * Registers supporting framework types.
	 */
	private void init() {
		add(String.class, new StringTransformer());
		add(Handle.class, new HandleTransformer());
		add(NativeObject.class, new NativeObjectTransformer());
		add(EnumMask.class, new EnumMaskTransformer());
		add(NativeReference.class, new NativeReferenceTransformer());
	}
}
