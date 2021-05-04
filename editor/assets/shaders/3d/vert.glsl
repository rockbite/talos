attribute vec4 a_position;
attribute vec4 a_color;

varying vec4 v_color;

uniform mat4 u_projTrans;

void main () {
    v_color = a_color;
    gl_Position = u_projTrans * a_position;
}