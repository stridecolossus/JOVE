package org.sarge.jove.foreign;

import static java.util.Objects.requireNonNull;

import java.util.*;
import java.util.logging.Logger;

import org.sarge.jove.common.*;
import org.sarge.jove.common.Handle.HandleNativeTransformer;
import org.sarge.jove.common.NativeObject.NativeObjectTransformer;
import org.sarge.jove.foreign.NativeReference.NativeReferenceTransformer;
import org.sarge.jove.foreign.NativeStructure.StructureNativeTransformer;
import org.sarge.jove.util.*;

/**
 * The <i>transformer registry</i> is used to retrieve the transformer for supported native types.
 * <p>
 * Notes:
 * <ul>
 * <li>An <i>array</i> of a registered type is automatically also supported</li>
 * <li>Collection types cannot be supported (due to type-erasure)</li>
 * </ul>
 * @author Sarge
 */
@SuppressWarnings("rawtypes")
public class TransformerRegistry {
	public interface Factory<T> {

		NativeTransformer<T, ?> transformer(Class<T> type, TransformerRegistry registry);

		static <T> Factory<T> of(NativeTransformer<T, ?> transformer) {
			return (type, registry) -> transformer;
		}
	}

	private final Logger log = Logger.getLogger(TransformerRegistry.class.getName());
	private final Map<Class<?>, Factory<?>> registry = new HashMap<>();

	/**
	 * Registers a native transformer.
	 * @param transformer Native transformer
	 * @throws IllegalArgumentException if {@link #type} is {@code null} or cannot be supported
	 */
	public <T> void register(Class<T> type, NativeTransformer<T, ?> transformer) {
		register(type, Factory.of(transformer));
	}

	/**
	 *
	 * @param <T>
	 * @param type
	 * @param factory
	 */
	public <T> void register(Class<T> type, Factory<T> factory) {
		requireNonNull(type);
		requireNonNull(factory);
		validate(type);
		log.info("Register: " + type.getName());
		add(type, factory);
	}

	/**
	 * @throws IllegalArgumentException if the given type cannot be transformed
	 */
	private static void validate(Class<?> type) {
		if(Collection.class.isAssignableFrom(type)) {
			throw new IllegalArgumentException("Collections cannot be transformed");
		}
	}

	/**
	 * Registers a transformer.
	 * @param type			Domain type
	 * @param factory		Transformer factory
	 */
	private void add(Class<?> type, Factory<?> factory) {
		registry.put(type, factory);
	}

	/**
	 * Looks up the native transformer for the given Java type.
	 * @param type Java type
	 * @return Transformer
	 * @throws IllegalArgumentException if the type is not supported by this registry
	 */
	public NativeTransformer get(Class<?> type) {
		validate(type);
		final Factory factory = factory(type);
		return factory.transformer(type, this);
	}

	private Factory<?> factory(Class<?> type) {
		final Factory factory = registry.get(type);
//		final NativeTransformer transformer = registry.get(type);
		if(factory == null) {
			if(type.isArray()) {
				// TODO - multi-dimension arrays???
				final var component = type.getComponentType();
				final var delegate = get(component);
				final var transformer = new ArrayNativeTransformer(type, delegate);
				final Factory array = Factory.of(transformer);
				log.info("Register array: %s[] -> %s".formatted(component.getName(), delegate));
				add(type, array);
				return array;
			}
			else {
				//final NativeTransformer derived = find(type);
				final Factory derived = find(type);
				log.info("Register subclass: %s -> %s".formatted(type.getName(), derived)); // derived.type().getName()));
				add(type, derived);
				//registry.put(type, derived);
				return derived;
			}
		}
		else {
			return factory;
		}
	}

	/**
	 * Finds a native transformer for the given subclass.
	 */
//	@SuppressWarnings("unchecked")
	private Factory find(Class<?> type) {
		return registry
				.keySet()
				.stream()
				.filter(e -> e.isAssignableFrom(type))
				.map(registry::get)
				.findAny()
				.orElseThrow(() -> new IllegalArgumentException("Unsupported type: " + type));
	}

	/**
	 * Creates a registry with support for all standard built-in types.
	 * @return Default mapper registry
	 */
	public static TransformerRegistry create() {

		final var registry = new TransformerRegistry();
		PrimitiveNativeTransformer.register(registry); // TODO
		registry.register(String.class, new StringNativeTransformer());
		registry.register(NativeReference.class, new NativeReferenceTransformer());
		registry.register(IntEnum.class, IntEnumNativeTransformer.FACTORY);
		registry.register(BitMask.class, new BitMaskNativeTransformer());
		registry.register(Handle.class, new HandleNativeTransformer());
		registry.register(NativeObject.class, new NativeObjectTransformer());
		registry.register(NativeStructure.class, StructureNativeTransformer.FACTORY);

		//////////////

//		// Register primitive types
//		final var registry = new TransformerRegistry();
//		//PrimitiveNativeTransformer.primitives().forEach(e -> register registry::register);
//
//		// Register reference types
//		final NativeTransformer[] mappers = {
//				new IntEnumNativeTransformer(),
//				new BitMaskNativeTransformer(),
//				new StringNativeTransformer(),
//				new NativeReferenceTransformer(),
//				new HandleNativeTransformer(),
//				new NativeObjectTransformer(),
//				new StructureNativeTransformer(registry),
//		};
//		for(var m : mappers) {
//			registry.add(m);
//		}

		return registry;
	}
}
