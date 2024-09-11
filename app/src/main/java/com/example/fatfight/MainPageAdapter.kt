package com.example.fatfight

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView

class MainPageAdapter(private val allData: ArrayList<AssetData>, private val activity: Activity, private val updateCalorieStatus: () -> Unit) : RecyclerView.Adapter<MainPageAdapter.ViewHolder>() {
    private var disableSubtractButton = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainPageAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.card_layout, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        activity as MainActivity
        holder.itemImage.setImageBitmap(activity.imageBitmapList[position])
        holder.itemTitle.text = allData[position].label
        holder.itemLabels.text = allData[position].mealType
        val kcal = allData[position].calories.toString() + " kcal"
        holder.itemKcal.text = kcal

        // Opens a url from the allData list using the "let" function to avoid a null reference.
        holder.itemImage.setOnClickListener {
            val url = allData[position].url.toString()
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            activity.startActivity(intent)
        }
        holder.addCalories.setOnClickListener{
            disableSubtractButton = false
            val sharedPreferences = activity.getSharedPreferences("my_app_settings", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()

            val currentCalories = sharedPreferences.getInt("today's_calories", 0)
            val caloriesToAdd = allData[position].calories ?: 0 // Elvis-operator

            val newCalorieCount = currentCalories + caloriesToAdd

            editor.putInt("today's_calories", newCalorieCount)
            editor.apply()
            updateCalorieStatus()
        }
        holder.subtractCalories.setOnClickListener{
            if(disableSubtractButton) return@setOnClickListener
            val sharedPreferences = activity.getSharedPreferences("my_app_settings", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()

            val currentCalories = sharedPreferences.getInt("today's_calories", 0)
            val caloriesToAdd = allData[position].calories ?: 0 // Elvis-operator

            var newCalorieCount = currentCalories - caloriesToAdd
            if (newCalorieCount <= 0) {
                newCalorieCount = 0
                disableSubtractButton = true
            }

            editor.putInt("today's_calories", newCalorieCount)
            editor.apply()
            updateCalorieStatus()
        }

        // Check if the item has been liked
        holder.itemLiked = allData[position].itemLiked
        // Set the appropriate image for the like button
        if(holder.itemLiked)holder.itemLike.setImageResource(R.drawable.heart_fill)
        else holder.itemLike.setImageResource(R.drawable.heart)
        holder.itemHref = allData[position].href.toString()

//      Toggles like button on click for each view in recyclerview
        holder.itemLike.setOnClickListener {
            when (holder.itemLiked) {
                false -> {
                    holder.itemLike.setImageResource(R.drawable.heart_fill)
                    holder.itemLiked = true
                    // add item self url to database liked_recipes
                    activity.database.insertNewLikedRecipe(holder.itemHref)
                }
                true -> {
                    holder.itemLike.setImageResource(R.drawable.heart)
                    holder.itemLiked = false
                    // Remove item self url to database liked_recipes
                    activity.database.deleteLikedRecipe(holder.itemHref)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        val sharedPreferences =
            (activity as MainActivity).getSharedPreferences("my_app_settings", Context.MODE_PRIVATE)
        return sharedPreferences.getInt("max_search_results", 0)
    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        var itemImage : ImageView
        var itemTitle : TextView
        var itemLabels : TextView
        var itemKcal : TextView
        var itemLike : ImageButton
        var itemLiked : Boolean = false
        lateinit var itemHref: String
        var addCalories : Button
        var subtractCalories : Button

        init {
            itemImage = itemView.findViewById(R.id.card_item_image)
            itemTitle = itemView.findViewById(R.id.card_item_title)
            itemLabels = itemView.findViewById(R.id.card_item_labels)
            itemKcal = itemView.findViewById(R.id.card_item_calories)
            itemLike = itemView.findViewById(R.id.card_item_btn_like)
            addCalories = itemView.findViewById(R.id.card_item_btn_add)
            subtractCalories = itemView.findViewById(R.id.card_item_btn_subtract)
        }
    }
}