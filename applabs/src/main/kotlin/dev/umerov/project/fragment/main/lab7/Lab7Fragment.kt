package dev.umerov.project.fragment.main.lab7

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SimpleAdapter
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatSpinner
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import dev.umerov.project.R
import dev.umerov.project.activity.ActivityFeatures
import dev.umerov.project.activity.ActivityUtils.supportToolbarFor
import dev.umerov.project.fragment.base.BaseMvpFragment
import dev.umerov.project.listener.OnSectionResumeCallback
import dev.umerov.project.model.SectionItem
import dev.umerov.project.model.main.labs.Lab7Film


class Lab7Fragment : BaseMvpFragment<Lab7Presenter, ILab7View>(),
    ILab7View {
    private var films: AppCompatSpinner? = null
    private var adapter: SimpleAdapter? = null
    private var mList: ArrayList<Map<String, String>> = ArrayList()
    override fun onResume() {
        super.onResume()
        if (requireActivity() is OnSectionResumeCallback) {
            (requireActivity() as OnSectionResumeCallback).onSectionResume(SectionItem.MAIN)
        }
        val actionBar = supportToolbarFor(this)
        if (actionBar != null) {
            actionBar.setTitle(R.string.lab_7)
            actionBar.subtitle = null
        }
        ActivityFeatures.Builder()
            .begin()
            .setBarsColored(requireActivity(), true)
            .build()
            .apply(requireActivity())
    }

    override fun getPresenterFactory(saveInstanceState: Bundle?) = Lab7Presenter()

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_lab7, container, false)
        (requireActivity() as AppCompatActivity).setSupportActionBar(root.findViewById(R.id.toolbar))

        films = root.findViewById(R.id.item_films)
        adapter = SimpleAdapter(
            requireActivity(),
            mList,
            R.layout.item_film,
            arrayOf("Title", "Genre", "Year"),
            intArrayOf(R.id.item_title, R.id.item_genre, R.id.item_year)
        )
        android.R.layout.simple_list_item_2
        films?.adapter = adapter

        val mTitle: TextInputEditText = root.findViewById(R.id.edit_title)
        val mGenre: TextInputEditText = root.findViewById(R.id.edit_genre)
        val mYear: TextInputEditText = root.findViewById(R.id.edit_year)

        root.findViewById<FloatingActionButton>(R.id.add).setOnClickListener {
            presenter?.fireAdd(
                Lab7Film(
                    mTitle.text.toString(),
                    mGenre.text.toString(),
                    mYear.text.toString().toInt()
                )
            )
            mTitle.setText("")
            mGenre.setText("")
            mYear.setText("")
        }
        return root
    }

    companion object {
        fun newInstance(): Lab7Fragment {
            return Lab7Fragment()
        }
    }

    override fun setData(list: List<Lab7Film>) {
        mList.clear()
        for (i in list) {
            mList.add(i.toMap())
        }
        adapter?.notifyDataSetChanged()
    }

    override fun showMessage(@StringRes res: Int) {
        customToast?.setDuration(Toast.LENGTH_SHORT)?.showToast(res)
    }
}
