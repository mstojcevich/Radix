uniform sampler2D texture;

varying vec3 vPosition;
varying vec2 vTexCoord;
varying vec4 vColor;

uniform int enableTexturing;
uniform int enableColors;

void main() {
  if(enableTexturing > 0) {
    gl_FragColor = texture2D(texture, vTexCoord);
    if(enableColors > 0) {
      gl_FragColor *= vColor;
    }
  } else if(enableColors > 0) {
    gl_FragColor = vColor;
  }
}
