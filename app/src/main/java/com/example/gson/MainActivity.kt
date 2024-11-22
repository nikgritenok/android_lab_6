package com.example.gson

import java.io.IOException
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import timber.log.Timber
import okhttp3.OkHttpClient
import okhttp3.Request
import com.google.gson.Gson
import android.content.Intent

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
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        Timber.plant(Timber.DebugTree())
        Timber.d("Timber is initialized")

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
                                    logEveryFifthPhoto(wrapper.photos.photo)
                                    generatePhotoLinks(wrapper.photos.photo)

                                    runOnUiThread {
                                        photoAdapter = Adapter(wrapper.photos.photo, this@MainActivity)
                                        recyclerView.adapter = photoAdapter
                                    }
                                } else {
                                    Timber.e("No photos found")
                                }
                            } catch (e: Exception) {
                                Timber.e(e, "Failed to parse response")
                            }
                        } else {
                            Timber.e("Response body is null")
                        }
                    }
                } else {
                    Timber.e("Unexpected response: ${response.code}")
                }
            }
        })
    }

    private fun logEveryFifthPhoto(photos: List<Photo>) {
        for (i in photos.indices) {
            if ((i + 1) % 5 == 0) {
                val photo = photos[i]
                Timber.d("id: ${photo.id}, owner: ${photo.owner}, secret: ${photo.secret}\n" +
                        "server: ${photo.server}, farm: ${photo.farm}, title: ${photo.title}\n" +
                        "ispublic: ${photo.ispublic}, isfriend: ${photo.isfriend}, isfamily: ${photo.isfamily}")
            }
        }
    }

    private fun generatePhotoLinks(photos: List<Photo>) {
        val photoLinks = photos.map { photo ->
            "https://farm${photo.farm}.staticflickr.com/${photo.server}/${photo.id}_${photo.secret}_z.jpg"
        }
        photoLinks.forEach { link ->
            Timber.d("Photo link: $link")
        }
    }

    override fun onPhotoClick(photo: Photo) {
        val imageUrl = "https://farm${photo.farm}.staticflickr.com/${photo.server}/${photo.id}_${photo.secret}_z.jpg"
        val intent = Intent(this, PicViewer::class.java)
        intent.putExtra("picLink", imageUrl)
        startActivity(intent)
    }
}