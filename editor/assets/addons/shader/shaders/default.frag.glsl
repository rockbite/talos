#ifdef GL_ES
 #define LOWP lowp
     precision mediump float;
 #else
     #define LOWP
 #endif

@Control[range(0.0, 1.0)]
uniform float externalNumber;

varying LOWP vec4 v_color;
varying vec3 v_normal;
varying vec3 v_position;

 void main()
 {
     vec3 lightPos = vec3(10.0, 7.0, 12.0);
     vec3 lightColor = vec3(1.0);
     vec3 norm = v_normal;
     vec3 lightDir = normalize(lightPos - v_position);
     float diff = max(dot(norm, lightDir), 0.1);
     vec3 diffuse = diff * lightColor;

     vec3 nc = (norm.xyz + 1.0) / 2.0;

     gl_FragColor = v_color * vec4(diffuse, 1.0 * externalNumber);
 }