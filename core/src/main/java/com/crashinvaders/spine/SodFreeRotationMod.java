package com.crashinvaders.spine;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.crashinvaders.common.SecondOrderDynamics2D;
import com.esotericsoftware.spine.Bone;
import com.esotericsoftware.spine.Skeleton;

public class SodFreeRotationMod implements ISkeletonModifier {

    private static final Vector2 tmpVec = new Vector2();

    private float mix = 1f;

    private final Bone bone;
    private final Vector2 target = new Vector2();

    private final SecondOrderDynamics2D sod = new SecondOrderDynamics2D();

    public SodFreeRotationMod(Skeleton skeleton, String boneName) {
        if (skeleton == null) throw new IllegalArgumentException("skeleton cannot be null.");
        this.bone = skeleton.findBone(boneName);
        if (bone == null) throw new IllegalArgumentException("Cannot find bone with the name: " + boneName);

        target.set(computeRotationTarget());
        sod.reset(2f, 1f, 0f, target.x, target.y);
    }

    @Override
    public void update(CustomSkeletonActor skeletonActor, float deltaTime) {
        computeRotationTarget();
        float srcRotTargetX = tmpVec.x;
        float srcRotTargetY = tmpVec.y;
        sod.update(deltaTime, srcRotTargetX, srcRotTargetY);
        float dstRotation = tmpVec
            .set(sod.getPosX(), sod.getPosY())
            .sub(bone.getWorldX(), bone.getWorldY())
            .angleDeg();
        bone.setRotation(MathUtils.lerp(bone.getRotation(), bone.worldToLocalRotation(dstRotation), mix));
//        bone.updateAppliedTransform();
    }

    @Override
    public void reset() {
        target.set(computeRotationTarget());
        sod.moveInstant(target.x, target.y);
    }

    public void setSodParams(float f, float z, float r) {
        sod.configure(f, z, r);
    }

    public void setMix(float mix) {
        this.mix = mix;
    }

    private Vector2 computeRotationTarget() {
        return tmpVec
                .set(100f, 0f)
                .rotateDeg(bone.localToWorldRotation(bone.getRotation()))
                .add(bone.getWorldX(), bone.getWorldY());
    }
}
