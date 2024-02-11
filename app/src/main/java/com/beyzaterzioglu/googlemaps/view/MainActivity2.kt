package com.beyzaterzioglu.googlemaps.view

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import androidx.viewbinding.ViewBinding
import com.beyzaterzioglu.googlemaps.R
import com.beyzaterzioglu.googlemaps.adapter.PlaceAdapter
import com.beyzaterzioglu.googlemaps.databinding.ActivityMain2Binding

import com.beyzaterzioglu.googlemaps.model.Place
import com.beyzaterzioglu.googlemaps.roomdb.PlaceDatabase
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

class MainActivity2 : AppCompatActivity() {
    private lateinit var binding: ActivityMain2Binding
    private val compositeDisposable=CompositeDisposable()
    private lateinit var places: ArrayList<Place>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityMain2Binding.inflate(layoutInflater)
        val view=binding.root
        setContentView(view)

        places = ArrayList()
        val db= Room.databaseBuilder(applicationContext,PlaceDatabase::class.java,"Places").build()
        val placeDao=db.placeDao()

        compositeDisposable.add(
            placeDao.getAll()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleResponse)
        )
      //  supportActionBar?.setDisplayHomeAsUpEnabled(true)


    }
private fun handleResponse(placeList: List<Place>)
{
  binding.recyclerView.layoutManager= LinearLayoutManager(this)
    val placeAdapter= PlaceAdapter(placeList)
    binding.recyclerView.adapter=placeAdapter
}
    override fun onCreateOptionsMenu(menu: Menu?): Boolean { //oluşturulan menu ana aktiviteye bağlanıyor
        val menuInflater=menuInflater //xml ile kod bağlanırken kullanılır "inflater"
        menuInflater.inflate(R.menu.place_menu,menu) //hangi menüyü gösterecek
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean { // menuden herhangi bir eleman seçilirse ne olacak
       if(item.itemId== R.id.add_place)
       {
        val intent=Intent(this, MapsActivity::class.java) // buradan mapsactivty gidilecek
           intent.putExtra("info","new")
        startActivity(intent)
       }
           return super.onOptionsItemSelected(item)
    }

}


