#ifdef GL_ES
#define LOWP lowp
precision mediump float;
#else
#define LOWP
#endif

uniform sampler2D u_texture;

in vec4 v_color;
in vec2 v_texCoords;

out vec4 fragmentColor;

void main () {
    vec4 diffuse = texture(u_texture, v_texCoords);
    fragmentColor = vec4(diffuse * v_color);
}
