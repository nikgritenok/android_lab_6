package com.example.newactivity

import android.content.Intent;
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.Button;


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val btnShowPic: Button = findViewById(R.id.btn_show_pic)

        btnShowPic.setOnClickListener {
            val intent = Intent(this, PicActivity::class.java)

            val imageUrl = "https://steamuserimages-a.akamaihd.net/ugc/961981982172238631/F5D6143501DDE25885242E0128042D4699E44A0E/?imw=512&imh=512&ima=fit&impolicy=Letterbox&imcolor=%23000000&letterbox=true"
            intent.putExtra("picLink", imageUrl)

            startActivity(intent)
        }

    }



}