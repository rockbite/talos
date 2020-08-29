#ifdef GL_ES
 #define LOWP lowp
     precision mediump float;
 #else
     #define LOWP
 #endif

 varying LOWP vec4 v_color;
 varying vec2 v_texCoords;
 uniform sampler2D u_texture;

 void main()
 {
     vec4 color = texture2D(u_texture, v_texCoords);
     color.rgb *= color.a;

     float alpha = color.a;

     color.a = mix((1.0 - color.a) * color.a, color.a, color.a);

     //color.rgb *= v_color.a * v_color.rgb;

     color.a *= v_color.a;

     // maybe color should be affecting more low alpha areas
     vec3 colorAffected = color.rgb * v_color.a * v_color.rgb;
     vec3 colorNotAffected = color.rgb * v_color.a;
     color.rgb = mix(colorAffected, colorNotAffected, alpha);

     gl_FragColor = color;
 }