package dev.umerov.project.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import dev.umerov.project.Extra
import dev.umerov.project.R
import dev.umerov.project.activity.slidr.Slidr
import dev.umerov.project.activity.slidr.model.SlidrConfig
import dev.umerov.project.fragment.filemanagerselect.FileManagerSelectFragment
import dev.umerov.project.settings.CurrentTheme

class FileManagerSelectActivity : NoMainActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Slidr.attach(
            this,
            SlidrConfig.Builder().scrimColor(CurrentTheme.getColorBackground(this)).build()
        )
        if (savedInstanceState == null) {
            attachFragment()
        }
    }

    private fun attachFragment() {
        val args = Bundle()
        args.putString(Extra.PATH, intent.extras?.getString(Extra.PATH))
        args.putString(Extra.EXT, intent.extras?.getString(Extra.EXT))
        if (intent.extras?.containsKey(Extra.TITLE) == true) {
            args.putString(Extra.TITLE, intent.extras?.getString(Extra.TITLE))
        }
        val fileManagerFragment = FileManagerSelectFragment()
        fileManagerFragment.arguments = args
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment, fileManagerFragment)
            .commit()
    }

    companion object {
        fun makeFileManager(context: Context, path: String, ext: String?): Intent {
            val intent = Intent(context, FileManagerSelectActivity::class.java)
            intent.putExtra(Extra.PATH, path)
            intent.putExtra(Extra.EXT, ext)
            return intent
        }

        fun makeFileManager(context: Context, path: String, ext: String?, header: String?): Intent {
            val intent = Intent(context, FileManagerSelectActivity::class.java)
            intent.putExtra(Extra.PATH, path)
            intent.putExtra(Extra.EXT, ext)
            header?.let {
                intent.putExtra(Extra.TITLE, it)
            }
            return intent
        }
    }
}
