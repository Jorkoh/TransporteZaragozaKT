package com.jorkoh.transportezaragozakt.destinations.more

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.GridView
import androidx.fragment.app.Fragment
import com.jorkoh.transportezaragozakt.R
import daio.io.dresscode.dressCodeStyleId
import daio.io.dresscode.getDressCodes


/**
 * Fragment containing the theme picker
 */
open class ThemePickerFragment : Fragment(), OnItemClickListener {


    private lateinit var gridView: GridView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.theme_picker, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        gridView = view.findViewById(R.id.gridView)
        val themes = getThemes()
        gridView.adapter = ThemePickerAdapter(themes, requireContext())
        gridView.onItemClickListener = this
        scrollToCurrentTheme(themes)
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val theme = (gridView.adapter as ThemePickerAdapter).getItem(position)
        requireActivity().dressCodeStyleId = theme.themeId
    }

    private fun scrollToCurrentTheme(themes: List<CustomTheme>) {
        var selectedTheme = -1
        run {
            themes.forEachIndexed { index, theme ->
                if (theme.isMatchingColorScheme(requireContext())) {
                    selectedTheme = index
                    return@run
                }
            }
        }
        if (selectedTheme != -1) {
                if (selectedTheme < gridView.firstVisiblePosition || selectedTheme > gridView.lastVisiblePosition) {
                    gridView.setSelection(selectedTheme)
                }
        }
    }

    private fun getThemes(): List<CustomTheme> {
        val themes = mutableListOf<CustomTheme>()

        requireActivity().getDressCodes().forEach { dressCode ->
            val attributes = requireActivity().obtainStyledAttributes(dressCode.value.themeId, R.styleable.StyleAttrs)
            themes.add(
                CustomTheme(
                    dressCode.value.name,
                    dressCode.value.themeId,
                    attributes.getColor(R.styleable.StyleAttrs_colorPrimary, Color.BLACK),
                    attributes.getColor(R.styleable.StyleAttrs_colorSecondary, Color.BLACK),
                    attributes.getColor(R.styleable.StyleAttrs_android_colorBackground, Color.BLACK)
                )
            )
            attributes.recycle()
        }

        return themes.sortedBy { it.name }
    }

    data class CustomTheme(
        val name: String,
        val themeId: Int,
        val primaryColor: Int,
        val secondaryColor: Int,
        val backgroundColor: Int
    ) {
        fun isMatchingColorScheme(context: Context): Boolean {
            val attributes = context.obtainStyledAttributes(R.styleable.StyleAttrs)

            val currentPrimary = attributes.getColor(R.styleable.StyleAttrs_colorPrimary, Color.BLACK)
            val currentSecondary = attributes.getColor(R.styleable.StyleAttrs_colorSecondary, Color.BLACK)
            val currentBackground = attributes.getColor(R.styleable.StyleAttrs_android_colorBackground, Color.BLACK)

            attributes.recycle()

            return currentPrimary == primaryColor && currentSecondary == secondaryColor && currentBackground == backgroundColor
        }
    }
}