package com.example.androidtest

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.text.TextRecognizer
import java.security.AccessControlContext

class ProcessImage : AppCompatActivity() {
    fun doOCR(applicationContext: Context, bitmap: Bitmap) : String
    {
        val textRecognizer = TextRecognizer.Builder(applicationContext).build()

        val imageFrame: Frame = Frame.Builder()
            .setBitmap(bitmap) // your image bitmap
            .build()

        var imageText = ""

        val textBlocks = textRecognizer.detect(imageFrame)

        for (i in 0 until textBlocks.size()) {
            val textBlock = textBlocks[textBlocks.keyAt(i)]
            println(textBlock.value)
            imageText = imageText + ' ' + textBlock.value // return string
        }

        return imageText
    }

    fun processImageText(imageText: String) : ArrayList<String>
    {
        val split_text = imageText.split("\\s".toRegex())

        Log.d("successo", split_text.joinToString())

        val rightGuesses: ArrayList<String> = ArrayList()
        val food = Food().food

        for(i in split_text){
            if(
                food.contains(i.toLowerCase())
                || food.contains(i.toLowerCase() + "s")
                || food.contains(i.toLowerCase().dropLast(1))
            )
            {
                rightGuesses.add(i)
                Log.d("successa", i)
            }
        }
        return rightGuesses

    }

}