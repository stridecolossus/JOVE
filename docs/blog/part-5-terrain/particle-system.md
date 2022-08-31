---
title: Particle System
---

---

## Contents

- [Overview](#overview)
- [Particle System](#particle-system)
- [Intersections](#intersections)

---

## Introduction

### Overview

In this chapter we will implement a _particle system_ that can be configured to support multiple scenarios, such as falling snow, smoke, explosions, etc.
This will build on the point-cloud and geometry shader functionality introduced in the previous chapter.

TODO - software vs hardware

### Requirements

To determine some requirements and constraints for what is considered to be a particle system we sketched the properties of a few scenarios:

scenario    | generation    | emitter   | trajectory    | forces        | bounds        | rendering         |
--------    | ----------    | -------   | ----------    | ----------    | ------        | ---------         |
snow        | constant      | box       | down          | breeze        | ground        | texture           |
fountain    | constant      | cone      | up            | gravity       | ground        | blue              |
sparks      | periodic      | point     | ballistic     | gravity       | ground        | colour fade       |
smoke       | constant      | cone      | up            | n/a           | time          | colour fade       |
explosion   | once          | sphere    | expanding     | n/a           | time          | colour fade       |
fireworks   | periodic      | point     | ballistic     | gravity       | time          | colour            |
balls       | once          | box       | constant      | n/a           | bounding box  | random colour     |

Where:

* A _ballistic_ trajectory implies the particle is 'fired' (and generally implies a gravitational influence).

* The _bounds_ specifies whether particles have a finite lifetime or are constrained by some geometric surface.

* A _colour fade_ indicates that the particle colour would fade over time, e.g. smoke particles would perhaps start as grey and fade to black.

* The bouncing _balls_ scenario is something of special case where a group of particles are continually reflected off the inside of an enclosing bounding box.

From the above the following requirements can be derived for particles:

* Generated according to a configurable policy, either a one-off allocation or replenished per-frame.

* Initialised according to the _emitter_ and _trajectory_ properties.

* Optionally has a finite lifetime.

* Can collide with a geometric surface or terrain where the particle either stops, is destroyed, or is reflected (e.g. sparks would bounce a bit).

* Has a colour which is either a constant or fades over time.

### Design

For the particle system design we considered two approaches (there are almost certainly others) for how the _trajectory_ of a particle could be specified:

* A particle has a movement vector which is a mutable property applied to its position on each frame.

* A particle has a trajectory _function_ that calculates the position of the particle at a given instant.

There are pros and cons to either of these approaches: A trajectory function is appealing in that it encapsulates all the information about how that particle moves over time, and is therefore largely immutable, simpler to comprehend, and more easily tested.  However the iterative, mutable approach is probably simpler to implement, if a little more complex to configure.

After some trials we opted for the iterative approach where the configuration of the particle trajectory is comprised of:

* An _emitter_ for the initial position of the particle.

* A factory for the initial movement vector.

* One-or-more influences that mutate the particle on each frame.

Note that the particle colour will be determined in the fragment shader, implying that particles have a creation timestamp used to calculate the colour based on age.

We will progressively build up particle system functionality using the _sparks_ scenario as a test case as this covers all the requirements.

---

## Particle System

### Framework

The logical starting point is the definition of a _particle_ comprising a trajectory and creation timestamp:

```java
public class Particle {
    private final long time;
    private Point pos;
    private Vector dir;

    public void move(Vector vec) {
        pos = pos.add(vec);
    }

    public void add(Vector vec) {
        dir = dir.add(vec);
    }

    void buffer(ByteBuffer bb) {
        pos.buffer(bb);
    }
}
```

The _particle system_ is a controller implemented as an animation:

```java
public class ParticleSystem implements Animation {
    private PositionFactory pos = PositionFactory.ORIGIN;
    private VectorFactory vec = VectorFactory.of(Vector.Y);
    private final List<Particle> particles = new ArrayList<>();
}
```

The two configurable factories specify the _emitter_ of the particle system, where the _position factory_ initialises the starting position of new particles:

```java
@FunctionalInterface
public interface PositionFactory {
    /**
     * @return Particle position
     */
    Point position();

    /**
     * Origin factory.
     */
    PositionFactory ORIGIN = of(Point.ORIGIN);

    /**
     * Creates a position factory at the given point.
     * @param pos Position
     * @return Literal position factory
     */
    static PositionFactory of(Point pos) {
        return () -> pos;
    }
}
```

And the _vector factory_ initialises the movement vector:

```java
@FunctionalInterface
public interface VectorFactory {
    /**
     * Generates the initial particle movement vector.
     * @param pos Initial particle position
     * @return Movement vector
     */
    Vector vector(Point pos);

    /**
     * Creates a factory with a fixed initial vector.
     * @param vec Movement vector
     * @return Literal vector factory
     */
    static VectorFactory of(Vector vec) {
        return ignored -> vec;
    }
}
```

These interfaces provide factory methods for literal positions and vectors, more specialised implementations will be added as the demo progresses.

New particles can now be programatically added to the system:

```java
public void add(int num, long time) {
    for(int n = 0; n < num; ++n) {
        Point start = pos.position();
        Vector dir = vec.vector(start);
        Particle p = new Particle(time, start, dir);
        particles.add(p);
    }
}
```

On a frame update the position of each particle is moved by its current direction:

```java
@Override
public boolean update(Animator animator) {
    float elapsed = animator.elapsed() / 1000f; // TODO
    for(Particle p : particles) {
        move(p, elapsed);
    }
    return false;
}
```

Which delegates to the following helper method:

```java
private void move(Particle p, float elapsed) {
    Vector vec = p.direction().multiply(elapsed);
    p.move(vec);
}
```

Note that the _elapsed_ duration is scaled to milliseconds-per-second, i.e. the movement vector is assumed to be expressed as a velocity in seconds.

### Model

To render the particles the following model implementation is essentially an adapter for the particle system:

```java
public class ParticleModel extends AbstractModel {
    private static final CompoundLayout LAYOUT = CompoundLayout.of(Point.LAYOUT);

    private final ParticleSystem sys;

    public ParticleModel(ParticleSystem sys) {
        super(Primitive.POINTS, LAYOUT);
        this.sys = notNull(sys);
    }

    @Override
    public int count() {
        return sys.size();
    }

    @Override
    public Bufferable vertices() {
        return vertices;
    }
}
```

Where the vertex buffer delegates to the `buffer` method of the particles:

```java
private final Bufferable vertices = new Bufferable() {
    @Override
    public int length() {
        return sys.size() * LAYOUT.stride();
    }

    @Override
    public void buffer(ByteBuffer bb) {
        for(Particle p : sys.particles()) {
            p.buffer(bb);
        }
    }
};
```

Note that the original model `Header` is now composed into the model class since the draw `count` is no longer a static property (and a separate header record provided little benefit anyway).
A skeleton implementation is also introduced with an empty index buffer.

### Influences

With a basic framework in place we can now introduce further functionality for the various particle system use-cases outlined above.

An _influence_ modifies some property of the particles on each frame: 

```java
public interface Influence {
    /**
     * Applies this influence to the given particle.
     * @param p             Particle to influence
     * @param elapsed       Elapsed scalar
     */
    void apply(Particle p, float elapsed);
}
```

The simplest implementation simulates an acceleration (e.g. gravity) by modifying the particle movement vector:

```java
static Influence of(Vector vec) {
    return (p, elapsed) -> p.add(vec.multiply(elapsed));
}
```

Influences are applied on each `update` by the following helper:

```java
private void influence(Particle p, float elapsed) {
    for(Influence inf : influences) {
        inf.apply(p, elapsed);
    }
}
```

### Generation

To bound a particle system an optional `lifetime` property is added and expired particles are removed at the start of the `update` method:

```java
if(lifetime < Long.MAX_VALUE) {
    long expired = animator.time() - lifetime;
    particles.removeIf(p -> p.time() < expired);
}
```

In most cases the particle system will be required to generate new particles on each frame, which is configured by the following policy:

```java
public interface Policy {
    /**
     * Determines the number of particles to add on each frame.
     * @param current Current number of particles
     * @return New particles to generate
     */
    int count(int current);

    /**
     * Policy for a particle system that does not generate new particles.
     */
    Policy NONE = ignored -> 0;
}
```

A basic incremental implementation is provided:

```java
static Policy increment(int inc) {
    return ignored -> inc;
}
```

And an adapter that caps the maximum number of particles:

```java
default Policy max(int max) {
    return current -> {
        int count = Policy.this.count(current);
        return Math.min(count, max - current);
    };
}
```

A new step is added to the end of the `update` method to generate new particles according to the configured policy, scaled by the _elapsed_ modifier:

```java
void generate(float elapsed) {
    float num = policy.count(size()) * elapsed;
    if(num > 0) {
        add((int) num, time);
    }
}
```

### Box Emitter

Several scenarios require an emitter defined as a box (or rectangle) which is implemented by a new type specified by min-max extents:

```java
public record Bounds(Point min, Point max) {
    public Point centre() {
        return min.add(max).multiply(MathsUtil.HALF);
    }
}
```

Particles can now be randomly generated within this volume:

```java
static PositionFactory box(Bounds bounds, VectorRandomiser randomiser) {
    Point min = bounds.min();
    Vector range = Vector.between(min, bounds.max());
    return () -> {
        Vector vec = randomiser.randomise().multiply(range);
        return new Point(vec).add(min);
    };
}
```

Which uses a new utility class to generate a randomised vector:

```java
public class VectorRandomiser {
    private final Random random;
    private final float[] array = new float[3];

    public Vector randomise() {
        for(int n = 0; n < array.length; ++n) {
            array[n] = random.nextFloat();
        }
        return new Vector(array);
    }
}
```

Note that in this case `multiply` is a new method on the the vector class that performs a component-wise multiplication operation.

### Cone Emitter

Scenarios that involve ballistic trajectories require an emitter that randomises the initial movement vector:

```java
public class ConeVectorFactory implements VectorFactory {
    private final Vector normal;
    private final float radius;
    private final Random random;
}
```

TODO

### Integration

fountain
point origin
cone
gravity influence
simple age cull

---

## Intersections

### Rays

To support particle collisions we introduce _ray intersections_ where a _ray_ is specified as a vector relative to an origin:

```java
public interface Ray {
    /**
     * @return Ray origin
     */
    Point origin();

    /**
     * @return Ray direction
     */
    Vector direction();
}
```

The following helper is used to determine a point on the ray at a given distance from the origin, i.e. solves the line equation:

```java
default Point point(float dist) {
    Point origin = this.origin();
    Vector dir = this.direction();
    return origin.add(dir.multiply(dist));
}
```

Next a new abstraction defines some arbitrary geometry that can be intersected by a ray:

```java
public interface Intersects {
    /**
     * Determines the intersections of this surface with the given ray.
     * @param ray Ray
     * @return Intersections
     */
    Iterator<Intersection> intersections(Ray ray);
}
```

Note that the results are returned as an _iterator_ as opposed to a stream or list, this allows intersections to be lazily evaluated as required.  For example, picking only requires knowing _whether_ an intersection has occurred, calculating the actual intersection points (and particularly the surface normals) would be an unnecessary overhead.

An _intersection_ records the distance of the result on a given ray:

```java
public class Intersection {
    private final Ray ray;
    private final float dist;
    private final Function<Point, Vector> normal;
    private Point pos;
}
```

Where the intersection point is calculated using the above helper method:

```java
public Point point() {
    if(pos == null) {
        pos = ray.point(dist);
    }
    return pos;
}
```

The surface `normal` is also lazily evaluated since it is only relevant for reflecting particles:

```java
public Vector normal() {
    return normal.apply(point());
}
```

Finally a convenience constant is added for the case of an empty set of results:

```java
Iterator<Intersection> NONE = new Iterator<>() {
    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public Intersection next() {
        throw new NoSuchElementException();
    }
};
```

### Planes

The simplest collision surface implementation is a _plane_ defined by a normal and a distance from the origin:

```java
public record Plane(Vector normal, float distance) implements Intersects {
    public float distance(Point pt) {
        return normal.dot(pt) + distance;
    }
}
```

In addition to the canonical constructor a plane can be specified by the normal and a point on the plane:

```java
public static Plane of(Vector normal, Point pt) {
    return new Plane(normal, -pt.dot(normal));
}
```

Finally a plane can be constructed from a triangle of points lying in the plane:

```java
public static Plane of(Point a, Point b, Point c) {
    Vector u = Vector.between(a, b);
    Vector v = Vector.between(b, c);
    Vector normal = u.cross(v).normalize();
    return of(normal, a);
}
```

The standard ray-plane intersection algorithm is implemented as follows:

```java
public Iterator<Intersection> intersections(Ray ray) {
    // Calc denominator
    float denom = normal.dot(ray.direction());

    // Stop if parallel
    if(MathsUtil.isZero(denom)) {
        return NONE;
    }

    // Calc intersection
    float t = -distance(ray.origin()) / denom;
    if(t < 0) {
        return NONE;
    }

    // Build intersection
    return List.of(new Intersection(ray, t, normal)).iterator();
}
```

Note that here the surface normal is not lazily evaluated since it is a property of the plane itself.

Although not relevant for particle collisions the notion of a plane _half-space_ is also introduced here for completeness:

```java
public enum HalfSpace {
    POSITIVE,
    NEGATIVE,
    INTERSECT
}
```

The half-space defines the _sides_ of the plane with respect to the normal, where the positive half-space is in _front_ of the plane.

The half-space for a given distance from the plane is determined by the following helper:

```java
public static HalfSpace of(float d) {
    if(d < 0) {
        return NEGATIVE;
    }
    else
    if(d > 0) {
        return POSITIVE;
    }
    else {
        return INTERSECT;
    }
}
```

Which is used to determine the half-space for a given point:

```java
public HalfSpace halfspace(Point pt) {
    return HalfSpace.of(distance(pt));
}
```

### Collisions

Particle collisions are configured by an intersecting surface and an associated action:

```java
public class ParticleSystem implements Animation {
    private final Map<Intersects, CollisionAction> surfaces = new HashMap<>();

    public enum CollisionAction {
        DESTROY,
        STOP,
        REFLECT
    }
}
```

The particle class is modified to implement the new `Ray` abstraction and can now be tested for collisions:

```java
private void collide(Particle p) {
    for(var entry : surfaces.entrySet()) {
        Intersects surface = entry.getKey();
        Iterator<Intersection> intersections = surface.intersections(p);
        if(!intersections.hasNext()) {
            continue;
        }
        ...
    }
}
```

The destroy and stop actions delegate to new particle mutators:

```java
switch(entry.getValue()) {
    case DESTROY -> p.destroy();
    case STOP -> p.stop();
    case REFLECT -> ...
}
```

Where a destroyed particle is signalled by a `null` position:

```java
public boolean isAlive() {
    return pos != null;
}

void destroy() {
    pos = null;
}
```

And similarly for a particle that has been stopped:

```java
public boolean isIdle() {
    return vec == null;
}

void stop() {
    vec = null;
}
```

### Reflection

The reflection case is slightly more involved, here the particle is reflected about the first (arbitrarily selected) intersection:

```java
case REFLECT -> {
    Intersection intersection = surface.intersections(p).next();
    p.reflect(intersection.point(), intersection.normal());
}
```

Which delegates to the following new particle mutator:

```java
void reflect(Point intersection, Vector normal) {
    pos = notNull(intersection);
    vec = vec.reflect(normal);
}
```

Which in turn delegates to a new method to reflect a vector about a normal:

```java
public Vector reflect(Vector normal) {
    float f = -2f * dot(normal);
    return normal.multiply(f).add(this);
}
```

Note that as things stand this approach simply moves the particle to the intersection point and reflects the movement vector.
This may produce poor results for large elapsed durations since the distance travelled (or remaining) is not taken into account.

### Parallel Refactor

There are now several issues with the existing code:

* Particles that are destroyed by a collision are removed using `removeAll` which is extremely inefficient for this scenario, especially for large numbers of particles.

* The time and elapsed parameters have to be passed around the various methods and lambdas, making the code somewhat messy.

* Ideally the process would employ parallel streams rather than imperative, single-threaded loops.

Therefore a new local helper class is implemented that encapsulates the update process:

```java
private class Helper {
    private static final float SECONDS = 1f / TimeUnit.SECONDS.toMillis(1);

    private final long time;
    private final float elapsed;

    private Helper(Animator animator) {
        this.time = animator.time();
        this.elapsed = animator.elapsed() * SECONDS;
    }
}
```

An instance is created on each frame and the code is simplified to the main steps:

```java
public boolean update(Animator animator) {
    Helper helper = new Helper(animator);
    helper.expire();
    helper.update();
    helper.cull();
    helper.generate();
    return false;
}
```

Expired particles are now destroyed as a parallel stream operation:

```java
void expire() {
    if(lifetime == Long.MAX_VALUE) {
        return;
    }

    long expiry = time - lifetime;

    particles
        .parallelStream()
        .filter(p -> p.time() < expiry)
        .forEach(Particle::destroy);
}
```

And the update step is refactored similarly to the following cleaner and parallelised code:

```java
void update() {
    particles
        .parallelStream()
        .filter(Predicate.not(Particle::isIdle))
        .forEach(this::update);
}
```

Which invokes the following method comprising the existing code for each particle:

```java
private void update(Particle p) {
    influence(p);
    move(p);
    collide(p);
}
```

Finally particles that have been destroyed by a collision are culled:

```java
void cull() {
    particles = particles
        .parallelStream()
        .filter(Particle::isAlive)
        .collect(toCollection(ArrayList::new));
}
```

Note that the reference to the `particles` collection is now mutable and is over-written when destroyed particles are culled.

### Integration

The existing demo is modified 

by the introduction of a collision 

the particle lifetime is left as the default (infinite lifetime) and replaced by a collision surface that

sparks
floor
reflection

TODO - multiple scenarios ~ Spring profile

significantly faster, can see multiple cores

---

## Summary

In this chapter a configurable particle system was implemented with support for planes and ray intersections.
