package com.example.project5_randompokemon

import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.codepath.asynchttpclient.AsyncHttpClient
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler
import okhttp3.Headers
import android.widget.ImageView
import android.widget.TextView
import kotlin.random.Random
import org.json.JSONException
import kotlinx.coroutines.*
import java.net.URL
import android.widget.LinearLayout


class MainActivity : AppCompatActivity() {

    private var tvPokemonName: TextView? = null
    private var ivPokemonSprite: ImageView? = null
    private var typesContainer: LinearLayout? = null
    private var btnGetPokemon: Button? = null
    private var tvStatus: TextView? = null

    private val typeColors = mapOf(
        "normal" to "#A8A878",
        "fire" to "#F08030",
        "water" to "#6890F0",
        "electric" to "#F8D030",
        "grass" to "#78C850",
        "ice" to "#98D8D8",
        "fighting" to "#C03028",
        "poison" to "#A040A0",
        "ground" to "#E0C068",
        "flying" to "#A890F0",
        "psychic" to "#F85888",
        "bug" to "#A8B820",
        "rock" to "#B8A038",
        "ghost" to "#705898",
        "dragon" to "#7038F8",
        "dark" to "#705848",
        "steel" to "#B8B8D0",
        "fairy" to "#EE99AC"
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvPokemonName = findViewById(R.id.tvPokemonName)
        ivPokemonSprite = findViewById(R.id.ivPokemonSprite)
        typesContainer = findViewById(R.id.typesContainer)
        btnGetPokemon = findViewById(R.id.btnGetPokemon)
        tvStatus = findViewById(R.id.tvStatus)

        btnGetPokemon?.setOnClickListener {
            getRandomPokemon()
        }

        getRandomPokemon()
    }

    private fun getRandomPokemon() {
        tvStatus?.text = "Loading Pokémon..."
        val randomPokemonId = Random.nextInt(1, 1000)
        val apiUrl = "https://pokeapi.co/api/v2/pokemon/$randomPokemonId"

        val client = AsyncHttpClient()

        client.get(apiUrl, object : JsonHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Headers, json: JSON) {
                try {

                    val pokemonName = json.jsonObject.getString("name")
                    val spriteUrl = json.jsonObject.getJSONObject("sprites")
                        .getString("front_default")


                    val typesArray = json.jsonObject.getJSONArray("types")
                    val types = mutableListOf<String>()

                    for (i in 0 until typesArray.length()) {
                        val typeObject = typesArray.getJSONObject(i)
                        val typeName = typeObject.getJSONObject("type").getString("name")
                        types.add(typeName)
                    }


                    runOnUiThread {
                        // Capitalize the first letter of the name
                        val formattedName = pokemonName.replaceFirstChar { it.uppercase() }

                        tvPokemonName?.text = formattedName

                        // Clear previous types
                        typesContainer?.removeAllViews()

                        // Add type boxes for each type with appropriate colors
                        types.forEach { type ->
                            val typeColor = typeColors[type] ?: "#5F9EA0" // Default color if type not found
                            val typeView = TextView(this@MainActivity).apply {
                                text = type.replaceFirstChar { it.uppercase() }
                                setTextColor(getColor(android.R.color.white))
                                textSize = 14f
                                setPadding(32, 16, 32, 16)
                                // Set background with the type color
                                setBackgroundColor(android.graphics.Color.parseColor(typeColor))
                            }

                            val layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            ).apply {
                                setMargins(0, 0, 16, 0)
                            }

                            typesContainer?.addView(typeView, layoutParams)
                        }

                        // Load image
                        if (spriteUrl != "null") {
                            loadImageFromUrl(spriteUrl)
                        } else {
                            ivPokemonSprite?.setImageBitmap(null)
                        }

                        tvStatus?.text = "Pokémon loaded successfully!"
                    }

                } catch (e: JSONException) {
                    e.printStackTrace()
                    runOnUiThread {
                        tvPokemonName?.text = "Error parsing data"
                        tvStatus?.text = "Error loading Pokémon data"
                    }
                }
            }

            override fun onFailure(
                statusCode: Int,
                headers: Headers?,
                response: String,
                throwable: Throwable?
            ) {
                runOnUiThread {
                    tvPokemonName?.text = "Failed to load Pokémon"
                    tvStatus?.text = "Network error. Try again."
                }
                throwable?.printStackTrace()
            }
        })
    }

    private fun loadImageFromUrl(imageUrl: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL(imageUrl)
                val bitmap = BitmapFactory.decodeStream(url.openStream())

                withContext(Dispatchers.Main) {
                    ivPokemonSprite?.setImageBitmap(bitmap)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        tvPokemonName = null
        ivPokemonSprite = null
        typesContainer = null
        btnGetPokemon = null
        tvStatus = null
    }
}