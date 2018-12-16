package coml.anwesh.uiprojects.linkedlinetopipestepview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import coml.anwesh.uiprojects.linetopipestepview.LineToPipeStepView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LineToPipeStepView.create(this)
        fullScreen()
    }
}

fun MainActivity.fullScreen() {
    supportActionBar?.hide()
    window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
}
