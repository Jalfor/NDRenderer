#version 300 es

flat in vec3 fNormal;

out vec4 outputColor;

void main()
{
    vec3 dirLight = normalize(vec3(1.f, -1.f, -1.f));
    vec3 diffuseColor = vec3(0.2f, 1.f, 1.f);

    outputColor = vec4(1.f, 1.f, 1.f, 0.1f);
    //outputColor = vec4(0.25f * (fNormal + 3.f), 0.4f);
    //outputColor = vec4(diffuseColor * 0.5f * abs(dot(fNormal, dirLight)), 0.4f);
}