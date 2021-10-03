---
title: A Java implementation of the Vulkan API
---

## Introduction

TODO

why
what 
how

- challenge of why not
- difficulty of adapting native library to OO design

goals
- clarity/simplicity
- maintainability
- testability
- well documented third party libraries

approach
- minimal work to achieve goal
- but aggressive refactoring
- aim to build as we would for professional contract
- on other hand per project so can reinvent any wheels we choose

overview
- note about part 1 optional
- incremental approach building on each chapter
- list of upcoming planned features

what is Vulkan

---

## Contents

- Part 1 - Code Generation
    - [Code Generation](blog/part-1-generation/generation)


- Part 2 - Rendering a Triangle
    - [Vulkan Instance](blog/part-2-triangle/instance)
    - [Devices](blog/part-2-triangle/devices)
    - [Presentation](blog/part-2-triangle/presentation)
    - [Render Pass](blog/part-2-triangle/render-pass)
    - [Pipeline](blog/part-2-triangle/pipeline)
    - [Rendering](blog/part-2-triangle/rendering)

- Part 3 - Textured Cube
    - [Vertex Buffers](blog/part-3-cube/vertex-buffers)
    - [Memory Allocator](blog/part-3-cube/memory-allocator)
    - [Texture Sampling](blog/part-3-cube/textures)
    - [Descriptor Sets](blog/part-3-cube/descriptor-sets)
    - [Perspective Projection](blog/part-3-cube/perspective)
    - [The Render Loop and Synchronisation](blog/part-3-cube/sync)
   
- Part 4 - Models
    - [Model Loader](blog/part-4-models/model-loader)
    - [Depth Buffers](blog/part-4-models/depth-buffer)
    - [Input Handling](blog/part-4-models/input-handling)
    
