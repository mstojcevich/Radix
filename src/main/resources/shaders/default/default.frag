uniform sampler2D texSampler;
void main() {
    if (true)
        gl_FragColor = texture2D(texSampler, gl_TexCoord[0].st);
    else
        gl_FragColor = gl_Color;
}