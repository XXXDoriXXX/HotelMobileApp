package com.example.hotelapp.classes

import android.os.Parcel
import android.os.Parcelable
import com.google.android.material.datepicker.CalendarConstraints

class BusyDatesValidator(
    private val busyDates: List<Pair<Long, Long>> = listOf()
) : CalendarConstraints.DateValidator, Parcelable {

    override fun isValid(date: Long): Boolean {
        return busyDates.none { (start, end) ->
            date in start..end
        }
    }

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(busyDates.size)
        for ((start, end) in busyDates) {
            dest.writeLong(start)
            dest.writeLong(end)
        }
    }

    companion object CREATOR : Parcelable.Creator<BusyDatesValidator> {
        override fun createFromParcel(parcel: Parcel): BusyDatesValidator {
            val size = parcel.readInt()
            val busyDates = mutableListOf<Pair<Long, Long>>()
            repeat(size) {
                val start = parcel.readLong()
                val end = parcel.readLong()
                busyDates.add(Pair(start, end))
            }
            return BusyDatesValidator(busyDates)
        }

        override fun newArray(size: Int): Array<BusyDatesValidator?> {
            return arrayOfNulls(size)
        }
    }
}
