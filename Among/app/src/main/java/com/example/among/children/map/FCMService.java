package com.example.among.children.map;

import android.Manifest;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class FCMService extends FirebaseMessagingService {
    String[] permission_list = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };
    GoogleMap map;
    LocationManager locationManager;


    public FCMService() {
    }
    //토큰값이 새롭게 갱신되면 호출되는 메소드 - 토큰값을 가지고 있게 되는데 거의 변경되지 않는다.
    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        Log.d("myfcm","new token:"+s);
    }
    //firebase서버에서 메시지가 도착하면 자동으로 호출되는 메소드
    // 안드로이드 OS가 별도의 쓰레드를 발생시켜서 호출하게 된다.
    //Toast를 띄운다던가 이런 작업을 하려면 별도의 핸들러를 이용해서 작업을 해야 한다.
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        setMyLocation(location);
        //도착한 메시지가 있다면...
        if(remoteMessage.getNotification()!=null){
            //메시지를 추출한다. 메인쓰레드를 얻어와서 작업해야 한다.
            final String message = remoteMessage.getNotification().getBody();
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getBaseContext(),message,Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    //location정보를 지도에 셋팅하는 메소드
    public void setMyLocation(Location myLocation) {
        Log.d("myloc", "위도:" + myLocation.getLatitude());
        Log.d("myloc", "경도:" + myLocation.getLongitude());

        //내 위도경도
        LatLng myloc = new LatLng(myLocation.getLatitude(),
                myLocation.getLongitude());
        String LocationData = myloc.toString();
        Log.d("LocationData","위치정보");


    }



}
