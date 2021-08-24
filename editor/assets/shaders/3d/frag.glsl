#ifdef GL_ES
#define LOWP lowp
precision mediump float;
#else
#define LOWP
#endif
varying LOWP vec4 v_color;
varying vec2 v_texCoords;

uniform sampler2D u_texture;



void main () {
    vec4 diffuse = texture2D(u_texture, v_texCoords);
    diffuse += vec4(1.0);
    gl_FragColor = v_color * diffuse;
}
