package org.persianbms.andromeda

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.annotation.UiThread

class MainActivity : AppCompatActivity() {

    var backPressInterceptor: BackPressInterceptor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onBackPressed() {
        backPressInterceptor?.let {
            if (it.shouldInterceptBackPress()) {
                return
            }
        }

        super.onBackPressed()
    }

    interface BackPressInterceptor {
        @UiThread
        fun shouldInterceptBackPress(): Boolean
    }
}
