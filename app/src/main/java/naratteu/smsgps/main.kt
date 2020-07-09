package naratteu.smsgps

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.telephony.SmsManager
import android.telephony.SmsMessage
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.lang.Exception


class main : BroadcastReceiver() {
    val TAG = "sgLog"
    override fun onReceive(ctx: Context, b: Intent) {
        Log.d(TAG, "onReceive smsgps")
        val pdus = b.extras!!.get("pdus") as Array<Object>
        for (p in pdus)
            receive(ctx, SmsMessage.createFromPdu(p as ByteArray))
    }
    fun receive(ctx: Context, sm: SmsMessage) {
        Log.d(TAG, "onReceive: " + sm)
        Log.d(TAG, "onReceive: " + sm.originatingAddress)
        Log.d(TAG, "onReceive: " + sm.messageBody)

        if(sm.messageBody.contains("#마지막위치")) {
            //퍼미션이 둘중 하나라도 있을경우
            if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION  ) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ) {
                try {
                    val locationManager = ctx.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
                    val loc : Location? = locationManager!!.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    val lat : Double = loc!!.latitude;
                    val lon : Double = loc!!.longitude;
                    val d = "https://www.google.com/maps/dir//$lat,$lon/"//"lat : $lat \nlon :  $lon"
                    sendSMS(sm.originatingAddress, d)//sm.messageBody)
                } catch (e : Exception) {
                    val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(ctx)
                    //fusedLocationClient.requestLocationUpdates()//이거쓰면 정기적으로 수신받을수 있음.
                    fusedLocationClient.lastLocation.addOnSuccessListener { location : Location? ->
                        Log.d(TAG, "onReceive: $location")//gps꺼져있으면  null됨.
                        // Got last known location. In some rare situations this can be null.

                        val loc : Location = (location as Location)
                        val lat : Double = loc.latitude;
                        val lon : Double = loc.longitude;
                        val d = "https://www.google.com/maps/dir//$lat,$lon/"//"lat : $lat \nlon :  $lon"
                        sendSMS(sm.originatingAddress, d)//sm.messageBody)

                        //https://www.google.com/maps/dir//37.4810151,126.8829085/
                        //일케 보내
                    }
                }
            }
        }
    }

    fun sendSMS(phoneNumber: String?, message: String) {
        Log.d(TAG, "sendSMS: $phoneNumber $message")
        SmsManager.getDefault().sendTextMessage(phoneNumber, null, message, null, null)
    }
}