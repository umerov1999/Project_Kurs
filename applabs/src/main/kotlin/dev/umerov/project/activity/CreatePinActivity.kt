package dev.umerov.project.activity

import android.os.Bundle
import dev.umerov.project.R
import dev.umerov.project.fragment.pin.createpin.CreatePinFragment

class CreatePinActivity : NoMainActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragment, CreatePinFragment.newInstance())
                .commit()
        }
    }
}