#version 450
#extension GL_ARB_separate_shader_objects : enable

layout(binding = 1) uniform ubo {
    mat4 proj;
    mat4 view;
    mat4 model;
};

layout(location = 0) in vec3 inPosition;
layout(location = 1) in vec2 inTexCoord;

layout(location = 0) out vec2 outTexCoord;

void main() {
    gl_Position = proj * view * model * vec4(inPosition, 1.0);
    outTexCoord = inTexCoord;
}
