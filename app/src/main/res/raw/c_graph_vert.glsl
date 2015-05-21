#version 300 es

layout (location = 0) in vec4 vertex;

layout(std140) uniform Globals
{
    mat4 projectionMatrix;
    float projectionConstant;
    float[3] padding;
};

void main()
{
    gl_PointSize = 5.f;
    gl_Position = projectionMatrix * vertex;
}