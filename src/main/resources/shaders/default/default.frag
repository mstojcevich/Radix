uniform sampler2D texSampler;
varying vec3 normal;

struct SimpleDirectionalLight
{
   vec3 vColor;
   vec3 vDirection;
   float fAmbientIntensity;
};

void main() {
    SimpleDirectionalLight sunLight;

    sunLight.fAmbientIntensity = 0.3;
    sunLight.vDirection.x = -0.6;
    sunLight.vDirection.y = -0.7;
    sunLight.vDirection.z = 0.4;

    float fDiffuseIntensity = max(0.0, dot(normalize(normal), -sunLight.vDirection));

    if (true)
        gl_FragColor = texture2D(texSampler, gl_TexCoord[0].st)*gl_Color*(sunLight.fAmbientIntensity+fDiffuseIntensity);
    else
        gl_FragColor = gl_Color;
}