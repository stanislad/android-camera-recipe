package com.example.androidtest

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.*
import java.io.File
import java.io.IOException

private const val FILE_NAME = "photo.jpg"
private const val REQUEST_CODE = 42
private lateinit var photoFile: File
private val client = OkHttpClient()

class MainActivity : AppCompatActivity() {
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
//            val takenImage = data?.extras?.get("data") as Bitmap
            val takenImage = BitmapFactory.decodeFile(photoFile.absolutePath)

            val rotatedBitmap = takenImage.rotate(90f)

            imageView.setImageBitmap(rotatedBitmap)
            val image_text = ProcessImage().doOCR(applicationContext,rotatedBitmap)
            val rightGuesses = ProcessImage().processImageText(image_text)
            displayText(rightGuesses)

            val params = rightGuesses.joinToString(",+")

            run("https://api.spoonacular.com/recipes/findByIngredients?ingredients=$params&number=5&apiKey=07639bcabe6742da912059c48ffd7010")
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
        val arrayAdapter: ArrayAdapter<*>

        // access the listView from xml file
        var mListView = findViewById<ListView>(R.id.list_view)
        arrayAdapter = ArrayAdapter(this,
                android.R.layout.simple_list_item_1, rightGuesses)
        mListView.adapter = arrayAdapter
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
        println(data)

//        if (data != null) {
//            for(i in data){
//                println(i)
//            }
//        }

    }

    fun Bitmap.rotate(degrees: Float): Bitmap
    {
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
    }
}

