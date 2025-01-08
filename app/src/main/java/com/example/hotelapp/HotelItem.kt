package com.example.hotelapp

class HotelItem(val id:Int,val name:String, val image:String, val title: String, val desc:String,   val rooms: List<RoomItem> =listOf()) {
    fun addRoom(room: RoomItem): HotelItem {
        return HotelItem(id, name, image, title, desc, rooms + room)
    }
}