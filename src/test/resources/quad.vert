#version 450
#extension GL_ARB_separate_shader_objects : enable

layout(binding = 0) uniform View {
	mat4 projection;
	mat4 modelview;
} view;

layout(location = 0) in vec3 pos;
layout(location = 1) in vec2 coords;

layout(location = 0) out vec2 fragCoords;

void main() {
    gl_Position = view.projection * view.modelview * vec4(pos, 1.0);
    fragCoords = coords;
}
