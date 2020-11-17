---
title: Input Handling
---

## Overview

In this chapter we will be taking a break from Vulkan to add support for input event processing and a camera controller.

There are several ways we could have gone about implementing event handling and we did try several different approaches.

We discuss the rationale for the design we eventually ended up with and illustrate some of the challenges we faced.

---

## Analysis

### Requirements

There are a number of differing types of events provided by GLFW that we will support:

type                    | arguments                     | device
----                    | ---------                     | ------
keyboard                | key, action, modifiers        | keyboard
mouse position          | x, y                          | mouse
mouse button            | button, actions, modifiers    | mouse
mouse wheel             | value                         | mouse
window enter/leave      | boolean                       | window
window focus            | boolean                       | window
controller button       | button, press/release         | controller
controller axis         | axis, value                   | controller
controller hat          | hat, press/release            | controller

(There are a few others but that should be enough for starters!)

A _controller_ is defined here as a joystick, gamepad or console controller.

Whilst we could simply use the GLFW functionality directly in our applications there are compelling reasons to introduce a layer of abstraction:

- The GLFW API exposes some underlying details (such as window handle pointers) that we would prefer to hide if possible.

- Some events are implemented by GLFW as callback handlers e.g. `MousePositionListener` and others as query functions, e.g. `glfwGetJoystickAxes()`.

- Several of the event types map to the same general forms - for example the mouse, controller and hat button events are essentially equivalent.

- Traditional event callbacks mix application logic and the event handling framework reducing code re-usability and testability.

Based on these observations we enumerate the following requirements for our design:

- Map the various events to a smaller number of general types.

- Encapsulate the underlying GLFW code that generates input events.

- Separate the event handling framework from the application logic.

- Provide a mechanism to map events to application logic that requires minimal configuration (or ideally none).

- Provide functionality to allow an application to query supported events without having to invoke specific API methods.

> Apparently GLFW version 4 will deprecate callbacks in favour of query methods but we will cross that bridge if and when we upgrade the native library.

### Design

After a bit of analysis we determine that the various events can be generalised to the following:

type        | arguments             | range                         | examples
----        | ---------             | -----                         | --------
position    | x, y                  | n/a                           | mouse move, joystick, controller touch pad
button      | id, action, modifiers | number of buttons or keys     | key, mouse button, controller button
axis        | id, value             | number of axes                | mouse wheel, joystick throttle
boolean     | boolean               | n/a                           | window enter/leave

The _range_ is the possible number of events of a given type.

Our initial design will consist of the following components:

- an _event type_ for each of the above.

- a general _input event_ record.

- a _device_ that generates events and delegates to a single handler.

The approach we are aiming for is probably best illustrated with some pseudo-code:

```java
// Create input device
Device dev = new SomeDevice();

// Initialise device
Handler handler = new Handler();
dev.enable(handler);

// Bind event from this device to an action
Event.Type type = dev.getEventType("SomeEvent");
Action action = event -> { ... };
handler.bind(type, action);
```

---

## Mouse Wheel

As it turns out the _axis_ event type is probably the simplest case - we will implement an end-to-end solution for the mouse wheel axis.

### Input Event

We start with the definition of a generic input event and its associated type:

```java
public interface InputEvent<T extends Type> {
    /**
     * @return Type of this event
     */
    T type();

    /**
     * @return X coordinate
     */
    float x();

    /**
     * @return Y coordinate
     */
    float y();

    /**
     * An <i>event type</i> is the descriptor for an input event.
     */
    interface Type {
        /**
         * @return Event name
         */
        String name();
    }
}
```

The implementation for an axis is quite straight forward:

```java
public class Axis implements Type {
    private final String name;

    /**
     * Constructor.
     * @param name Axis name
     */
    public Axis(String name) {
        this.name = notEmpty(name);
    }

    @Override
    public String name() {
        return name;
    }

    /**
     * Creates an axis input event.
     * @param value Axis value
     * @return New axis event
     */
    public Event create(float value) {
        return new Event(value);
    }

    /**
     * Axis event.
     */
    public final class Event implements InputEvent {
        private final float value;

        /**
         * Constructor.
         * @param value Axis value
         */
        private Event(float value) {
            this.value = value;
        }

        @Override
        public float x() {
            return value;
        }

        @Override
        public float y() {
            return value;
        }

        @Override
        public Axis type() {
            return Axis.this;
        }
    }
}
```

Notes:

- The axis event returns the same value for both methods.

- The mouse wheel generates axis _increments_ whereas a joystick throttle (for example) generates _absolute_ axis values.

> All events implement the X and Y coordinates even if they are not required which is not very object orientated - we did try other approaches using generics, double-dispatching, etc. but the results were always ugly, at least this one is simple.

### Device

Next we define the device:

```java
interface Device {
    /**
     * @return Device name
     */
    String name();
    
    /**
     * @return Event sources for this device
     */
    Set<Source<?>> sources();
}
```

We introduce an _event source_ that generates events to a given handler:

```java
interface Source<T extends InputEvent> {
    /**
     * @return Events generated by this source
     */
    List<? extends Type> types();

    /**
     * Enables generation of events.
     * @param handler Event handler
     */
    void enable(Consumer<InputEvent<?>> handler);

    /**
     * Disables event generation.
     */
    void disable();
}
```

The purpose of the event source is to provide:

- an additional binding point for an application, i.e. we can bind to a specific type of event or **all** events from a given source.

- a means for an application to programatically query the events that are supported by a device.

For the purposes of our end-to-end walk-through we create a _mouse device_ with an event source for the wheel axis:

```java
public class MouseDevice implements Device {
    private final Window window;

    /**
     * Constructor.
     * @param window Parent window
     */
    MouseDevice(Window window) {
        this.window = notNull(window);
    }

    @Override
    public String name() {
        return "Mouse";
    }

    public Source<Axis.Event> wheel() {
        ...
    }

    @Override
    public Set<Source<?>> sources() {
        return Set.of(wheel(), ...);    // TODO - others
    }
}
```

For convenience we provide an explicit `wheel()` accessor as well as returning it in the `sources()` method.

The mouse wheel source is comprised of a single axis which is bound to a GLFW `MouseScrollListener` when enabled:

```java
public Source<Axis.Event> wheel() {
    return new Source<>() {
        private final Axis wheel = new Axis("Wheel");

        @Override
        public List<Axis> types() {
            return List.of(wheel);
        }

        @Override
        public void enable(Consumer<InputEvent<?>> handler) {
            final MouseScrollListener listener = (ptr, x, y) -> handler.accept(wheel.create((float) y));
            apply(listener);
        }

        @Override
        public void disable() {
            apply(null);
        }

        private void apply(MouseScrollListener listener) {
            window.library().glfwSetScrollCallback(window.handle(), listener);
        }
    };
}
```

### Integration #1

To exercise the mouse wheel we first create a mouse device for the window:

```java
class Window {
    /**
     * @return New mouse device
     */
    public MouseDevice mouse() {
        return new MouseDevice(this);
    }
}
```

We can then enable generation of events from this device and dump the events to the console:

```java
final Consumer<InputEvent<?>> handler = System.out::println;
final MouseDevice mouse = window.mouse();
mouse.wheel().enable(handler);
```

After testing that we can see the events being generated we bodge the event handler to move the model when we use the mouse wheel:

```java
final AtomicInteger z = new AtomicInteger();
final Consumer<InputEvent<?>> handler = event -> z.addAndGet((int) event.y());

...

while(...) {
    // Poll input events
    desktop.poll();

    // Update camera translation
    final Matrix trans = new Matrix.Builder()
        .identity()
        .column(3, new Point(0, 0, -z.get()))
        .build();

    // Update scene matrix
    final Matrix view = rot.multiply(trans);
    final Matrix matrix = proj.multiply(view).multiply(modelMatrix);
    uniform.load(matrix);

    ...
}
```

Notes:
- We use an atomic integer because we need an effectively `final` value in the handler lambda.
- The camera translation is moved to the render loop so that it is recalculated on each frame.
- We also add `poll()` for input events.

We should now be able to move the model in the Z direction using the mouse wheel.

This satisfies our first three requirements (for the mouse wheel anyway) but doesn't achieve anything that we couldn't have done without all this framework.  We will persevere with the implementation for the other events and devices so that we have an overall picture of whether our design is working (or not as the case may be).

---

## Camera

Next we will replace the hard-coded view transformation with a _camera_ model.  We can then implement _position events_ and a _mouse pointer_ event source to control the camera using the mouse.

### Camera Model

The camera is a model class representing the orientation and position of the viewer:

```java
public class Camera {
    private Point pos = Point.ORIGIN;
    private Vector dir = Vector.Z_AXIS;
    private Vector up = Vector.Y_AXIS;
}
```

Under the hood the camera direction is actually the inverse of the view direction (since we move the scene in the opposite direction to the camera) hence:

```java
/**
 * @return Camera view direction
 */
public Vector direction() {
    return dir.invert();
}

/**
 * Sets the camera view direction.
 * @param dir View direction (assumes normalized)
 */
public void direction(Vector dir) {
    this.dir = dir.invert();
    dirty();
}
```

The camera (or view transformation) matrix is calculated as follows:

```java
public Matrix matrix() {
    // Determine right axis
    right = up.cross(dir).normalize();

    // Determine up axis
    final Vector y = dir.cross(right).normalize();

    // Calculate translation component
    final Matrix trans = Matrix.translation(new Vector(pos).invert());

    // Build rotation matrix
    final Matrix rot = new Matrix.Builder()
        .identity()
        .row(0, right)
        .row(1, y.invert())
        .row(2, dir)
        .build();

    // Create camera matrix
    matrix = rot.multiply(trans);
}
```

The translation matrix is built using a new helper:

```java
public static Matrix translation(Vector vec) {
    return new Builder().identity().column(3, vec).build();
}
```

Notes:

- The translation component is also inverted (for the same reason as the camera direction).
- We invert the local up axis of the camera since the Y axis is inverted for Vulkan.
- The camera only updates the matrix when any of its properties have been modified (signalled by the `dirty()` method).

We provide various mutators to move the camera position:

```java
/**
 * Moves the camera to a new position.
 * @param pos New position
 */
public void move(Point pos) {
    this.pos = notNull(pos);
    dirty();
}

/**
 * Moves the camera by the given vector.
 * @param vec Movement vector
 */
public void move(Vector vec) {
    pos = pos.add(vec);
    dirty();
}

/**
 * Moves the camera by the given distance in the current view direction.
 * @param dist Distance to move
 * @see #direction()
 */
public void move(float dist) {
    move(dir.scale(dist));
}

/**
 * Moves the camera by the given distance in the current right axis.
 * @param dist Distance to strafe
 * @see #right()
 */
public void strafe(float dist) {
    move(right.scale(dist));
}
```

Finally we add the following helper to point the camera at a given location:

```java
public void look(Point pt) {
    dir = Vector.of(pt, pos).normalize();
    dirty();
}
```

### Integration #2

We replace the hard-coded matrices with the new camera class and check that the scene is still correctly rendered:

```java
// Init camera
final Camera cam = new Camera();
cam.move(new Point(0, 0, 1));

// Bind mouse wheel
final AtomicInteger z = new AtomicInteger();
final Consumer<InputEvent<?>> handler = event -> cam.move(event.y());

while(...) {
    // Poll input events
    desktop.poll();

    // Update scene matrix
    Matrix matrix = proj.multiply(cam.matrix()).multiply(modelMatrix);
    uniform.load(matrix);
    ...
}
```

There are plenty of opportunities for making a mess of all these matrices so we:
- dump the rotation and translation components of the camera to the console to compare against the previous hand-coded matrices.
- double-check that the matrices are composed in the correct order (both in the camera and when we upload to the uniform buffer).
- check that the camera direction and translation are inverted with respect to the scene.
- ensure that the unit-test coverage is as high as possible to trap any simple bugs or flaws.

### Position Events

The next type of event we tackle is the _position event_ that is used for a joystick or the mouse pointer device:

```java
public final class Position implements Type {
    private final String name;

    public Position(String name) {
        this.name = notEmpty(name);
    }

    @Override
    public String name() {
        return name;
    }

    /**
     * Position event instance.
     */
    public record Event(Position type, float x, float y) implements InputEvent {
    }
}
```

We add a second source to the mouse device for the pointer:

```java
public Source<Position.Event> pointer() {
    return new Source<>() {
        private final Position pos = new Position("Pointer");

        @Override
        public List<Position> types() {
            return List.of(pos);
        }

        @Override
        public void enable(InputEvent.Handler handler) {
            final MousePositionListener listener = (ptr, x, y) -> handler.accept(new Position.Event(pos, (float) x, (float) y));
            apply(listener);
        }

        @Override
        public void disable() {
            apply(null);
        }

        private void apply(MousePositionListener listener) {
            window.library().glfwSetCursorPosCallback(window.handle(), listener);
        }
    };
}
```

### Orbital Camera

The camera class is a simple model class - to implement richer functionality we introduce a  _camera controller_ that is invoked by action handlers.

#### Controller

An _orbital_ (or arcball) camera controller rotates the view position about a target point-of-interest.

Rather than complicate the existing camera or derive a new sub-class we opt to create a separate _controller_ class:

```java
public class OrbitalCameraController {
    private final Camera cam;
    private Point target = Point.ORIGIN;
    private final Dimensions dim;
}
```

Moving the camera around the _target_ involves the following steps on a positional input event:
1. Map the position to yaw-pitch angles.
2. Calculate the resultant camera position.
3. Point the camera at the target.

This is implemented in the `update()` method of the controller:

```java
public void update(float x, float y) {
    final float phi = horizontal.interpolate(x / dim.width());
    final float theta = vertical.interpolate(y / dim.height());
    final Point pos = Sphere.point(phi - MathsUtil.HALF_PI, theta, orbit.radius);
    cam.move(target.add(pos));
    cam.look(target);
}
```

The _phi_ angle is a counter-clockwise rotation about the Y axis (or _yaw_ angle) and _theta_ is the vertical rotation (or _pitch_ angle).  Note that a _phi_ of zero 'points' in the X direction - hence we fiddle the angle by 90 degrees in the `update()` method to rotate to the negative Z axis.

#### Supporting Geometry

An _interpolator_ is a mathematical function to scale a value over a range (such as an animation duration):

```java
@FunctionalInterface
public interface Interpolator {
    /**
     * Applies this interpolator to the given value.
     * @param value Value to be interpolated
     * @return Interpolated value
     */
    float interpolate(float value);

    /**
     * Creates a linear (or <i>lerp</i>) interpolator over the given range.
     * @param start     Range start
     * @param end       Range end
     * @return Linear interpolator
     * @see #lerp(float, float, float)
     */
    static Interpolator linear(float start, float end) {
        return value -> start + value * (end - start);
    }
}
```

The interpolator provides a variety of interpolation functions (often referred to as _tweening_ or _easing functions_) that will be used in future chapters.
Here we use a simple _linear_ interpolation that maps the window dimensions to yaw-pitch angles (in radians):

```java
private Interpolator horizontal = Interpolator.linear(0, MathsUtil.TWO_PI);
private Interpolator vertical = Interpolator.linear(-MathsUtil.HALF_PI, MathsUtil.HALF_PI);
```

Note that the ranges are different for the two rotation axes.

To calculate the camera position we implement a _sphere_ geometry class which calculates a point on the surface of the sphere given a radius and the yaw-pitch angles:

```java
public record Sphere(float radius) {
    ...
    
    public static Point point(float phi, float theta, float radius) {
        final float cos = MathsUtil.cos(theta);
        final float x = radius * cos * MathsUtil.cos(phi);
        final float y = radius * MathsUtil.sin(theta);
        final float z = radius * cos * MathsUtil.sin(phi);
        return new Point(x, y, z);
    }
}
```

#### Zoom

The orbital controller also supports a _zoom_ function to move the eye position towards or away from the target:

```java
public void zoom(float inc) {
    final float actual = orbit.zoom(-inc);
    cam.move(actual);
}
```

The _orbit_ properties are factored out to a helper class passed to the controller in its constructor:

```java
public static final class Orbit {
    private final float min;
    private final float max;
    private final float scale;

    private float radius;

    /**
     * Default constructor.
     */
    public Orbit() {
        this(1, Integer.MAX_VALUE, 1);
    }

    /**
     * Constructor.
     * @param min       Minimum radius
     * @param max       Maximum radius
     * @param scale     Zoom scalar
     */
    public Orbit(float min, float max, float scale) {
        if(min >= max) throw new IllegalArgumentException("Invalid zoom range");
        this.min = positive(min);
        this.max = max;
        this.scale = positive(scale);
        this.radius = min;
    }

    /**
     * Increments this radius and clamps to the specified range.
     * @param inc Radius increment
     * @return Actual increment
     */
    private float zoom(float inc) {
        final float prev = radius;
        radius = MathsUtil.clamp(prev + inc * scale, min, max);
        return radius - prev;
    }
}
```

This class clamps the zoom radius to the specified range and prevents the camera being moved onto the target position.

### Integration #3

We can now add an orbital camera controller to the demo and bind it to the mouse pointer and wheel:

```java
// Init camera controller
OrbitalCameraController controller = new OrbitalCameraController(cam, chain.extents(), new Orbit(0.75f, 25, 0.1f));
controller.radius(3);

// Enable mouse
final MouseDevice mouse = window.mouse();
final var pointer = mouse.pointer();
final var wheel = mouse.wheel();
pointer.enable(event -> controller.update(event.x(), event.y()));
wheel.enable(event -> controller.zoom(event.y()));
```

Moving the mouse should rotate the camera about the scene and the mouse-wheel is used to zoom.

Sweet.

---

## Keyboard

The final type of event we will implement in this chapter is a _button event_ for the keyboard and mouse button devices.

### Button Events

A _button event_ is slightly more complex in that it also has an _action_ and a mask of keyboard _modifiers_:

```java
public final class Button implements Type, InputEvent {
    private final String id;
    private final int action;
    private final int mods;

    /**
     * Constructor.
     * @param id        Button identifier
     * @param op        Action 0..2
     * @param mods      Modifiers bit-mask
     */
    public Button(String id, int action, int mods) {
        this.id = notEmpty(id);
        this.action = range(action, 0, 2);
        this.mods = zeroOrMore(mods);
    }

    @Override
    public String name() {
        ...
    }

    @Override
    public Button type() {
        return this;
    }

    @Override
    public float x() {
        throw new UnsupportedOperationException();
    }

    @Override
    public float y() {
        throw new UnsupportedOperationException();
    }
}
```

Note that a button is both an input event **and** its associated type, i.e. unlike the other types a button event does not require any additional data.

We add the following enumerations and accessors to map the GLFW action and modifiers:

```java
public enum Operation {
    RELEASE,
    PRESS,
    REPEAT
}

public enum Modifier implements IntegerEnumeration {
    SHIFT(0x0001),
    CONTROL(0x0002),
    ALT(0x0004),
    SUPER(0x0008),
    CAPS_LOCK(0x0010),
    NUM_LOCK(0x0020)

    private final int value;
}

public Operation operation() {
    return Operation.OPERATIONS[action];
}

public Set<Modifier> modifiers() {
    return IntegerEnumeration.enumerate(Modifier.class, mods);
}
```

The name of a button is a compound string:

```java
@Override
public String name() {
    final StringJoiner str = new StringJoiner(DELIMITER);
    str.add(id);
    str.add(operation().name());
    if(mods > 0) {
        str.add(modifiers().stream().map(Enum::name).collect(joining(DELIMITER)));
    }
    return str.toString();
}
```

For example:

```java
new Button("NAME", 1, 0x0001 | 0x0002)
```

has the name

```
NAME-PRESS-SHIFT-CONTROL
```

### Keyboard Buttons

The keyboard device itself is relatively simple:

```java
public class KeyboardDevice implements Device {
    private final Window window;

    KeyboardDevice(Window window) {
        this.window = notNull(window);
    }

    @Override
    public String name() {
        return "Keyboard";
    }

    @Override
    public Set<Source<?>> sources() {
        return Set.of(keyboard());
    }

    /**
     * @return New keyboard event source
     */
    private Source<Button> keyboard() {
        ...
    }
}
```

However GLFW returns key **codes** that are defined as macros in the header (mapped to a US keyboard layout).  We _could_ simply replicate this as an enumeration but that would require tedious manual text monkeying - instead we opt to load the keys from a text file which requires less formatting and a simple loader class:

```java
private static class KeyTable {
    /**
     * Singleton instance.
     */
    public static final KeyTable INSTANCE = new KeyTable();

    private final Map<Integer, String> table = load();

    private KeyTable() {
    }

    /**
     * Maps a key code to name.
     */
    String map(int code) {
        final String name = table.get(code);
        if(name == null) throw new IllegalArgumentException("Unknown key code: " + code);
        return name;
    }

    /**
     * Loads the standard key table.
     */
    private static Map<Integer, String> load() {
        try(final InputStream in = KeyTable.class.getResourceAsStream("/key.table.txt")) {
            if(in == null) throw new RuntimeException("Cannot find key names resource");
            return new BufferedReader(new InputStreamReader(in))
                    .lines()
                    .map(StringUtils::split)
                    .collect(toMap(tokens -> Integer.parseInt(tokens[1].trim()), tokens -> tokens[0].trim()));
        }
        catch(IOException e) {
            throw new RuntimeException(e);
        }
    }
}
```

The key table is a simple text file (sample shown):

```
SPACE              32
APOSTROPHE         39
COMMA              44
```

The button event source can now be implemented as follows:

```java
private Source<Button> keyboard() {
    return new Source<>() {
        @Override
        public List<Button> types() {
            return List.of();
        }

        @Override
        public void enable(InputEvent.Handler handler) {
            // Create callback adapter
            final KeyListener listener = (ptr, key, scancode, action, mods) -> {
                final String name = KeyTable.INSTANCE.map(key);
                final Button button = new Button(name, action, mods);
                handler.accept(button);
            };

            // Register callback
            apply(listener);
        }

        @Override
        public void disable() {
            apply(null);
        }

        /**
         * Sets the GLFW keyboard listener.
         * @param listener Keyboard listener
         */
        private void apply(KeyListener listener) {
            window.library().glfwSetKeyCallback(window.handle(), listener);
        }
    };
}
```

Notes:

- The `types()` for the keyboard source is empty since the application will generally refer to keys by name.

- GLFW also provides the _scancode_ and the `glfwGetKeyName` API method but this only seems to support a subset of the expected keys.

- At the time of writing the `KeyTable` is hidden as we assume that the GLFW key-codes will not be required outside of the callback listener, we can always expose the table (and refactor as a proper singleton) if this assumption turns out to be wrong.

### Mouse Buttons

Finally we implement the mouse buttons source:

```java
public Source<Button> buttons() {
    return new Source<>() {
        /**
         * @return Number of mouse buttons
         */
        private int count() {
            // TODO - uses AWT, not supported by GLFW
            return MouseInfo.getNumberOfButtons();
        }

        private final Button[] buttons = IntStream
                .rangeClosed(1, count())
                .mapToObj(n -> "Button-" + n)
                .map(Button::of)
                .toArray(Button[]::new);

        @Override
        public List<Button> types() {
            return Arrays.asList(buttons);
        }

        @Override
        public void enable(InputEvent.Handler handler) {
            final MouseButtonListener listener = (ptr, button, action, mods) -> {
                // TODO - action/mods
                handler.accept(buttons[button]);
            };
            apply(listener);
        }

        @Override
        public void disable() {
            apply(null);
        }

        private void apply(MouseButtonListener listener) {
            window.library().glfwSetMouseButtonCallback(window.handle(), listener);
        }
    };
}
```

Notes:

- The `types()` accessor returns a pre-defined list of mouse buttons.

- Surprisingly GLFW does not provide a method to query the number of available mouse buttons, for the moment we implement `count()` using an AWT helper.

### Integration #4

To _finally_ allow the demo to be terminated gracefully we enable keyboard events and bind an action to a _running_ flag in the render loop:

```java
// Bind stop event
final AtomicBoolean running = new AtomicBoolean(true);
final Consumer<InputEvent<?>> keyHandler = key -> {
    if(key.type().name().equals("ESCAPE")) {
        running.set(false);
    }
};
window.keyboard().enable(keyHandler);
...

while(running.get()) {
    // Poll input events
    desktop.poll();
    ...
}
```

The key handler is very ugly (and precisely the sort of switching logic we are trying to avoid) - we will address this in the next section.

If we now run the code again we should be able to ESCAPE the demo.

(We really should have done this much earlier!)

---

## Action Bindings

To make all this work worthwhile the final piece of functionality is to implement a _bindings_ class that maps events to arbitrary _actions_.

### Bindings Class

The bindings class maintains the mappings between event type(s) and actions:

```java
public class Bindings implements Consumer<InputEvent<?>> {
    private final Map<Action<?>, Set<Type>> actions = new HashMap<>();
    private final Map<Type, Action<?>> bindings = new HashMap<>();
    
    ...
    
    @Override
    public void accept(InputEvent event) {
        final Action<?> action = bindings.get(event.type());
        if(action != null) {
            action.accept(event);
        }
    }
}
```

An _action_ is a simple event consumer:

```java
interface Action<T extends Type> extends Consumer<InputEvent<?>> {
    // Marker interface
}
```

Notes:

- The bindings class is itself an event handler.

- Many events can be bound to the same action.

An event is bound to an action as follows:

```java
/**
 * Binds an input event to the given action.
 * @param <T> Event type
 * @param type          Input event
 * @param action        Action handler
 * @throws IllegalStateException if the event is already bound
 */
public <T extends Type> void bind(T type, Action<T> action) {
    Check.notNull(type);
    Check.notNull(action);
    if(bindings.containsKey(type)) throw new IllegalStateException("Event is already bound: " + type);
    actions.computeIfAbsent(action, ignored -> new HashSet<>()).add(type);
    bindings.put(type, action);
}
```

Note that `bind()` is a generic method that will cause a compile-time error if the types of the event and handler do not match (though note bindings are wild-carded internally).

An action can also be bound to an event source (i.e. to handle **all** events generated by that source):

```java
/**
 * Binds an event source to the given action.
 * @param <T> Event type
 * @param src           Event source
 * @param action        Action handler
 * @throws IllegalArgumentException if the source does not have exactly <b>one</b> event type
 */
public <T extends Type> void bind(Source<T> src, Action<T> action) {
    final var<T> list = src.types();
    if(list.size() != 1) throw new IllegalArgumentException("Bound source can only have a one event type: " + src);
    bind(list.get(0), action);
}
```

The bindings class also provides various methods to query and manage bindings (not shown here).

### Integration Finale

We can now refactor the demo to replace the cumbersome event handler lambdas with












### Conclusions

Hopefully the purpose of the event handling framework combined with the bindings class now makes some degree of sense:

- The event handling framework satisfies the requirements of encapsulating the underlying workings of GLFW and reducing the various events to a smaller, more generic subset.

- The bindings class follows the separation of concerns principle by splitting the event handling logic from the application actions (or at least helps).

- This functionality could be used (for example) to implement keyboard/controller bindings in a game without having to craft or refactor event handlers.

- In addition we provide a persistence mechanism to save and load bindings (not shown here).

In the next section we will illustrate how the bindings can be used to control the camera in the model demo.

---














### Global Flip

Up until this point we have just dealt with the fact that the Y direction in Vulkan is **down** (which is inverted compared to OpenGL and just about every other 3D framework).

However we came across a global solution[^invert] that handily flips the Vulkan viewport by specifying a 'negative' viewport rectangle.

We add a _flip_ setting to the `ViewportStageBuilder` which is applied when we populate the viewport descriptor:

```java
private void populate(VkViewport viewport) {
    if(flip) {
        viewport.x = rect.x();
        viewport.y = rect.y() + rect.height();
        viewport.width = rect.width();
        viewport.height = -rect.height();
    }
    else {
        viewport.x = rect.x();
        viewport.y = rect.y();
        viewport.width = rect.width();
        viewport.height = rect.height();
    }
    ...
}
```

Notes:

- This solution is only supported in Vulkan version 1.1.x or above.

- Note that the Y coordinate of the viewport origin is also shifted to the bottom-left of the rectangle.

- To avoid breaking our existing code the _flip_ setting is off by default.

---







## Integration





mouselook
- look controller
- WASD movement
- wheel - forward/back

common
- escape
- space -> toggle and-then home
- home - reset view

---

## References

[^invert]: [Flipping the Vulkan viewport](https://www.saschawillems.de/blog/2019/03/29/flipping-the-vulkan-viewport/)
