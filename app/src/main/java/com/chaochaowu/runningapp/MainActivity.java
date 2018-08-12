package com.chaochaowu.runningapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.PolylineOptions;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Predicate;

/**
 * @author chaochaowu
 */
public class MainActivity extends AppCompatActivity implements AMap.OnMyLocationChangeListener {

    @BindView(R.id.tv_distance)
    TextView mTvDistance;
    @BindView(R.id.tv_speed)
    TextView mTvSpeed;
    @BindView(R.id.map)
    MapView mMapView;

    private Context mContext;
    /**
     * 当前定位与标记点的判断距离,小于这个距离说明已到达标记点
     */
    private static final int DISTANCE_TO_MARKER = 20;
    /**
     * 异常距离，如果超过这个距离，则说明移动距离异常,避免定位抖动造成的误差
     */
    private static final int DISTANCE_ERROR = 50;
    private AMap mAMap;
    private ArrayList<Marker> markers;
    private LatLng currentLatLng;
    private long totalDistance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mMapView.onCreate(savedInstanceState);
        mContext = this;
        //初始化地图
        initMap();
        //在地图上绘制标记点
        drawMarkers(Utils.getLatLngs());
        //设置位置变化的监听
        mAMap.setOnMyLocationChangeListener(this);
    }

    /**
     * 初始化地图参数
     */
    private void initMap() {
        mAMap = mMapView.getMap();
        MyLocationStyle myLocationStyle;
        myLocationStyle = new MyLocationStyle();
        myLocationStyle.showMyLocation(true);
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);
        myLocationStyle.interval(2000);
        mAMap.setMyLocationStyle(myLocationStyle);
        mAMap.moveCamera(CameraUpdateFactory.zoomTo(19));
        mAMap.setMyLocationEnabled(true);
        mAMap.getUiSettings().setMyLocationButtonEnabled(true);
        mAMap.getUiSettings().setCompassEnabled(true);
        mAMap.getUiSettings().setScaleControlsEnabled(true);
    }

    /**
     * 在地图上绘制标记点
     *
     * @param latLngs 标记点的经纬度
     */
    private void drawMarkers(ArrayList<LatLng> latLngs) {
        markers = new ArrayList<>();
        for (LatLng latLng : latLngs) {
            MarkerOptions markerOption = new MarkerOptions();
            markerOption.position(latLng)
                    .icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.not_arrived)));
            markers.add(mAMap.addMarker(markerOption));
        }
    }

    /**
     * 位置变化的监听操作
     *
     * @param location 位置变化后的位置
     */
    @Override
    public void onMyLocationChange(Location location) {
        //首次定位时设置当前经纬度
        if (currentLatLng == null) {
            currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        }
        LatLng lastLatLng = currentLatLng;
        currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        //计算当前定位与前一次定位的距离，如果距离异常或是距离为0,则不做任何操作
        float movedDistance = AMapUtils.calculateLineDistance(currentLatLng, lastLatLng);
        if (movedDistance > DISTANCE_ERROR || movedDistance == 0) {
            return;
        }
        //绘制移动路线
        mAMap.addPolyline(new PolylineOptions().add(lastLatLng, currentLatLng).width(10).color(Color.argb(255, 1, 1, 1)));
        totalDistance += movedDistance;
        //在界面上显示总里程和当前的速度
        displayInfo(totalDistance,location.getSpeed());
        //计算当前定位与各个标记点的距离，如果小于判定距离，则认为到达此标记点
        Observable.fromIterable(markers)
                .filter(new Predicate<Marker>() {
                    @Override
                    public boolean test(Marker marker) throws Exception {
                        return AMapUtils.calculateLineDistance(currentLatLng, marker.getPosition()) < DISTANCE_TO_MARKER;
                    }
                })
                .subscribe(new Consumer<Marker>() {
                    @Override
                    public void accept(Marker marker) throws Exception {
                        //到达标记点后，将标记点设为已到达的样式
                        marker.setIcon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.arrived)));
                    }
                });
    }

    /**
     * 在界面上显示总路程和当前的速度
     * @param totalDistance 总路程
     * @param speed 当前速度
     */
    @SuppressLint("DefaultLocale")
    private void displayInfo(long totalDistance, float speed) {
        mTvDistance.setText(String.format("总路程：%d m", totalDistance));
        mTvSpeed.setText(String.format("当前速度: %s m/s", speed));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

}
