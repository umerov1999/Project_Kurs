package dev.umerov.project.fragment.main.lab6

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import dev.umerov.project.R
import dev.umerov.project.activity.ActivityFeatures
import dev.umerov.project.activity.ActivityUtils.supportToolbarFor
import dev.umerov.project.fragment.base.BaseMvpFragment
import dev.umerov.project.listener.OnSectionResumeCallback
import dev.umerov.project.model.SectionItem

class Lab6Fragment : BaseMvpFragment<Lab6Presenter, ILab6View>(),
    ILab6View {
    private var mHiddenWord: MaterialTextView? = null
    private var mTryCount: MaterialTextView? = null
    private var mEditWord: TextInputEditText? = null

    private var wordTaked = listOf(
        "Лейтенант",
        "Курлык",
        "Цапля",
        "Епифанцев",
        "Полковник",
        "Вилка",
        "Гауптвахта",
        "Шашки",
        "Погона",
        "Братишка"
    ).shuffled()[0]

    private var wordState: String = ""
    private var tryCount = 5

    override fun onResume() {
        super.onResume()
        if (requireActivity() is OnSectionResumeCallback) {
            (requireActivity() as OnSectionResumeCallback).onSectionResume(SectionItem.MAIN)
        }
        val actionBar = supportToolbarFor(this)
        if (actionBar != null) {
            actionBar.setTitle(R.string.lab_6)
            actionBar.subtitle = null
        }
        ActivityFeatures.Builder()
            .begin()
            .setBarsColored(requireActivity(), true)
            .build()
            .apply(requireActivity())
    }

    override fun getPresenterFactory(saveInstanceState: Bundle?) = Lab6Presenter()

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_lab6, container, false)
        (requireActivity() as AppCompatActivity).setSupportActionBar(root.findViewById(R.id.toolbar))

        mHiddenWord = root.findViewById(R.id.item_hidden_word)
        mTryCount = root.findViewById(R.id.item_count_try)
        mEditWord = root.findViewById(R.id.edit_word)

        for (i in wordTaked.indices) {
            wordState += "*"
        }

        savedInstanceState?.let {
            wordTaked = it.getString(HIDDEN_WORD, wordTaked)
            wordState = it.getString(STATE_WORD, wordState)
            tryCount = it.getInt(TRY_COUNT, tryCount)
        }

        mHiddenWord?.text = wordState
        mTryCount?.text = getString(R.string.attempts_count, tryCount)

        root.findViewById<MaterialButton>(R.id.button_try_letter).setOnClickListener {
            if (mEditWord?.text.toString().isEmpty()) {
                return@setOnClickListener
            }
            val char = mEditWord?.text.toString()[0]
            mEditWord?.setText("")
            var finded = false
            for (i in wordTaked.indices) {
                if (wordTaked[i].lowercase() == char.lowercase()) {
                    finded = true
                    wordState =
                        StringBuilder(wordState).also { it.setCharAt(i, wordTaked[i]) }.toString()
                }
            }
            if (!finded) {
                tryCount--
                mTryCount?.text = getString(R.string.attempts_count, tryCount)
                if (tryCount <= 0) {
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                    return@setOnClickListener
                }
            }
            mHiddenWord?.text = wordState
            if (!wordState.contains("*")) {
                mEditWord?.isEnabled = false
                root.findViewById<MaterialButton>(R.id.button_try_letter).visibility = View.GONE
                root.findViewById<MaterialButton>(R.id.button_try_word).visibility = View.GONE
            }
        }
        root.findViewById<MaterialButton>(R.id.button_try_word).setOnClickListener {
            if (mEditWord?.text.toString().isEmpty()) {
                return@setOnClickListener
            }
            if (wordTaked.lowercase() != mEditWord?.text.toString().lowercase()) {
                mEditWord?.setText("")
                tryCount--
                mTryCount?.text = getString(R.string.attempts_count, tryCount)
                if (tryCount <= 0) {
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                    return@setOnClickListener
                }
            } else {
                wordState = wordTaked
                mHiddenWord?.text = wordState
                mEditWord?.isEnabled = false
                root.findViewById<MaterialButton>(R.id.button_try_letter).visibility = View.GONE
                root.findViewById<MaterialButton>(R.id.button_try_word).visibility = View.GONE
            }
        }
        return root
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(HIDDEN_WORD, wordTaked)
        outState.putString(STATE_WORD, wordState)
        outState.putInt(TRY_COUNT, tryCount)
        super.onSaveInstanceState(outState)
    }

    companion object {
        fun newInstance(): Lab6Fragment {
            return Lab6Fragment()
        }

        const val HIDDEN_WORD = "hidden_word"
        const val TRY_COUNT = "try_count"
        const val STATE_WORD = "state_word"
    }

    override fun showMessage(@StringRes res: Int) {
        customToast?.setDuration(Toast.LENGTH_SHORT)?.showToast(res)
    }
}
