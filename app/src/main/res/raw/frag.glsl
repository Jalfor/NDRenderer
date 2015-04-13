#version 300 es

flat in vec3 fNormal;

out vec4 outputColor;

void main()
{
    outputColor = vec4(1.f, 1.f, 1.f, 0.4f);
    vec3 normal = fNormal;
    //outputColor = vec4(0.25f * (normal + 3), 0.4);
}