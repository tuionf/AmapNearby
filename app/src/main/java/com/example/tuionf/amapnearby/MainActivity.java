package com.example.tuionf.amapnearby;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.TextOptions;
import com.amap.api.maps.overlay.PoiOverlay;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.core.SuggestionCity;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements AMapLocationListener,PoiSearch.OnPoiSearchListener{


    private MapView mMapView;
    private AMap aMap;
    private Button setMapType;
    private Button searchPoi;
    private Button nearestLocationPoi;
    private UiSettings mUiSettings;//定义一个UiSettings对象
    private AMapLocationClient mLocationClient = null;
    private AMapLocationClientOption mLocationOption = null;
    private Marker locationMarker ;
    private TextView tvResult;
    private PoiSearch.Query query;
    private LatLng currentLatLng;
    private PoiSearch poiSearch;
    private LatLonPoint lp ;
    private PoiResult poiResult; // poi返回的结果
    private List<LatLng> latlngPoiLists;
    private ArrayList<LatLng> latlngLists;
    private LatLng latlngPoi;//poi的 点
    private LatLng[] latlngs = new LatLng[30];
    private static final String TAG = "MainActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mMapView = (MapView) findViewById(R.id.map);
        setMapType = (Button) findViewById(R.id.setMapType);
        tvResult = (TextView) findViewById(R.id.tvResult);
        searchPoi = (Button) findViewById(R.id.searchPoi);
        nearestLocationPoi = (Button) findViewById(R.id.nearestLocationPoi);
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，实现地图生命周期管理
        mMapView.onCreate(savedInstanceState);

        searchPoi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                poiSearch();
                Log.d(TAG, "onClick: 搜索");
            }
        });

        nearestLocationPoi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNearestLocation();
            }
        });


        init();

    }

    private void init() {
        aMap = mMapView.getMap();
        aMap.setMapType(AMap.MAP_TYPE_NORMAL);
        mUiSettings = aMap.getUiSettings();//实例化UiSettings类
        //显示实时路况图层
        aMap.setTrafficEnabled(true);
        startLocation();
    }

    private void startLocation(){
        //初始化定位
        mLocationClient = new AMapLocationClient(getApplicationContext());
        //初始化AMapLocationClientOption对象
        mLocationOption = new AMapLocationClientOption();
        //设置定位模式为高精度模式。
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //设置定位回调监听
        mLocationClient.setLocationListener(this);
        //获取一次定位结果
        mLocationOption.setOnceLocation(true);
        //设置是否返回地址信息（默认返回地址信息）
        mLocationOption.setNeedAddress(true);
        //给定位客户端对象设置定位参数
        mLocationClient.setLocationOption(mLocationOption);
        //启动定位
        mLocationClient.startLocation();
    }


    /*
    * AMapLocationListener 需要实现的方法
    * */
    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (aMapLocation != null){
            if (aMapLocation.getErrorCode() == 0){
                //取出经纬度
                currentLatLng = new LatLng(aMapLocation.getLatitude(),aMapLocation.getLongitude());
                if (locationMarker == null) {
                    MarkerOptions markerOptions =new MarkerOptions().position(currentLatLng).snippet("当前位置").draggable(true).setFlat(true);
                    locationMarker = aMap.addMarker(markerOptions);
                    locationMarker.showInfoWindow();
                    aMap.addText(new TextOptions().position(currentLatLng).text(aMapLocation.getAddress()));
                    //固定标签在屏幕中央
                    locationMarker.setPositionByPixels(mMapView.getWidth() / 2,mMapView.getHeight() / 2);
                } else {
                    //已经添加过了，修改位置即可
                    locationMarker.setPosition(currentLatLng);
                }

                //然后可以移动到定位点,使用animateCamera就有动画效果
                aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));//参数提示:1.经纬度 2.缩放级别
            }else {
                //定位失败时，可通过ErrCode（错误码）信息来确定失败的原因，errInfo是错误信息，详见错误码表。
                Log.e(TAG,"location Error, ErrCode:" +aMapLocation.getErrorCode() + ", errInfo:"+ aMapLocation.getErrorInfo());
            }
        }
    }


    /*
    * OnPoiSearchListener 需要实现的方法
    * */
    @Override
    public void onPoiItemSearched(PoiItem poiItem, int i) {

    }

    /*
    * OnPoiSearchListener 需要实现的方法
    * */
    @Override
    public void onPoiSearched(PoiResult result, int rCode) {
        Log.d(TAG, "onPoiSearched: rCode"+rCode);
        if (rCode == 1000) {
            if (result != null && result.getQuery() != null) {// 搜索poi的结果
                if (result.getQuery().equals(query)) {// 是否是同一条
                    poiResult = result;
                    // 取得搜索到的poiitems有多少页
                    List<PoiItem> poiItems = poiResult.getPois();// 取得第一页的poiitem数据，页数从数字0开始
                    List<SuggestionCity> suggestionCities = poiResult
                            .getSearchSuggestionCitys();// 当搜索不到poiitem数据时，会返回含有搜索关键字的城市信息

                    if (poiItems != null && poiItems.size() > 0) {
                        int size = poiItems.size();
                        Log.d(TAG, "onPoiSearched: "+size);
                        aMap.clear();// 清理之前的图标
                        PoiOverlay poiOverlay = new PoiOverlay(aMap, poiItems);
                        poiOverlay.removeFromMap();
                        poiOverlay.addToMap();
                        poiOverlay.zoomToSpan();

                        for (int i = 0;i < size;i++){
                            double latitudePoi = poiItems.get(i).getLatLonPoint().getLatitude();
                            double longitudePoi = poiItems.get(i).getLatLonPoint().getLongitude();
                            latlngPoi = new LatLng(latitudePoi,longitudePoi);
                            latlngs[i] = latlngPoi;
                            Log.d(TAG, "onPoiSearched: "+i+"+++"+latitudePoi+";;;"+longitudePoi+"==");
                        }
                        Log.d(TAG, "onPoiSearched: for走完了");
                    } else if (suggestionCities != null
                            && suggestionCities.size() > 0) {
//                        showSuggestCity(suggestionCities);
                    } else {
                        Toast.makeText(this,"无结果0",Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                Toast.makeText(this,"无结果1",Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this,"无结果11",Toast.LENGTH_SHORT).show();
        }
    }

    private void poiSearch(){
        Log.d(TAG, "poiSearch: 搜索");
        //keyWord表示搜索字符串，
        //第二个参数表示POI搜索类型，二者选填其一,第三个参数是城市code
        String keyWord = "中原银行";
        query = new PoiSearch.Query(keyWord,"","");
        // 设置每页最多返回多少条poiitem
        query.setPageSize(20);
        // 设置查第一页，从0开始计数
        query.setPageNum(0);

        lp = new LatLonPoint(currentLatLng.latitude, currentLatLng.longitude);

        if (lp != null){
            Log.d(TAG, "poiSearch: 搜索----------");
            //构造 PoiSearch 对象，并设置监听。
            poiSearch = new PoiSearch(this,query);
            poiSearch.setOnPoiSearchListener(this);

            //设置周边搜索的中心点以及半径
              poiSearch.setBound(new PoiSearch.SearchBound(lp, 15000, true));
            //调用 PoiSearch 的 searchPOIAsyn() 方法发送请求。
            poiSearch.searchPOIAsyn();
        }
    }

    private void getNearestLocation(){
//        latlngPoiLists.get(0).latitude
        int size = latlngs.length;
        float [] distances = new float[size];
        for (int i = 0; i < size; i++) {
            float distance = AMapUtils.calculateLineDistance(currentLatLng,latlngs[i]);
            distances[i] = distance;
            Log.d(TAG, "getNearestLocation: 距离"+ distances[i]);
        }

//        Log.d(TAG, "getNearestLocation: 最近距离"+ distance);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();

    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，实现地图生命周期管理
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，实现地图生命周期管理
        mMapView.onPause();
    }

}
