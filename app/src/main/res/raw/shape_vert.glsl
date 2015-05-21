#version 300 es

layout (location = 0) in vec4 vertex;
layout (location = 1) in vec3 normal;

flat out vec3 fNormal;   //Normal for the fragment shader

layout(std140) uniform Globals
{
    mat4 projectionMatrix;
    float projectionConstant;
    float[3] padding;
};

void main()
{
    fNormal = normal;
    gl_Position = projectionMatrix * vertex;
}