package com.metaphoresodplayground

import com.kotcrab.vis.ui.VisUI
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ktx.async.KtxAsync
import ktx.scene2d.Scene2DSkin

class App : KtxGame<KtxScreen>() {
    override fun create() {
        KtxAsync.initiate()

        VisUI.load(VisUI.SkinScale.X2)
        Scene2DSkin.defaultSkin = VisUI.getSkin()

        addScreen(SodScreen())
        setScreen<SodScreen>()
    }

    override fun dispose() {
        super.dispose()
        VisUI.dispose()
    }
}

