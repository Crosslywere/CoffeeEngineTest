#version 460 core
#define LEVEL 2.0
layout (location = 0) out vec4 oFragColor;
uniform vec3 uColor;
in vec3 iNorm;
void main() {
    vec3 color = (iNorm + uColor) / 2.0;
//    color = floor(color * LEVEL) / LEVEL;
    color = normalize(color);
    oFragColor = vec4(color, 1.0);
}