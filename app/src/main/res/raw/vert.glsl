#version 300 es

layout (location = 0) in vec4 vertex;
layout (location = 1) in vec4 prevVertex;
layout (location = 2) in vec4 nextVertex;

smooth out vec3 normal;

uniform float frameTime;

layout(std140) uniform Globals
{
    mat4 projectionMatrix;
    float projectionConstant;
    float[3] padding;
};

mat4 rotMatXY = mat4(
     cos(frameTime), sin(frameTime), 0, 0,
    -sin(frameTime), cos(frameTime), 0, 0,
    0              , 0             , 1, 0,
    0              , 0             , 0, 1
);

mat4 rotMatYZ = mat4(
    1        , 0                , 0       , 0,
    0        ,  cos(frameTime)  , sin(frameTime), 0,
    0        , -sin(frameTime)  , cos(frameTime), 0,
    0        , 0                , 0             , 1
);

mat4 rotMatXW = mat4(
     cos(frameTime), 0, 0, sin(frameTime),
    0              , 1, 0, 0,
    0              , 0, 1, 0,
    -sin(frameTime), 0, 0, cos(frameTime)
);

vec3 projectDown(vec4 vertex4d)
{
    return vertex4d.xyz * (projectionConstant / (projectionConstant + vertex4d[3]));
}

void main()
{
    vec4 rotVertex = rotMatXW * rotMatYZ * vertex;
    vec4 rotPrevVertex = rotMatXW * rotMatYZ * prevVertex;
    vec4 rotNextVertex = rotMatXW * rotMatYZ * nextVertex;

    vec3 vertex3d = projectDown(rotVertex);
    vec3 prevVertex3d = projectDown(rotPrevVertex);
    vec3 nextVertex3d = projectDown(rotNextVertex);

    //Correct version (Broken, but looks awesome)
    normal = normalize(cross(prevVertex3d - vertex3d, nextVertex3d - vertex3d));

    vertex3d += vec3(0f, 0f, -10.0f);

    gl_Position = projectionMatrix* vec4(vertex3d, 1.f);
}