package com.example.hotelapp.classes

import android.os.Parcel
import android.os.Parcelable
import com.google.android.material.datepicker.CalendarConstraints

class CompositeValidator(
    private val validators: List<CalendarConstraints.DateValidator>
) : CalendarConstraints.DateValidator, Parcelable {

    override fun isValid(date: Long): Boolean {
        return validators.all { it.isValid(date) }
    }

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {

    }

    companion object CREATOR : Parcelable.Creator<CompositeValidator> {
        override fun createFromParcel(parcel: Parcel): CompositeValidator {
            return CompositeValidator(listOf())
        }

        override fun newArray(size: Int): Array<CompositeValidator?> {
            return arrayOfNulls(size)
        }
    }
}