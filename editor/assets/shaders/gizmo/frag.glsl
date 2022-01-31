#ifdef GL_ES
#define LOWP lowp
precision mediump float;
#else
#define LOWP
#endif


varying LOWP vec4 v_color;

void main () {
    gl_FragColor = v_color;
}
