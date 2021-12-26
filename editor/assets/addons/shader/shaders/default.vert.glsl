attribute vec4 a_position;
attribute vec4 a_color;
attribute vec2 a_texCoord0;
attribute vec3 a_normal;

uniform mat4 u_projTrans;

varying vec4 v_color;
varying vec3 v_normal;
varying vec3 v_position;
varying vec2 v_texCoords;

void main()
{
    v_color = a_color;
    v_color.a = v_color.a * (256.0/255.0);
    v_normal = a_normal;
    v_position = a_position.xyz;
    v_texCoords = a_texCoord0;
    gl_Position =  u_projTrans * a_position;
}
