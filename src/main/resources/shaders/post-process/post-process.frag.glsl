uniform sampler2D fboTexture;
varying vec3 vPosition;
varying vec2 vTexCoord;

uniform float animTime;
uniform int nextGen;

void main() {
  if((vTexCoord.y < 0.2 || vTexCoord.y > 0.8) && nextGen > 0) {
    gl_FragColor = vec4(0.0, 0.0, 0.0, 1.0);
  } else {
    if(nextGen > 0) {
      vec2 newTexCoord = vec2(float(int(vTexCoord.x*200.0))/200.0, float(int(vTexCoord.y*200.0))/200.0);
      gl_FragColor = texture2D(fboTexture, newTexCoord);
    } else {
      gl_FragColor = texture2D(fboTexture, vTexCoord);
    }
  }
}

