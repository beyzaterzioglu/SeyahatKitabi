package com.beyzaterzioglu.googlemaps.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity
class Place(
    @ColumnInfo(name="name")
    var name:String,

    @ColumnInfo(name="latitude")
    var latitude: Double,

    @ColumnInfo(name = "longitude")
    var longitude: Double) : Serializable { // Bu ifade sayesinde Adapterdeki
    //PutExtra kısmını kullanabiliriz.

    @PrimaryKey(autoGenerate = true)
    var id = 0
}