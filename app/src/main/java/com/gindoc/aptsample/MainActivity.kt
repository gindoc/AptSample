package com.gindoc.aptsample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.gindoc.apt.annotation.BindView
import com.gindoc.apt.library.BindViewTools

class MainActivity : AppCompatActivity() {


    @BindView(R.id.text1)
    lateinit var text1: TextView

    @BindView(R.id.text2)
    lateinit var text2: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        BindViewTools.bind(this)

        text1.text = "text1"
        text2.text = "text2"
    }
}