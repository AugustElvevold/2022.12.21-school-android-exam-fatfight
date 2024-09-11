package com.example.fatfight

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL

class FragmentMain : Fragment() {

    private lateinit var mainActivity: MainActivity
    private lateinit var mainPageRV: RecyclerView
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var searchHistoryView: RecyclerView
    private lateinit var calorieStatus: ProgressBar
    private lateinit var labelCalorieStatus: TextView
    private lateinit var btnSettings: ImageButton
    private lateinit var btnSearch: Button
    lateinit var searchInput: EditText


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_main, container, false)
        mainActivity = activity as MainActivity

        setUpViews(view)
        setUpListeners()
        setUpLayouts()

        if(mainActivity.allData.isEmpty()) newApiQuery("") else showAllData()

        updateCalorieStatus()
        return view
    }

    private fun setUpViews(view: View){
        layoutManager = LinearLayoutManager(activity)
        btnSettings = view.findViewById(R.id.btn_settings)
        btnSearch = view.findViewById(R.id.btn_search)
        mainPageRV = view.findViewById(R.id.recycler_view_main_page)
        searchInput = view.findViewById(R.id.input_search_text)
        searchHistoryView = view.findViewById(R.id.recycler_view_search_history)
        calorieStatus = view.findViewById(R.id.calorie_status)
        // Set max daily calories
        calorieStatus.max = mainActivity.sharedPreferences.getInt("daily_calories", 0)
        labelCalorieStatus = view.findViewById(R.id.label_calorie_status)
    }

    private fun setUpListeners(){
        btnSettings.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_activity_main, FragmentSettings())
                .commit()
        }
        btnSearch.setOnClickListener {
            unFocusInputSearch()
            doSearch(searchInput.text.toString())
        }
        searchInput.setOnFocusChangeListener{ _, hasFocus ->
            if (hasFocus) showSearchHistoryMenu()
            else unFocusInputSearch()
        }
        searchInput.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                unFocusInputSearch()
                doSearch(searchInput.text.toString())
            }
            false
        }
    }

    private fun setUpLayouts(){
        mainPageRV.layoutManager = layoutManager
        searchHistoryView.layoutManager = LinearLayoutManager(activity)
        // Start search history view as invisible so its not interfering with scrolling on main view
        searchHistoryView.visibility = View.INVISIBLE
    }

    private fun doRandomSearch(){
        if(mainActivity.allData.isEmpty()) doSearch("") else showAllData()
    }

    private fun updateCalorieStatus(){
        // Reference to the SharedPreferences object for the app
        val limit = mainActivity.sharedPreferences.getInt("daily_calories", 0)
        val progress = mainActivity.sharedPreferences.getInt("today's_calories", 0)

        // This code (102-107) almost works. Somehow it gets confused whenever progress is over the limit the second time or more...
        if(progress >= limit) {
            calorieStatus.progressDrawable = ContextCompat.getDrawable(mainActivity, R.drawable.calorie_progress_bar_max)
        } else {
            calorieStatus.progressDrawable = ContextCompat.getDrawable(mainActivity, R.drawable.calorie_progress_bar)
        }
        calorieStatus.progress = progress
        labelCalorieStatus.text = resources.getString(R.string.progress_string, progress, limit)
    }

    fun doSearch(searchWord: String){
        mainActivity.updateMealType()
        newApiQuery(searchWord)
        mainActivity.database.insertSearchWord(searchWord)
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun newApiQuery(string: String){
        // Do query to api and populate recyclerview with the new data
        GlobalScope.launch(Dispatchers.Main){
            mainActivity.allData = downloadAssetList(string)
            initiateEmptyImages()
            showAllData()
            downloadImages()
        }
    }

    private fun showAllData(){
        mainPageRV.adapter = MainPageAdapter(mainActivity.allData, mainActivity) { updateCalorieStatus() }
    }

    private fun initiateEmptyImages() {
        val maxSearchResults = mainActivity.sharedPreferences.getInt("max_search_results", 0)

        val emptyBitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)

        for (i in 0..maxSearchResults) {
            val exist = mainActivity.imageBitmapList.getOrNull(i)
            if (exist != null) {
                mainActivity.imageBitmapList[i] = emptyBitmap
            } else {
                mainActivity.imageBitmapList.add(emptyBitmap)
            }
        }
    }

    private suspend fun downloadImages() {
        val maxSearchResults = mainActivity.sharedPreferences.getInt("max_search_results", 0)

        withContext(Dispatchers.Default) {
            (0 until maxSearchResults).forEach { index ->
                val imageBytes = URL(mainActivity.allData[index].imageLink).readBytes()
                val image = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                mainActivity.imageBitmapList[index] = image
                mainActivity.runOnUiThread {
                    mainPageRV.adapter?.notifyItemChanged(index)
                }
            }
        }
    }

    private suspend fun downloadAssetList(searchWord: String): ArrayList<AssetData>{
        // List to fill with AssetData objects
        val allData = ArrayList<AssetData>()

        // Required base keys for making a api call
        val appKey = "f18f48085b2b72b2969f2577e330fc25"
        val appId = "4a2fdf00"
        val required = "https://api.edamam.com/api/recipes/v2?type=public&app_key=$appKey&app_id=$appId"

        // Random recipes true or false
        val random = "&random=${(mainActivity.sharedPreferences.getInt("random_recipe", 0) == 0)}"

        // Optional search parameters
        val queryText = if (searchWord.isNotEmpty()) "&q=$searchWord" else ""
        val mealType = if (mainActivity.mealType.isNotEmpty()) "&mealType=${mainActivity.mealType}" else ""
        val dietType = if (mainActivity.getDietType().isNotEmpty()) "&diet=${mainActivity.getDietType()}" else ""

        // Gets list of liked items from database
        val likedRecipes = mainActivity.database.getAllLikedRecipes()

        withContext(Dispatchers.Default) {
            // Constructing the URL for the API call
            val assetData = URL(required + queryText + mealType + dietType + random).readText()

            // Retrieves the data from the API and parses it into an array of JSON objects.
            val assetDataArray = (JSONObject(assetData).get("hits") as JSONArray)
            // Creates a new AssetData object for each item in the Array, sets its properties using the data from the JSON object
            (0 until assetDataArray.length()).forEach { index ->
                val dataItem = AssetData()
                val recipeItem = (assetDataArray.get(index) as JSONObject).getJSONObject("recipe")
                dataItem.label = recipeItem.getString("label")
                dataItem.mealType = recipeItem.getJSONArray("mealType").getString(0)
                dataItem.url = recipeItem.getString("url")

                val calPerPortion = (recipeItem.getInt("calories")) / (recipeItem.getInt("yield"))
                dataItem.calories = calPerPortion

                dataItem.imageLink = recipeItem.getString("image")


                val linkItem = (assetDataArray.get(index) as JSONObject).getJSONObject("_links")
                val self = linkItem.getJSONObject("self")
                dataItem.href = self.getString("href")

                // Checks if item is stored in database table of liked items, and applies liked status true or false
                dataItem.itemLiked = dataItem.href in likedRecipes

                allData.add(dataItem)
            }
        }
        return allData
    }

    private fun showSearchHistoryMenu(){
        val searchHistoryList = mainActivity.database.getListOfSearchWords(10) as ArrayList<String>
        searchHistoryView.adapter = SearchHistoryAdapter(searchHistoryList, this)
        searchHistoryView.visibility = View.VISIBLE
    }

    fun unFocusInputSearch() {
        // Clear the focus from the EditText
        searchInput.clearFocus()

        // Hide the keyboard if it is currently visible
        val inputMethodManager = searchInput.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(searchInput.windowToken, 0)

        // Hide search history menu
        searchHistoryView.visibility = View.INVISIBLE
    }
}