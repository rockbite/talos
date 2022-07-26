#ifdef GL_ES
 #define LOWP lowp
     precision mediump float;
 #else
     #define LOWP
 #endif

 varying LOWP vec4 v_color;
 varying vec2 v_texCoords;
 uniform sampler2D u_texture;

 uniform float alpha;

 uniform float regionU;
 uniform float regionV;
 uniform float regionU2;
 uniform float regionV2;


 void main()
 {
    vec4 color = texture2D(u_texture, v_texCoords);
    vec4 fillColor = vec4(49.0 / 255.0, 146.0 / 255.0, 72.0 / 255.0, 1.0);
    vec4 bgColor = vec4(58.0 / 255.0, 58.0 / 255.0, 58.0 / 255.0, 1.0);

    float pi = 3.1415926;

    vec2 pos = vec2((v_texCoords.x - regionU)/(regionU2 - regionU), (v_texCoords.y - regionV)/(regionV2 - regionV));
    pos.y = 1.0 - pos.y;
    pos -= 0.5;
    pos = normalize(pos);

    float pixelAngle = (atan(pos.x, pos.y) + pi)/(2*pi);

    float ratio = step(alpha, pixelAngle);

    vec4 result = mix(fillColor, bgColor, ratio);

    result.a *=  v_color.a * color.a;

    gl_FragColor = result;
 }
