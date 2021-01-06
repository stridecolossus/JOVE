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
    - [Swapchain and Render Pass](blog/part-2-triangle/swapchain)
    - [Pipelines](blog/part-2-triangle/pipeline)
    - [Rendering Commands](blog/part-2-triangle/commands)

- Part 3 - Textured Cube
    - [Vertex Buffers](blog/part-3-cube/vertex-buffers)
    - [Memory Allocator](blog/part-3-cube/memory-allocator)
    - [Textures and Descriptor Sets](blog/part-3-cube/textures)
    - [Perspective Projection](blog/part-3-cube/perspective)
    - [The Render Loop and Synchronisation](blog/part-3-cube/sync)
   
- Part 4 - Models
    - [Model Loader](blog/part-4-models/model-loader)
    - [Depth Buffers](blog/part-4-models/depth-buffer)
    - [Input Handling](blog/part-4-models/input-handling)
    
