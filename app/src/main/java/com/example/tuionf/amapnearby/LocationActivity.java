package com.example.tuionf.amapnearby;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class LocationActivity extends Activity {

    //声明AMapLocationClient类对象
    private AMapLocationClient locationClient = null;
    private TextView tvReult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
        tvReult = (TextView) findViewById(R.id.tvReult);

        initLocation();
        startLocation();
    }

    private void initLocation() {
        //1. 初始化  AMapLocationClient类对象
        locationClient = new AMapLocationClient(getApplicationContext());
        //2. 设置定位参数
        locationClient.setLocationOption(setLocationOption());
        //3. 设置定位监听回调
        locationClient.setLocationListener(aMapLocationListener);
    }

    private AMapLocationClientOption setLocationOption() {
        AMapLocationClientOption mLocationOption = new AMapLocationClientOption();
        //设置定位模式 可选的模式有高精度、仅设备、仅网络。默认为高精度模式
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        mLocationOption.setHttpTimeOut(30000);//可选，设置网络请求超时时间。默认为30秒。在仅设备模式下无效
        mLocationOption.setInterval(2000);//可选，设置定位间隔。默认为2秒
        mLocationOption.setNeedAddress(true);//可选，设置是否返回逆地理地址信息。默认是ture
        mLocationOption.setOnceLocation(false);//可选，设置是否单次定位。默认是false
        return mLocationOption;
    }

    private void startLocation() {
        locationClient.startLocation();
    }

    AMapLocationListener aMapLocationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation aMapLocation) {
            if (null != aMapLocation) {
                //解析定位结果

                StringBuffer sb = new StringBuffer();
                //errCode等于0代表定位成功，其他的为定位失败，具体的可以参照官网定位错误码说明
                if (aMapLocation.getErrorCode() == 0) {
                    sb.append("定位成功" + "\n");
                    sb.append("定位类型: " + aMapLocation.getLocationType() + "\n");
                    sb.append("经    度    : " + aMapLocation.getLongitude() + "\n");
                    sb.append("纬    度    : " + aMapLocation.getLatitude() + "\n");
                    sb.append("精    度    : " + aMapLocation.getAccuracy() + "米" + "\n");
                    sb.append("提供者    : " + aMapLocation.getProvider() + "\n");

                    if (aMapLocation.getProvider().equalsIgnoreCase(
                            android.location.LocationManager.GPS_PROVIDER)) {
                        // 以下信息只有提供者是GPS时才会有
                        sb.append("速    度    : " + aMapLocation.getSpeed() + "米/秒" + "\n");
                        sb.append("角    度    : " + aMapLocation.getBearing() + "\n");
                        // 获取当前提供定位服务的卫星个数
                        sb.append("星    数    : "
                                + aMapLocation.getSatellites() + "\n");
                    } else {
                        // 提供者是GPS时是没有以下信息的
                        sb.append("国    家    : " + aMapLocation.getCountry() + "\n");
                        sb.append("省            : " + aMapLocation.getProvince() + "\n");
                        sb.append("市            : " + aMapLocation.getCity() + "\n");
                        sb.append("城市编码 : " + aMapLocation.getCityCode() + "\n");
                        sb.append("区            : " + aMapLocation.getDistrict() + "\n");
                        sb.append("区域 码   : " + aMapLocation.getAdCode() + "\n");
                        sb.append("地    址    : " + aMapLocation.getAddress() + "\n");
                        sb.append("兴趣点    : " + aMapLocation.getPoiName() + "\n");
                        //定位完成的时间
                        sb.append("定位时间: " + formatUTC(aMapLocation.getTime(), "yyyy-MM-dd HH:mm:ss:sss") + "\n");
                    }

                    tvReult.setText(sb.toString());
                } else {
                    tvReult.setText("定位失败，loc is null");
                }
            }
        }
    };

    /**
     * 格式化时间
     * */
    private static SimpleDateFormat sdf = null;
    public synchronized static String formatUTC(long l, String strPattern) {
        if (TextUtils.isEmpty(strPattern)) {
            strPattern = "yyyy-MM-dd HH:mm:ss";
        }
        if (sdf == null) {
            try {
                sdf = new SimpleDateFormat(strPattern, Locale.CHINA);
            } catch (Throwable e) {
            }
        } else {
            sdf.applyPattern(strPattern);
        }
        if (l <= 0l) {
            l = System.currentTimeMillis();
        }
        return sdf == null ? "NULL" : sdf.format(l);
    }



}
