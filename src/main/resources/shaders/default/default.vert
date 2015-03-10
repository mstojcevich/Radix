uniform sampler2D texSampler;
varying vec3 normal;
varying vec2 texCoord;

void main() {
    gl_Position = ftransform();
    gl_FrontColor = gl_Color;
    normal = gl_Normal;
    texCoord = gl_MultiTexCoord0.xy;
}