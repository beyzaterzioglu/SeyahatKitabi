package com.beyzaterzioglu.googlemaps.view

import android.content.pm.PackageManager

import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.room.Room
import com.beyzaterzioglu.googlemaps.R


import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.beyzaterzioglu.googlemaps.databinding.ActivityMapsBinding
import com.beyzaterzioglu.googlemaps.model.Place
import com.beyzaterzioglu.googlemaps.roomdb.PlaceDao
import com.beyzaterzioglu.googlemaps.roomdb.PlaceDatabase
import com.google.android.material.snackbar.Snackbar
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

class MapsActivity : AppCompatActivity(), OnMapReadyCallback ,GoogleMap.OnMapLongClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var locationManager : LocationManager // bu bir konum yöneticisidir.
    private lateinit var locationListener: LocationListener
    private lateinit var permissionLauncher:ActivityResultLauncher<String>
    private lateinit var sharedPreferences: SharedPreferences
    private var trackBoolean : Boolean?=null
    private var selectedLatitude: Double? = null
    private var selectedLongitude: Double? = null
    private lateinit  var db : PlaceDatabase
    private lateinit  var placeDao: PlaceDao
    val compositeDisposable=CompositeDisposable() //İşlem yapıldıkça hafızada yer tutmaya başlanır. Bu işlemleri de
    //compositedisposable nesnesi içine koyarak "kullan at" yapıyoruz.
    var placeFromMain:Place?=null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)



        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        registerLauncher()


        selectedLatitude=0.0
        selectedLongitude=0.0
        binding.saveButton.isEnabled=false;
        sharedPreferences=this.getSharedPreferences("com.beyzaterzioglu.googlemaps", MODE_PRIVATE) // sadece benim tarafımdan kullanılıyor
        trackBoolean= false // uygulama açıldığında bu kısım fdalse olduğunda bir kerelik "onLocationChanged" çalıştır.
        db = Room.databaseBuilder(
            applicationContext,
            PlaceDatabase::class.java, "Places"
        )//.allowMainThreadQueries() // main threadde yapılacak işlemlere izin ver
            // küçük  verilerde işe yarasa da doğru bir işlem değildir. Doğrusu "RxJava"dır.
            .build()

        placeDao = db.placeDao()

    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMapLongClickListener(this) //bu kısım olmazsa haritayla listener arası bağlantıyı kuramıyoruz
        // Add a marker in Sydney and move the camera
        val intent = intent
        val info = intent.getStringExtra("info")

        if (info == "new") {
            // yeni bir şey ekleniyor

            // butonlardan delete görünmemeli yeni bir ekleme yapılıyorsa


            binding.deleteButton.visibility=View.GONE

            val sydney = LatLng(41.02199057592054, 28.98712203044344) // koordinatlar
            mMap.addMarker(
                MarkerOptions().position(sydney).title("Marker in Sydney")
            ) // kırmızı nokta
            mMap.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    sydney,
                    15f
                )
            ) // harita ilk açıldığında görünecek nokta

            // casting : ne seçeceğimizi bilmediğinden döndüğü şey tam olarak LocationManager olmaz.
            // bu sebeple de "as LocationManager" kısmını ekleyerek ne olarak kullanacğımızı belirtmemiz gerekir.
            locationManager = this.getSystemService(LOCATION_SERVICE) as LocationManager
            locationListener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    trackBoolean = sharedPreferences.getBoolean(
                        "trackBoolean",
                        false
                    ) // eğer böyle bir şey kayıtlı değilse ilk değeri false verecek
                    if (trackBoolean == false) {
                        // konum değişirse ne olacağını tanımladığımız alan
                        val UserLocation =
                            LatLng(
                                location.latitude,
                                location.longitude
                            ) // enlem ve boylam alıyoruz.
                        mMap.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                UserLocation,
                                15f
                            )

                        )// kullanıcı neredeyse kamera oradan başlayacak
                        sharedPreferences.edit().putBoolean("trackBoolean", true)
                            .apply() // bu kısımda da değer true olarak değiştirilerek bu kısmın tekrar çalışamsını engellemiş oluyoruz.
                    }
                }

            }
            //Erişim İzni Alma
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                ) {
                    // kullanıcıya izin istemekle ilgili bir mesajk yollamalısın
                    Snackbar.make(
                        binding.root,
                        "Perimission needed for location",
                        Snackbar.LENGTH_INDEFINITE
                    ).setAction("give permission")
                    {
                        //butona tıklanınca ne olacak
                        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)

                    }.show()
                } else { // yine izin istenmeli

                    permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            } else {
                // izin var
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    0,
                    0f,
                    locationListener
                )
                val lastLocation =
                    locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) // son konumu istiyor.
                if (lastLocation != null) {
                    // eğer daha önceden konum alınmadıysa burası boş olabileceği iğçin bu kontrolu yaparız.
                    val lastUserLocation =
                        LatLng(lastLocation.latitude, lastLocation.longitude) // çevirme işlemi
                    mMap.moveCamera((CameraUpdateFactory.newLatLngZoom(lastUserLocation, 1f)))
                }
                mMap.isMyLocationEnabled = true // bulunduğu konum mavi bir noktayla işaretleniyor.


            }

        } else {
            //gelen veriyi yükle
            mMap.clear()
            placeFromMain=intent.getSerializableExtra("SelectedPlace") as? Place

            placeFromMain?.let{
                // boş değilse yapılacak işlemler
                val latlng=LatLng(it.latitude,it.longitude)
                mMap.addMarker(MarkerOptions().position(latlng).title(it.name))
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng,15f))

                binding.placeText.setText(it.name)
                binding.saveButton.visibility=View.GONE
                binding.deleteButton.visibility=View.VISIBLE
            }

        }
    }
private fun registerLauncher(){
    permissionLauncher=registerForActivityResult(ActivityResultContracts.RequestPermission())
    {result->
        // izin verildi ya da verilmedi

        if(result){
            // izin verildi
            if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            {
                // izin verildiğinden yüzde yüz emin olmnak için
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0f,locationListener)
                val lastLocation=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) // son konumu istiyor.

                if(lastLocation != null)
                {
                    // eğer daha önceden konum alınmadıysa burası boş olabileceği iğçin bu kontrolu yaparız.
                    val lastUserLocation=LatLng(lastLocation.latitude,lastLocation.longitude) // çevirme işlemi
                    mMap.moveCamera((CameraUpdateFactory.newLatLngZoom(lastUserLocation,15f)))
                }
            }


        }
        else
        {
            //izin verilmedi
            Toast.makeText(this@MapsActivity,"Permission needed!",Toast.LENGTH_LONG).show()
        }
    }

}

    override fun onMapLongClick(p0: LatLng) { //uzun tıklanınca ne olacak
        // harita üzerinde uzun tıklamalrda işaretlemenin oluşması için
        // bu metodu yazıyoruz. mainde de ekstra ekleme yaptık
        // "OnMapLongClickListener diye
      mMap.clear() // daha önce yapılmış olan işaretlemyi siler.

        // p0 olarak verilen parametre nereye tıklanıdığı bilgisini verir.

        mMap.addMarker(MarkerOptions().position(p0)) // işaretleme ekleme
        selectedLatitude=p0.latitude // seçilen yerleri herhangi bir değişkene atamak
        selectedLongitude=p0.longitude

        binding.saveButton.isEnabled=true;
     }

    fun save(view : View){
        if(selectedLatitude!= null && selectedLongitude!=null)
        {
            val place= Place(binding.placeText.text.toString(),selectedLatitude!!,selectedLongitude!!)
            compositeDisposable.add( //Schedulers.io() io thread'e ulaşmak için kullanılan RxJavadan gelen bir sınıf
                placeDao.insert(place)
                    .subscribeOn(Schedulers.io()) //kullanılacak yer  AndroidSchedulers.mainThread()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::handleResponse)// işelm bittikten sonra ne olacağını burada söyleyeceğiz.
               // this::handleResponse refereans verdik
            )
        }

    }
    private fun handleResponse()
    {
        val intent=Intent(this,MainActivity2::class.java)

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) // açık olan tüm aktiviteleri kapat
        startActivity(intent)
    }
    fun delete(view : View){
        placeFromMain?.let {
            compositeDisposable.add(
                placeDao.delete(it)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::handleResponse)
            )
        }

    }

    override fun onDestroy() {
        super.onDestroy()
       compositeDisposable.clear()// kullan at işlemi
      // bu uygulamada küçük veriler olduğu için şart değil
    }
}
