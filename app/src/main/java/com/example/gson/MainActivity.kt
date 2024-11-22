package com.example.gson

import android.content.Intent
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import timber.log.Timber

data class Photo(
    val id: String,
    val owner: String,
    val secret: String,
    val server: String,
    val farm: Int,
    val title: String,
    val ispublic: Int,
    val isfriend: Int,
    val isfamily: Int
)

data class PhotoPage(
    val page: Int,
    val pages: Int,
    val perpage: Int,
    val total: Int,
    val photo: List<Photo>
)

data class Wrapper(
    val photos: PhotoPage,
    val stat: String
)

interface OnPhotoClickListener {
    fun onPhotoClick(photo: Photo)
}

class MainActivity : AppCompatActivity(), OnPhotoClickListener {
    private val client = OkHttpClient()
    private val gson = Gson()
    private lateinit var recyclerView: RecyclerView
    private lateinit var photoAdapter: Adapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        recyclerView = findViewById(R.id.rView)
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        loadPhotos()
    }

    private fun loadPhotos() {
        val url = "https://api.flickr.com/services/rest/?method=flickr.photos.search&api_key=ff49fcd4d4a08aa6aafb6ea3de826464&tags=cat&format=json&nojsoncallback=1"

        val request = Request.Builder().url(url).build()
        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                Timber.e(e, "Failed to fetch photos")
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                if (response.isSuccessful) {
                    response.use {
                        val bodyString = it.body?.string()
                        if (bodyString != null) {
                            try {
                                val wrapper = gson.fromJson(bodyString, Wrapper::class.java)

                                if (wrapper.photos.photo.isNotEmpty()) {
                                    runOnUiThread {
                                        photoAdapter = Adapter(wrapper.photos.photo, this@MainActivity)
                                        recyclerView.adapter = photoAdapter
                                    }
                                }
                            } catch (e: Exception) {
                                Timber.e(e, "Failed to parse response")
                            }
                        }
                    }
                }
            }
        })
    }


    override fun onPhotoClick(photo: Photo) {
        val imageUrl = "https://farm${photo.farm}.staticflickr.com/${photo.server}/${photo.id}_${photo.secret}_z.jpg"
        val intent = Intent(this, PicViewer::class.java)
        intent.putExtra("picLink", imageUrl)
        startActivityForResult(intent, 1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK && data != null) {
            val imageUrl = data.getStringExtra("imageUrl")
            val isFavorite = data.getBooleanExtra("isFavorite", false)

            if (isFavorite) {
                val snackBar = Snackbar.make(
                    findViewById(R.id.main),
                    "Картинка добавлена в избранное",
                    Snackbar.LENGTH_LONG
                )
                snackBar.setAction("Открыть") {
                    val browserIntent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(imageUrl))
                    startActivity(browserIntent)
                }
                snackBar.show()
            }
        }
    }
}
