attribute vec3 a_position;
attribute vec3 a_normal;
attribute vec4 a_color;

uniform mat4 u_projTrans;

varying vec4 v_color;

void main () {
    v_color = a_color;

    gl_Position = u_projTrans * vec4(a_position, 1.0);
}
