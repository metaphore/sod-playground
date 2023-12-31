/******************************************************************************
 * Spine Runtimes License Agreement
 * Last updated July 28, 2023. Replaces all prior versions.
 *
 * Copyright (c) 2013-2023, Esoteric Software LLC
 *
 * Integration of the Spine Runtimes into software or otherwise creating
 * derivative works of the Spine Runtimes is permitted under the terms and
 * conditions of Section 2 of the Spine Editor License Agreement:
 * http://esotericsoftware.com/spine-editor-license
 *
 * Otherwise, it is permitted to integrate the Spine Runtimes into software or
 * otherwise create derivative works of the Spine Runtimes (collectively,
 * "Products"), provided that each user of the Products must obtain their own
 * Spine Editor license and redistribution of the Products in any form must
 * include this license and copyright notice.
 *
 * THE SPINE RUNTIMES ARE PROVIDED BY ESOTERIC SOFTWARE LLC "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL ESOTERIC SOFTWARE LLC BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES,
 * BUSINESS INTERRUPTION, OR LOSS OF USE, DATA, OR PROFITS) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THE
 * SPINE RUNTIMES, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *****************************************************************************/

package com.crashinvaders.spine;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.spine.AnimationState;
import com.esotericsoftware.spine.Skeleton;
import com.esotericsoftware.spine.SkeletonRenderer;
import com.esotericsoftware.spine.SkeletonRendererDebug;

/** A scene2d actor that draws a skeleton. */
public class CustomSkeletonActor extends Actor {

    private final Array<ISkeletonModifier> skeletonModifiers = new Array<>();

	private SkeletonRenderer renderer;
	private Skeleton skeleton;
	AnimationState state;
	private boolean resetBlendFunction = true;

    private SkeletonRendererDebug rendererDebug = null;

	/** Creates an uninitialized SkeletonActor. The renderer, skeleton, and animation state must be set before use. */
	public CustomSkeletonActor() {
	}

	public CustomSkeletonActor(SkeletonRenderer renderer, Skeleton skeleton, AnimationState state) {
		this.renderer = renderer;
		this.skeleton = skeleton;
		this.state = state;
	}

	public void act (float delta) {
		state.update(delta);
		state.apply(skeleton);

        skeleton.getRootBone().setRotation(getRotation());
        skeleton.setPosition(getX(), getY());
        skeleton.updateWorldTransform();

        super.act(delta);

        for (int i = 0; i < skeletonModifiers.size; i++) {
            skeletonModifiers.get(i).update(this, delta);
        }
	}

	public void draw (Batch batch, float parentAlpha) {
		int blendSrc = batch.getBlendSrcFunc(), blendDst = batch.getBlendDstFunc();
		int blendSrcAlpha = batch.getBlendSrcFuncAlpha(), blendDstAlpha = batch.getBlendDstFuncAlpha();

		Color color = skeleton.getColor();
		float oldAlpha = color.a;
		skeleton.getColor().a *= parentAlpha;

		renderer.draw(batch, skeleton);

		if (resetBlendFunction) batch.setBlendFunctionSeparate(blendSrc, blendDst, blendSrcAlpha, blendDstAlpha);

		color.a = oldAlpha;
	}

    @Override
    public void drawDebug(ShapeRenderer shapes) {
        super.drawDebug(shapes);

        if (rendererDebug != null) {
            rendererDebug.draw(skeleton);
        }
    }

    public SkeletonRenderer getRenderer () {
		return renderer;
	}

	public void setRenderer (SkeletonRenderer renderer) {
		this.renderer = renderer;
	}

	public Skeleton getSkeleton () {
		return skeleton;
	}

	public void setSkeleton (Skeleton skeleton) {
		this.skeleton = skeleton;
	}

	public AnimationState getAnimationState () {
		return state;
	}

	public void setAnimationState (AnimationState state) {
		this.state = state;
	}

	public boolean getResetBlendFunction () {
		return resetBlendFunction;
	}

	/** If false, the blend function will be left as whatever {@link SkeletonRenderer#draw(Batch, Skeleton)} set. This can reduce
	 * batch flushes in some cases, but means other rendering may need to first set the blend function. Default is true. */
	public void setResetBlendFunction (boolean resetBlendFunction) {
		this.resetBlendFunction = resetBlendFunction;
	}

    public void addSkeletonModifier(ISkeletonModifier skeletonModifier) {
        this.skeletonModifiers.add(skeletonModifier);
    }

    public void setRendererDebug(SkeletonRendererDebug rendererDebug) {
        this.rendererDebug = rendererDebug;
    }

    public void resetModifiers() {
        for (ISkeletonModifier modifier : skeletonModifiers) {
            modifier.reset();
        }
    }
}
