package com.example.hotelapp.Holder

import HotelItem
import com.example.hotelapp.classes.OrderItem
import com.example.hotelapp.classes.RoomItem

object HotelHolder {
    var currentHotel: HotelItem? = null
    var currentRoom: RoomItem? = null
    var roomList: MutableList<RoomItem> = mutableListOf()
    var orders: MutableList<OrderItem> = mutableListOf()
    var lastSelectedRoom: RoomItem? = null
}