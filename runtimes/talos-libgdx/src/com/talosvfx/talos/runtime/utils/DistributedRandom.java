package com.talosvfx.talos.runtime.utils;

import com.badlogic.gdx.utils.*;
import com.sun.org.apache.regexp.internal.RE;

import java.util.Iterator;
import java.util.Random;

public class DistributedRandom {


    private int seed;

    Pool<Distribution> distributionPool;
    IntMap<Distribution> map = new IntMap<>();

    private long lastClear;

    public DistributedRandom() {
        distributionPool = new Pool<Distribution>() {
            @Override
            protected Distribution newObject () {
                return new Distribution();
            }
        };
        lastClear = TimeUtils.millis();
    }

    private void clearMap() {
        if(TimeUtils.millis() - lastClear > 10 * 1000) {
            Iterator<IntMap.Entry<Distribution>> iterator = map.iterator();
            while (iterator.hasNext()) {
                IntMap.Entry<Distribution> entry = iterator.next();
                if (entry.value.isExpired()) {
                    distributionPool.free(entry.value);
                    iterator.remove();
                }
            }
            lastClear = TimeUtils.millis();
        }
    }

    public void setSeed(int seed) {
        this.seed = seed;

        clearMap();

        if(!map.containsKey(seed)) {
            Distribution distribution = distributionPool.obtain();
            distribution.setSeed(seed);
            map.put(seed, distribution);
        }
    }

    public float nextFloat() {
        return map.get(seed).nextFloat();
    }

    public class Distribution implements Pool.Poolable {

        private final int RESOLUTION = 10;

        private long lastAccess;

        Random random = new Random();

        public float[] data = new float[RESOLUTION];

        public Distribution() {
            reset();
        }

        @Override
        public void reset () {
            for(int i = 0; i < RESOLUTION; i++) {
                data[i] = 1f/RESOLUTION;
            }
            lastAccess = TimeUtils.millis();
        }

        public void setSeed (int seed) {
            random.setSeed(seed);
        }

        private void normalize(boolean heal) {
            float sum = 0;
            for(int i = 0; i < RESOLUTION; i++) {
                if (1f/RESOLUTION - data[i] > 0) {
                    data[i] += (1f/RESOLUTION - data[i])/3f;
                }
                sum+= data[i];
            }
            for(int i = 0; i < RESOLUTION; i++) {
                data[i] *= 1f/sum;
            }
        }

        public float nextFloat () {
            lastAccess = TimeUtils.millis();

            normalize(true);

            float roll = random.nextFloat();
            float cursor = 0;
            int segment = 0;
            for(int i = 0; i < RESOLUTION; i++) {
                if(roll >= cursor && roll <= cursor + data[i]) {
                    segment = i;
                    break;
                }
                cursor += data[i];
            }

            //leave footprint
            float step = 1f/ RESOLUTION;
            data[segment] -= step;
            if(data[segment] < 0) {
                data[segment] = 0;
            }
            for(int i = 1; i <= (RESOLUTION/4); i++) {
                float reduce = i * step * (1f/(1 + RESOLUTION/4));
                if(segment-i >= 0) {
                    data[segment - i] -= reduce;
                    if(data[segment - i] < 0) {
                        data[segment - i] = 0;
                    }
                }
                if(segment + i < data.length) {
                    data[segment + i] -= reduce;
                    if(data[segment + i] < 0) {
                        data[segment + i] = 0;
                    }
                }
            }

            System.out.println(segment);

            return (1f/RESOLUTION) * (segment + random.nextFloat());
        }

        public boolean isExpired() {
            return TimeUtils.millis() - lastAccess > 5 * 1000;
        }
    }
}
