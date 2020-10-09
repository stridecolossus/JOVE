#version 450 core

layout(binding = 0) uniform sampler2D texSampler;

layout(location = 0) in vec2 texCoord;

layout(location = 0) out vec4 outColor;

void main(void) {
    outColor = texture(texSampler, texCoord);
//	outColor = vec4(texCoord.x, texCoord.y, 0, 1);
}
