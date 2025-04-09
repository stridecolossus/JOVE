package org.sarge.jove.foreign;

import static java.util.Objects.requireNonNull;

import java.util.*;
import java.util.logging.Logger;

import org.sarge.jove.common.*;
import org.sarge.jove.common.Handle.HandleTransformer;
import org.sarge.jove.common.NativeObject.NativeObjectTransformer;
import org.sarge.jove.foreign.NativeReference.NativeReferenceTransformer;
import org.sarge.jove.foreign.NativeStructure.StructureTransformer;
import org.sarge.jove.foreign.ReturnedArray.ReturnedArrayTransformer;
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
	private final StructureTransformer.Builder builder = new StructureTransformer.Builder(this);

	/**
	 * Looks up the native transformer for the given Java type.
	 * @param type Java type
	 * @return Native transformer
	 * @throws IllegalArgumentException for an unsupported type
	 */
	public Transformer get(Class<?> type) {
		final Transformer transformer = registry.get(type);
		if(transformer == null) {
			final Transformer created = create(type);
			add(type, created);
			return created;
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
			final var component = get(type.getComponentType());
			return new ArrayTransformer(component);
		}
		else
		if(IntEnum.class.isAssignableFrom(type)) {
			return new IntEnumTransformer((Class<? extends IntEnum>) type);
		}
		else
		if(NativeStructure.class.isAssignableFrom(type)) {
			return builder.build((Class<? extends NativeStructure>) type);
		}
		else
		if(ReturnedArray.class.isAssignableFrom(type)) {
			return new ReturnedArrayTransformer(this);
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
				.orElseThrow(() -> new IllegalArgumentException("Unsupported reference type: " + type));
	}

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
		IdentityTransformer.primitives().forEach(registry::add);
		registry.init();
		return registry;
	}

	/**
	 * Registers supporting framework types.
	 */
	private void init() {
		add(String.class, new StringTransformer());
		add(NativeReference.class, new NativeReferenceTransformer());
		add(Handle.class, new HandleTransformer());
		add(NativeObject.class, new NativeObjectTransformer());
		add(EnumMask.class, new EnumMaskTransformer());
	}
}
