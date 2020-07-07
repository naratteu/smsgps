package naratteu.smsgps

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.telephony.SmsManager
import android.telephony.SmsMessage
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.getSystemService
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices


class main : BroadcastReceiver() {
    override fun onReceive(p0: Context?, p1: Intent?) {
        Log.d("rec34", "onReceive call")
        Toast.makeText(p0, "문자왔어용", Toast.LENGTH_LONG).show()
        p1?.extras?.let { bundle ->
            var pdus : Array<Object> = bundle.get("pdus") as Array<Object>
            //var msgs : Array<SmsMessage> = Array(pdus.size)
            for (p in pdus)
            {
                var sm : SmsMessage = SmsMessage.createFromPdu(p as ByteArray)
                Log.d("rec34", "onReceive: " + sm)
                Log.d("rec34", "onReceive: " + sm.originatingAddress)
                Log.d("rec34", "onReceive: " + sm.messageBody)

                if(sm.messageBody.contains("어디야"))
                {
                    val locationManager = p0!!.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
                    if (ActivityCompat.checkSelfPermission(
                            p0!!,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                            p0!!,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        val loc : Location? = locationManager!!.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                        val lat : Double = loc!!.latitude;
                        val lon : Double = loc!!.longitude;
                        val d = "lat : $lat \nlon :  $lon"
                        sendSMS(sm.originatingAddress, d)//sm.messageBody)
                    } else {
                        val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(p0)
                        fusedLocationClient.lastLocation.addOnSuccessListener { location : Location? ->
                            Log.d("rec34", "onReceive: $location")//gps꺼져있으면  null됨.
                            // Got last known location. In some rare situations this can be null.

                            val loc : Location = (location as Location)
                            val lat : Double = loc.latitude;
                            val lon : Double = loc.longitude;
                            val d = "lat : $lat \nlon :  $lon"
                            sendSMS(sm.originatingAddress, d)//sm.messageBody)
                        }
                        Log.d("rec34", "onReceive: 퍼미션없음")

                    }
                }
            }
        }
    }

    private fun sendSMS(phoneNumber: String?, message: String) {
        Log.d("rec34", "sendSMS: $phoneNumber $message")
        SmsManager.getDefault().sendTextMessage(phoneNumber, null, message, null, null)
    }
}