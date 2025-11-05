package com.example.project5_randompokemon

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codepath.asynchttpclient.AsyncHttpClient
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler
import okhttp3.Headers
import org.json.JSONException
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var pokemonRecyclerView: RecyclerView
    private val pokemonList = mutableListOf<Pokemon>()
    private lateinit var refreshButton: Button
    private lateinit var adapter: PokemonAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        pokemonRecyclerView = findViewById(R.id.pokemon_recycler_view)
        refreshButton = findViewById(R.id.btnRefresh)

        adapter = PokemonAdapter(pokemonList)
        pokemonRecyclerView.adapter = adapter
        pokemonRecyclerView.layoutManager = LinearLayoutManager(this)

        refreshButton.setOnClickListener {
            refreshPokemonList()
        }

        // Load initial Pokémon
        refreshPokemonList()
    }

    private fun refreshPokemonList() {
        // Disable button during loading to prevent multiple rapid clicks
        refreshButton.isEnabled = false
        refreshButton.text = "Loading..."

        fetchMultiplePokemon(20) // Fetch 20 new random Pokémon
    }


    private fun fetchMultiplePokemon(count: Int) {
        val client = AsyncHttpClient()
        var completedRequests = 0

        // Clear the list first
        pokemonList.clear()

        // Notify adapter that data changed
        runOnUiThread {
            adapter.notifyDataSetChanged()
        }

        // Fetch multiple random Pokémon
        for (i in 1..count) {
            val randomPokemonId = Random.nextInt(1, 1026) // Current number of pokemon +1
            val apiUrl = "https://pokeapi.co/api/v2/pokemon/$randomPokemonId"

            client.get(apiUrl, object : JsonHttpResponseHandler() {
                override fun onSuccess(statusCode: Int, headers: Headers, json: JSON) {
                    try {
                        // Parse JSON response
                        val pokemonName = json.jsonObject.getString("name")
                        val pokemonId = json.jsonObject.getInt("id")

                        // 1/1000 chance for shiny
                        val isShiny = Random.nextInt(1000) == 0


                        val spriteUrl = if (isShiny) {
                            // Shiny sprite url
                            "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/shiny/$pokemonId.png"
                        } else {
                            json.jsonObject.getJSONObject("sprites")
                                .getString("front_default")
                        }

                        // Get types
                        val typesArray = json.jsonObject.getJSONArray("types")
                        val types = mutableListOf<String>()

                        for (i in 0 until typesArray.length()) {
                            val typeObject = typesArray.getJSONObject(i)
                            val typeName = typeObject.getJSONObject("type").getString("name")
                            types.add(typeName)
                        }

                        // Add Pokémon to list
                        synchronized(pokemonList) {
                            pokemonList.add(Pokemon(pokemonName, spriteUrl, types, pokemonId, isShiny))
                        }

                    } catch (e: JSONException) {
                        e.printStackTrace()
                    } finally {
                        completedRequests++
                        checkIfAllRequestsCompleted(count)
                    }
                }

                override fun onFailure(
                    statusCode: Int,
                    headers: Headers?,
                    response: String,
                    throwable: Throwable?
                ) {
                    completedRequests++
                    checkIfAllRequestsCompleted(count)
                    throwable?.printStackTrace()
                }
            })
        }
    }

    private fun checkIfAllRequestsCompleted(totalRequests: Int) {
        if (pokemonList.size == totalRequests) {
            // All requests completed, update RecyclerView
            runOnUiThread {
                // Sort by Pokémon ID / Dex number and update adapter
                val sortedList = pokemonList.sortedBy { it.id }
                pokemonList.clear()
                pokemonList.addAll(sortedList)
                adapter.notifyDataSetChanged()

                // Re-enable refresh button
                refreshButton.isEnabled = true
                refreshButton.text = "Refresh"
            }
        }
    }
    private fun setupRecyclerView() {
        // Sort by Dex number / ID
        val sortedList = pokemonList.sortedBy { it.id }

        val adapter = PokemonAdapter(sortedList)
        pokemonRecyclerView.adapter = adapter
        pokemonRecyclerView.layoutManager = LinearLayoutManager(this)


        pokemonRecyclerView.addItemDecoration(
            androidx.recyclerview.widget.DividerItemDecoration(
                this,
                LinearLayoutManager.VERTICAL
            )
        )
    }
}