package com.crashinvaders.spine;

public interface ISkeletonModifier {
    void update(CustomSkeletonActor skeletonActor, float deltaTime);
    void reset();
}
