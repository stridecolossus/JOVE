---
title: Input Handling
---

## Overview

In this chapter we will be taking a break from Vulkan to add support for input event processing and a camera controller.

There are several ways we could have gone about implementing event handling and we did try several different approaches.

We discuss the rationale for our design as it is covered and illustrate some of the challenges we faced.

---

## Input Events

### Requirements

There are a number of differing types of events provided by GLFW that we will support:

type                    | arguments                 | device
----                    | ---------                 | ------
keyboard                | key, press/release        | keyboard
mouse position          | x, y                      | mouse
mouse button            | button, press/release     | mouse
mouse wheel             | value                     | mouse
window enter/leave      | boolean                   | window
window focus            | boolean                   | window
controller button       | button, press/release     | controller
controller axis         | axis, value               | controller
controller hat          | hat, press/release        | controller

(There are a few others but that should be enough for starters!)

A _controller_ is defined here as a joystick, gamepad or console controller.

Whilst we could simply use the GLFW functionality directly in our applications there are compelling reasons to introduce a layer of abstraction:

- The GLFW API exposes some underlying details (such as window handle pointers) that we would prefer to hide if possible.

- Some events are implemented by GLFW as callback handlers e.g. `MousePositionListener` and others as query functions, e.g. `glfwGetJoystickAxes()`.

- Several of the event types map to the same general forms - for example the mouse, controller and hat button events are essentially equivalent.

- Traditional event callbacks mix application logic and the event handling framework reducing code re-usability and testability.

Based on these observations we declare the following requirements for our design:

- Map the various events to a smaller number of general types.

- Encapsulate the underlying GLFW code that generates input events.

- Separate the event handling framework from the application logic.

- Provide a mechanism to map events to application logic that requires minimal configuration (or ideally none).

- Provide functionality to allow an application to query supported events without having to invoke specific API methods.

TODO - orientation, touch-screen devices

### Design

After a bit of analysis we determine that the various events can be generalised to the following:

type        | arguments             | range                         | examples
----        | ---------             | -----                         | --------
position    | x, y                  | n/a                           | mouse move, joystick, controller touch pad
button      | id, press/release     | number of buttons or keys     | key, mouse button, controller button
axis        | id, value             | number of axes                | mouse wheel, joystick throttle
boolean     | boolean               | n/a                           | window enter/leave

The _range_ is the possible number of events of a given type.

Our initial design will consists of the following components:

- an _event type_ for each of the above.

- a general _input event_ record.

- a _device_ that generates events and delegates to a single handler.

As it turns out the _axis_ event type is probably the simplest case - we will implement an end-to-end solution for the mouse wheel axis.

### Implementation

#### Input Event

We start with the definition of a generic input event and its associated type:

```java
public interface InputEvent {
    /**
     * @return Type of this event
     */
    Type type();

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

The implementation for an axis is relatively simple:

```java
public class Axis implements InputEvent.Type {
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

- As usual we have omitted equality, hash and to-string methods.

- All events are required to implement the X and Y coordinate even if they do not require them - this probably seems a pointless and not very object-orientated design, we did try other approaches using generic types, double-dispatching, etc. but the end result was always ugly (from both the perspective of the code and the client), at least this one is simple.

- For the axis event type we return the same value for both coordinates.

#### Device

Next we define a device:

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

which is comprised of a number of event _sources_ that generate events:

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
    void enable(Handler handler);

    /**
     * Disables event generation.
     */
    void disable();
}
```

The purpose of the event sources is to provide a mechanism such that an application could programatically query the types of events that are supported by a device.

For the mouse wheel we create a _mouse device_ with a source for the wheel axis:

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
        return Set.of(pointer(), buttons(), wheel());
    }
}
```

Note that for convenience we provide the explicit `wheel()` accessor as well as returning the wheel in the `sources()` method.

The mouse wheel implementation is comprised of a single axis which is bound to a GLFW `MouseScrollListener` when enabled:

```java
public Source<Axis.Event> wheel() {
    return new Source<>() {
        private final Axis wheel = new Axis("Wheel");

        @Override
        public List<Axis> types() {
            return List.of(wheel);
        }

        @Override
        public void enable(InputEvent.Handler handler) {
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

The _event handler_ is a simple marker interface:

```java
interface InputEvent {
    /**
     * A <i>handler</i> accepts an input event.
     */
    @FunctionalInterface
    interface Handler extends Consumer<InputEvent> {
    }
}
```

### Integration

To exercise the mouse wheel we first create a mouse device for a given window:

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
final InputEvent.Handler handler = System.out::println;
final MouseDevice mouse = window.mouse();
mouse.wheel().enable(handler);
```

This satisfies our first three requirements (for the mouse wheel anyway) but doesn't achieve anything that we couldn't have done without all this framework - we will complete implementation for the other event types then turn our attention to integrating event handling into the model demo.

---

## More Events

In this section we implement the remaining events and devices.

TODO - joystick

### Events

#### Position

A _position event_ is used for the mouse pointer, a joystick or a controller touchpad:

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

#### Buttons

A _button event_ is slightly more complex in that it also has an _action_ and keyboard _modifiers_:

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

For example the `new Button("NAME", 1, 0x0001 | 0x0002)` has the name `NAME-PRESS-SHIFT-CONTROL`.

### Devices

#### Mouse

The mouse pointer is a _position_ event:

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

Finally we implement the mouse buttons:

```java
public Source<Button> buttons() {
    return new Source<>() {
        /**
         * @return Number of mouse buttons
         */
        private int count() {
            // TODO - uses AWT but not supported by GLFW
            return MouseInfo.getNumberOfButtons();
        }

        private final Button[] buttons = IntStream.rangeClosed(1, count()).mapToObj(n -> "Button-" + n).map(Button::of).toArray(Button[]::new);

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

Note that `types()` returns the pre-defined mouse buttons sized to the number available as determined by the `count()` method.

#### Keyboard

The keyboard device itself is quite simple:

```java
public class KeyboardDevice implements InputEvent.Device {
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

However GLFW returns key **codes** that are defined as macros in the header (mapped to a US keyboard layout).  We _could_ simply replicate this as an enumeration but that would require painful manual formatting - instead we opt to load the keys from a text file which requires minimal translation and a simple loader class:

```java
/**
 * The <i>key table</i> maps between GLFW key codes and names.
 */
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

---

## Action Bindings

To make all this work worthwhile the final piece of functionality is to implement a _bindings_ class that maps events to arbitrary _actions_.

### Bindings Class

The bindings class is itself an event handler:

```java
public class Bindings implements Handler {
    private final Map<Handler, Set<Type>> actions = new HashMap<>();
    private final Map<Type, Handler> bindings = new HashMap<>();
    
    ...

    @Override
    public void accept(InputEvent event) {
        final Handler action = bindings.get(event.type());
        if(action != null) {
            action.accept(event);
        }
    }
}
```

An event is bound as follows:

```java
/**
 * Binds an input event to the given action.
 * @param type          Input event
 * @param action        Action handler
 * @throws IllegalStateException if the event is already bound
 */
public void bind(Type type, Handler action) {
    Check.notNull(type);
    if(bindings.containsKey(type)) throw new IllegalStateException("Event is already bound: " + type);
    actions.computeIfAbsent(action, ignored -> new HashSet<>()).add(type);
    bindings.put(type, action);
}
```

Bindings can be queried by the following accessors:

```java
/**
 * Helper - Looks up the bindings for the given action.
 * @param action Action
 * @return Bindings
 */
private Set<Type> get(Handler action) {
    final var bindings = actions.get(action);
    if(bindings == null) throw new IllegalArgumentException("Action not present: " + action);
    return bindings;
}

/**
 * Looks up all events bound to the given action.
 * @param action Action handler
 * @return Input events bound to the given action
 * @throws IllegalArgumentException if the action is not present in this set of bindings
 */
public Stream<Type> bindings(Handler action) {
    return get(action).stream();
}

/**
 * Looks up the action bound to an event.
 * @param type Input type
 * @return Action
 */
public Optional<Handler> binding(Type type) {
    return Optional.ofNullable(bindings.get(type));
}
```

And removed:

```java
/**
 * Removes the binding for the given type of event.
 * @param type Event type
 */
public void remove(Type type) {
    final Handler action = bindings.remove(type);
    if(action != null) {
        actions.get(action).remove(type);
    }
}

/**
 * Removes <b>all</b> bindings for the given action.
 * @param action Action
 * @throws IllegalArgumentException if the action is not present
 */
public void remove(Handler action) {
    final var set = get(action);
    set.forEach(bindings::remove);
    set.clear();
}

/**
 * Removes <b>all</b> bindings.
 */
public void clear() {
    actions.values().forEach(Set::clear);
    bindings.clear();
}
```

### Conclusions

Hopefully the purpose of the event handling framework combined with the bindings class now makes some degree of sense:

- The event handling framework satisfies the requirements of encapsulating the underlying workings of GLFW and reducing the various events to a smaller, more generic subset.

- The bindings class follows the separation of concerns principle by separating the event handling logic from the application actions (or at least helps).

- This functionality could be used (for example) to implement keyboard/controller bindings in a game without having to craft or refactor event handlers.

- In addition we provide a persistence mechanism to save and load bindings (not shown here).

In the next section we will illustrate how the bindings can be used to control the camera in the model demo.

---

## Camera Controller

The second task for this chapter is to wrap up the view transform matrix into a camera class.

The camera has two properties that are essentially equivalent to the two matrices we crafted by hand in the previous chapter:
- the _eye_ position
- the view direction

```java
public class Camera {
    private Point pos = Point.ORIGIN;
    private Vector dir = Vector.Z_AXIS.invert();
    private Vector up = Vector.Y_AXIS;

    ...

    public Matrix matrix() {
    }
}
```

Notes:
- We also introduce the _up_ direction.
- The camera view direction is _into_ the screen (the negative-Z direction).

The matrix is re-calculated whenever one of the camera properties is modified:

```java
public Matrix matrix() {
    if(dirty) {
        update();
        dirty = false;
    }
    return matrix;
}
```

The process of calculating the view transform matrix is:
1. Determine the _right_ and _up_ axes of the camera.
2. Create a rotation matrix comprised of the three camera axes.
3. Multiply by the translation matrix.

Following the tutorial our implementation uses a slight trick to inject the translation component into the final matrix without needing an intermediate multiplication:

```java
private void update() {
    // Determine right axis
    right = dir.cross(up).normalize();

    // Determine up axis
    final Vector y = right.cross(dir).normalize();

    // Calculate translation component
    final Vector trans = new Vector(right.dot(pos), y.dot(pos), -dir.dot(pos));

    // Build camera matrix
    matrix = new Matrix.Builder()
        .identity()
        .row(0, right)
        .row(1, y)
        .row(2, dir.invert())
        .column(3, trans)
        .build();
}
```

This is almost certainly a pointless 'optimisation' that we might actually remove at some point.

TODO - or implement matrix * homogenous vector?

We add the _dot product_ operator to the tuple class:

```java
public final float dot(Tuple t) {
    return x * t.x + y * t.y + z * t.z;
}
```

and the _cross product_ to the vector class:

```java
public Vector cross(Vector vec) {
    final float x = this.y * vec.z - this.z * vec.y;
    final float y = this.z * vec.x - this.x * vec.z;
    final float z = this.x * vec.y - this.y * vec.x;
    return new Vector(x, y, z);
}
```

Finally we also implement various mutators to the camera class to move the eye position and change the view direction.

In particular we add the following method to point the camera at a given position:

```java
public void look(Point pt) {
    dir = Vector.of(pt, pos).normalize();
    dirty();
}
```

TODO - invalid position -> zero length

And another method to set the view direction given a yaw-pitch orientation:

```java
/**
 * Sets the camera orientation to the given yaw and pitch angles (radians).
 * @param yaw       Yaw
 * @param pitch     Pitch
 */
public void orientation(float yaw, float pitch) {
    final float cos = MathsUtil.cos(pitch);
    final float x = MathsUtil.cos(yaw) * cos;
    final float y = MathsUtil.sin(pitch);
    final float z = MathsUtil.sin(-yaw) * cos;
    dir = new Vector(x, y, z).normalize();
    dirty();
}
```

TODO - fails at poles

