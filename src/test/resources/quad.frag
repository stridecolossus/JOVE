#version 450
#extension GL_ARB_separate_shader_objects : enable

layout(binding = 1) uniform sampler2D texSampler;

layout(location = 0) in vec2 fragCoords;
layout(location = 1) flat in int index;

layout(location = 0) out vec4 outColour;

vec3 colours[6] = vec3[](
	vec3(1.0, 0.0, 0.0),
	vec3(0.0, 1.0, 0.0),
	vec3(0.0, 0.0, 1.0),
	vec3(1.0, 1.0, 0.0),
	vec3(1.0, 1.0, 1.0),
	vec3(1.0, 1.0, 1.0)
);

void main() {
    outColour = texture(texSampler, fragCoords) * vec4(colours[index], 1.0);
}
