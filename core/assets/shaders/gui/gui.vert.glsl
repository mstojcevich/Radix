attribute vec3 position;
attribute vec2 texCoord;
attribute vec4 color;

varying vec3 vPosition;
varying vec2 vTexCoord;
varying vec4 vColor;

uniform float animTime;
uniform mat4 u_projectionViewMatrix;

void main() {
    vPosition = position;
    vTexCoord = texCoord;
    vColor = color;

    gl_Position = u_projectionViewMatrix*vec4(position, 1.0);
}