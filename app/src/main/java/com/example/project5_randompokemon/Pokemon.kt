package com.example.project5_randompokemon

data class Pokemon(
    val name: String,
    val spriteUrl: String,
    val types: List<String>,
    val id: Int,
    val isShiny: Boolean = false
)