attribute vec3 position;
attribute vec2 texCoord;
attribute vec4 color;

varying vec3 vPosition;
varying vec2 vTexCoord;
varying vec4 vColor;
uniform float animTime;

void main() {
    vPosition = position;
    vTexCoord = texCoord;
    vColor = color;

    gl_Position = gl_ModelViewProjectionMatrix*vec4(position, 1.0);
}