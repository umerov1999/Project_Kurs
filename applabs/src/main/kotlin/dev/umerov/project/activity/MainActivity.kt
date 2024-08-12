package dev.umerov.project.activity

import android.Manifest
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
import dev.umerov.project.activity.lab13.Lab13Activity
import dev.umerov.project.activity.qr.CameraScanActivity
import dev.umerov.project.fragment.PreferencesFragment
import dev.umerov.project.fragment.SecurityPreferencesFragment
import dev.umerov.project.fragment.filemanager.FileManagerFragment
import dev.umerov.project.fragment.main.MainFragment
import dev.umerov.project.fragment.main.finance.FinanceOperationsFragment
import dev.umerov.project.fragment.main.finance.FinanceTabsFragment
import dev.umerov.project.fragment.main.lab1.Lab1Fragment
import dev.umerov.project.fragment.main.lab10.Lab10Fragment
import dev.umerov.project.fragment.main.lab11.Lab11TabsFragment
import dev.umerov.project.fragment.main.lab12.Lab12Fragment
import dev.umerov.project.fragment.main.lab14.Lab14Fragment
import dev.umerov.project.fragment.main.lab15.Lab15Fragment
import dev.umerov.project.fragment.main.lab16.Lab16Fragment
import dev.umerov.project.fragment.main.lab17.Lab17Fragment
import dev.umerov.project.fragment.main.lab19.Lab19Fragment
import dev.umerov.project.fragment.main.lab2.Lab2Fragment
import dev.umerov.project.fragment.main.lab3.Lab3Fragment
import dev.umerov.project.fragment.main.lab4.Lab4Fragment
import dev.umerov.project.fragment.main.lab5.Lab5Fragment
import dev.umerov.project.fragment.main.lab6.Lab6Fragment
import dev.umerov.project.fragment.main.lab7.Lab7Fragment
import dev.umerov.project.fragment.main.lab8.Lab8Fragment
import dev.umerov.project.fragment.main.lab9.Lab9Fragment
import dev.umerov.project.fragment.main.shoppinglist.ShoppingListFragment
import dev.umerov.project.fragment.main.shoppingproducts.ShoppingProductsFragment
import dev.umerov.project.fragment.main.staff.StaffFragment
import dev.umerov.project.fragment.theme.ThemeFragment
import dev.umerov.project.getParcelableExtraCompat
import dev.umerov.project.listener.AppStyleable
import dev.umerov.project.listener.BackPressCallback
import dev.umerov.project.listener.CanBackPressedCallback
import dev.umerov.project.listener.OnSectionResumeCallback
import dev.umerov.project.listener.UpdatableNavigation
import dev.umerov.project.modalbottomsheetdialogfragment.ModalBottomSheetDialogFragment
import dev.umerov.project.modalbottomsheetdialogfragment.OptionRequest
import dev.umerov.project.model.SectionItem
import dev.umerov.project.module.ProjectNative
import dev.umerov.project.nonNullNoEmpty
import dev.umerov.project.place.Place
import dev.umerov.project.place.PlaceFactory.getMainPlace
import dev.umerov.project.place.PlaceFactory.getPreferencesPlace
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
import dev.umerov.project.util.Logger
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
            handleIntent(intent)
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
            handleIntent(intent)
        }
    }

    private fun startEnterPinActivity() {
        requestEnterPin.launch(EnterPinActivity.getIntent(this))
    }

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
                handleIntent(intent)
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
        Logger.d(TAG, "onNewIntent, intent: $intent")
        handleIntent(intent)
    }

    private val requestQRScan = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
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
            SectionItem.MAIN -> {
                mBottomNavigation?.menu?.findItem(R.id.menu_main)?.isChecked = true
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

    private fun handleIntent(intent: Intent?): Boolean {
        if (intent == null) {
            return false
        }
        Logger.d(TAG, "handleIntent, extras: ${intent.extras}, action: ${intent.action}")

        when {
            ACTION_OPEN_PLACE == intent.action -> {
                val place: Place = intent.getParcelableExtraCompat(Extra.PLACE) ?: return false
                openPlace(place)
                return true
            }
        }
        if (Utils.hasTiramisu() && needHelp(
                NOTIFICATION_PERMISSION,
                1
            ) && !AppPerms.hasNotificationPermissionSimple(this)
        ) {
            requestNPermission.launch()
        }
        if (!AppPerms.hasReadWriteStoragePermission(this)) {
            requestReadWritePermission.launch()
            return true
        }
        openNavigationPage(
            SectionItem.MAIN,
            clearBackStack = false
        )
        return true
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
        val args: Bundle = place.prepareArguments()
        when (place.type) {
            Place.MAIN -> {
                attachToFront(MainFragment.newInstance())
            }

            Place.PREFERENCES -> {
                attachToFront(PreferencesFragment())
            }

            Place.SETTINGS_THEME -> {
                attachToFront(ThemeFragment())
            }

            Place.SECURITY -> attachToFront(SecurityPreferencesFragment())

            Place.FILE_MANAGER -> {
                attachToFront(FileManagerFragment.newInstance(args))
            }

            Place.STAFF -> {
                attachToFront(StaffFragment.newInstance())
            }

            Place.LAB_1 -> {
                attachToFront(Lab1Fragment.newInstance())
            }

            Place.LAB_2 -> {
                attachToFront(Lab2Fragment.newInstance())
            }

            Place.LAB_3 -> {
                attachToFront(Lab3Fragment.newInstance())
            }

            Place.LAB_4 -> {
                attachToFront(Lab4Fragment.newInstance(false))
            }

            Place.LAB_4_1 -> {
                attachToFront(Lab4Fragment.newInstance(true))
            }

            Place.LAB_5 -> {
                attachToFront(Lab5Fragment.newInstance())
            }

            Place.LAB_6 -> {
                attachToFront(Lab6Fragment.newInstance())
            }

            Place.LAB_7 -> {
                attachToFront(Lab7Fragment.newInstance())
            }

            Place.LAB_8 -> {
                attachToFront(Lab8Fragment.newInstance())
            }

            Place.LAB_9 -> {
                attachToFront(Lab9Fragment.newInstance())
            }

            Place.LAB_10 -> {
                attachToFront(Lab10Fragment.newInstance())
            }

            Place.LAB_11 -> {
                attachToFront(Lab11TabsFragment.newInstance())
            }

            Place.LAB_12 -> {
                attachToFront(Lab12Fragment.newInstance())
            }

            Place.LAB_13 -> {
                startActivity(Lab13Activity.newInstance(this))
            }

            Place.LAB_14 -> {
                attachToFront(Lab14Fragment.newInstance())
            }

            Place.LAB_15 -> {
                attachToFront(Lab15Fragment.newInstance())
            }

            Place.LAB_16 -> {
                attachToFront(Lab16Fragment.newInstance())
            }

            Place.LAB_17 -> {
                attachToFront(Lab17Fragment.newInstance(false))
            }

            Place.LAB_18 -> {
                attachToFront(Lab17Fragment.newInstance(true))
            }

            Place.LAB_19 -> {
                attachToFront(Lab19Fragment.newInstance())
            }

            Place.SNAKE -> {
                startActivity(SnakeActivity.newInstance(this))
            }

            Place.SHOPPING_LIST -> {
                attachToFront(ShoppingListFragment.newInstance())
            }

            Place.SHOPPING_PRODUCTS -> {
                attachToFront(ShoppingProductsFragment.newInstance(args))
            }

            Place.FINANCE -> {
                attachToFront(FinanceTabsFragment.newInstance())
            }

            Place.FINANCE_OPERATIONS -> {
                attachToFront(FinanceOperationsFragment.newInstance(args))
            }
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
            SectionItem.MAIN -> {
                openPlace(getMainPlace())
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
            R.id.menu_main -> {
                openNavigationPage(
                    SectionItem.MAIN,
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

    companion object {
        const val ACTION_OPEN_PLACE = "dev.umerov.project.activity.MainActivity.openPlace"
    }
}
