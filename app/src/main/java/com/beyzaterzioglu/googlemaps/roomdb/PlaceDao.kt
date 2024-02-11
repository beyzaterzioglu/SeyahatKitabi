package com.beyzaterzioglu.googlemaps.roomdb

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.beyzaterzioglu.googlemaps.model.Place
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable

@Dao
interface PlaceDao {
    @Query("SELECT * FROM place")
    fun getAll(): Flowable<List<Place>> //Flowable JxJava'dan gelir. Geriye bir şey döneceksek kullanılır.
    @Insert
    fun insert(place: Place):Completable // Completable da RxJava'dan gelir geriye dönme işlemi olmadığı için tercih edilir.
    @Delete
    fun delete(place: Place):Completable
}