#version 300 es

layout (location = 0) in vec4 vertex;
layout (location = 1) in vec4 prevVertex;
layout (location = 2) in vec4 nextVertex;

layout(std140) uniform Globals
{
    mat4 projectionMatrix;
    float projectionConstant;
    float[3] padding;
};

vec3 projectDown(vec4 vertex4d)
{
    return vertex4d.xyz * (projectionConstant / (projectionConstant + vertex4d[3]));
}

void main()
{
    vec3 vertex3d = projectDown(vertex);
    vertex3d += vec3(0.5f, 0, -20.0f);
    gl_Position = projectionMatrix * vec4(vertex3d, 1.f);
}