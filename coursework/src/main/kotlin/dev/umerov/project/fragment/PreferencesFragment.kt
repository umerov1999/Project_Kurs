package dev.umerov.project.fragment

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.AppCompatSeekBar
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textview.MaterialTextView
import de.maxr1998.modernpreferences.AbsPreferencesFragment
import de.maxr1998.modernpreferences.PreferenceScreen
import de.maxr1998.modernpreferences.PreferencesAdapter
import de.maxr1998.modernpreferences.PreferencesExtra
import de.maxr1998.modernpreferences.helpers.DISABLED_RESOURCE_ID
import de.maxr1998.modernpreferences.helpers.onClick
import de.maxr1998.modernpreferences.helpers.onSeek
import de.maxr1998.modernpreferences.helpers.onSelectionChange
import de.maxr1998.modernpreferences.helpers.pref
import de.maxr1998.modernpreferences.helpers.screen
import de.maxr1998.modernpreferences.helpers.seekBar
import de.maxr1998.modernpreferences.helpers.singleChoice
import de.maxr1998.modernpreferences.helpers.subScreen
import de.maxr1998.modernpreferences.helpers.switch
import dev.umerov.project.BuildConfig
import dev.umerov.project.Constants
import dev.umerov.project.Constants.forceDeveloperMode
import dev.umerov.project.Extra
import dev.umerov.project.Includes
import dev.umerov.project.Includes.provideApplicationContext
import dev.umerov.project.R
import dev.umerov.project.activity.ActivityFeatures
import dev.umerov.project.activity.ActivityUtils
import dev.umerov.project.activity.EnterPinActivity
import dev.umerov.project.activity.FileManagerSelectActivity
import dev.umerov.project.kJson
import dev.umerov.project.listener.BackPressCallback
import dev.umerov.project.listener.CanBackPressedCallback
import dev.umerov.project.listener.OnSectionResumeCallback
import dev.umerov.project.listener.UpdatableNavigation
import dev.umerov.project.model.SectionItem
import dev.umerov.project.model.SlidrSettings
import dev.umerov.project.model.db.CoinOperation
import dev.umerov.project.module.ProjectNative
import dev.umerov.project.nonNullNoEmpty
import dev.umerov.project.place.PlaceFactory
import dev.umerov.project.settings.CurrentTheme.getColorPrimary
import dev.umerov.project.settings.CurrentTheme.getColorSecondary
import dev.umerov.project.settings.Settings
import dev.umerov.project.settings.backup.SettingsBackup
import dev.umerov.project.trimmedNonNullNoEmpty
import dev.umerov.project.util.Utils
import dev.umerov.project.util.Utils.getAppVersionName
import dev.umerov.project.util.coroutines.CancelableJob
import dev.umerov.project.util.coroutines.CoroutinesUtils.delayTaskFlow
import dev.umerov.project.util.coroutines.CoroutinesUtils.syncSingle
import dev.umerov.project.util.coroutines.CoroutinesUtils.syncSingleSafe
import dev.umerov.project.util.coroutines.CoroutinesUtils.toMain
import dev.umerov.project.util.serializeble.AbsDtoAdapter.Companion.asJsonObjectSafe
import dev.umerov.project.util.serializeble.AbsDtoAdapter.Companion.asPrimitiveSafe
import dev.umerov.project.util.serializeble.AbsDtoAdapter.Companion.hasArray
import dev.umerov.project.util.serializeble.AbsDtoAdapter.Companion.hasObject
import dev.umerov.project.util.serializeble.json.Json
import dev.umerov.project.util.serializeble.json.JsonObjectBuilder
import dev.umerov.project.util.serializeble.json.intOrNull
import dev.umerov.project.util.serializeble.json.jsonObject
import dev.umerov.project.util.serializeble.json.put
import dev.umerov.project.util.serializeble.prefs.Preferences
import dev.umerov.project.util.toast.CustomSnackbars
import dev.umerov.project.util.toast.CustomToast.Companion.createCustomToast
import dev.umerov.project.view.MySearchView
import dev.umerov.project.view.natives.rlottie.RLottieImageView
import kotlinx.serialization.builtins.ListSerializer
import okio.buffer
import okio.source
import java.io.File
import java.io.FileOutputStream

class PreferencesFragment : AbsPreferencesFragment(), PreferencesAdapter.OnScreenChangeListener,
    BackPressCallback, CanBackPressedCallback {
    private var preferencesView: RecyclerView? = null
    private var layoutManager: LinearLayoutManager? = null
    private var searchView: MySearchView? = null
    private var sleepDataDisposable = CancelableJob()
    private val SEARCH_DELAY = 2000
    override val keyInstanceState: String = "root_preferences"

    @Suppress("DEPRECATION")
    private val exportSettings = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            try {
                val file = File(
                    result.data?.getStringExtra(Extra.PATH),
                    "project_settings_backup.json"
                )
                val root = JsonObjectBuilder()
                val app = JsonObjectBuilder()
                app.put("version", getAppVersionName(requireActivity()))
                app.put("settings_format", Constants.EXPORT_SETTINGS_FORMAT)
                root.put("app", app.build())
                val settings = SettingsBackup().doBackup()
                root.put("settings", settings)

                root.put(
                    "db", kJson.encodeToJsonElement(
                        ListSerializer(CoinOperation.serializer()),
                        Includes.stores.projectStore().fetchCoinOperationsAllForBackup()
                            .syncSingle()
                    )
                )
                val bytes = Json { prettyPrint = true }.printJsonElement(root.build()).toByteArray(
                    Charsets.UTF_8
                )
                val out = FileOutputStream(file)
                val bom = byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte())
                out.write(bom)
                out.write(bytes)
                out.flush()
                provideApplicationContext().sendBroadcast(
                    Intent(
                        Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                        Uri.fromFile(file)
                    )
                )
                createCustomToast(requireActivity(), view)?.showToast(
                    R.string.success,
                    file.absolutePath
                )

            } catch (e: Exception) {
                createCustomToast(requireActivity(), view)?.showToastThrowable(e)
            }
        }
    }

    private val importSettings = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            try {
                val file =
                    File(
                        result.data?.getStringExtra(Extra.PATH) ?: return@registerForActivityResult
                    )
                if (file.exists()) {
                    val objApp = kJson.parseToJsonElement(file.source().buffer()).jsonObject
                    if (objApp["app"]?.asJsonObjectSafe?.get("settings_format")?.asPrimitiveSafe?.intOrNull != Constants.EXPORT_SETTINGS_FORMAT) {
                        createCustomToast(requireActivity(), view)?.setDuration(Toast.LENGTH_LONG)
                            ?.showToastError(R.string.wrong_settings_format)
                        return@registerForActivityResult
                    }
                    if (hasObject(objApp, "settings")) {
                        SettingsBackup().doRestore(objApp["settings"]?.jsonObject)
                        createCustomToast(requireActivity(), null)?.setDuration(Toast.LENGTH_LONG)
                            ?.showToastSuccessBottom(
                                R.string.need_restart
                            )
                    }

                    if (hasArray(objApp, "db")) {
                        objApp["db"]?.let {
                            val s = kJson.decodeFromJsonElement(
                                ListSerializer(CoinOperation.serializer()),
                                it
                            )
                            for (i in s) {
                                Includes.stores.projectStore().addOperation(i).syncSingleSafe()
                            }
                        }
                    }
                }
                createCustomToast(requireActivity(), view)?.showToast(R.string.success)
            } catch (e: Exception) {
                createCustomToast(requireActivity(), view)?.showToastThrowable(e)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root =
            inflater.inflate(R.layout.preference_project_list_fragment, container, false)
        searchView = root.findViewById(R.id.searchview)
        searchView?.setRightButtonVisibility(false)
        searchView?.setLeftIcon(R.drawable.magnify)
        searchView?.setQuery("", true)
        layoutManager = LinearLayoutManager(requireActivity())
        val isNull = createPreferenceAdapter()
        preferencesView = (root.findViewById<RecyclerView>(R.id.recycler_view)).apply {
            layoutManager = this@PreferencesFragment.layoutManager
            adapter = preferencesAdapter
            layoutAnimation = AnimationUtils.loadLayoutAnimation(
                requireActivity(),
                R.anim.preference_layout_fall_down
            )
        }
        if (isNull) {
            preferencesAdapter?.onScreenChangeListener = this
            loadInstanceState({ createRootScreen() }, root)
        }

        searchView?.let {
            it.setOnBackButtonClickListener(
                object : MySearchView.OnBackButtonClickListener {
                    override fun onBackButtonClick() {
                        if (it.text.nonNullNoEmpty() && it.text?.trimmedNonNullNoEmpty() == true) {
                            preferencesAdapter?.findPreferences(
                                requireActivity(),
                                (it.text ?: return).toString(),
                                root
                            )
                        }
                    }
                }
            )
            it.setOnQueryTextListener(object : MySearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    sleepDataDisposable.cancel()
                    if (query.nonNullNoEmpty() && query.trimmedNonNullNoEmpty()) {
                        preferencesAdapter?.findPreferences(requireActivity(), query, root)
                    }
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    sleepDataDisposable.cancel()
                    sleepDataDisposable.set(delayTaskFlow(SEARCH_DELAY.toLong())
                        .toMain {
                            if (newText.nonNullNoEmpty() && newText.trimmedNonNullNoEmpty()) {
                                preferencesAdapter?.findPreferences(
                                    requireActivity(),
                                    newText,
                                    root
                                )
                            }
                        })
                    return false
                }
            })
        }
        return root
    }

    override fun onBackPressed(): Boolean {
        return !goBack()
    }

    override fun canBackPressed(): Boolean {
        return canGoBack()
    }

    override fun beforeScreenChange(screen: PreferenceScreen): Boolean {
        preferencesView?.let { preferencesAdapter?.stopObserveScrollPosition(it) }
        return true
    }

    override fun onScreenChanged(screen: PreferenceScreen, subScreen: Boolean, animation: Boolean) {
        searchView?.visibility = if (screen.getSearchQuery() == null) View.VISIBLE else View.GONE
        if (animation) {
            preferencesView?.scheduleLayoutAnimation()
        }
        preferencesView?.let { preferencesAdapter?.restoreAndObserveScrollPosition(it) }
        val actionBar = ActivityUtils.supportToolbarFor(this)
        if (actionBar != null) {
            if (screen.key == "root" || screen.title.isEmpty() && screen.titleRes == DISABLED_RESOURCE_ID) {
                actionBar.setTitle(R.string.settings)
            } else if (screen.titleRes != DISABLED_RESOURCE_ID) {
                actionBar.setTitle(screen.titleRes)
            } else {
                actionBar.title = screen.title
            }
            actionBar.subtitle = null
        }
        if (requireActivity() is UpdatableNavigation) {
            (requireActivity() as UpdatableNavigation).onUpdateNavigation()
        }
    }

    private fun createRootScreen() = screen(requireActivity()) {
        subScreen("general_preferences") {
            titleRes = R.string.general_settings
            iconRes = R.drawable.preferences_settings
            singleChoice(
                "language_ui",
                selItems(R.array.array_language_names, R.array.array_language_items),
                parentFragmentManager
            ) {
                iconRes = R.drawable.lang_settings
                initialSelection = "0"
                titleRes = R.string.language_ui
                onSelectionChange {
                    requireActivity().recreate()
                }
            }
            pref(KEY_APP_THEME) {
                iconRes = R.drawable.select_colored
                titleRes = R.string.choose_theme_title
                onClick {
                    PlaceFactory.getSettingsThemePlace().tryOpenWith(requireActivity())
                    true
                }
            }

            singleChoice(
                KEY_NIGHT_SWITCH,
                selItems(R.array.night_mode_names, R.array.night_mode_values),
                parentFragmentManager
            ) {
                initialSelection = "-1"
                titleRes = R.string.night_mode_title
                iconRes = R.drawable.night_mode_pref
                onSelectionChange {
                    AppCompatDelegate.setDefaultNightMode(it.toInt())
                }
            }

            singleChoice(
                "theme_overlay",
                selItems(R.array.theme_overlay_names, R.array.theme_overlay_values),
                parentFragmentManager
            ) {
                initialSelection = "0"
                titleRes = R.string.theme_overlay
                onSelectionChange {
                    requireActivity().recreate()
                }
            }

            seekBar("font_size_int") {
                min = -3
                max = 9
                default = 0
                step = 1
                showTickMarks = true
                titleRes = R.string.font_size
                onSeek {
                    sleepDataDisposable.cancel()
                    sleepDataDisposable.set(delayTaskFlow(1000)
                        .toMain {
                            requireActivity().recreate()
                        })
                }
            }

            singleChoice(
                "viewpager_page_transform",
                selItems(
                    R.array.array_pager_transform_names,
                    R.array.array_pager_transform_anim_items
                ),
                parentFragmentManager
            ) {
                initialSelection = "0"
                titleRes = R.string.viewpager_page_transform
            }

            pref("slidr_settings") {
                titleRes = R.string.slidr_settings
                onClick {
                    SlidrEditDialog().show(parentFragmentManager, "SlidrPrefs")
                    true
                }
            }
        }
        pref("security") {
            titleRes = R.string.security
            iconRes = R.drawable.security_settings
            onClick {
                onSecurityClick()
                true
            }
        }

        subScreen("dev_settings") {
            iconRes = R.drawable.developer_mode
            titleRes = R.string.dev_settings
            switch("developer_mode") {
                defaultValue = forceDeveloperMode
                titleRes = R.string.developer_mode
                iconRes = R.drawable.developer_mode
            }
        }

        subScreen("import_export_settings") {
            iconRes = R.drawable.preferences_settings
            titleRes = R.string.import_export_settings
            pref("export_settings") {
                titleRes = R.string.export_settings
                onClick {
                    exportSettings.launch(
                        FileManagerSelectActivity.makeFileManager(
                            requireActivity(),
                            Environment.getExternalStorageDirectory().absolutePath,
                            "dirs"
                        )
                    )
                    true
                }
            }
            pref("import_settings") {
                titleRes = R.string.import_settings
                onClick {
                    importSettings.launch(
                        FileManagerSelectActivity.makeFileManager(
                            requireActivity(),
                            Environment.getExternalStorageDirectory().absolutePath,
                            "json"
                        )
                    )
                    true
                }
            }
        }
        pref("reset_settings") {
            titleRes = R.string.reset_settings
            iconRes = R.drawable.refresh_settings
            onClick {
                CustomSnackbars.createCustomSnackbars(view)
                    ?.setDurationSnack(Snackbar.LENGTH_LONG)
                    ?.themedSnack(R.string.reset_settings)
                    ?.setAction(
                        R.string.button_yes
                    ) {
                        val preferences =
                            Preferences(PreferenceScreen.getPreferences(provideApplicationContext()))
                        SettingsBackup.AppPreferencesList().let {
                            preferences.encode(
                                SettingsBackup.AppPreferencesList.serializer(),
                                "",
                                it
                            )
                        }
                        requireActivity().finish()
                    }?.show()
                true
            }
        }
        pref("version") {
            iconRes = R.drawable.app_info_settings
            titleRes = R.string.app_name
            summary = BuildConfig.VERSION_NAME
            onClick {
                val view = View.inflate(requireActivity(), R.layout.dialog_about_us, null)
                val anim: RLottieImageView = view.findViewById(R.id.lottie_animation)
                if (ProjectNative.isNativeLoaded) {
                    anim.fromRes(
                        R.raw.project,
                        Utils.dp(170f),
                        Utils.dp(170f),
                        intArrayOf(
                            0x000000,
                            getColorPrimary(requireActivity()),
                            0xffffff,
                            getColorSecondary(requireActivity())
                        )
                    )
                    anim.playAnimation()
                }
                MaterialAlertDialogBuilder(requireActivity())
                    .setView(view)
                    .show()
                true
            }
        }
    }

    private val requestPin = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            PlaceFactory.securitySettingsPlace.tryOpenWith(requireActivity())
        }
    }

    private fun onSecurityClick() {
        if (Settings.get().security().hasPinHash) {
            requestPin.launch(Intent(requireActivity(), EnterPinActivity::class.java))
        } else {
            PlaceFactory.securitySettingsPlace.tryOpenWith(requireActivity())
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as AppCompatActivity).setSupportActionBar(view.findViewById(R.id.toolbar))
    }

    override fun onResume() {
        super.onResume()
        val actionBar = ActivityUtils.supportToolbarFor(this)
        if (actionBar != null) {
            if (preferencesAdapter?.currentScreen?.key == "root" || preferencesAdapter?.currentScreen?.title.isNullOrEmpty() && (preferencesAdapter?.currentScreen?.titleRes == DISABLED_RESOURCE_ID || preferencesAdapter?.currentScreen?.titleRes == 0)
            ) {
                actionBar.setTitle(R.string.settings)
            } else if (preferencesAdapter?.currentScreen?.titleRes != DISABLED_RESOURCE_ID && preferencesAdapter?.currentScreen?.titleRes != 0) {
                preferencesAdapter?.currentScreen?.titleRes?.let { actionBar.setTitle(it) }
            } else {
                actionBar.title = preferencesAdapter?.currentScreen?.title
            }
            actionBar.subtitle = null
        }
        if (requireActivity() is OnSectionResumeCallback) {
            (requireActivity() as OnSectionResumeCallback).onSectionResume(SectionItem.SETTINGS)
        }
        if (requireActivity() is UpdatableNavigation) {
            (requireActivity() as UpdatableNavigation).onUpdateNavigation()
        }
        searchView?.visibility =
            if (preferencesAdapter?.currentScreen?.getSearchQuery() == null) View.VISIBLE else View.GONE
        ActivityFeatures.Builder()
            .begin()
            .setBarsColored(requireActivity(), true)
            .build()
            .apply(requireActivity())
    }

    class SlidrEditDialog : DialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val view = View.inflate(requireActivity(), R.layout.entry_slidr_settings, null)

            val verticalSensitive =
                view.findViewById<AppCompatSeekBar>(R.id.edit_vertical_sensitive)
            val horizontalSensitive =
                view.findViewById<AppCompatSeekBar>(R.id.edit_horizontal_sensitive)
            val textHorizontalSensitive: MaterialTextView =
                view.findViewById(R.id.text_horizontal_sensitive)
            val textVerticalSensitive: MaterialTextView =
                view.findViewById(R.id.text_vertical_sensitive)

            val verticalVelocityThreshold =
                view.findViewById<AppCompatSeekBar>(R.id.edit_vertical_velocity_threshold)
            val horizontalVelocityThreshold =
                view.findViewById<AppCompatSeekBar>(R.id.edit_horizontal_velocity_threshold)
            val textHorizontalVelocityThreshold: MaterialTextView =
                view.findViewById(R.id.text_horizontal_velocity_threshold)
            val textVerticalVelocityThreshold: MaterialTextView =
                view.findViewById(R.id.text_vertical_velocity_threshold)

            val verticalDistanceThreshold =
                view.findViewById<AppCompatSeekBar>(R.id.edit_vertical_distance_threshold)
            val horizontalDistanceThreshold =
                view.findViewById<AppCompatSeekBar>(R.id.edit_horizontal_distance_threshold)
            val textHorizontalDistanceThreshold: MaterialTextView =
                view.findViewById(R.id.text_horizontal_distance_threshold)
            val textVerticalDistanceThreshold: MaterialTextView =
                view.findViewById(R.id.text_vertical_distance_threshold)

            verticalSensitive.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    if (fromUser && progress < 20) {
                        verticalSensitive.progress = 20
                        textVerticalSensitive.text = getString(R.string.slidr_sensitive, 20)
                    } else {
                        textVerticalSensitive.text = getString(R.string.slidr_sensitive, progress)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
            horizontalSensitive.setOnSeekBarChangeListener(object :
                SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    if (fromUser && progress < 20) {
                        horizontalSensitive.progress = 20
                        textHorizontalSensitive.text = getString(R.string.slidr_sensitive, 20)
                    } else {
                        textHorizontalSensitive.text = getString(R.string.slidr_sensitive, progress)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {}
                override fun onStopTrackingTouch(seekBar: SeekBar) {}
            })
            verticalVelocityThreshold.setOnSeekBarChangeListener(object :
                SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    if (fromUser && progress < 4) {
                        verticalVelocityThreshold.progress = 4
                        textVerticalVelocityThreshold.text =
                            getString(R.string.slidr_velocity_threshold, 4)
                    } else {
                        textVerticalVelocityThreshold.text =
                            getString(R.string.slidr_velocity_threshold, progress)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
            horizontalVelocityThreshold.setOnSeekBarChangeListener(object :
                SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    if (fromUser && progress < 4) {
                        horizontalVelocityThreshold.progress = 4
                        textHorizontalVelocityThreshold.text =
                            getString(R.string.slidr_velocity_threshold, 4)
                    } else {
                        textHorizontalVelocityThreshold.text =
                            getString(R.string.slidr_velocity_threshold, progress)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {}
                override fun onStopTrackingTouch(seekBar: SeekBar) {}
            })
            verticalDistanceThreshold.setOnSeekBarChangeListener(object :
                SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    if (fromUser && progress < 4) {
                        verticalDistanceThreshold.progress = 4
                        textVerticalDistanceThreshold.text =
                            getString(R.string.slidr_distance_threshold, 4)
                    } else {
                        textVerticalDistanceThreshold.text =
                            getString(R.string.slidr_distance_threshold, progress)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
            horizontalDistanceThreshold.setOnSeekBarChangeListener(object :
                SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    if (fromUser && progress < 4) {
                        horizontalDistanceThreshold.progress = 4
                        textHorizontalDistanceThreshold.text =
                            getString(R.string.slidr_distance_threshold, 4)
                    } else {
                        textHorizontalDistanceThreshold.text =
                            getString(R.string.slidr_distance_threshold, progress)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {}
                override fun onStopTrackingTouch(seekBar: SeekBar) {}
            })
            val settings = Settings.get()
                .main().slidrSettings
            verticalSensitive.progress = (settings.vertical_sensitive * 100).toInt()
            horizontalSensitive.progress = (settings.horizontal_sensitive * 100).toInt()

            textHorizontalSensitive.text = getString(
                R.string.slidr_sensitive,
                (settings.horizontal_sensitive * 100).toInt()
            )
            textVerticalSensitive.text =
                getString(
                    R.string.slidr_sensitive,
                    (settings.vertical_sensitive * 100).toInt()
                )

            verticalVelocityThreshold.progress =
                (settings.vertical_velocity_threshold * 10).toInt()
            horizontalVelocityThreshold.progress =
                (settings.horizontal_velocity_threshold * 10).toInt()

            textHorizontalVelocityThreshold.text = getString(
                R.string.slidr_velocity_threshold,
                (settings.horizontal_velocity_threshold * 10).toInt()
            )
            textVerticalVelocityThreshold.text = getString(
                R.string.slidr_velocity_threshold,
                (settings.vertical_velocity_threshold * 10).toInt()
            )

            verticalDistanceThreshold.progress =
                (settings.vertical_distance_threshold * 100).toInt()
            horizontalDistanceThreshold.progress =
                (settings.horizontal_distance_threshold * 100).toInt()

            textHorizontalDistanceThreshold.text = getString(
                R.string.slidr_distance_threshold,
                (settings.horizontal_distance_threshold * 100).toInt()
            )
            textVerticalDistanceThreshold.text = getString(
                R.string.slidr_distance_threshold,
                (settings.vertical_distance_threshold * 100).toInt()
            )

            return MaterialAlertDialogBuilder(requireActivity())
                .setView(view)
                .setCancelable(true)
                .setNegativeButton(R.string.button_cancel, null)
                .setNeutralButton(R.string.set_default) { _, _ ->
                    Settings.get()
                        .main().setSlidrSettings(SlidrSettings().set_default())
                    parentFragmentManager.setFragmentResult(
                        PreferencesExtra.RECREATE_ACTIVITY_REQUEST,
                        Bundle()
                    )
                    dismiss()
                }
                .setPositiveButton(R.string.button_ok) { _, _ ->
                    val st = SlidrSettings()
                    st.horizontal_sensitive = horizontalSensitive.progress.toFloat() / 100
                    st.vertical_sensitive = verticalSensitive.progress.toFloat() / 100

                    st.horizontal_velocity_threshold =
                        horizontalVelocityThreshold.progress.toFloat() / 10
                    st.vertical_velocity_threshold =
                        verticalVelocityThreshold.progress.toFloat() / 10

                    st.horizontal_distance_threshold =
                        horizontalDistanceThreshold.progress.toFloat() / 100
                    st.vertical_distance_threshold =
                        verticalDistanceThreshold.progress.toFloat() / 100
                    Settings.get()
                        .main().setSlidrSettings(st)
                    parentFragmentManager.setFragmentResult(
                        PreferencesExtra.RECREATE_ACTIVITY_REQUEST,
                        Bundle()
                    )
                    dismiss()
                }.create()
        }
    }

    override fun onDestroy() {
        sleepDataDisposable.cancel()
        preferencesView?.let { preferencesAdapter?.stopObserveScrollPosition(it) }
        preferencesAdapter?.onScreenChangeListener = null
        preferencesView?.adapter = null
        super.onDestroy()
    }

    companion object {
        private const val KEY_APP_THEME = "app_theme"
        private const val KEY_NIGHT_SWITCH = "night_switch"
    }
}
