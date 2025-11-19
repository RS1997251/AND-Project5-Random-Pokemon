package com.example.project5_randompokemon

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*
import java.net.URL
import android.graphics.Bitmap
import android.graphics.BitmapFactory

class PokemonAdapter(private val pokemonList: List<Pokemon>) :
    RecyclerView.Adapter<PokemonAdapter.ViewHolder>() {

    // Pokémon type to color mapping
    private val typeColors = mapOf(
        "normal" to "#A8A878",    // Grayish brown
        "fire" to "#F08030",      // Orange
        "water" to "#6890F0",     // Blue
        "electric" to "#F8D030",  // Yellow
        "grass" to "#78C850",     // Green
        "ice" to "#98D8D8",       // Light blue
        "fighting" to "#C03028",  // Red
        "poison" to "#A040A0",    // Purple
        "ground" to "#E0C068",    // Tan
        "flying" to "#A890F0",    // Lavender
        "psychic" to "#F85888",   // Pink
        "bug" to "#A8B820",       // Olive green
        "rock" to "#B8A038",      // Gold
        "ghost" to "#705898",     // Dark purple
        "dragon" to "#7038F8",    // Royal blue
        "dark" to "#705848",      // Brown
        "steel" to "#B8B8D0",     // Silver
        "fairy" to "#EE99AC"      // Light pink
    )

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val pokemonName: TextView = view.findViewById(R.id.tvPokemonName)
        val pokemonSprite: ImageView = view.findViewById(R.id.ivPokemonSprite)
        val typesContainer: LinearLayout = view.findViewById(R.id.typesContainer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pokemon, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val pokemon = pokemonList[position]

        // Set Pokémon name with special formatting for shinies
        val nameText = if (pokemon.isShiny) {
            "✨ ${pokemon.name.replaceFirstChar { it.uppercase() }} ✨"
        } else {
            pokemon.name.replaceFirstChar { it.uppercase() }
        }
        holder.pokemonName.text = nameText

        // Set background based on shiny status
        if (pokemon.isShiny) {
            holder.itemView.setBackgroundResource(R.drawable.rounded_card_bg_shiny)
            holder.pokemonName.setTextColor(holder.itemView.context.getColor(android.R.color.black))
        } else {
            holder.itemView.setBackgroundResource(R.drawable.rounded_card_bg)
            holder.pokemonName.setTextColor(holder.itemView.context.getColor(android.R.color.white))
        }

        // Load sprite
        loadImageFromUrl(pokemon.spriteUrl, holder.pokemonSprite)

        // Clear previous types and add new ones
        holder.typesContainer.removeAllViews()
        pokemon.types.forEach { type ->
            val typeView = TextView(holder.itemView.context).apply {
                text = type.replaceFirstChar { it.uppercase() }
                // Adjust text color for shinies
                val textColor = if (pokemon.isShiny) {
                    holder.itemView.context.getColor(android.R.color.black)
                } else {
                    holder.itemView.context.getColor(android.R.color.white)
                }
                setTextColor(textColor)
                textSize = 12f
                setPadding(24, 12, 24, 12)

                // Set type color
                val typeColor = typeColors[type] ?: "#5F9EA0"
                background = createRoundedDrawable(typeColor)
            }

            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 8, 0)
            }

            holder.typesContainer.addView(typeView, layoutParams)
        }


        holder.itemView.setOnClickListener {
            if (pokemon.isShiny) {
                android.widget.Toast.makeText(
                    holder.itemView.context,
                    "✨ Shiny ${pokemon.name.replaceFirstChar { it.uppercase() }}! ✨",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun getItemCount() = pokemonList.size

    private fun loadImageFromUrl(imageUrl: String, imageView: ImageView) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL(imageUrl)
                val bitmap = BitmapFactory.decodeStream(url.openStream())

                withContext(Dispatchers.Main) {
                    imageView.setImageBitmap(bitmap)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun createRoundedDrawable(colorHex: String): android.graphics.drawable.GradientDrawable {
        val drawable = android.graphics.drawable.GradientDrawable()
        drawable.shape = android.graphics.drawable.GradientDrawable.RECTANGLE
        drawable.cornerRadius = 16f
        drawable.setColor(android.graphics.Color.parseColor(colorHex))
        return drawable
    }
}