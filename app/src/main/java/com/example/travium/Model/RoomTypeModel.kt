package com.example.travium.Model

data class RoomTypeModel(
val roomTypeId: String,
val name: String,
val description: String,
val price: Double,
val capacity: Int,
val totalRooms: Int,
val availableRooms: Int,
val amenities: List<String>
)
