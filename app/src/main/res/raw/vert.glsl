#version 300 es

layout (location = 0) in vec4 vertex;
layout (location = 1) in vec4 prevVertex;
layout (location = 2) in vec4 nextVertex;

smooth out vec3 normal;

layout(std140) uniform Globals
{
    mat4 projectionMatrix;
    float projectionConstant;
    float[3] padding;
};

mat4 rotMatZ = mat4(
     cos(1.0), sin(1.0)    , 0, 0,
    -sin(1.0), cos(1.0)    , 0, 0,
    0        , 0           , 1, 0,
    0        , 0           , 0, 1
);

mat4 rotMatX = mat4(
    1        , 0         , 0       , 0,
    0        , cos(1.0)  , sin(1.0), 0,
    0        , sin(1.0)  , cos(1.0), 0,
    0        , 0         , 0       , 1
);

vec3 projectDown(vec4 vertex4d)
{
    return vertex4d.xyz * (projectionConstant / (projectionConstant + vertex4d[3]));
}

void main()
{
    vec3 vertex3d = projectDown(vertex);
    vec3 prevVertex3d = projectDown(prevVertex);
    vec3 nextVertex3d = projectDown(nextVertex);

    //Correct version
    //normal = normalize(cross(prevVertex3d - vertex3d, nextVertex3d - vertex3d));

    vertex3d = mat3(rotMatX) * mat3(rotMatZ) * vertex3d;
    vertex3d += vec3(0f, 0f, -20.0f);

    //Temporary while transparancy isn't implemented (it looks cooler)
    normal = normal = normalize(cross(prevVertex3d, nextVertex3d));

    gl_Position = projectionMatrix * vec4(vertex3d, 1.f);
}