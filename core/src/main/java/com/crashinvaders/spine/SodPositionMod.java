package com.crashinvaders.spine;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.crashinvaders.common.SecondOrderDynamics2D;
import com.esotericsoftware.spine.Bone;
import com.esotericsoftware.spine.Skeleton;
import com.esotericsoftware.spine.utils.SkeletonActor;

public class SodPositionMod implements ISkeletonModifier {

    private final Vector2 tmpVec = new Vector2();

    private float mix = 1f;

    private final Bone bone;

    private final SecondOrderDynamics2D sod = new SecondOrderDynamics2D();

    public SodPositionMod(Skeleton skeleton, String boneName) {
        if (skeleton == null) throw new IllegalArgumentException("skeleton cannot be null.");
        this.bone = skeleton.findBone(boneName);
        if (bone == null) throw new IllegalArgumentException("Cannot find bone with the name: " + boneName);

        sod.reset(2f, 1f, 0f, bone.getWorldX(), bone.getWorldY());
    }

    @Override
    public void update(SkeletonActor skeletonActor, float deltaTime) {
        float srcPosX = bone.getWorldX();
        float srcPosY = bone.getWorldY();
        sod.update(deltaTime, srcPosX, srcPosY);
        float dstPosX = sod.getPosX();
        float dstPosY = sod.getPosY();
        bone.setWorldX(MathUtils.lerp(srcPosX, dstPosX, mix));
        bone.setWorldY(MathUtils.lerp(srcPosY, dstPosY, mix));
//        bone.worldToLocal(tmpVec.set(bone.getWorldX(), bone.getWorldY()));
//        bone.setPosition(tmpVec.x, tmpVec.y);
//        bone.updateAppliedTransform();
    }

    @Override
    public void reset() {
        sod.moveInstant(bone.getWorldX(), bone.getWorldY());
    }

    public void updateInstant() {
        sod.moveInstant(bone.getWorldX(), bone.getWorldY());
    }

    public void setSodParams(float f, float z, float r) {
        sod.configure(f, z, r);
    }

    public void setMix(float mix) {
        this.mix = mix;
    }
}
