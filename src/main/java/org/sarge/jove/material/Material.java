package org.sarge.jove.material;

import static org.sarge.lib.util.Check.notEmpty;
import static org.sarge.lib.util.Check.notNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.material.Material.Property.Binder;
import org.sarge.jove.material.Material.Property.Policy;
import org.sarge.lib.collection.StrictMap;

/**
 * A <i>material</i> maps a set of properties to the parameters of the active shader.
 * <p>
 * Materials are comprised of:
 * <ul>
 * <li>shader parameters</li>
 * <li>active texture units</li>
 * <li>render properties</li>
 * TODO
 * </ul>
 * The {@link Policy} of a property specifies whether the value is managed by the client or
 * @author Sarge
 */
public final class Material {
	/**
	 * Empty material.
	 */
	public static final Material NONE = new Material("none", null, Map.of());

	/**
	 * A <i>material property</i> maps a value to a shader parameter.
	 */
	public static final class Property {
		/**
		 * A <i>binder</i> applies a material property value to a shader parameter.
		 */
		@FunctionalInterface
		public interface Binder {
			/**
			 * Applies a material property to the given shader parameter.
			 * @param param Shader parameter
			 * @throws IllegalArgumentException if the given parameter is not valid for this binder
			 */
			void apply(Shader.Parameter param);

			/**
			 * @return Size of the bound value
			 */
			default int size() {
				return 1;
			}
		}

		/**
		 * Update policy.
		 */
		public enum Policy {
			/**
			 * Value is only changed by the client.
			 */
			MANUAL,

			/**
			 * Value changes per frame, e.g. elapsed frame duration.
			 */
			FRAME,

			/**
			 * Value changes per node, e.g. modelview matrix.
			 */
			NODE,
		}

		private final Property.Binder binder;
		private final Property.Policy policy;

		private Shader.Parameter param;

		/**
		 * Constructor.
		 * @param binder		Property binder
		 * @param policy		Update policy
		 */
		public Property(Property.Binder binder, Property.Policy policy) {
			this.binder = notNull(binder);
			this.policy = notNull(policy);
		}

		/**
		 * @return Property binder
		 */
		public Property.Binder binder() {
			return binder;
		}

		/**
		 * @return Update policy for this property
		 */
		public Property.Policy policy() {
			return policy;
		}

		/**
		 * @return Shader parameter bound to this entry
		 */
		public Shader.Parameter parameter() {
			return param;
		}

		/**
		 * Binds the given shader parameter to this property.
		 * @param param Shader parameter to bind
		 * @throws IllegalStateException if this property has already been bound
		 */
		private void bind(Shader.Parameter param) {
			assert this.param == null;
			this.param = notNull(param);
		}

		/**
		 * Applies this property.
		 */
		private void apply() {
			assert param != null;
			binder.apply(param);
		}

		@Override
		public String toString() {
			return ToStringBuilder.reflectionToString(this);
		}
	}

	private final String name;
	private final Optional<Shader> shader;
	private final Map<String, Property> mat;

	private boolean bound;

	/**
	 * Constructor.
	 * @param name			Material name
	 * @param shader		Optional shader program
	 * @param mat			Entries
	 */
	Material(String name, Shader shader, Map<String, Property> mat) {
		this.name = notEmpty(name);
		this.shader = Optional.ofNullable(shader);
		this.mat = Map.copyOf(mat);
	}

	/**
	 * @return Material name
	 */
	public String name() {
		return name;
	}

	/**
	 * @return Shader program for this material
	 */
	public Optional<Shader> shader() {
		return shader;
	}

	/**
	 * @return Material properties
	 */
	public Map<String, Property> properties() {
		return new HashMap<>(mat);
	}

	/**
	 * Looks up a material property.
	 * @param name Property name
	 * @return Property
	 */
	public Property property(String name) {
		return mat.get(name);
	}

	/**
	 * Binds the properties of this material to the active shader.
	 * @param shader Shader parameter mapper
	 * @throws IllegalStateException if a property cannot be bound to its corresponding shader parameter
	 * @throws IllegalStateException if this material has already been bound to a shader
	 */
	void bind(Function<String, Shader.Parameter> shader) {
		// Check binding
		if(bound) throw new IllegalStateException("Material has already been bound");
		bound = true;

		// Bind properties
		for(Map.Entry<String, Property> e : mat.entrySet()) {
			// Lookup shader parameter
			final String name = e.getKey();
			final Shader.Parameter param = shader.apply(name);
			if(param == null) throw new IllegalStateException("Unknown shader parameter: " + name);

			// Bind property
			final Property prop = e.getValue();
			prop.bind(param);
		}

		// TODO
		// - check for orphaned parameters at render-time
		// - NB unused properties are ignored
	}

	/**
	 * Applies this material.
	 * @param policy Property update policy
	 * @throws IllegalStateException if this material has not been bound to a shader
	 */
	void apply(Property.Policy policy) {
		if(!bound) throw new IllegalStateException("Material has not been bound");
		mat.values().stream().filter(e -> e.policy == policy).forEach(Property::apply);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	/**
	 * Builder for a material.
	 */
	public static class Builder {
		private final String name;
		private final Map<String, Property> mat = new StrictMap<>();
		private Shader shader;

		/**
		 * Constructor.
		 * @param name Material name
		 */
		public Builder(String name) {
			this.name = notEmpty(name);
		}

		/**
		 * Sets the shader program for this material.
		 * @param shader Shader program
		 */
		public Builder shader(Shader shader) {
			this.shader = shader;
			return this;
		}

		/**
		 * Adds a material property.
		 * @param name			Property name
		 * @param binder		Binder
		 * @param policy		Update policy
		 * @throws IllegalArgumentException for a duplicate property name
		 */
		public Builder add(String name, Binder prop, Policy policy) {
			add(name, new Property(prop, policy));
			return this;
		}

		/**
		 * Adds a material property.
		 * @param name			Property name
		 * @param prop			Property
		 * @throws IllegalArgumentException for a duplicate property name
		 * @see #add(String, Binder, Policy)
		 */
		public Builder add(String name, Property prop) {
			mat.put(name, prop);
			return this;
		}

// TODO
// lights
//		private final Map<String, TextureUnit> textures = new LinkedHashMap<>();
//		private final Map<String, MaterialProperty> properties = new StrictMap<>();
//		private final Map<String, RenderProperty> render = new StrictMap<>();

		/**
		 * Constructs this material.
		 * @return New material
		 */
		public Material build() {
			return new Material(name, shader, mat);
		}
	}
}
