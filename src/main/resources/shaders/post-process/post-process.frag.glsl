uniform sampler2D fboTexture;
varying vec3 vPosition;
varying vec2 vTexCoord;

uniform float animTime;

void main() {
  gl_FragColor = texture2D(fboTexture, vTexCoord);
}

