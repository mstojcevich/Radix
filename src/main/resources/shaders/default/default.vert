uniform sampler2D texSampler;
varying vec3 normal;
varying vec2 texCoord;
uniform vec3 chunkOffset;

uniform int animTime;
uniform int enableWave;

void main() {
    if(enableWave > 0) {
        vec4 pos = gl_Vertex;

        float waveLength = 1.0;
        float waveTime = 0.005;
        float waveHeight = 0.1;

        pos.y -= 0.2;

        float posYbuf = (pos.z+chunkOffset.z) / waveLength + float(animTime) * waveTime * waveLength;

        pos.y -= sin(posYbuf) * waveHeight + sin(posYbuf / 7.0) * waveHeight;
        gl_Position = gl_ModelViewProjectionMatrix * pos;
    } else {
        gl_Position = ftransform();
    }
    gl_FrontColor = gl_Color;
    normal = gl_Normal;
    texCoord = gl_MultiTexCoord0.xy;
}