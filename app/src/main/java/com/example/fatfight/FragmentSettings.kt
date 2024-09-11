package com.example.fatfight

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment

class FragmentSettings : Fragment() {

    private lateinit var numberMaxCalories: EditText
    private lateinit var numberMaxSearchResults: EditText

    private lateinit var mainActivity: MainActivity

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        mainActivity = activity as MainActivity

        // Set up views
        numberMaxCalories = view.findViewById(R.id.number_max_calories)
        numberMaxSearchResults = view.findViewById(R.id.number_max_search_results)

        setSettingValues()

        // Initialize the button object
        val btnBack = view.findViewById<ImageButton>(R.id.btn_fragment_back)
        btnBack.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_activity_main, FragmentMain())
                .commit()
        }

        setSpinners(view)

        setEventListeners()

        return view
    }

    private fun setSpinners(view: View){
        //  Set spinner views
        val dietTypeSpinner: Spinner = view.findViewById(R.id.spinner_diet_type)
        ArrayAdapter.createFromResource(mainActivity, R.array.diet_type, android.R.layout.simple_spinner_item).also {
                adapter -> adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            dietTypeSpinner.adapter = adapter
        }
        val mealTypeSpinner: Spinner = view.findViewById(R.id.spinner_meal_type)
        ArrayAdapter.createFromResource(mainActivity, R.array.meal_type, android.R.layout.simple_spinner_item).also {
                adapter -> adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            mealTypeSpinner.adapter = adapter
        }
        val randomRecipe: Spinner = view.findViewById(R.id.random_feed_input)
        ArrayAdapter.createFromResource(mainActivity, R.array.random_Feed_array, android.R.layout.simple_spinner_item). also {
                adapter -> adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            randomRecipe.adapter = adapter
        }

        setSpinnerSelected(dietTypeSpinner, "diet_type")
        setSpinnerSelected(mealTypeSpinner, "meal_type")
        setSpinnerSelected(randomRecipe, "random_recipe")

        // Set spinner listeners
        setSpinnerSelectionListener(dietTypeSpinner, "diet_type")
        setSpinnerSelectionListener(mealTypeSpinner, "meal_type")
        setSpinnerSelectionListener(randomRecipe, "random_recipe")
    }

    private fun setSpinnerSelected(spinner: Spinner, key: String){
        // Set sharedPreferences val
        val option = mainActivity.sharedPreferences.getInt(key, 0)
        spinner.setSelection(option)
    }

    private fun setSpinnerSelectionListener(spinner: Spinner, key: String) {
        val editor = mainActivity.sharedPreferences.edit()
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                editor.putInt(key, position).apply()
            }
            override fun onNothingSelected(p0: AdapterView<*>?) { spinner.setSelection(0) }
        }
    }


    private fun setEventListeners(){
        saveSharedPreferences(numberMaxCalories, mainActivity.sharedPreferences, "daily_calories", 10000)
        saveSharedPreferences(numberMaxSearchResults, mainActivity.sharedPreferences, "max_search_results", 20)
    }

    private fun saveSharedPreferences(
        numberMaxSearchResults: EditText,
        sharedPreferences: SharedPreferences,
        key: String,
        maxValue: Int
    ) {
        numberMaxSearchResults.addTextChangedListener(afterTextChanged = {
            val input = numberMaxSearchResults.text.toString()
            val maxSearchResults = if (input.isEmpty() || input.toInt() < 1) 1 else input.toInt()
            val valueToSave = if (maxSearchResults > maxValue) maxValue else maxSearchResults
            sharedPreferences.edit().putInt(key, valueToSave).apply()
        })
    }


    private fun setSettingValues(){
        // Initialize values
        numberMaxCalories.setText(mainActivity.sharedPreferences.getInt("daily_calories", 0).toString())
        numberMaxSearchResults.setText(mainActivity.sharedPreferences.getInt("max_search_results", 0).toString())
    }
}