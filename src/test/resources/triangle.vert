#version 450
#extension GL_ARB_separate_shader_objects : enable

layout(binding = 0) uniform Temp {
	vec4 pos;
} ubo;

layout(location = 0) in vec3 inPosition;
layout(location = 1) in vec4 inColor;

layout(location = 0) out vec4 fragColor;

void main() {
    gl_Position = vec4(inPosition, 1.0);
    fragColor = inColor;
}
