package com.example.hotelapp

object HotelHolder {
    var currentHotel: HotelItem? = null
    var currentRoom: RoomItem? = null
    var roomList: MutableList<RoomItem> = mutableListOf()
}