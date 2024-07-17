package dev.umerov.project.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.iterator
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationBarView
import dev.umerov.project.Extra
import dev.umerov.project.R
import dev.umerov.project.activity.qr.CameraScanActivity
import dev.umerov.project.fragment.PreferencesFragment
import dev.umerov.project.fragment.SecurityPreferencesFragment
import dev.umerov.project.fragment.main.balance.BalanceFragment
import dev.umerov.project.fragment.main.coin.CoinFragment
import dev.umerov.project.fragment.theme.ThemeFragment
import dev.umerov.project.listener.AppStyleable
import dev.umerov.project.listener.BackPressCallback
import dev.umerov.project.listener.CanBackPressedCallback
import dev.umerov.project.listener.OnSectionResumeCallback
import dev.umerov.project.listener.UpdatableNavigation
import dev.umerov.project.modalbottomsheetdialogfragment.ModalBottomSheetDialogFragment
import dev.umerov.project.modalbottomsheetdialogfragment.OptionRequest
import dev.umerov.project.model.SectionItem
import dev.umerov.project.model.db.CoinOperationType
import dev.umerov.project.module.ProjectNative
import dev.umerov.project.nonNullNoEmpty
import dev.umerov.project.place.Place
import dev.umerov.project.place.PlaceFactory.getBalancePlace
import dev.umerov.project.place.PlaceFactory.getPastePlace
import dev.umerov.project.place.PlaceFactory.getPreferencesPlace
import dev.umerov.project.place.PlaceFactory.getTakePlace
import dev.umerov.project.place.PlaceProvider
import dev.umerov.project.settings.CurrentTheme.getNavigationBarColor
import dev.umerov.project.settings.CurrentTheme.getStatusBarColor
import dev.umerov.project.settings.CurrentTheme.getStatusBarNonColored
import dev.umerov.project.settings.Settings
import dev.umerov.project.settings.theme.ThemesController.currentStyle
import dev.umerov.project.settings.theme.ThemesController.nextRandom
import dev.umerov.project.util.AppPerms
import dev.umerov.project.util.AppPerms.requestPermissionsAbs
import dev.umerov.project.util.AppPerms.requestPermissionsResultAbs
import dev.umerov.project.util.HelperSimple.NOTIFICATION_PERMISSION
import dev.umerov.project.util.HelperSimple.needHelp
import dev.umerov.project.util.Utils
import dev.umerov.project.util.Utils.hasVanillaIceCream
import dev.umerov.project.util.ViewUtils.keyboardHide
import dev.umerov.project.util.coroutines.CompositeJob
import dev.umerov.project.util.toast.CustomToast.Companion.createCustomToast

class MainActivity : AppCompatActivity(), OnSectionResumeCallback, AppStyleable, PlaceProvider,
    NavigationBarView.OnItemSelectedListener, UpdatableNavigation {
    private var mBottomNavigation: BottomNavigationView? = null

    @SectionItem
    private var mCurrentFrontSection: Int = SectionItem.NULL
    private var mToolbar: Toolbar? = null
    private var mViewFragment: FragmentContainerView? = null
    private var mLastBackPressedTime: Long = 0
    private val DOUBLE_BACK_PRESSED_TIMEOUT = 2000
    private val TAG = "MainActivity_LOG"
    private val mCompositeDisposable = CompositeJob()
    private val requestReadWritePermission = requestPermissionsResultAbs(
        arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE
        ), {
            handleIntent(true)
        }, {
            finish()
        })

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val requestNPermission = requestPermissionsAbs(
        arrayOf(
            Manifest.permission.POST_NOTIFICATIONS
        )
    ) {
        createCustomToast(this, mViewFragment)?.showToast(R.string.success)
    }

    private val mOnBackStackChangedListener =
        FragmentManager.OnBackStackChangedListener {
            resolveToolbarNavigationIcon()
            keyboardHide(this)
        }

    private val requestEnterPin = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode != RESULT_OK) {
            finish()
        } else {
            handleIntent(true)
        }
    }

    private fun startEnterPinActivity() {
        requestEnterPin.launch(EnterPinActivity.getIntent(this))
    }

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        delegate.applyDayNight()
        savedInstanceState ?: nextRandom()
        setTheme(currentStyle())
        Utils.prepareDensity(this)
        super.onCreate(savedInstanceState)

        savedInstanceState ?: run {
            if (Settings.get().security().isUsePinForEntrance && Settings.get().security()
                    .hasPinHash
            ) {
                startEnterPinActivity()
            } else {
                handleIntent(true)
            }
        }
        setContentView(noMainContentView)
        mBottomNavigation = findViewById(R.id.bottom_navigation_menu)
        mBottomNavigation?.setOnItemSelectedListener(this)
        mViewFragment = findViewById(R.id.fragment)

        supportFragmentManager.addOnBackStackChangedListener(mOnBackStackChangedListener)
        resolveToolbarNavigationIcon()
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val front: Fragment? = frontFragment
                if (front is BackPressCallback) {
                    if (!(front as BackPressCallback).onBackPressed()) {
                        return
                    }
                }
                if (supportFragmentManager.backStackEntryCount == 1 || supportFragmentManager.backStackEntryCount <= 0) {
                    if (mLastBackPressedTime < 0
                        || mLastBackPressedTime + DOUBLE_BACK_PRESSED_TIMEOUT > System.currentTimeMillis()
                    ) {
                        supportFinishAfterTransition()
                        return
                    }
                    mLastBackPressedTime = System.currentTimeMillis()
                    mViewFragment?.let {
                        createCustomToast(it.context, mViewFragment, mBottomNavigation)
                            ?.setDuration(Toast.LENGTH_SHORT)
                            ?.showToast(R.string.click_back_to_exit)
                    }
                } else {
                    supportFragmentManager.popBackStack()
                }
            }
        })
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(Utils.updateActivityContext(newBase))
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(false)
    }

    private val requestQRScan = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val scanner = result.data?.extras?.getString(Extra.URL)
            if (scanner.nonNullNoEmpty()) {
                MaterialAlertDialogBuilder(this)
                    .setIcon(R.drawable.qr_code)
                    .setMessage(scanner)
                    .setTitle(getString(R.string.scan_qr))
                    .setNeutralButton(R.string.button_copy) { _, _ ->
                        val clipboard = getSystemService(
                            CLIPBOARD_SERVICE
                        ) as ClipboardManager?
                        val clip = ClipData.newPlainText("response", scanner)
                        clipboard?.setPrimaryClip(clip)
                        createCustomToast(this, null)?.showToast(R.string.copied_to_clipboard)
                    }
                    .setCancelable(true)
                    .create().show()
            }
        }
    }

    private fun resolveToolbarNavigationIcon() {
        mToolbar ?: return
        val manager: FragmentManager = supportFragmentManager
        if (manager.backStackEntryCount > 1 || frontFragment is CanBackPressedCallback && (frontFragment as CanBackPressedCallback).canBackPressed()) {
            mToolbar?.setNavigationIcon(R.drawable.arrow_left)
            mToolbar?.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
        } else {
            mToolbar?.setNavigationIcon(R.drawable.client_round)
            mToolbar?.setNavigationOnClickListener {
                val menus = ModalBottomSheetDialogFragment.Builder()
                if (Settings.get()
                        .main().nightMode == AppCompatDelegate.MODE_NIGHT_YES || Settings.get()
                        .main().nightMode == AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY || Settings.get()
                        .main().nightMode == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                ) {
                    menus.add(
                        OptionRequest(
                            0,
                            getString(R.string.day_mode_title),
                            R.drawable.ic_outline_wb_sunny,
                            false
                        )
                    )
                } else {
                    menus.add(
                        OptionRequest(
                            0,
                            getString(R.string.night_mode_title),
                            R.drawable.ic_outline_nights_stay,
                            false
                        )
                    )
                }
                menus.add(
                    OptionRequest(
                        1,
                        getString(R.string.scan_qr),
                        R.drawable.qr_code,
                        false
                    )
                )
                menus.show(
                    supportFragmentManager,
                    "left_options"
                ) { _, option ->
                    when {
                        option.id == 0 -> {
                            if (Settings.get().main()
                                    .nightMode == AppCompatDelegate.MODE_NIGHT_YES || Settings.get()
                                    .main()
                                    .nightMode == AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY || Settings.get()
                                    .main()
                                    .nightMode == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                            ) {
                                Settings.get().main()
                                    .switchNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                            } else {
                                Settings.get().main()
                                    .switchNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                            }
                        }

                        option.id == 1 && ProjectNative.isNativeLoaded -> {
                            val intent =
                                Intent(this@MainActivity, CameraScanActivity::class.java)
                            requestQRScan.launch(intent)
                        }
                    }
                }
            }
        }
    }

    @get:LayoutRes
    private val noMainContentView: Int
        get() = R.layout.activity_main
    private val frontFragment: Fragment?
        get() = supportFragmentManager.findFragmentById(R.id.fragment)

    override fun onSectionResume(@SectionItem section: Int) {
        mCurrentFrontSection = section
        mBottomNavigation?.menu ?: return
        for (i in (mBottomNavigation?.menu ?: return).iterator()) {
            i.isChecked = false
        }

        when (section) {
            SectionItem.BALANCE -> {
                mBottomNavigation?.menu?.findItem(R.id.balance)?.isChecked = true
            }

            SectionItem.PASTE -> {
                mBottomNavigation?.menu?.findItem(R.id.paste)?.isChecked = true
            }

            SectionItem.TAKE -> {
                mBottomNavigation?.menu?.findItem(R.id.take)?.isChecked = true
            }

            SectionItem.NULL -> {

            }

            SectionItem.SETTINGS -> {
                mBottomNavigation?.menu?.findItem(R.id.menu_settings)?.isChecked = true
            }
        }
    }

    @Suppress("DEPRECATION")
    override fun setStatusbarColored(colored: Boolean, invertIcons: Boolean) {
        val w = window
        if (!hasVanillaIceCream()) {
            w.statusBarColor =
                if (colored) getStatusBarColor(this) else getStatusBarNonColored(
                    this
                )
            w.navigationBarColor =
                if (colored) getNavigationBarColor(this) else Color.BLACK
        }
        val ins = WindowInsetsControllerCompat(w, w.decorView)
        ins.isAppearanceLightStatusBars = invertIcons
        ins.isAppearanceLightNavigationBars = invertIcons

        if (!Utils.hasMarshmallow()) {
            w.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        }
    }

    @get:IdRes
    private val mainContainerViewId: Int
        get() = R.id.fragment

    private fun handleIntent(main: Boolean) {
        if (main) {
            if (Utils.hasTiramisu() && needHelp(
                    NOTIFICATION_PERMISSION,
                    1
                ) && !AppPerms.hasNotificationPermissionSimple(this)
            ) {
                requestNPermission.launch()
            }
            if (!AppPerms.hasReadWriteStoragePermission(this)) {
                requestReadWritePermission.launch()
                return
            }
            openNavigationPage(
                SectionItem.BALANCE,
                clearBackStack = false
            )
        }
    }

    private fun attachToFront(fragment: Fragment, animate: Boolean = true) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        if (animate) fragmentTransaction.setCustomAnimations(
            R.anim.fragment_enter,
            R.anim.fragment_exit
        )
        fragmentTransaction
            .replace(mainContainerViewId, fragment)
            .addToBackStack(null)
            .commitAllowingStateLoss()
    }

    override fun openPlace(place: Place) {
        //val args: Bundle = place.prepareArguments()
        when (place.type) {
            Place.BALANCE -> {
                attachToFront(BalanceFragment.newInstance())
            }

            Place.PASTE -> {
                attachToFront(CoinFragment.newInstance(CoinOperationType.PASTE))
            }

            Place.TAKE -> {
                attachToFront(CoinFragment.newInstance(CoinOperationType.TAKE))
            }

            Place.PREFERENCES -> {
                attachToFront(PreferencesFragment())
            }

            Place.SETTINGS_THEME -> {
                attachToFront(ThemeFragment())
            }

            Place.SECURITY -> attachToFront(SecurityPreferencesFragment())
        }
    }

    override fun setSupportActionBar(toolbar: Toolbar?) {
        mToolbar?.setNavigationOnClickListener(null)
        mToolbar?.setOnMenuItemClickListener(null)
        super.setSupportActionBar(toolbar)
        mToolbar = toolbar
        resolveToolbarNavigationIcon()
    }

    override fun onUpdateNavigation() {
        resolveToolbarNavigationIcon()
    }

    override fun onDestroy() {
        mCompositeDisposable.cancel()
        supportFragmentManager.removeOnBackStackChangedListener(mOnBackStackChangedListener)

        //if (!isChangingConfigurations) {
        //unbindFromAudioPlayService()
        //}
        super.onDestroy()
    }

    private fun clearBackStack() {
        supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }

    private fun openNavigationPage(
        @SectionItem item: Int,
        clearBackStack: Boolean
    ) {
        if (item == mCurrentFrontSection) {
            return
        }
        if (clearBackStack) {
            clearBackStack()
        }
        mCurrentFrontSection = item
        when (item) {
            SectionItem.BALANCE -> {
                openPlace(getBalancePlace())
            }

            SectionItem.PASTE -> {
                openPlace(getPastePlace())
            }

            SectionItem.TAKE -> {
                openPlace(getTakePlace())
            }

            SectionItem.NULL -> {
                throw UnsupportedOperationException()
            }

            SectionItem.SETTINGS -> {
                openPlace(getPreferencesPlace())
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.balance -> {
                openNavigationPage(
                    SectionItem.BALANCE,
                    clearBackStack = true
                )
                true
            }

            R.id.paste -> {
                openNavigationPage(
                    SectionItem.PASTE,
                    clearBackStack = true
                )
                true
            }

            R.id.take -> {
                openNavigationPage(
                    SectionItem.TAKE,
                    clearBackStack = true
                )
                true
            }

            R.id.menu_settings -> {
                openNavigationPage(
                    SectionItem.SETTINGS,
                    clearBackStack = false
                )
                true
            }

            else -> false
        }
    }
}
