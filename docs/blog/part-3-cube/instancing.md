---
title: Instancing
---

## Overview


### BASIC INSTANCING

add x 4 instances

```java
        final Command draw = (api, handle) -> api.vkCmdDraw(handle, cube.count(), 4, 0, 0);
```

crude fiddle to vertex shader to spread out instances by fiddling X coord of all vertices
2x2 grid of cubes

```glsl
void main() {
    gl_Position = proj * view * model * vec4(inPosition, 1.0);
    gl_Position.x += (gl_InstanceIndex % 2) * 2;
    gl_Position.y += (gl_InstanceIndex / 2) * 3;
    outTexCoord = inTexCoord;
}
```

new shader (?)

```java
        final Shader vert = shaderLoader.load("spv.cube.instanced.vert");
```

move camera back and up/left a bit

```java
        final Matrix trans = new Matrix.Builder()
                .identity()
                .column(3, new Point(-1, -1, -3.5f))
                .build();
```


### UNIFORM BUFFER

TODO - time/elapsed from runner

mat4[] array in UBO
replaces single model matrix
populated in update()

top left - as previous
top right - cosine interpolation
bottom left - squared
bottom right - scaled smooth step, pulsating

could have done all this in shader ofc
data pushed every frame from host => less efficient when more data

### OTHERS

desc set per object
not recommended
binding overhead

push constants

