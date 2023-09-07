package com.example.myapplication

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.annotations.SerializedName

private const val NAME = "name"

class MainActivity : AppCompatActivity() {
    private val test = "fafsfasfasf"
    @SerializedName("sernamea")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val a = "a"
        val b = 1
        setContentView(R.layout.activity_main)
        Toast.makeText(this, test, Toast.LENGTH_SHORT).show()
    }
}