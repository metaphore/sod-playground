package com.metaphoresodplayground

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Buttons
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.*
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.crashinvaders.spine.SodPositionMod
import com.crashinvaders.spine.SodRotateToParentMod
import com.esotericsoftware.spine.*
import com.esotericsoftware.spine.utils.SkeletonActor
import com.kotcrab.vis.ui.VisUI
import com.kotcrab.vis.ui.util.FloatDigitsOnlyFilter
import com.kotcrab.vis.ui.widget.VisLabel
import ktx.actors.onChange
import ktx.actors.onClick
import ktx.app.KtxScreen
import ktx.app.clearScreen
import ktx.assets.DisposableContainer
import ktx.assets.DisposableRegistry
import ktx.collections.map
import ktx.scene2d.container
import ktx.scene2d.horizontalGroup
import ktx.scene2d.scene2d
import ktx.scene2d.vis.*
import java.lang.NumberFormatException

@Suppress("DELEGATED_MEMBER_HIDES_SUPERTYPE_OVERRIDE")
class SodScreen : KtxScreen, DisposableRegistry by DisposableContainer() {

    companion object {
        val tmpVec = Vector2();
    }

    private val shapes = ShapeRenderer().alsoRegister()
    private val batch = PolygonSpriteBatch().alsoRegister()
    private val stage = Stage(ExtendViewport(1920f, 1080f), batch).alsoRegister()

    private val contentRoot : Group
    private val skelActor : SkeletonActor

    private val sodModPosition : SodPositionMod
    private val sodModRotation : SodRotateToParentMod

    init {
        contentRoot = Group()
        stage.addActor(contentRoot)

        val skelAtlas = TextureAtlas(Gdx.files.internal("skeletons/chibi-stickers-pro.atlas")).alsoRegister()
        val skelData = SkeletonJson(skelAtlas).let {
            it.scale = 0.5f
            it.readSkeletonData(Gdx.files.internal("skeletons/chibi-stickers.json"))
        }
        skelActor = SkeletonActor(
                SkeletonRenderer(),
                Skeleton(skelData),
                AnimationState(AnimationStateData(skelData)))
            .apply {
                x = 600f
                y = 300f
                skeleton.setSkin("erikari")
                animationState.setAnimation(0, "movement/idle-front", true)
//                animationState.setAnimation(10, "test-lock-bone", true)
//                animationState.setAnimation(0, "test", true)

                setRendererDebug(SkeletonRendererDebug(shapes).apply {
                    setMeshHull(false)
                    setMeshTriangles(false)
                    setBoundingBoxes(false)
                    val scale = 2.0f
                    setWidth(2f * scale)
                    setScale(1f * scale)
                })
//                debug = true
            }

        contentRoot.addActor(skelActor)

        skelActor.skeleton.setPosition(skelActor.x, skelActor.y)
        skelActor.skeleton.updateWorldTransform()

        sodModPosition = SodPositionMod(skelActor.skeleton, "head-base").also {
            it.setSodParams(4f, 0.6f, 1.5f)
            skelActor.addSkeletonModifier(it)
        }
//        sodModRotation = SodFreeRotationMod(skelActor.skeleton, "sod-tip").also {
//            it.setSodParams(2f, 1f, 4f)
//            skelActor.addSkeletonModifier(it)
//        }
        sodModRotation = SodRotateToParentMod(skelActor.skeleton, "head-base").also {
            it.setSodParams(2f, 1f, 4f)
            skelActor.addSkeletonModifier(it)
        }

    }

    // Setup input.
    init {
        stage.root.addListener(object : InputListener() {
            var lastX = 0f
            var lastY = 0f
            var pressedButton = -1;

            override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                if (event.isHandled)
                    return false

                if (pressedButton >= 0)
                    return false

                when (button) {
                    Buttons.LEFT -> {
                        pressedButton = button
                        moveTarget(x, y)
                        lastX = x
                        lastY = y
                        return true;
                    }
                    Buttons.RIGHT -> {
                        contentRoot.isVisible
                        pressedButton = button
                        lastX = x
                        lastY = y
                        return true;
                    }
                    else -> return false;
                }
            }

            override fun touchDragged(event: InputEvent, x: Float, y: Float, pointer: Int) {
                when (pressedButton) {
                    Buttons.LEFT -> {
                        moveTarget(x, y)
                        lastX = x
                        lastY = y
                    }
                    Buttons.RIGHT -> {
                        val xDelta = x - lastX
                        val angleDif = xDelta * 0.1f;
                        skelActor.rotation += angleDif
                        lastX = x
                        lastY = y
                    }
                }
            }

            override fun touchUp(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int) {
                pressedButton = -1;
            }

            private fun moveTarget(x: Float, y: Float) {
                skelActor.setPosition(x, y, Align.center)
            }

            private fun screenToLocal(actor: Actor, screenX: Float, screenY: Float): Vector2 {
                return actor.screenToLocalCoordinates(Companion.tmpVec.set(screenX, screenY))
            }
        })
    }

    override fun show() {
        super.show()

        Gdx.input.inputProcessor = stage

        createUi();
    }

    override fun hide() {
        super.hide()

        Gdx.input.inputProcessor = null
    }

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
        shapes.projectionMatrix = stage.viewport.camera.combined
    }

    override fun render(delta: Float) {
        val deltaTime = Gdx.graphics.deltaTime

        clearScreen(0f, 0f, 0f, 1f)

//        if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
//            val screenX = Gdx.input.getX(0)
//            val screenY = Gdx.input.getY(0)
//            stage.screenToStageCoordinates(tmpVec.set(screenX.toFloat(), screenY.toFloat()))
//            val stageX = tmpVec.x
//            val stageY = tmpVec.y
//
//            skelActor.setPosition(stageX, stageY, Align.center)
//        }

        stage.act(deltaTime)
        stage.draw()
    }

    private fun createUi() {
        val skin = VisUI.getSkin()
        val colorBlue = skin.getColor("vis-blue")

        val root = scene2d.container {
            setFillParent(true)
            fill()
            align(Align.right)
            width(400f)

            visTable {
                background(skin.getRegion("white").let {
                    val drawable = TextureRegionDrawable(it)
                    drawable.tint(Color(0x101010ff))
                })
                defaults().align(Align.left).growX().padTop(4f)
                touchable = Touchable.enabled
                onClick {  }
                align(Align.topLeft)
                pad(24f)

                visTable {
                    visLabel("Rotation SOD") { cell -> cell.growX() }
                    visTextButton("Reset").onChange { sodModRotation.reset() }
                }
                row()
                separator { cell -> cell.pad(8f, 0f, 8f ,0f) }
                row().padTop(12f)
                // Mix value.
                val lblMixValue : VisLabel
                horizontalGroup {
                    space(8f)
                    visLabel("Mix", "small")
                    lblMixValue = visLabel("", "small") { color = colorBlue }
                }
                row()
                visSlider(0f, 1f, 0.1f) {
                    value = 1f
                    onChange {
                        sodModRotation.setMix(this.value)
                        lblMixValue.setText("%.1f".format(this.value))
                    }
                    lblMixValue.setText("%.1f".format(this.value))
                }
                row().padTop(12f)
                // F Z R params
                visLabel("Config", "small")
                row().padTop(8f)
                visTable {
                    defaults().padRight(8f)
                    visLabel("F", "small")
                    val edtF = visTextField("", "small") { cell ->
                        cell.minWidth(0f).uniformX().growX()
                        textFieldFilter = FloatDigitsOnlyFilter(true)
                        text = sodModRotation.f.toString()
                    }
                    visLabel("Z", "small")
                    val edtZ = visTextField("", "small") { cell ->
                        cell.minWidth(0f).uniformX().growX()
                        textFieldFilter = FloatDigitsOnlyFilter(true)
                        text = sodModRotation.z.toString()
                    }
                    visLabel("R", "small")
                    val edtR = visTextField("", "small") { cell ->
                        cell.minWidth(0f).uniformX().growX().padRight(0f)
                        textFieldFilter = FloatDigitsOnlyFilter(true)
                        text = sodModRotation.r.toString()
                    }
                    fun updateFromView() {
                        val f: Float
                        val z: Float
                        val r: Float
                        try {
                            f = edtF.text.toFloat()
                            z = edtZ.text.toFloat()
                            r = edtR.text.toFloat()
                        } catch (e: NumberFormatException) {
                            return
                        }
                        if (f > 0f) {
                            sodModRotation.setSodParams(f, z, r)
                        }
                    }
                    edtF.onChange { updateFromView() }
                    edtZ.onChange { updateFromView() }
                    edtR.onChange { updateFromView() }
                }

                row().padTop(24f)
                visLabel("Skeleton") { cell -> cell.growX() }
                row()
                separator { cell -> cell.pad(8f, 0f, 8f ,0f) }
                row().padTop(12f)
                visLabel("Skin", "small")
                row().padTop(4f)
                visSelectBoxOf(skelActor.skeleton.data.skins.map { skin -> skin.name }.also {
                    it.insert(0, "<NONE>")
                }).apply {
                    selected = skelActor.skeleton.skin.name
                    onChange {
                        val skinName = selected
                        if (skinName == null || "<NONE>".equals(skinName)) {
                            skelActor.skeleton.skin = null
                        } else {
                            skelActor.skeleton.setSkin(skinName)
                        }
                        skelActor.skeleton.setSlotsToSetupPose()
                    }
                }
                row().padTop(24f)
                visLabel("Animation", "small")
                row().padTop(4f)
                visSelectBoxOf(skelActor.skeleton.data.animations.map { anim -> anim.name }.also {
                    it.insert(0, "<NONE>")
                }).apply {
                    onChange {
                        val animName = selected
                        if (animName == null || "<NONE>".equals(animName)) {
                            skelActor.animationState.clearTrack(0)
                        } else {
                            skelActor.animationState.setAnimation(0, animName, true)
                        }
                    }
                }
            }
        }
        stage.addActor(root)
    }
}
