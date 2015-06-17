#version 300 es

flat in vec3 fNormal;

out vec4 outputColor;

layout(std140) uniform Globals
{
    mat4 projectionMatrix;
    vec4 color;
    float projectionConstant;
    float[3] padding;
};

void main()
{
    vec3 dirLight = normalize(vec3(1.f, -1.f, -1.f));

    //outputColor = color;  //Plain color
    //outputColor = vec4(0.25f * (fNormal + 3.f), 0.4f);    //Normal coloring
    outputColor = vec4(color.xyz * (abs(dot(fNormal, dirLight))), color.w);    //Directional lighting
}