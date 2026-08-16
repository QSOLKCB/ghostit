package com.osv01d.client.model

enum class Speaker { USER, HECTOR, SYSTEM, ERROR }
data class ChatMessage(val id: Long, val speaker: Speaker, val text: String)
