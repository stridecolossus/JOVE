package org.sarge.jove.particle;

import static org.sarge.lib.util.Check.notNull;

import java.io.*;

import org.sarge.jove.geometry.*;
import org.sarge.jove.geometry.Ray.Intersected;
import org.sarge.jove.io.ResourceLoader;
import org.sarge.jove.particle.ParticleSystem.Characteristic;
import org.sarge.jove.util.Randomiser;
import org.sarge.lib.util.*;

/**
 * Loader for a particle system.
 * @author Sarge
 */
public class ParticleSystemLoader implements ResourceLoader<Element, ParticleSystem> {
	private final ElementLoader loader = new ElementLoader();
	private final Randomiser randomiser;

	/**
	 * Constructor.
	 * @param randomiser Randomiser
	 */
	public ParticleSystemLoader(Randomiser randomiser) {
		this.randomiser = notNull(randomiser);
	}

	@Override
	public Element map(InputStream in) throws IOException {
		return loader.load(new InputStreamReader(in));
	}

	@Override
	public ParticleSystem load(Element root) throws IOException {
		// Init particle system
		final Characteristic[] chars = characteristics(root);
		final ParticleSystem sys = new ParticleSystem(chars);
		root.child("policy").map(Element::first).map(this::policy).ifPresent(sys::policy);
		root.child("lifetime").map(Element::text).map(Integer::parseInt).ifPresent(sys::lifetime);

		// Load emitter position
		root
				.child("position")
				.map(Element::first)
				.map(this::position)
				.ifPresent(sys::position);

		// Load emitter direction
		root
				.child("vector")
				.map(Element::first)
				.map(this::direction)
				.ifPresent(sys::vector);

		// Load influences
		root
				.child("influences")
				.stream()
				.flatMap(Element::children)
				.map(this::influence)
				.forEach(sys::add);

		// Load collision surfaces
		root
				.child("surfaces")
				.stream()
				.flatMap(Element::children)
				.forEach(e -> surface(e, sys));

		return sys;
	}

	/**
	 * Loads the particle system characteristics.
	 */
	private static Characteristic[] characteristics(Element root) {
		return root
				.children("characteristic")
				.map(Element::text)
				.map(Characteristic::valueOf)
				.toArray(Characteristic[]::new);
	}

	/**
	 * Loads the generation policy.
	 */
	private GenerationPolicy policy(Element root) {
		return switch(root.name()) {
			case "none" -> GenerationPolicy.NONE;

			case "fixed" -> GenerationPolicy.fixed(integer(root, "num"));

			case "incremental" -> {
				final int inc = integer(root, "increment");
				final int max = integer(root, "max");
				yield new IncrementGenerationPolicy(inc, max);
			}

			default -> throw root.exception("Unknown generation policy");
		};
	}

	/**
	 * Loads the emitter position factory.
	 */
	private PositionFactory position(Element root) {
		return switch(root.name()) {
			case "origin" -> PositionFactory.ORIGIN;

			case "literal" -> {
				final Point p = point(root, null);
				yield PositionFactory.of(p);
			}

			case "box" -> {
				final Point min = point(root, "min");
				final Point max = point(root, "max");
				final Bounds bounds = new Bounds(min, max);
				yield PositionFactory.box(bounds, randomiser);
			}

			case "sphere" -> {
				final Point centre = point(root, "centre");
				final float radius = value(root, "radius");
				final var sphere = new SphereVolume(centre, radius);
				yield PositionFactory.sphere(sphere, randomiser);
			}

			default -> throw root.exception("Unknown position factory");
		};
	}

	/**
	 * Loads the emitter direction factory.
	 */
	private VectorFactory direction(Element root) {
		return switch(root.name()) {
			case "literal" -> VectorFactory.of(vector(root, null));

			case "random" -> VectorFactory.random(randomiser);

			case "cone" -> {
				final Vector normal = vector(root, "normal");
				final float radius = value(root, "radius");
				yield new ConeVectorFactory(normal, radius, randomiser);
			}

			default -> throw root.exception("Unknown vector factory");
		};
	}

	/**
	 * Loads an influence.
	 */
	private Influence influence(Element root) {
		return switch(root.name()) {
			case "literal" -> Influence.of(vector(root, null));
			case "velocity" -> Influence.velocity(value(root, null));
			default -> throw root.exception("Unknown influence");
		};
	}

	private void surface(Element root, ParticleSystem sys) {
		final Collision collision = collision(root);
		final Intersected surface = surface(root.first());
		sys.add(surface, collision);
	}

	private Intersected surface(Element root) {
		return switch(root.name()) {
			case "plane" -> plane(root);
			case "behind" -> plane(root).behind();
			case "negative" -> plane(root).negative();
			default -> throw root.exception("Unknown collision surface");
		};
//
//		// .behind()
//		return new Plane(Axis.Y, 0).behind();
	}

	private static Plane plane(Element root) {
		final Vector normal = vector(root, "normal");
		final float dist = value(root, "distance");
		return new Plane(normal, dist);
	}

	private Collision collision(Element root) {
		return switch(root.name()) {
			case "destroy" -> Collision.DESTROY;
			case "stop" -> Collision.STOP;
			case "reflect" -> {
				final float absorb = root.child("absorb").map(Element::text).map(Float::parseFloat).orElse(1f);
				yield new ReflectionCollision(absorb);
			}
			default -> throw root.exception("Unknown collision action");
		};
	}

	/**
	 * Loads an integer attribute.
	 */
	private static int integer(Element root, String name) {
		try {
			return Integer.parseInt(root.first(name).text().trim());
		}
		catch(NumberFormatException e) {
			throw root.exception(e.getMessage());
		}
	}

	/**
	 * Loads a floating-point attribute.
	 */
	private static float value(Element root, String name) {
		try {
			return Float.parseFloat(root.first(name).text().trim());
		}
		catch(NumberFormatException e) {
			throw root.exception(e.getMessage());
		}
	}

	/**
	 * Loads a comma-delimited literal vector.
	 */
	private static Vector vector(Element root, String name) {
		// Load text or attribute value
		final String text = name == null ? root.text() : root.first(name).text();

		// Tokenize
		final String[] parts = text.trim().split(",");
		if(parts.length != 3) throw root.exception("Invalid tuple");

		// Convert to XYZ floats
		final float[] array = new float[3];
		try {
			for(int n = 0; n < array.length; ++n) {
				array[n] = Integer.parseInt(parts[n].trim());
			}
		}
		catch(NumberFormatException e) {
			throw root.exception(e.getMessage());
		}

		// Create tuple
		return new Vector(array);
	}

	/**
	 * Loads a literal point.
	 */
	private static Point point(Element root, String name) {
		return new Point(vector(root, name));
	}
}
