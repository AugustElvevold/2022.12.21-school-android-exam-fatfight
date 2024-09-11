package com.example.fatfight

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.util.*
import kotlin.collections.ArrayList

class Database(context: Context): SQLiteOpenHelper(context, "database.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        // create the table for storing data
        db.execSQL(
            "CREATE TABLE search_history (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "search_word TEXT NOT NULL)"
        )

        db.execSQL(
            "CREATE TABLE liked_recipes (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "recipe TEXT NOT NULL)"
        )
    }


    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS search_history")
        db.execSQL("DROP TABLE IF EXISTS liked_recipes")
        onCreate(db)
    }

    // SQL injection resistant add search word to database
    fun insertSearchWord(string: String) {
        if(string.isEmpty())return
        val searchWord = makeFirstLetterUppercase(string)

        // Escapes function if the last added search word is the same as the new
        if(getLastSearchWord() == searchWord)return

        deleteMatchingSearchWord(searchWord)

        val db = this.writableDatabase
        val statement = db.compileStatement("INSERT INTO search_history (search_word) VALUES (?)")
        statement.bindString(1, searchWord)
        statement.execute()
    }

    private fun makeFirstLetterUppercase(str: String): String {
        return str.substring(0, 1).uppercase(Locale.ROOT) + str.substring(1).lowercase(Locale.ROOT)
    }

    private fun deleteMatchingSearchWord(searchWord: String) {
        writableDatabase.delete("search_history", "search_word = ?", arrayOf(searchWord))
    }

    // Returns the last search word in the database
    private fun getLastSearchWord(): String? {
        val word = readableDatabase.rawQuery("SELECT * FROM search_history ORDER BY id DESC LIMIT 1", null)
        word.use { cursor ->
            if (cursor.moveToFirst()) {
                return cursor.getString(1)
            }
        }
        return null
    }

    fun getListOfSearchWords(numberOfRows: Int): List<String> {
        val searchWords = mutableListOf<String>()
        val rs = readableDatabase.rawQuery("SELECT * FROM search_history ORDER BY id DESC LIMIT $numberOfRows", null)
        rs.use { cursor ->
            if (cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndexOrThrow("search_word")
                for (i in 0 until cursor.count) {
                    searchWords.add(cursor.getString(columnIndex))
                    cursor.moveToNext()
                }
            }
        }
        return searchWords
    }

    // Delete search history
    fun truncateSearchHistoryTable() {
        writableDatabase.execSQL("DELETE FROM search_history;")
    }

    fun insertNewLikedRecipe(href: String){
        val values = ContentValues()
        values.put("recipe", href)
        writableDatabase.insert("liked_recipes", null, values)
    }

    fun deleteLikedRecipe(recipe: String) {
        val db = this.writableDatabase
        db.delete("liked_recipes", "recipe = ?", arrayOf(recipe))
    }

    fun getAllLikedRecipes(): ArrayList<String> {
        val recipes = ArrayList<String>()
        val rs = readableDatabase.query("liked_recipes", null, null, null, null, null, null)
        rs.use { cursor ->
            if (cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndexOrThrow("recipe")
                for (i in 0 until cursor.count) {
                    recipes.add(cursor.getString(columnIndex))
                    cursor.moveToNext()
                }
            }
        }
        return recipes
    }
}