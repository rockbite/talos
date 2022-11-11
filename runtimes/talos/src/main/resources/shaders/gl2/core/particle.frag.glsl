#ifdef GL_ES
#define LOWP lowp
precision mediump float;
#else
#define LOWP
#endif

uniform sampler2D u_texture;

varying vec4 v_color;
varying vec2 v_texCoords;


void main () {
    vec4 diffuse = texture2D(u_texture, v_texCoords);
    gl_FragColor = vec4(diffuse * v_color);
}
