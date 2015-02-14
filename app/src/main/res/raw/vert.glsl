#version 300 es

layout (location = 0) in vec4 vertex;
layout (location = 1) in vec4 prevVertex;
layout (location = 2) in vec4 nextVertex;

void main()
{
    gl_Position = vec4(vertex.xy * 0.1, 1, 1);
}