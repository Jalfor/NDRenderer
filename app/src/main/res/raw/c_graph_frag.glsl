#version 300 es

smooth in vec3 fColor;

out vec4 outputColor;

void main()
{
    //outputColor = vec4(1.f, 1.f, 1.f, 0.3f);
    outputColor = vec4(fColor, 1.f);
}