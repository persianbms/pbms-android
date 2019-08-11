package org.persianbms.andromeda

import android.os.Bundle
import android.view.MenuItem
import androidx.annotation.MenuRes
import androidx.annotation.UiThread
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class MainActivity : AppCompatActivity() {

    var backPressInterceptor: BackPressInterceptor? = null

    var webBundle: Bundle? = null

    fun setFragmentMenu(@MenuRes menuId: Int) {
        L.i("inflating fragment menu")
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        toolbar.menu.clear()
        toolbar.inflateMenu(menuId)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            val homeFragment = HomeFragment()
            supportFragmentManager.beginTransaction().add(R.id.fragment_container, homeFragment, "home").commit()
        }

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        toolbar.setOnMenuItemClickListener(object: Toolbar.OnMenuItemClickListener {
            override fun onMenuItemClick(item: MenuItem?): Boolean {
                if (item == null) {
                    return false
                }
                if (item.itemId == R.id.about) {
                    onShowAbout()
                    return true
                }

                return false
            }
        })
    }

    override fun onBackPressed() {
        backPressInterceptor?.let {
            if (it.shouldInterceptBackPress()) {
                return
            }
        }

        super.onBackPressed()
    }

    private fun onShowAbout() {
        val fm = supportFragmentManager
        val aboutFragment = AboutFragment.newInstance()
        val tx = fm.beginTransaction()

        tx.setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit)
        tx.replace(R.id.fragment_container, aboutFragment)
        tx.addToBackStack(null).commit()

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        toolbar.menu.clear()
    }

    interface BackPressInterceptor {
        @UiThread
        fun shouldInterceptBackPress(): Boolean
    }
}
