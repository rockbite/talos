#ifdef GL_ES
#define LOWP lowp
precision mediump float;
#else
#define LOWP
#endif

uniform sampler2D u_textures[MAX_TEXTURE_UNITS];

varying LOWP vec4 v_color;
varying vec2 v_texCoords;
varying float v_texture_index;


vec4 sampleTextureArray (int index, vec2 texCoords) {
    %SAMPLE_TEXTURE_ARRAY_CODE%
}

void main () {
    gl_FragColor = v_color * sampleTextureArray(int(v_texture_index), v_texCoords);
}
