# version 130

uniform sampler2D texSampler;
varying vec3 normal;
varying vec2 texCoord;
uniform vec3 chunkOffset;
attribute int blockID;

uniform int animTime;
uniform int enableWave;
uniform int blocksPerTexMapRow;
uniform int texMapBlockWidth;
uniform int texMapWidth;
uniform int texMapHeight;

void main() {
    if(enableWave > 0) {
        vec4 pos = fract(gl_Vertex);

        float waveLength = 1.0;
        float waveTime = 0.002;
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

    vec2 uvMult = vec2(dot(normal.zxy, gl_Vertex.xyz),
                   dot(normal.yzx, gl_Vertex.xyz));
    vec2 texPosPix = vec2(float((2 % (blocksPerTexMapRow+1)) * texMapBlockWidth),
                            float((2 / (blocksPerTexMapRow+1)) * texMapBlockWidth));
    vec2 uvStart = texPosPix / vec2(float(texMapWidth), float(texMapHeight));
    texCoord = uvStart + fract(uvMult)*vec2(32.0/128.0, 32.0/128.0);
}