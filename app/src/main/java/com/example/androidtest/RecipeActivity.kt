package com.example.androidtest

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import org.json.JSONArray
import org.json.JSONTokener

class RecipeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe)

        val ss: String? = intent.getStringExtra("data").toString()

//        Toast.makeText(applicationContext, ss, Toast.LENGTH_SHORT).show()

        val recipes : ArrayList<String> = arrayListOf()

        val jsonArray = JSONTokener(ss).nextValue() as JSONArray
        for (i in 0 until jsonArray.length()) {

            val title = jsonArray.getJSONObject(i).getString("title")
            recipes.add(title)
            println(title)
        }
        displayText(recipes)

    }

    private fun displayText(rightGuesses: ArrayList<String>)
    {
        // access the listView from xml file
        val mListView = findViewById<ListView>(R.id.list_view_recipes)
        val arrayAdapter: ArrayAdapter<String> = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line, rightGuesses
        )
        mListView?.adapter = arrayAdapter
        mListView?.choiceMode = ListView.CHOICE_MODE_MULTIPLE
    }
}