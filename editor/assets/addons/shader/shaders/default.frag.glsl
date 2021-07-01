#ifdef GL_ES
 #define LOWP lowp
     precision mediump float;
 #else
     #define LOWP
 #endif

 @Control[range(0.0, 1.0)]
 uniform float externalNumber;

 void main()
 {
     gl_FragColor = vec4(1.0, 1.0 * externalNumber, 0.0, 1.0);
 }