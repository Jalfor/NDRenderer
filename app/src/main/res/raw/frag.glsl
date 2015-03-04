#version 300 es

smooth in vec3 normal;

out vec4 outputColor;

void main()
{
    outputColor = vec4(0.5 * (normal + 1.0), 0.4);
}