package org.sarge.jove.particle;

import static org.sarge.lib.util.Check.notNull;

import java.io.*;

import org.sarge.jove.common.Colour;
import org.sarge.jove.geometry.*;
import org.sarge.jove.geometry.Plane.HalfSpace;
import org.sarge.jove.geometry.Ray.Intersected;
import org.sarge.jove.io.ResourceLoader;
import org.sarge.jove.particle.ParticleSystem.Characteristic;
import org.sarge.jove.util.Randomiser;
import org.sarge.lib.util.*;
import org.sarge.lib.util.Element.Content;

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

		// Load maximum number of particles
		root.optional("max").map(Element::text).map(Content::toInteger).ifPresent(sys::max);

		// Load generation policy
		root
				.optional("policy")
				.map(Element::child)
				.map(this::policy)
				.ifPresent(sys::policy);

		// Load particle lifetime
		root
				.optional("lifetime")
				.map(Element::text)
				.map(c -> c.transform(Converter.DURATION))
				.ifPresent(sys::lifetime);

		// Load emitter position
		root
				.optional("position")
				.map(Element::child)
				.map(this::position)
				.ifPresent(sys::position);

		// Load emitter direction
		root
				.optional("vector")
				.map(Element::child)
				.map(this::direction)
				.ifPresent(sys::vector);

		// Load particle colour
		root
				.optional("colour")
				.map(Element::child)
				.map(this::colour)
				.ifPresent(sys::colour);

		// Load influences
		root
				.optional("influences")
				.stream()
				.flatMap(Element::children)
				.map(this::influence)
				.forEach(sys::add);

		// Load collision surfaces
		root
				.optional("surfaces")
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
				.map(Content::toString)
				.map(Characteristic::valueOf)
				.toArray(Characteristic[]::new);
	}

	/**
	 * Loads the generation policy.
	 */
	private GenerationPolicy policy(Element root) {
		return switch(root.name()) {
			case "none" -> GenerationPolicy.NONE;

			case "fixed" -> {
				final int num = root.text().toInteger();
				yield GenerationPolicy.fixed(num);
			}

			case "increment" -> {
				final int inc = root.text().toInteger();
				yield new IncrementGenerationPolicy(inc);
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
				final Point p = root.text().transform(this::point);
				yield PositionFactory.of(p);
			}

			case "box" -> {
				final Point min = root.child("min").text().transform(this::point);
				final Point max = root.child("max").text().transform(this::point);
				final Bounds bounds = new Bounds(min, max);
				yield PositionFactory.box(bounds, randomiser);
			}

			case "sphere" -> {
				final Point centre = root.child("centre").text().transform(this::point);
				final float radius = root.child("radius").text().toFloat();
				final var sphere = new Sphere(centre, radius);
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
			case "literal" -> VectorFactory.of(root.text().transform(this::vector));

			case "random" -> VectorFactory.random(randomiser);

			case "cone" -> {
				final Vector normal = root.child("normal").text().transform(this::vector);
				final float radius = root.child("radius").text().toFloat();
				yield new ConeVectorFactory(normal, radius, randomiser);
			}

			default -> throw root.exception("Unknown vector factory");
		};
	}

	private ColourFactory colour(Element root) {
		return switch(root.name()) {
			case "constant" -> {
				final float[] col = tuple(root.text().toString(), 4);
				yield ColourFactory.of(Colour.of(col));
			}

			case "interpolated" -> {
				final float[] start = tuple(root.child("start").text().toString(), 4);
				final float[] end = tuple(root.child("end").text().toString(), 4);
				yield ColourFactory.interpolated(Colour.of(start), Colour.of(end));
			}

			default -> throw root.exception("Unknown colour factory");
		};
	}

	/**
	 * Loads an influence.
	 */
	private Influence influence(Element root) {
		return switch(root.name()) {
			case "literal" -> Influence.of(root.text().transform(this::vector));
			case "velocity" -> Influence.velocity(root.child("velocity").text().toFloat());
			default -> throw root.exception("Unknown influence");
		};
	}

	private void surface(Element root, ParticleSystem sys) {
		final Collision collision = collision(root);
		final Intersected surface = surface(root.child());
		sys.add(surface, collision);
	}

	private Intersected surface(Element root) {
		return switch(root.name()) {
			case "plane" -> plane(root);
			case "behind" -> plane(root).behind();
			case "negative" -> plane(root).halfspace(HalfSpace.NEGATIVE);
			default -> throw root.exception("Unknown collision surface");
		};
	}

	private Plane plane(Element root) {
		final Vector normal = root.child("normal").text().transform(this::vector);
		final float dist = root.child("distance").text().toFloat();
		return new Plane(normal, dist);
	}

	private Collision collision(Element root) {
		return switch(root.name()) {
			case "destroy" -> Collision.DESTROY;
			case "stop" -> Collision.STOP;
			case "reflect" -> {
				final float absorb = root.optional("absorb").map(Element::text).map(Content::toFloat).orElse(1f);
				yield new ReflectionCollision(absorb);
			}
			default -> throw root.exception("Unknown collision action");
		};
	}

	private static float[] tuple(String text, int size) {
		// Tokenize
		final String[] parts = text.split(",");
		if(parts.length != size) throw new IllegalArgumentException("Expected tuple");

		// Convert to XYZ floats
		final float[] array = new float[size];
		for(int n = 0; n < size; ++n) {
			array[n] = Float.parseFloat(parts[n].trim());
		}

		return array;
	}


	/**
	 * Loads a comma-delimited literal vector.
	 * @throws IllegalArgumentException if the vector is not a valid tuple
	 */
	private Vector vector(String text) {
		if(text.length() > 2) {
			return new Vector(tuple(text, 3));
		}
		else
		if(text.startsWith("-")) {
			return axis(text.substring(1)).invert();
		}
		else {
			return axis(text);
		}
	}

	private static Axis axis(String axis) {
		return switch(axis) {
			case "X" -> Axis.X;
			case "Y" -> Axis.Y;
			case "Z" -> Axis.Z;
			default -> throw new IllegalArgumentException("Invalid axis: " + axis);
		};
	}

	/**
	 * Loads a literal point.
	 */
	private Point point(String text) {
		return new Point(tuple(text, 3));
	}
}
