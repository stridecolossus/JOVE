package org.sarge.jove.scene.graph;

public class Material {

}


//material
//- inherited
//- name
//- texture(s?) ~ descriptor sets
//- pipeline -> layout -> descriptor sets, push constant/ranges(?)
//- render queue? opaque, translucent, etc
//- properties: named 'variables' -> shader
//- out: bind pipeline & descriptors to sequence
//
//material properties
//- name
//- type: equivalent to GLSL types: int, float, vector[size], matrix[order], etc
//- size (bytes)
//- array length?
//- mutability: fixed, variable, per-frame, per-node, etc
//- maps to uniform, push constant, shader constants?
