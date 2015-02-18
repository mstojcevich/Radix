uniform sampler2D texSampler;

void main() {
    gl_FrontColor = gl_Color;
    gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
    gl_TexCoord[0]=gl_MultiTexCoord0;
}