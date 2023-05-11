package com.talosvfx.talos.editor.project2.debug;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.profiling.GLErrorListener;
import com.badlogic.gdx.graphics.profiling.GLProfiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.badlogic.gdx.graphics.profiling.GLInterceptor.resolveErrorNumber;

public class DebugUtils {

    private static final Logger logger = LoggerFactory.getLogger(DebugUtils.class);
    GLProfiler glProfiler;

    public static final GLErrorListener LOGGING_LISTENER = new GLErrorListener() {
        @Override
        public void onError (int error) {
            String place = null;
            try {
                final StackTraceElement[] stack = Thread.currentThread().getStackTrace();
                for (int i = 0; i < stack.length; i++) {
                    if ("check".equals(stack[i].getMethodName())) {
                        if (i + 1 < stack.length) {
                            final StackTraceElement glMethod = stack[i + 1];
                            place = glMethod.getMethodName();
                        }
                        break;
                    }
                }
            } catch (Exception ignored) {
            }

            if (place != null) {
                logger.error("GLProfiler", "Error " + resolveErrorNumber(error) + " from " + place);
            } else {
                logger.error("GLProfiler", "Error " + resolveErrorNumber(error) + " at: ", new Exception());
                // This will capture current stack trace for logging, if possible
            }
        }
    };
    public void enableGpuDebugging () {
        if (glProfiler == null) {
            glProfiler = new GLProfiler(Gdx.graphics);
        }
        glProfiler.setListener(LOGGING_LISTENER);
        glProfiler.enable();
    }

    public void disableGpuDebugging () {
        if (glProfiler != null) {
            glProfiler.disable();
        }
    }
}
