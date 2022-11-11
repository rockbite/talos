package com.talosvfx.talos.runtime.utils;

import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.TimeUtils;

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

        private long lastAccess;

        Random random = new Random();

        private int segment = 0;

        public Distribution() {
            reset();
        }

        @Override
        public void reset () {
            segment = 0;
            lastAccess = TimeUtils.millis();
        }

        public void setSeed (int seed) {
            random.setSeed(seed);
        }

        public float nextFloat () {
            lastAccess = TimeUtils.millis();

            if(segment == 0) {
                segment = random.nextBoolean() ? 1 : 2;
            } else if(segment == 1) {
                segment = random.nextBoolean() ? 0 : 2;
            } else {
                if (segment == 2) segment = random.nextBoolean() ? 0 : 1;
            }

            return (1f/3f) * (segment + random.nextFloat());
        }

        public boolean isExpired() {
            return TimeUtils.millis() - lastAccess > 5 * 1000;
        }
    }
}
