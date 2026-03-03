package com.example.tutorial.com.example.tutorial.domain.model

object SymptomCatalog {
    data class Item(val id: String, val label: String)

    val items = listOf(
        Item("dizzy", "Dizziness"),
        Item("faint", "Near-fainting"),
        Item("brainfog", "Brain fog"),
        Item("palpit", "Palpitations"),
        Item("fatigue", "Fatigue"),
        Item("shaky", "Shaky"),
        Item("dyspnea", "Short breath"),
        Item("chest", "Chest pain"),
        Item("headache", "Headache"),
        Item("nausea", "Nausea")
    )

    val map by lazy { items.associateBy { it.id } }
}
