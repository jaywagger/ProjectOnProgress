package com.example.among.children.map;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;

import com.example.among.R;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;


public class LocationMap extends AppCompatActivity
        implements OnMapReadyCallback {
    Toolbar toolbar;
    String[] permission_list = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };
    GoogleMap map;
    LocationManager locationManager;

    // 구글 맵에 표시할 마커에 대한 옵션 설정
    MarkerOptions myLocationMarker;
    MarkerOptions friendMarker1;
    MarkerOptions friendMarker2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_map);

        //툴바
        toolbar = findViewById(R.id.toolbarMap);
        toolbar.setTitle("어머니 위치");
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);

        //뒤로가기
        ActionBar actionBar1 = getSupportActionBar();
        actionBar1.setHomeButtonEnabled(true);
        actionBar1.setDisplayHomeAsUpEnabled(true);

        //버전
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permission_list, 1000);
        } else {
            init();
        }
    }

    //뒤로가기 홈
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //권한 체크
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int result : grantResults) {
            if (result == PackageManager.PERMISSION_DENIED) {
                return;
            }
        }
        init();
    }

    //맵정보 추출
    public void init() {
        FragmentManager manager = getSupportFragmentManager();
        SupportMapFragment mapFragment = (SupportMapFragment) manager.findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    //지도가 준비되면 자동으로 호출되는 메소
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        if (map != null) {
            getMyLocation();
        }
    }

    //location을 추출 - 현재 나의 위치정보를 추출
    public void getMyLocation() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (String permission : permission_list) {
                if (checkSelfPermission(permission) ==
                        PackageManager.PERMISSION_DENIED) {
                    return;
                }
            }
        }
        //이전에 측정했었던 값을 가져오고 - 새롭게 측정하는데 시간이 많이 걸릴 수 있으므로
        Location gps_loc =
                locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        Location network_loc =
                locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (gps_loc != null) {
            setMyLocation((gps_loc));
        } else {
            if (network_loc != null) {
                setMyLocation(network_loc);
            }
        }
        Log.d("myloc", "===================================");

        //현재 측정한 값도 가져오고
        MyLocationListener locationListener =
                new MyLocationListener();
        //현재 활성화되어 있는 Provider를 체크
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    3000, 5, locationListener);
        }
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    3000, 5, locationListener);
        }

    }

    //location정보를 지도에 셋팅하는 메소드
    public void setMyLocation(Location myLocation) {
        Log.d("myloc", "위도:" + myLocation.getLatitude());
        Log.d("myloc", "경도:" + myLocation.getLongitude());

        //내 위도경도 - 카메라 중심
        LatLng myloc = new LatLng(myLocation.getLatitude(),
                myLocation.getLongitude());

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(myloc, 10);
        showMyLocationMarker(myLocation);
        showMyFriendLocationMarker(myLocation);

        //MapStyleOptions mapStyleOptions = new MapStyleOptions();

      /*  makerOptions.position(myloc);
        makerOptions.title("현재위치");
        makerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));*/


        //현재 위치 마커 표시
        //map.addMarker(makerOptions).showInfoWindow();

        //시작 카메라 위치는 현재 나의 위치로 설정함
        map.moveCamera(cameraUpdate);

        map.setMapStyle(MapStyleOptions.loadRawResourceStyle(
                this, R.raw.style_json));


        //현재 위치를 포인트로 표시하는 작업
        map.setMyLocationEnabled(true);
    }

    //현재 위치가 변경되거나 Provider에 변화가 있을때 반응할 수 있도록 설정
    class MyLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            //현재 위치가 변경되면 호출되는 메서드 (위도 & 경도) 3초에 한번씩 미세하게 변하는 중
            setMyLocation(location);
            //그만 움직이게 하려면. 리스너연결 해제
            //locationManager.removeUpdates(this);

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    }

    private void showMyLocationMarker(Location myLocation) {
        if (myLocationMarker == null) {
            myLocationMarker = new MarkerOptions();
            //현재 내위치
            myLocationMarker.position(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()));
            myLocationMarker.title("● 내 위치\n");
            myLocationMarker.snippet("● GPS로 확인한 위치");

            int height = 100;
            int width = 100;
            //myLocationMarker.icon(BitmapDescriptorFactory.fromResource(R.drawable.map));

            BitmapDrawable bitmapdraw = (BitmapDrawable)getResources().getDrawable(R.drawable.a1);
            Bitmap b = bitmapdraw.getBitmap();
            Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
            myLocationMarker.icon(BitmapDescriptorFactory.fromBitmap(smallMarker));
            map.addMarker(myLocationMarker);


        } else {
            myLocationMarker.position(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()));
        }
    }
    private void showMyFriendLocationMarker(Location myLocation) {
        if (friendMarker1 == null) {
            String msg = "● 짱아\n"
                    + "● 010-1234-1234";
            friendMarker1 = new MarkerOptions();
            friendMarker1.position(new LatLng(myLocation.getLatitude()-0.03, myLocation.getLongitude()+0.02));
            friendMarker1.title("● 위치\n");
            friendMarker1.snippet(msg);

            int height = 100;
            int width = 100;

            BitmapDrawable bitmapdraw = (BitmapDrawable)getResources().getDrawable(R.drawable.a5);
            Bitmap b = bitmapdraw.getBitmap();
            Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
            friendMarker1.icon(BitmapDescriptorFactory.fromBitmap(smallMarker));
            map.addMarker(friendMarker1);

        }
    }

}
