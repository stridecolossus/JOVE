# Overview

In this chapter we take a break from Vulkan and add support for a camera and input event processing based on the event handling functionality provided by GLFW.

There are several ways we could have gone about implementing event handling, we also discuss the rationale for the approach we have taken and illustrate some of the challenges we faced.

---

# Input Events

## Requirements

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

Based on these observations we enumerate the following requirements for our design:

- Map the various events to a smaller number of general types.

- Encapsulate the underlying GLFW code that generates input events.

- Separate the framework from the application logic that handles events.

- Provide a centralised mechanism for handling events, i.e. rather than having to define multiple handlers for each input case.

TODO - orientation, touch-screen devices

## Design

After a bit of analysis we determine that the various types of event can be generalised to the following:

type        | arguments             | range                         | examples
----        | ---------             | -----                         | --------
position    | x, y                  | n/a                           | mouse move
button      | id, press/release     | number of buttons or keys     | key, mouse button, controller button
axis        | id, value             | number of axes                | joystick, mouse wheel
boolean     | boolean               | n/a                           | window enter/leave

The _range_ is the possible number of events of a given type.

Out initial design consists of:

- An _input event type_ with implementations for each of the above.

- A _device_ abstraction, i.e. the keyboard, mouse, etc.

- Event _handlers_ implemented using simple functional interfaces, i.e. `Consumer`.

- A more specialised handler that allows the application developer to bind _actions_ to events.

This is probably best illustrated by some pseudo-code:

```java
// Create mouse handler
Device mouse = new MouseDevice();
Consumer<Position> pos = event -> camera.orientate(event.x, event.y);
mouse.enable(pos);

// Create keyboard bindings
Bindings bindings = new Bindings();
Device keyboard = new KeyboardDevice();
keyboard.enable(bindings);

// Define some actions
Action stop = () -> running = false;
Action move = new MoveAction(camera);

// Bind some events
bindings.bind(new Button("Escape"), stop);
bindings.bind(new Button("Up"), move(+1));
bindings.bind(new Button("Down"), move(-1));
```

This design satisfies our requirements and successfully abstracts over the GLFW framework meaning we have a suite of components that can be more easily maintained and tested.

## Input Events

As it turns out the simplest type of event is probably an axis device such as the mouse wheel - we will implement an end-to-end solution for event handling using the mouse wheel as a test case.

### Event Type

As a starting point we define a general _input event_ and its associated _type_ as follows:

```java
public interface InputEvent<T extends Type> {
    /**
     * @return Type of this event
     */
    Type type();

    /**
     * An <i>event type</i> is the descriptor for an input event.
     */
    interface Type {
        /**
         * @return Event type name
         */
        String name();
    }
}
```

The implementation for an axis is quite simple:

```java
public final class Axis implements InputEvent.Type {
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
    public final class Event implements InputEvent<Axis> {
        private final float value;

        /**
         * Constructor.
         * @param value Axis value
         */
        private Event(float value) {
            this.value = value;
        }

        /**
         * @return Axis value
         */
        public float value() {
            return value;
        }

        @Override
        public Type type() {
            return Axis.this;
        }
    }
}
```

### Devices

We define a _device_ that is comprised of a set of event sources (or generators):

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

An event _source_ generates events of a given type and can be enabled and disabled:

```java
interface Source<T extends Type> {
    /**
     * @return Type of events generated by this source
     */
    Class<T> type();

    /**
     * @return Events generated by this source
     */
    List<T> events();

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

The implementation for the mouse wheel registers a GLFW event listener which generates an axis event:

```java
class MouseDevice implements Device {
    /**
     * Mouse wheel.
     */
    private class Wheel implements Source<Axis> {
    }
}
```

The mouse wheel source registers a GLFW event listener and delegates axis events to the handler:

```java
private class Wheel implements Source<Axis> {
    private final Axis wheel = new Axis("Wheel");

    @Override
    public Class<Axis> type() {
        return Axis.class;
    }

    @Override
    public List<Axis> events() {
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
}
```

Eventually the mouse device will be extended to support mouse button and movement events.

### Unit-Test



## Action Bindings








Finally we add the following factory methods to the desktop window class:

```java
/**
 * @return New keyboard device
 */
public Device keyboard() {
    return new KeyboardDevice(this);
}

/**
 * @return New mouse device
 */
public Device mouse() {
    return new MouseDevice(this);
}
```



## ???

buttons
keyboard device
position and mouse move
mouse buttons

## Key Table

requirements for key table
mapping file to GLFW macros
button cache
intregration into keyboard device

## Action Bindings

To make all this work worthwhile the final step is to implement 

## Conclusion

This design is

- reusability

- maintainability & test

- cohesion

---

# Camera Controller

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

