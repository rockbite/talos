#ifdef GL_ES
 #define LOWP lowp
     precision mediump float;
 #else
     #define LOWP
 #endif

 #define GRADIENT_POINT_BUFFER 30

 varying LOWP vec4 v_color;
 varying vec2 v_texCoords;
 uniform sampler2D u_texture;

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
     for(int i = 1; i < u_pointCount; i++) {
         result = mix(result, u_gradientPoints[i].color, smoothstep(u_gradientPoints[i-1].alpha, u_gradientPoints[i].alpha, position));
     }
     gl_FragColor = result + v_color * texture2D(u_texture, v_texCoords) * 0.001;
 }