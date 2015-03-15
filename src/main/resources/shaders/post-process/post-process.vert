attribute vec2 position;
attribute vec4 color;
attribute vec2 texCoord;

varying vec2 vPosition;
varying vec4 vColor;
varying vec2 vTexCoord;

uniform sampler2D fboTexture;

void main() {
    vPosition = position;
    vColor = color;
    vTexCoord = texCoord;

    gl_Position = gl_ModelViewProjectionMatrix*vec4(position, 0.0, 1.0);
}