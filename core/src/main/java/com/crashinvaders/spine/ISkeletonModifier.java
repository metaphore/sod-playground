package com.crashinvaders.spine;

import com.esotericsoftware.spine.utils.SkeletonActor;

public interface ISkeletonModifier {
    void update(SkeletonActor skeletonActor, float deltaTime);
    void reset();
}
