attribute vec3 a_position;
uniform mat4 u_projViewTrans;

varying vec4 v_color;
attribute vec4 a_color;

attribute vec3 a_normal;
uniform mat3 u_normalMatrix;
varying vec3 v_normal;

attribute vec2 a_texCoord0;

uniform vec4 u_diffuseUVTransform;
varying vec2 v_diffuseUV;

uniform mat4 u_worldTrans;

uniform float u_opacity;
varying float v_opacity;

uniform float u_alphaTest;
varying float v_alphaTest;

varying vec2 v_rawUV;
varying vec3 v_position;

void main() {
    v_rawUV = a_texCoord0;
    v_position = a_position.xyz;

	v_diffuseUV = u_diffuseUVTransform.xy + a_texCoord0 * u_diffuseUVTransform.zw;

	v_color = a_color;

	v_opacity = u_opacity;
	v_alphaTest = u_alphaTest;

	vec4 pos = u_worldTrans * vec4(a_position, 1.0);

	gl_Position = u_projViewTrans * pos;

	vec3 normal = normalize(u_normalMatrix * a_normal);
	v_normal = normal;
}
