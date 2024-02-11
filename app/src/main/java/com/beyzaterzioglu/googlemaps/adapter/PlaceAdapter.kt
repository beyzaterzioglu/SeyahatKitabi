package com.beyzaterzioglu.googlemaps.adapter

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.Placeholder
import androidx.recyclerview.widget.RecyclerView
import com.beyzaterzioglu.googlemaps.databinding.RecyclerRowBinding
import com.beyzaterzioglu.googlemaps.model.Place
import com.beyzaterzioglu.googlemaps.view.MapsActivity

class PlaceAdapter(val placeList: List<Place>): RecyclerView.Adapter<PlaceAdapter.PlaceHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceHolder {
        val recyclerRowBinding: RecyclerRowBinding =
            RecyclerRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PlaceHolder(recyclerRowBinding)
    }


    override fun getItemCount(): Int {

        return placeList.size

    }

    override fun onBindViewHolder(holder: PlaceHolder, position: Int) {
        holder.recyclerRowBinding.recyclerViewTextView.text = placeList.get(position).name
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, MapsActivity::class.java)
            intent.putExtra("SelectedPlace", placeList.get(position))
            intent.putExtra("info", "old")//intent işlemi için yollama yapmamız lazım
            // ilk etapta PutExtra'da hata alırız.Çünkü burada bir sınıf yollarız Serialazible kelimesiyle çözeriz.
            holder.itemView.context.startActivity(intent)
        }

    }

    class PlaceHolder(val recyclerRowBinding: RecyclerRowBinding) :
        RecyclerView.ViewHolder(recyclerRowBinding.root) {

    }


}