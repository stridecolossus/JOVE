# Input Events

## Introduction

In this chapter we take a break from Vulkan and add support for input event processing into JOVE, building on the event handling functionality in GLFW.

There are several ways we could have gone about implementing event handling, this is just one of them (and probably not the best).
We discuss the rationale for the design we have chosen and illustrate some of challenges we faced.

## Design

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

- The GLFW API exposes several underlying details (such as window handle pointers) that we would prefer to hide if possible.

- There is some data we would like to encapsulate such as mapping button modifiers and looking up keyboard key-names.

- Some events are implemented by GLFW as callback handlers e.g. `MousePositionListener` and others as query functions, e.g. `glfwGetJoystickAxes()`.

- Several of the event types map to the same general forms.  For example the mouse, controller and hat button events have the same essential structure and data.

- Using GLFW callbacks (for example) mixes application logic and event handling which reduces code re-usability and makes testing more complex.

Based on these observations we enumerate the following requirements for our design:

- Map the various events to a smaller number of general types.

- Encapsulate the underlying GLFW code that generates input events.

- Implement a _centralised_ mechanism to handle _all_ types of input event.

This is probably best illustrated by some pseudo-code of what we are trying to achieve from the perspective of a developer using the library:

```java
// Create keyboard device
Device keyboard = new KeyboardDevice();
Consumer<Button.Event> button = ...
keyboard.enable(button);

// Create mouse device
Device mouse = new MouseDevice();
Consumer<Position> pos = ...
Consumer<Axis.Event> axis = ...
mouse.enable(pos);
mouse.enable(axis);
```

If we can achieve these requirements we can then implement a handler that binds events to _actions_ along these lines:

```java
// Create a camera
Camera camera = ...

// Create bindings
Bindings bindings = new Bindings();
bindings.bind(new Button("W"), camera.move(+1));
bindings.bind(new Button("A"), camera.move(-1));
bindings.bind(new Button("S"), camera.strafe(-1));
bindings.bind(new Button("D"), camera.strafe(+1));
bindings.bind(pos, camera::orientate);
bindings.bind(new Axis("MouseWheel"), new ZoomCameraAction(cam));

// Enable bindings
keyboard.enable(bindings);
mouse.enable(bindings);
```

This separates input event handling from the application logic allowing actions to be more easily re-used across applications and considerably simplifies testing.

## Event Types

After a bit of analysis we determine that the various types of event can be generalised to the following:

type        | arguments             | examples
----        | ---------             | -------
position    | x, y                  | mouse move
button      | id, press/release     | key, mouse button, controller button
axis        | id, value             | joystick, mouse wheel
boolean     | boolean               | window enter/leave

As a starting point we define an input event and its associated type as follows:

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

### Axis

As it turns out the simplest type of event is for an axis device such as the mouse wheel:

```java
public class Axis implements InputEvent.Type {
    private final int id;
    private final String name;

    /**
     * Constructor.
     * @param id        Axis identifier
     * @param name      Axis name
     */
    public Axis(int id, String name) {
        this.id = zeroOrMore(id);
        this.name = notEmpty(name);
    }

    @Override
    public String name() {
        return name;
    }
}
```

We implement a local class for an instance of this type of event:

```java
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
```

and a factory to create an instance:

```java
/**
 * Creates an axis input event.
 * @param value Axis value
 * @return New axis event
 */
public Event create(float value) {
    return new Event(value);
}
```

### Position

TODO

### Buttons

TODO

### Keyboard

## Devices
