uniform sampler2D fboTexture;
varying vec2 vPosition;
varying vec4 vColor;
varying vec2 vTexCoord;

void main() {
  gl_FragColor = vColor;
}