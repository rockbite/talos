/*******************************************************************************
 * Copyright 2019 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.talosvfx.talos.runtime.vfx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Pool;
import com.talosvfx.talos.runtime.vfx.modules.DrawableModule;
import com.talosvfx.talos.runtime.vfx.modules.HistoryParticlePointDataGeneratorModule;
import com.talosvfx.talos.runtime.vfx.modules.ParticleModule;
import com.talosvfx.talos.runtime.vfx.modules.ParticlePointDataGeneratorModule;

public class Particle implements Pool.Poolable {

    private IEmitter emitterReference;

    public Vector3 spawnPosition = new Vector3();

    public Vector3 localPosition = new Vector3();
    public Vector3 worldPosition = new Vector3();
    public Vector3 velocity = new Vector3();
    public Vector3 spinVelocity = new Vector3();
    public Vector3 acceleration = new Vector3();

    public float life;

    public Vector3 rotation = new Vector3();
    public Vector3 rotationChanged = new Vector3();
    public Vector2 size = new Vector2();

    public Vector2 pivot = new Vector2();

    public Color color = new Color();

    public Rectangle collisionRect = new Rectangle();
    public float collisionRestitution = 1.0f;
    public float collisionFriction = 0.0f;
    public boolean collisionLocalSpace = false;
    public float collisionLifetimeReduction = 1.0f;

    public float alpha; // alpha position from 0 to 1 in it's lifetime cycle

    public float seed;
    public int requesterID;

    public float durationAtInit;
    private float initialWorldRotation;


    public Particle() {
        // empty constructor
    }

    public void init(IEmitter emitterReference) {
        this.seed = MathUtils.random();
        this.requesterID = emitterReference.getScope().newParticleRequester();
        init(emitterReference, seed);
    }

    public void init(IEmitter emitterReference, float seed) {
        this.emitterReference = emitterReference;
        ParticleModule particleModule = emitterReference.getParticleModule();

        initialWorldRotation = emitterReference.getWorldRotation();
        Vector2 worldScale = emitterReference.getWorldScale();


        this.seed = seed;

        // inner variable defaults
        alpha = 0f;

        particleModule.updateScopeData(this);

        //Starting values
        life = particleModule.getLife();
        localPosition.set(particleModule.getSpawnPosition());
        if (!emitterReference.getEmitterModule().isAttached()) {
            localPosition.rotate(initialWorldRotation, 0, 0, 1).scl(worldScale.x, worldScale.y, 0);
        }

        rotation.set(particleModule.getSpawnRotation());
        acceleration.set(0, 0, 0);

        velocity.set(particleModule.getInitialVelocity());


        if (!emitterReference.getEmitterModule().isAttached()) {
            velocity.rotate(initialWorldRotation, 0, 0, 1).scl(worldScale.x, worldScale.y, 0);
        }


        spinVelocity.set(particleModule.getInitialSpinVelocity());


        spawnPosition.set(emitterReference.getEffectPosition());
        if (!emitterReference.getEmitterModule().isAttached()) {
//            spawnPosition.rotate(Vector3.Z, initialWorldRotation).scl(worldScale.x);
        }

        durationAtInit = emitterReference.getAlpha();
    }

    public void update (ParticleEmitterInstance particleEmitterInstance, float delta) {
        if(alpha == 1f) return;

        if(emitterReference == null) return;

        //scope data
        ParticleModule particleModule = emitterReference.getParticleModule();
        if(particleModule == null) return;

        life = particleModule.getLife(); // maybe should remove this

        alpha += delta/life;
        if(alpha > 1f) alpha = 1f;

        applyAlpha(alpha, delta);

        final DrawableModule drawableModule = emitterReference.getDrawableModule();
        if (drawableModule == null) return;

        ParticlePointDataGeneratorModule pointDataGenerator = drawableModule.getPointDataGenerator();
        if (pointDataGenerator != null) {
            //set the context free the points, and generate new particle point data

            int cacheMode = emitterReference.getScope().getRequestMode();
            int cacheRequestID = emitterReference.getScope().getRequesterID();

            emitterReference.getScope().setCurrentRequestMode(ScopePayload.PARTICLE_ALPHA);
            emitterReference.getScope().setCurrentRequesterID(this.requesterID);
            pointDataGenerator.generateParticlePointData(this, particleEmitterInstance.particlePointDataPool, particleEmitterInstance.groupPool);

            emitterReference.getScope().setCurrentRequestMode(cacheMode);
            emitterReference.getScope().setCurrentRequesterID(cacheRequestID);
        }
    }

    public void applyAlpha (float alpha, float delta) {
        ParticleModule particleModule = emitterReference.getParticleModule();
        if(particleModule == null) return;

        particleModule.updateScopeData(this);

        //Step teh particle data

        float worldRotation = emitterReference.getWorldRotation();
        //world rotation of 0 if not in attached mode
        if (!emitterReference.getEmitterModule().isAttached()) {
            worldRotation = 0;
        }


        Vector2 worldScale = emitterReference.getWorldScale();
        if (particleModule.hasPositionOverride()) {
            Vector3 temp = new Vector3();
            temp.set(particleModule.getSpawnPosition());

            localPosition.set(temp);

            temp.set(particleModule.getPositionOverride());
            localPosition.add(temp);

            worldPosition.set(localPosition);
            if (emitterReference.getEmitterModule().isAttached()) {
                worldPosition.rotate(Vector3.Z, worldRotation).scl(worldScale.x);
            } else {
                worldPosition.scl(worldScale.x, worldScale.y, 1f);
            }
        } else {
            final Vector3 drag = particleModule.getDrag();
            float dragX = drag.x;
            float dragY = drag.y;
            float dragZ = drag.z;

            if (particleModule.hasVelocityOverTime()) {
                //Velocity is driven by velocity over time
                final Vector3 velocityOverTime = particleModule.getVelocityOverTime();
                velocity.set(velocityOverTime);
                if (!emitterReference.getEmitterModule().isAttached()) {
                    velocity.rotate(initialWorldRotation, 0, 0, 1); //not attached rotate it
                    velocity.scl(worldScale.x, worldScale.y, 0);
                }
            } else {
                //Acceleration mutate by forces

                float ax = acceleration.x;
                float ay = acceleration.y;
                float az = acceleration.z;

                final Vector3 forces = particleModule.getForces();
                float forcesX = forces.x;
                float forcesY = forces.y;
                float forcesZ = forces.z;
                final Vector3 gravity = particleModule.getGravity();
                float gravityX = gravity.x * worldScale.x;
                float gravityY = gravity.y * worldScale.y;
                float gravityZ = gravity.z;

                ax += forcesX * delta;
                ay += forcesY * delta;
                az += forcesZ * delta;

                ax += gravityX * delta;
                ay += gravityY * delta;
                az += gravityZ * delta;

                acceleration.set(ax, ay, az);

                float vx = velocity.x;
                float vy = velocity.y;
                float vz = velocity.z;

                vx += ax * delta;
                vy += ay * delta;
                vz += az * delta;

                if (particleModule.hasDrag()) {
                    float compoundSize = 1f;//No size info here
                    float dragComponentX = Math.abs(0.5f * (velocity.x * velocity.x) * dragX * compoundSize);
                    float dragComponentY = Math.abs(0.5f * (velocity.y * velocity.y) * dragY * compoundSize);
                    float dragComponentZ = Math.abs(0.5f * (velocity.z * velocity.z) * dragZ * compoundSize);

                    //vx = x
                    vx = vx - (Math.signum(vx) * (dragComponentX * delta));
                    vy = vy - (Math.signum(vy) * (dragComponentY * delta));
                    vz = vz - (Math.signum(vz) * (dragComponentZ * delta));

                }

                velocity.set(vx, vy, vz);

            }

            float posX = localPosition.x;
            float posY = localPosition.y;
            float posZ = localPosition.z;

            posX += velocity.x * delta;
            posY += velocity.y * delta;
            posZ += velocity.z * delta;

            localPosition.set(posX, posY, posZ);
            worldPosition.set(localPosition);
            if (emitterReference.getEmitterModule().isAttached()) {
                worldPosition.rotate(Vector3.Z, worldRotation).scl(worldScale.x);
            }
        }

        if (particleModule.hasRotationOverride()) {
            rotation.set(particleModule.getSpawnRotation());
            rotation.add(worldRotation);
            rotation.add(particleModule.getRotationOverride());
        } else {
            if (particleModule.hasSpinVelocityOverTime()) {
                //Spin velocity is driven by velocity over time
                final Vector3 spinVelocityOverTime = particleModule.getSpinVelocityOverTime();
                spinVelocity.set(spinVelocityOverTime);
            } else {
                //drag probably
            }

            rotationChanged.x += spinVelocity.x * delta;
            rotationChanged.y += spinVelocity.y * delta;
            rotationChanged.z += spinVelocity.z * delta;

            rotation.set(particleModule.getSpawnRotation());
            rotation.add(worldRotation);
            rotation.add(rotationChanged);

        }

        if (emitterReference.getEmitterModule().isAligned()) {
            rotation.set(velocity);
        }

        pivot.set(particleModule.getPivot());

        // Update collision rect if defined
        if (particleModule.hasCollision()) {
            collisionRect.set(
                particleModule.getCollisionX(),
                particleModule.getCollisionY(),
                particleModule.getCollisionWidth(),
                particleModule.getCollisionHeight()
            );
            collisionRestitution = particleModule.getCollisionRestitution();
            collisionFriction = particleModule.getCollisionFriction();
            collisionLocalSpace = particleModule.isCollisionLocalSpace();
            collisionLifetimeReduction = particleModule.getCollisionLifetimeReduction();

            // Check for collision and bounce
            handleCollision();
        } else {
            collisionRect.set(0, 0, 0, 0);
        }

    }

    private static final float COLLISION_EPSILON = 0.001f;

    private void handleCollision() {
        if (collisionRect.width <= 0 || collisionRect.height <= 0) return;

        // Get particle position based on local/world space setting
        float particleX, particleY;
        if (collisionLocalSpace) {
            particleX = localPosition.x;
            particleY = localPosition.y;
        } else {
            particleX = getX();
            particleY = getY();
        }

        // Get world scale to apply to collision rectangle
        Vector2 worldScale = emitterReference.getWorldScale();
        float scaleX = worldScale.x;
        float scaleY = worldScale.y;

        // Scale the collision rectangle position and size
        float scaledX = collisionRect.x * scaleX;
        float scaledY = collisionRect.y * scaleY;
        float scaledWidth = collisionRect.width * scaleX;
        float scaledHeight = collisionRect.height * scaleY;

        // Check if particle point is inside the scaled collision rectangle
        if (particleX >= scaledX && particleX <= scaledX + scaledWidth &&
                particleY >= scaledY && particleY <= scaledY + scaledHeight) {

            // Calculate distances to each edge
            float distToLeft = particleX - scaledX;
            float distToRight = (scaledX + scaledWidth) - particleX;
            float distToBottom = particleY - scaledY;
            float distToTop = (scaledY + scaledHeight) - particleY;

            // Find the minimum distance to determine which edge to bounce from
            float minDistX = Math.min(distToLeft, distToRight);
            float minDistY = Math.min(distToBottom, distToTop);

            if (minDistX < minDistY) {
                // Bounce horizontally
                velocity.x = -velocity.x * collisionRestitution;
                velocity.y *= (1.0f - collisionFriction);

                if (distToLeft < distToRight) {
                    localPosition.x -= (distToLeft + COLLISION_EPSILON);
                } else {
                    localPosition.x += (distToRight + COLLISION_EPSILON);
                }
            } else {
                // Bounce vertically
                velocity.y = -velocity.y * collisionRestitution;
                velocity.x *= (1.0f - collisionFriction);

                if (distToBottom < distToTop) {
                    localPosition.y -= (distToBottom + COLLISION_EPSILON);
                } else {
                    localPosition.y += (distToTop + COLLISION_EPSILON);
                }
            }

            // Update world position after adjustment
            worldPosition.set(localPosition);

            // Apply lifetime reduction
            if (collisionLifetimeReduction <= 0) {
                alpha = 1.0f;
            } else if (collisionLifetimeReduction < 1.0f) {
                float remainingLifeFraction = 1.0f - alpha;
                float reducedRemaining = remainingLifeFraction * collisionLifetimeReduction;
                alpha = 1.0f - reducedRemaining;
            }
        }
    }

    public float getAttachedPositionX () {
        return emitterReference.getEffectPosition().x + worldPosition.x;
    }

    public float getAttachedPositionY () {
        return emitterReference.getEffectPosition().y + worldPosition.y;
    }

    public float getAttachedPositionZ () {
        return emitterReference.getEffectPosition().z + worldPosition.z;
    }

    public float getX() {
        if(emitterReference.getEmitterModule().isAttached()) {
            return getAttachedPositionX();
        } else {
            return spawnPosition.x + worldPosition.x;
        }
    }

    public float getY() {
        if(emitterReference.getEmitterModule().isAttached()) {
            return getAttachedPositionY();
        } else {
            return spawnPosition.y + worldPosition.y;
        }
    }

    public float getZ() {
        if(emitterReference.getEmitterModule().isAttached()) {
            return getAttachedPositionZ();
        } else {
            return spawnPosition.z + worldPosition.z;
        }
    }

    @Override
    public void reset() {
        localPosition.setZero();
        worldPosition.setZero();
        requesterID = -1;
    }

    public float getEmitterAlpha () {
        return emitterReference.getAlpha();
    }

    public IEmitter getEmitter () {
        return emitterReference;
    }

    public void notifyKill() {
        ParticleModule particleModule = emitterReference.getParticleModule();
        if(particleModule == null) return;
        particleModule.updateScopeData(this);
        ParticlePointDataGeneratorModule pointDataGenerator = emitterReference.getDrawableModule().getPointDataGenerator();
        if (pointDataGenerator instanceof HistoryParticlePointDataGeneratorModule) {
            ((HistoryParticlePointDataGeneratorModule)pointDataGenerator).onParticleKilled(this);
        }
    }

    public int getRequesterIDUniqueToGlobalScope () {
        int x = requesterID;
        int y = emitterReference.getEffectUniqueID();
        return ((x + y) * (x + y + 1)) / 2 + y;
    }

    public boolean hasCollision() {
        return collisionRect.width > 0 && collisionRect.height > 0;
    }
}
