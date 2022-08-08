#ifdef GL_ES
 #define LOWP lowp
     precision mediump float;
 #else
     #define LOWP
 #endif

 varying LOWP vec4 v_color;
 varying vec2 v_texCoords;
 uniform sampler2D u_texture;

 #define POINT_BUFFER 30
 uniform float alpha[POINT_BUFFER];
 uniform int alphaCount;

 uniform float regionU;
 uniform float regionV;
 uniform float regionU2;
 uniform float regionV2;


 void main()
 {
    vec4 color = texture2D(u_texture, v_texCoords);
    vec3 fillColor = vec3(49.0 / 255.0, 146.0 / 255.0, 72.0 / 255.0);
    vec3 bgColor = vec3(58.0 / 255.0, 58.0 / 255.0, 58.0 / 255.0);

    float pi = 3.1415926;

    vec2 pos = vec2((v_texCoords.x - regionU)/(regionU2 - regionU), (v_texCoords.y - regionV)/(regionV2 - regionV));
    pos.y = 1.0 - pos.y;
    pos -= 0.5;
    pos = normalize(pos);

    float pixelAngle = (atan(pos.x, pos.y) + pi)/(2.0*pi);

    vec4 result =  vec4(0.0, 0.0, 0.0, v_color.a * color.a);

    for(int i = 0; i < alphaCount; i++) {
         float alphaVal = alpha[i];
         float ratio = step(alphaVal, pixelAngle); // if ratio is 1 it's background if 0 we fill with green

         vec3 tempFill = fillColor.rgb;
         tempFill *= (1.0 / float(alphaCount));
         vec3 tempBg = bgColor.rgb;
         tempBg *= (1.0 / float(alphaCount));

         vec3 fill = mix(tempFill, tempBg, ratio);

         result.rgb += fill.rgb;
    }

    gl_FragColor = result;
 }
