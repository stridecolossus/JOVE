#version 450
#extension GL_ARB_separate_shader_objects : enable

layout(binding = 1) uniform sampler2D texSampler;

layout(location = 0) in vec2 fragCoords;

layout(location = 0) out vec4 outColour;

void main() {
    outColour = texture(texSampler, fragCoords) * vec4(fragCoords, 0.0, 1.0);
}
