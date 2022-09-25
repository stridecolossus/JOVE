---
title: Scene Graph
---

---

## Contents

- [Overview](#overview)
- [Scene Graph](#scene-graph)
- 

---

## Overview

---

## Scene Graph

mutable
multiple objects
static or dynamic

### Analysis

The rationale for implementing some sort of scene abstraction is based on the following assumptions:

* Generally a developer would prefer to think in terms of _objects_ in a _scene_ rather than vertex buffers, descriptor sets, etc.

* The visual properties of an object are best characterised by a _material_ abstracting over implementation details such as pipeline, samplers, etc.

* Objects are arranged spatially and/or logically depending on the application and _inherit_ transformations applied to their group.

* The scene should support functionality for _picking_ objects from a scene, applying frustum culling (or similar techniques), and testing for collisions and intersections.

There are also further requirements arising from how Vulkan and the GPU operate:

* The implementation should minimise state changes (i.e. pipelines, descriptor sets) to optimise performance.

* Many applications will require a further grouping mechanism to separate the following use-cases:
    * The general case of arbitrarily ordered _opaque_ models (often utilising the depth buffer).
    * Translucent geometry rendered in _reverse_ distance order from the camera __after__ the opaque scene.
    * Background models (e.g. skybox) which are usually rendered last.

* The transformation matrix for a given model can be implemented as a uniform buffer and/or push constants.

### Design

From the above the scene graph will be comprised of a hierarchical tree of _nodes_ with the following properties:

* A _world matrix_ which combines the local transform of a node with that of its ancestors.

* A _material_ that specifies the rendering properties (pipeline, descriptor set) and the _render queue_ of each node (opaque, translucent, etc).

* An optional _bounding volume_ used for culling, picking, etc.

To generate the render sequences with minimal state changes the nodes in a scene graph are grouped as follows:

1. Render queue.

2. Pipeline.

3. Descriptor set.

Initially we envisaged a Java streams-based solution for this requirement which seemed logical (collections, grouping, etc), but upon reflection decided against this approach for several reasons:

* The dynamic nature of a scene graph means the grouping would be evaluated on _every_ frame which feels wasteful, particularly for scenes that are static or with low volatility.

* The code would probably become quite complex and difficult to test.

* Introducing additional requirements and functionality would only exacerbate the complexity.

Therefore a scene graph will be comprised of __two__ data structures: the node hierarchy and a complimentary set of _groups_ imposing the render queue and state change groupings.  The appropriate groups for a node are determined _once_ when it is added to the scene.  Additionally the different grouping use-cases can be tested in relative isolation. ???





TODO
flags for uniform[n] / push?




---

## Summary

TODO
