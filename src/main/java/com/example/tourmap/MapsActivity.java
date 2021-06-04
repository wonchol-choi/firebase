package com.example.tourmap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.pedro.library.AutoPermissions;
import com.pedro.library.AutoPermissionsListener;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, AutoPermissionsListener {
    Spinner spTour;
    private GoogleMap mMap;
    Double tourLatLng[]=new Double[2]; //관광지의 위도, 경도 값을 가지는 배열
    Double myLatLng[]=new Double[2];  //내 위치의 위도, 경도 값을 가지는 배열
    String seoulTour[]={"국립중앙박물관","남산골한옥마을","예술의전당",
            "청계천","63빌딩","남산타워","경복궁","김치문화체험관",
            "서울올림픽기념관","국립민속박물관","서대문형무소역사관","창덕궁"};
    Double lat[]={37.5240867,37.5591447,37.4785361,37.5696512,37.5198158,
            37.5511147,37.5788408,37.5629457,37.5202976,37.5815645,37.5742887,37.5826041};
    Double lng[]={126.9803881,126.9936826,127.0107423,127.0056375,126.9403139,126.9878596,
            126.9770162,126.9851652,127.1159236,126.9789313,126.9562269,126.9919376};
    static final int NORMAL=1, HYBRID=2, MYPOSITION=3;
    int pos;
    LocationManager locationManager;
    boolean myCheck=true, myPosCheck=false;
    Marker marker;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setTitle("서울관광안내");
        AutoPermissions.Companion.loadAllPermissions(this,101);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        spTour=findViewById(R.id.spTour);
        mapFragment.getMapAsync(this);
        ArrayAdapter<String> adapter=new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item,seoulTour);
        spTour.setAdapter(adapter);
        if(myCheck==true){
            setMyposition();
        }
        spTour.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                tourLatLng[0]=lat[position];
                tourLatLng[1]=lng[position];
                pos=position;
                myPosCheck=false;
                tourMoveMap(tourLatLng);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }//onCreate메서드 끝~~

    //내 위치 정보 세팅
    public void setMyposition() {
        locationManager=(LocationManager)getSystemService(Context.LOCATION_SERVICE);
        try {
            Location location=locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
            if(location != null) {
                myLatLng[0]=location.getLatitude();
                myLatLng[1]=location.getLongitude();
                if(myPosCheck==true) {
                    tourMoveMap(myLatLng);
                }
            }else {
                showToast("내 위치 찾는 중....");
            }
            GPSListener gpsListener=new GPSListener();
            locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER,
                    10000,1,gpsListener);
        }catch (SecurityException e) {
            showToast("내 위치를 찾을 수 없습니다.");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        AutoPermissions.Companion.parsePermissions(this,requestCode,permissions,this);
        if(grantResults[0] == PackageManager.PERMISSION_DENIED){
            showToast("내 위치 접근을 거부했습니다.");
            myCheck=false;
        }else {
            myCheck=true;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, NORMAL,0,"일반지도");
        menu.add(0, HYBRID,0,"위성지도");
        menu.add(0, MYPOSITION,0,"내위치보기");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case NORMAL:
                //일반지도
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
            case HYBRID:
                //위성지도
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                break;
            case MYPOSITION:
                //내위치보기
                myPosCheck=true;
                setMyposition();
                break;
        }
        return false;
    }

    //토스트 메서드
    void showToast(String msg) {
        Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT).show();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
       /* LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));*/
    }

    //관광지 지도 메서드
    public void tourMoveMap(Double locationLatLng[]) {
        String address[]={"서울특별시 용산구 서빙고로 137 국립중앙박물관",
                "서울특별시 중구 퇴계로34길 28 남산한옥마을",
                "서울특별시 서초구 남부순환로 2364 국립국악원",
                "서울특별시 종로구 창신동",
                "서울특별시 영등포구 63로 50 한화금융센터_63",
                "서울특별시 용산구 남산공원길 105 N서울타워",
                "서울특별시 종로구 삼청로 37 국립민속박물관",
                "서울특별시 중구 명동2가 32-2",
                "서울특별시 송파구 올림픽로 448 서울올림픽파크텔",
                "서울특별시 종로구 삼청로 37 국립민속박물관",
                "서울특별시 서대문구 통일로 251 독립공원",
                "서울특별시 종로구 율곡로 99"};
        String tel[]={"02-2077-9000","02-2264-4412","02-580-1300","02-2290-6114",
                "02-789-5663","02-3455-9277","02-3700-3900","02-318-7051",
                "02-410-1354","02-3704-3114","02-360-8590","02-762-8261"};
        String tourSite[]={"http://www.museum.go.kr",
                "http://hanokmaeul.seoul.go.kr",
                "http://www.sac.or.kr",
                "http://www.cheonggyecheon.or.kr",
                "http://www.63.co.kr",
                "http://www.nseoultower.com",
                "http://www.royalpalace.go.kr",
                "http://www.visitseoul.net/kr/article/article.do?_method=view&art_id=49160&lang=kr&m=0004003002009&p=03",
                "http://www.88olympic.or.kr",
                "http://www.nfm.go.kr",
                "http://www.sscmc.or.kr/culture2",
                "http://www.cdg.go.kr"};
        LatLng findLatLng = new LatLng(locationLatLng[0], locationLatLng[1]);
        /*if(marker!=null) {
            marker.remove();
        }*/
        MarkerOptions myMarker=new MarkerOptions();
        myMarker.position(findLatLng);
        if(myPosCheck==true){
            myMarker.title("내가 서 있는 곳");
            myMarker.snippet("위도 : " + myLatLng[0]+" , 경도 : " + myLatLng[1]);
        }else {
            myMarker.title(address[pos]);
            myMarker.snippet(tel[pos]);
        }
        myMarker.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker));
        marker=mMap.addMarker(myMarker);
        marker.showInfoWindow();
        //mMap.addMarker(new MarkerOptions().position(findLatLng).title("찾은 관광지 위치"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(findLatLng,15));
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {
                if(myPosCheck==false) {
                    Uri uri = Uri.parse(tourSite[pos]);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                }
                return false;
            }
        });
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(@NonNull Marker marker) {
                if(myPosCheck==false) {
                    Uri uri = Uri.parse("tel:" + tel[pos]);
                    Intent intent = new Intent(Intent.ACTION_DIAL, uri);
                    startActivity(intent);
                }
            }
        });
    }

    @Override
    public void onDenied(int i, String[] strings) {

    }

    @Override
    public void onGranted(int i, String[] strings) {

    }
    //내 위치를 찾기 위한 GPS 클래스
    public class GPSListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) { //내 위치가 바뀔때 마다 수행
            myLatLng[0]=location.getLatitude();
            myLatLng[1]=location.getLongitude();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            switch (status) {
                case LocationProvider.OUT_OF_SERVICE:
                    showToast("GPS사용 불가능");
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    showToast("GPS일시중지");
                    break;
                case LocationProvider.AVAILABLE:
                    showToast("GPS서비스 사용 가능 상태");
                    break;
            }
        }

        @Override
        public void onProviderEnabled(String provider) {
            showToast("현재 GPS서비스 사용 가능 상태");
        }

        @Override
        public void onProviderDisabled(String provider) {
            showToast("현재 GPS서비스 사용 불가능 상태");
        }
    }
}