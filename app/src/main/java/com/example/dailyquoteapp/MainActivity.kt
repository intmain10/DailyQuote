package com.example.dailyquoteapp

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.dailyquoteapp.databinding.ActivityMainBinding
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.example.dailyquoteapp.model.Quotes
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.example.dailyquoteapp.api.RetrofitClient
import com.example.dailyquoteapp.model.UnsplashPhoto
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val bgIds = listOf(
        R.drawable.bg_nature,
        R.drawable.bg_abstract,
        R.drawable.bg_dark
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // New Quote button click
        binding.btnNewQuote.setOnClickListener {
            val fadeOut = ObjectAnimator.ofFloat(binding.cardView, "alpha", 1f, 0f)
            val fadeIn = ObjectAnimator.ofFloat(binding.cardView, "alpha", 0f, 1f)
            fadeOut.duration = 300
            fadeIn.duration = 300

            fadeOut.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    fetchQuoteAndImage()
                    fadeIn.start()
                }
            })
            fadeOut.start()
        }

        // Share as image
        binding.btnShareQuote.setOnClickListener {
            val bitmap = captureCardView(binding.cardView)
            shareBitmap(this, bitmap)
        }

        // Load first quote
        fetchQuoteAndImage()
    }

    private fun fetchQuote() {
        RetrofitClient.instance.getQuote().enqueue(object : Callback<Quotes> {
            override fun onResponse(call: Call<Quotes>, response: Response<Quotes>) {
                if (response.isSuccessful && response.body() != null) {
                    val quote = response.body()!!
                    binding.textQuote.text = "\"${quote.quote}\"\n\n- ${quote.author}"
                } else {
                    binding.textQuote.text = "Failed to load quote: ${response.code()}"
                }
            }

            override fun onFailure(call: Call<Quotes>, t: Throwable) {
                binding.textQuote.text = "Error: ${t.message}"
            }
        })
    }

    private fun fetchQuoteAndImage() {
        fetchQuote()
        fetchRandomImage()
    }

    private fun fetchRandomImage() {
        val apiKey = BuildConfig.UNSPLASH_ACCESS_KEY

        RetrofitClient.unsplashApi.getRandomImage().enqueue(object : Callback<UnsplashPhoto> {
            override fun onResponse(call: Call<UnsplashPhoto>, response: Response<UnsplashPhoto>) {
                if (response.isSuccessful) {
                    val imageUrl = response.body()?.urls?.regular
                    imageUrl?.let {
                        Glide.with(this@MainActivity)
                            .load(it)
                            .centerCrop()
                            .into(binding.backgroundImageView)
                    }
                } else {
                    Log.e("UNSPLASH_IMAGE", "API Response Error: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<UnsplashPhoto>, t: Throwable) {
                Log.e("UNSPLASH_IMAGE", "API Failure: ${t.message}")
            }
        })
    }

    private fun captureCardView(view: android.view.View): Bitmap {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    private fun shareBitmap(context: Context, bitmap: Bitmap) {
        val file = File(context.cacheDir, "shared_quote.png")
        file.outputStream().use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        startActivity(Intent.createChooser(shareIntent, "Share Quote"))
    }
}
