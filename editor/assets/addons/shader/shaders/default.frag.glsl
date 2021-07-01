#ifdef GL_ES
 #define LOWP lowp
     precision mediump float;
 #else
     #define LOWP
 #endif

varying LOWP vec4 v_color;

@Control[range(0.0, 1.0)]
uniform float externalNumber;

 void main()
 {
     gl_FragColor = vec4(1.0 * externalNumber, 1.0, 1.0 * externalNumber, 1.0) * v_color;
 }