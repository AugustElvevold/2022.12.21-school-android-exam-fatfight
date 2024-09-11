package com.example.fatfight

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    val database: Database = Database(this)
    lateinit var sharedPreferences: SharedPreferences

    var allData: ArrayList<AssetData> = arrayListOf()
    var imageBitmapList: ArrayList<Bitmap> = arrayListOf()

    lateinit var mealType: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPreferences = getSharedPreferences("my_app_settings", Context.MODE_PRIVATE)

        initiateSharedPreferences()
        updateMealType()
        setStartFragment()
    }

    private fun initiateSharedPreferences() {
        // Reference to the SharedPreferences object for the app
        val sharedPreferences = getSharedPreferences("my_app_settings", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // Check if the daily_calories value has been set in the shared preferences
        if (!sharedPreferences.contains("daily_calories")) {
            editor.putInt("daily_calories", 2500)
        }
        if (!sharedPreferences.contains("today's_calories")) {
            editor.putInt("today's_calories", 0)
        }
        if (!sharedPreferences.contains("max_search_results")) {
            editor.putInt("max_search_results", 5)
        }
        if (!sharedPreferences.contains("meal_type")) {
            editor.putInt("meal_type", 0)
        }
        if (!sharedPreferences.contains("diet_type")) {
            editor.putInt("diet_type", 0)
        }
        if (!sharedPreferences.contains("random_recipe")) {
            editor.putInt("random_recipe", 0)
        }
        // Save preferences
        editor.apply()
    }

    private fun setStartFragment(){
        // Initialize fragment main when app is launched
        val fragment = FragmentMain()
        supportFragmentManager.beginTransaction()
            .add(R.id.fragment_container_activity_main, fragment)
            .commit()
    }

    fun getDietType(): String {
        val mealTypeKey = sharedPreferences.getInt("diet_type", 0)

        if (mealTypeKey == 0) {
            return ""
        }
        val stringResources = resources.getStringArray(R.array.diet_type)
        return stringResources[mealTypeKey].toString().lowercase().replace(" ", "-")
    }

    fun updateMealType() {
        val mealTypeKey = sharedPreferences.getInt("meal_type", 0)

        // if mealTypeKey is 0 ("Automatic") function moves on to set mealType based on time of day
        if(mealTypeKey != 0){
            // if mealTypeKey is any other number, mealType is set based on chosen setting
            val stringResources = resources.getStringArray(R.array.meal_type)
            mealType = stringResources[mealTypeKey]
            return
        }

        // Get the current hour
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)

        // Check the time of day and applies the appropriate mealType
        mealType = when (hour) {
            in 0..5 -> "Snack"
            in 5..10 -> "Breakfast"
            in 11..14 -> "Lunch"
            in 15..19 -> "Dinner"
            in 20..23 -> "Snack"
            else -> ""
        }
    }
}