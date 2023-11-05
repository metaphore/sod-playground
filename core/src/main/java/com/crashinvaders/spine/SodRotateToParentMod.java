package com.crashinvaders.spine;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.crashinvaders.common.SecondOrderDynamics1D;
import com.esotericsoftware.spine.Bone;
import com.esotericsoftware.spine.Skeleton;

public class SodRotateToParentMod implements ISkeletonModifier {

    private static final Vector2 tmpVec = new Vector2();

    private final Bone bone;
    private final SecondOrderDynamics1D sod = new SecondOrderDynamics1D();

    private float mix = 1f;

    private float baseRotation = 0f;

    public SodRotateToParentMod(Skeleton skeleton, String boneName) {
        if (skeleton == null) throw new IllegalArgumentException("skeleton cannot be null.");
        this.bone = skeleton.findBone(boneName);
        if (bone == null) throw new IllegalArgumentException("Cannot find bone with the name: " + boneName);

        baseRotation = bone.getData().getRotation();
        sod.reset(2f, 1f, 0f, 270f); //TODO Initial rotation.
    }

    @Override
    public void update(CustomSkeletonActor skeletonActor, float deltaTime) {
//        float srcRotation = baseRotation + computeAngleToParent();
        float srcRotation = baseRotation + computeAngleToParent();
        sod.update(deltaTime, srcRotation);
        float dstRotation = sod.getPos();
        bone.setRotation(MathUtils.lerp(bone.getRotation(), bone.worldToLocalRotation(dstRotation), mix));
        bone.updateAppliedTransform();
    }

    @Override
    public void reset() {
        sod.moveInstant(baseRotation + computeAngleToParent());
        bone.setRotation(MathUtils.lerp(bone.getRotation(), bone.worldToLocalRotation(sod.getPos()), mix));
    }

    public void updateInstant() {
        throw new UnsupportedOperationException();
    }

    public void setSodParams(float f, float z, float r) {
        sod.configure(f, z, r);
    }

    public void setMix(float mix) {
        this.mix = mix;
    }

    public float getF() {
        return sod.getF();
    }

    public float getZ() {
        return sod.getZ();
    }

    public float getR() {
        return sod.getR();
    }

    private float computeAngleToParent() {
        Bone parentBone = bone.getParent();
        return tmpVec.set(bone.getWorldX(), bone.getWorldY())
            .sub(parentBone.getWorldX(), parentBone.getWorldY())
            .angleDeg();
    }
}
