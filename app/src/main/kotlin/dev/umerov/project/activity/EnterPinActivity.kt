package dev.umerov.project.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import dev.umerov.project.R
import dev.umerov.project.fragment.pin.enterpin.EnterPinFragment
import dev.umerov.project.util.Utils

open class EnterPinActivity : NoMainActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragment, EnterPinFragment.newInstance())
                .commit()
        }
    }

    companion object {
        fun getIntent(context: Context): Intent {
            return Intent(
                context,
                if (Utils.is600dp(context)) EnterPinActivity::class.java else EnterPinActivityPortraitOnly::class.java
            )
        }
    }
}