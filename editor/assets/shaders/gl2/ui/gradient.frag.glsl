#ifdef GL_ES
 #define LOWP lowp
     precision mediump float;
 #else
     #define LOWP
 #endif

#define GRADIENT_POINT_BUFFER 30
#define MAX_POINTS 30

uniform sampler2D u_textures[MAX_TEXTURE_UNITS];

varying LOWP vec4 v_color;
varying vec2 v_texCoords;
varying float v_texture_index;


vec4 sampleTextureArray (int index, vec2 texCoords) {
    %SAMPLE_TEXTURE_ARRAY_CODE%
}

 struct GradientPoint
 {
     vec4 color;
     float alpha;
 };


 uniform GradientPoint u_gradientPoints[GRADIENT_POINT_BUFFER];
 uniform int u_pointCount;

 void main()
 {
     vec4 result = u_gradientPoints[0].color;
     float position = v_texCoords.x;
     for(int i = 1; i < MAX_POINTS; i++) {
         if (i >= u_pointCount) break;
         result = mix(result, u_gradientPoints[i].color, smoothstep(u_gradientPoints[i-1].alpha, u_gradientPoints[i].alpha, position));
     }
     gl_FragColor = result + v_color * sampleTextureArray(int(v_texture_index), v_texCoords) * 0.001;
 }
