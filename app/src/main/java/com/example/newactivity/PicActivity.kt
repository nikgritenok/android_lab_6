package com.example.newactivity
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class PicActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pic_layout)

        title = "Картинка"

        val imageUrl = intent.getStringExtra("picLink")

        val imageView: ImageView = findViewById(R.id.picView)
        imageUrl?.let {
            Glide.with(this)
                .load(it)
                .into(imageView)
        }
    }
}
