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

Up until now our demo applications have generally consisted of a handful of models and the render sequences have been programatically generated.  This approach will obviously not scale to more complex scenarios for a variety of reasons:

* The _scene_ structure is hard-coded.

* The following elements of each object are also hard-coded:
    - The render sequence.
    - Population of the modelview matrix.
    - Vulkan state changes (i.e. pipelines and descriptor sets).

* The components of an object are currently unaggregated Vulkan domain classes, i.e. the VBO, optional index buffer and the draw command (generated from the model).

* Most real-world applications will require scenes to be mutable.

### Analysis

The rationale for implementing a scene abstraction is based on the following assumptions:

* Generally a developer will prefer to think in terms of _objects_ in the scene rather than vertex buffers, descriptor sets, etc.

* The visual properties of an object are best characterised by a _material_ abstracting over implementation details such as pipelines, samplers, etc.

* Objects in the scene are arranged spatially and/or logically depending on the application and _inherit_ transformations applied to their group.

* A mechanism is required to support _frustum culling_ and _picking_ of the objects in the scene.

There are also further requirements arising from how Vulkan and the GPU operate:

* The implementation should minimise expensive state changes (i.e. pipelines, descriptor sets) to optimise performance.

* Many applications will also require a further top-level grouping to separate the following use-cases:
    - The general case of arbitrarily ordered _opaque_ geometry (often utilising the depth buffer).
    - Translucent geometry rendered __after__ the opaque scene in order of _reverse_ distance from the camera.
    - Background models (e.g. skybox) usually rendered last.

* The transformation matrix for a given model can be implemented either as a uniform buffer or a push constant.

A _scene graph_ is a common abstraction for 3D applications to support these requirements.

### Design

From the above the scene graph will be comprised of a hierarchical tree of _nodes_ with the following properties:

* A _local transform_ that composes the _world matrix_ of a node with that of its ancestors.

* A _material_ specifying the rendering properties (pipeline, descriptor set) and the _render queue_ of each node.

* An optional _bounding volume_ used for culling and picking (and also to support collision tests).

The transform and material properties _can_ be inherited (or overridden) by an ancestor.  For example a node could have a local transformation or could inherit that of its parent, i.e. the local transform is the identity matrix in this case.  Bounding volumes are slightly more complicated and are deferred until later in the chapter.

The local transform also specifies a _matrix strategy_ that defines the mechanism for passing the various matrices (projection, view/camera and model) to the shader.  Implementations can then support multiple approaches as required, e.g. an aggregated modelview matrix as a push constant with a separate projection matrix in a static uniform buffer.  This also provides an extension point for alternative approaches later in development.

Initially there will be two node implementations:

* A _model node_ representing an object in the scene, containing a _mesh_ which aggregates the VBO, index and model header (primitive, layout and draw count).

* A _group node_ comprising a list of children which can inherit its properties and those of its ancestors.

To optimise state changes the _model nodes_ in a scene graph are grouped as follows:

1. Render queue.

2. Pipeline.

3. Descriptor set (textures and uniform buffers).

4. Push constant updates.

Initially we envisaged a Java streams-based solution for this logic which seemed logical given the code is dealing with collections.  However the dynamic nature of a scene graph means the groups would be evaluated on _every_ frame which feels wasteful, particularly for elements of the scene with low volatility.  Therefore the scene graph is comprised of __two__ components: the node hierarchy and a complimentary data structure representing the _render groups_ listed above.  The appropriate groups for a node are determined _once_ when it is added to the scene or a relevant property (e.g. the material) is modified.

The various properties of a node are inherited from its ancestors (in particular the world matrix), requiring a sub-tree of the scene to be _updated_ when the properties of a node are mutated.  This implies a _visitor_ operation performed on a node and its descendants to update the scene graph and render groups.  To minimise the amount of processing, invoking the update visitor is a responsibility of the application rather than an on-demand approach.

Finally the following Vulkan specific components render the scene:

* A _renderer_ that generates the command sequence for a model node (usually on each frame unless the scene is completely static).

* Matrix strategy implementations to support the basic cases outlined above.

TODO - VK enumerates the state change types: pipeline, DS/texture, push constants? how would node know which property applies to which type?
Q - introduce 'Texture' here?

---

## Summary

TODO
