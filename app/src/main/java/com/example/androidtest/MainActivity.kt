package com.example.androidtest

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.SparseBooleanArray
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.*
import org.json.JSONArray
import org.json.JSONTokener
import java.io.File
import java.io.IOException


private const val FILE_NAME = "photo.jpg"
private const val REQUEST_CODE = 42
private lateinit var photoFile: File
private val client = OkHttpClient()

class MainActivity : AppCompatActivity(), AdapterView.OnItemClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnTakePicture.setOnClickListener{
             val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            photoFile = getPhotoFile(FILE_NAME)

            // This DOESN'T work for API >= 24 (starting 2016)
            // takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoFile)

            val fileProvider = FileProvider.getUriForFile(this, "com.example.fileprovider", photoFile)
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider)

            if( takePictureIntent.resolveActivity(this.packageManager) != null){
                startActivityForResult(takePictureIntent, REQUEST_CODE)
            }else{
                Toast.makeText(this, "Unable to open camera ", Toast.LENGTH_SHORT).show()
            }

        }

        btnok.setOnClickListener{
            val mListView = findViewById<ListView>(R.id.list_view)
            val len: Int = mListView.count
            val checked: SparseBooleanArray = mListView.checkedItemPositions

            val array : ArrayList<String> = arrayListOf()

            for (i in 0 until len) if (checked[i]) {
                val item: String = mListView.getItemAtPosition(i) as String
                array.add(item)
            }
            //todo check if array isnt empty
            callApi(array)
        }
    }

    private fun getPhotoFile(fileName: String): File
    {
        // Use `getExternalFilesDir` on Context to access package-specific directories.
        val storageDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(fileName, ".jpg", storageDirectory)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    {
        if(requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK){

            val takenImage = BitmapFactory.decodeFile(photoFile.absolutePath)

            val rotatedBitmap = takenImage.rotate(90f)

            imageView.setImageBitmap(rotatedBitmap)
            val image_text = ProcessImage().doOCR(applicationContext, rotatedBitmap)
            val rightGuesses = ProcessImage().processImageText(image_text)
//            val rightGuesses = arrayListOf<String>("apple", "banana")
            displayText(rightGuesses)


        }else{
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    fun displayText(rightGuesses: ArrayList<String>)
    {
        //todo: old print to textView
        //val textView : TextView = findViewById<TextView>(R.id.textView)
        //textView.text = rightGuesses.joinToString()

        // use arrayadapter and define an array
        val arrayAdapter: ArrayAdapter<String>

        // access the listView from xml file
        val mListView = findViewById<ListView>(R.id.list_view)
        arrayAdapter = ArrayAdapter(this,
                android.R.layout.simple_list_item_multiple_choice, rightGuesses)
        mListView?.adapter = arrayAdapter
        mListView?.choiceMode = ListView.CHOICE_MODE_MULTIPLE
        mListView?.onItemClickListener = this
    }

    private fun callApi(rightGuesses: ArrayList<String>)
    {
        val params = rightGuesses.joinToString(",+")
        println(params)
        run("https://api.spoonacular.com/recipes/findByIngredients?ingredients=$params&number=25&apiKey=07639bcabe6742da912059c48ffd7010")
    }

    fun run(url: String) {
        val request = Request.Builder()
                .url(url)
                .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response) = responseCallback(response)
        })
    }

    fun responseCallback(response: Response)
    {
        val data = response.body()?.string()
        openRecipeActivity(data)
    }

    fun Bitmap.rotate(degrees: Float): Bitmap
    {
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
    }

    fun openRecipeActivity(data: String?)
    {
        val intent = Intent(this, RecipeActivity::class.java)
        // To pass any data to next activity
        intent.putExtra("data", data)
        // start your next activity
        startActivity(intent)
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

        var items:String = parent?.getItemAtPosition(position) as String
//        Toast.makeText(applicationContext, items, Toast.LENGTH_LONG).show()
    }
}

