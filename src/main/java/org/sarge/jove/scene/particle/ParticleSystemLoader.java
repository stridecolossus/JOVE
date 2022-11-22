package org.sarge.jove.scene.particle;

import static org.sarge.lib.util.Check.notNull;

import java.io.*;

import org.sarge.jove.common.Colour;
import org.sarge.jove.geometry.*;
import org.sarge.jove.geometry.Plane.HalfSpace;
import org.sarge.jove.geometry.Ray.Intersected;
import org.sarge.jove.io.ResourceLoader;
import org.sarge.jove.scene.particle.ParticleSystem.Characteristic;
import org.sarge.jove.util.*;
import org.sarge.lib.element.*;
import org.sarge.lib.element.Element.Content;
import org.sarge.lib.util.Converter;

/**
 * Loader for a particle system.
 * @author Sarge
 */
public class ParticleSystemLoader implements ResourceLoader<Element, ParticleSystem> {
	private final Randomiser randomiser;
	private final ElementLoader loader = new ElementLoader();
	private final LoaderRegistry<GenerationPolicy> policy;
	private final LoaderRegistry<PositionFactory> position;
	private final LoaderRegistry<VectorFactory> vector;
	private final LoaderRegistry<ColourFactory> colour;

	/**
	 * Constructor.
	 * @param randomiser Randomiser
	 */
	public ParticleSystemLoader(Randomiser randomiser) {
		this.randomiser = notNull(randomiser);
		this.policy = policy();
		this.position = position();
		this.vector = vector();
		this.colour = colour();
	}

	protected LoaderRegistry<GenerationPolicy> policy() {
		return new LoaderRegistry<GenerationPolicy>()
				.literal("none", GenerationPolicy.NONE)
				.register("fixed", Integer::parseInt, GenerationPolicy::fixed)
				.register("increment", Float::parseFloat, IncrementGenerationPolicy::new);
	}

	protected LoaderRegistry<PositionFactory> position() {
		return new LoaderRegistry<PositionFactory>()
				.literal("origin", PositionFactory.ORIGIN)
				.register("literal", Point.CONVERTER, PositionFactory::of)
				.register("sphere", e -> SpherePositionFactory.load(e, randomiser))
				.register("box", e -> BoxPositionFactory.load(e, randomiser))
				.register("circle", e -> PositionFactory.circle(Point.ORIGIN, Disc.load(e, randomiser)));
	}

	protected LoaderRegistry<VectorFactory> vector() {
		return new LoaderRegistry<VectorFactory>()
				.register("literal", Axis::parse, VectorFactory::of)
				.register("random", __ -> VectorFactory.random(randomiser))
				.register("cone", e -> VectorFactory.cone(Disc.load(e, randomiser)));
	}

	protected LoaderRegistry<ColourFactory> colour() {
		return new LoaderRegistry<ColourFactory>()
				.register("literal", Colour.CONVERTER, ColourFactory::of)
				.register("interpolated", this::interpolated);
	}

	// TODO - move to custom ColourInterpolator?
	private ColourFactory interpolated(Element e) {
		final Colour start = e.child("start").text().transform(Colour.CONVERTER);
		final Colour end = e.child("end").text().transform(Colour.CONVERTER);
		final Interpolator interpolator = e.optional("interpolator").map(Element::child).map(Interpolator::load).orElse(Interpolator.LINEAR);
		final var func = Colour.interpolator(start, end, interpolator);
		return func::apply;
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
		root
				.optional("max")
				.map(Element::text)
				.map(Content::toInteger)
				.ifPresent(sys::max);

		// Load generation policy
		root
				.optional("policy")
				.map(Element::child)
				.map(policy::load)
				.ifPresent(sys::policy);

		// Load particle lifetime
		root
				.optional("lifetime")
				.map(Element::text)
				.map(Content::toString)
				.map(Converter.DURATION)
				.ifPresent(sys::lifetime);

		// Load emitter position
		root
				.optional("position")
				.map(Element::child)
				.map(position::load)
				.ifPresent(sys::position);

		// Load emitter direction
		root
				.optional("vector")
				.map(Element::child)
				.map(vector::load)
				.ifPresent(sys::vector);

		// Load particle colour factory
		root
				.optional("colour")
				.map(Element::child)
				.map(colour::load)
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
	 * Loads an influence.
	 */
	private Influence influence(Element root) {
		return switch(root.name()) {
			case "literal" -> Influence.of(root.text().transform(Axis::parse));
			case "velocity" -> Influence.velocity(root.child("velocity").text().toFloat());
			default -> throw root.exception("Unknown influence");
		};
	}
	// TODO - factor out for extension

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
	// TODO - factor out for extension

	private Plane plane(Element root) {
		final Vector normal = root.child("normal").text().transform(Axis::parse);
		final float dist = root.child("distance").text().toFloat();
		return new Plane(new Normal(normal), dist);
	}
	// TODO - factor out to plane?

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
}
