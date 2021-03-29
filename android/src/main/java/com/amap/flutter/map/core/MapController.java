package com.amap.flutter.map.core;

import android.graphics.Bitmap;
import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.TextureMapView;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.CustomMapStyleOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.Poi;
import com.amap.api.maps.model.VisibleRegion;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeAddress;
import com.amap.api.services.geocoder.GeocodeQuery;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeAddress;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.flutter.map.MyMethodCallHandler;
import com.amap.flutter.map.utils.Const;
import com.amap.flutter.map.utils.ConvertUtil;
import com.amap.flutter.map.utils.LogUtil;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;

/**
 * @author whm
 * @date 2020/11/11 7:00 PM
 * @mail hongming.whm@alibaba-inc.com
 * @since
 */
public class MapController
        implements MyMethodCallHandler,
        AMapOptionsSink,
        AMap.OnMapLoadedListener,
        AMap.OnMyLocationChangeListener,
        AMap.OnCameraChangeListener,
        AMap.OnMapClickListener,
        AMap.OnMapLongClickListener,
        AMap.OnPOIClickListener,
        GeocodeSearch.OnGeocodeSearchListener {
    private final MethodChannel methodChannel;
    private final AMap amap;
    private final TextureMapView mapView;
    private MethodChannel.Result mapReadyResult;

    private static final String CLASS_NAME = "MapController";

    private boolean mapLoaded = false;

    private MethodChannel.Result geocoderResult;
    private GeocodeSearch geocoderSearch;
    private MethodChannel.Result reGeocoderResult;

    public MapController(MethodChannel methodChannel, TextureMapView mapView) {
        this.methodChannel = methodChannel;
        this.mapView = mapView;
        amap = mapView.getMap();

        amap.addOnMapLoadedListener(this);
        amap.addOnMyLocationChangeListener(this);
        amap.addOnCameraChangeListener(this);
        amap.addOnMapLongClickListener(this);
        amap.addOnMapClickListener(this);
        amap.addOnPOIClickListener(this);

        geocoderSearch = new GeocodeSearch(mapView.getContext());
        geocoderSearch.setOnGeocodeSearchListener(this);
    }

    @Override
    public String[] getRegisterMethodIdArray() {
        return Const.METHOD_ID_LIST_FOR_MAP;
    }


    @Override
    public void doMethodCall(@NonNull MethodCall call, @NonNull MethodChannel.Result result) {
        LogUtil.i(CLASS_NAME, "doMethodCall===>" + call.method);
        if (null == amap) {
            LogUtil.w(CLASS_NAME, "onMethodCall amap is null!!!");
            return;
        }
        switch (call.method) {
            case Const.METHOD_MAP_WAIT_FOR_MAP:
                if (mapLoaded) {
                    result.success(null);
                    return;
                }
                mapReadyResult = result;
                break;
            case Const.METHOD_MAP_SATELLITE_IMAGE_APPROVAL_NUMBER:
                if (null != amap) {
                    result.success(amap.getSatelliteImageApprovalNumber());
                }
                break;
            case Const.METHOD_MAP_CONTENT_APPROVAL_NUMBER:
                if (null != amap) {
                    result.success(amap.getMapContentApprovalNumber());
                }
                break;
            case Const.METHOD_MAP_VISIABLE_MAPBOUNDS:
                if (null != amap) {
                    VisibleRegion visibleRegion = amap.getProjection().getVisibleRegion();
                    LatLngBounds latLngBounds = visibleRegion.latLngBounds; //由可视区域的四个顶点形成的经纬度范围
                    LatLng southwest = latLngBounds.southwest; //西南角坐标
                    LatLng northeast = latLngBounds.northeast; //东北角坐标
                    Map<String, Object> arguments = new HashMap<>(2);
                    arguments.put("southwest", ConvertUtil.latLngToList(southwest));
                    arguments.put("northeast", ConvertUtil.latLngToList(northeast));//
                    result.success(arguments);
                }
                break;
            case Const.METHOD_MAP_UPDATE:
                if (amap != null) {
                    ConvertUtil.interpretAMapOptions(call.argument("options"), this);
                    result.success(ConvertUtil.cameraPositionToMap(getCameraPosition()));
                }
                break;
            case Const.METHOD_MAP_MOVE_CAMERA:
                if (null != amap) {
                    final CameraUpdate cameraUpdate = ConvertUtil.toCameraUpdate(call.argument("cameraUpdate"));
                    final Object animatedObject = call.argument("animated");
                    final Object durationObject = call.argument("duration");

                    moveCamera(cameraUpdate, animatedObject, durationObject);
                }
                break;
            case Const.METHOD_MAP_SET_RENDER_FPS:
                if (null != amap) {
                    amap.setRenderFps((Integer) call.argument("fps"));
                    result.success(null);
                }
                break;
            case Const.METHOD_MAP_TAKE_SNAPSHOT:
                if (amap != null) {
                    final MethodChannel.Result _result = result;
                    amap.getMapScreenShot(new AMap.OnMapScreenShotListener() {
                        @Override
                        public void onMapScreenShot(Bitmap bitmap) {
                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                            byte[] byteArray = stream.toByteArray();
                            bitmap.recycle();
                            _result.success(byteArray);
                        }

                        @Override
                        public void onMapScreenShot(Bitmap bitmap, int i) {

                        }
                    });
                }
                break;
            case Const.METHOD_MAP_CLEAR_DISK:
                if (null != amap) {
                    amap.removecache();
                    result.success(null);
                }
                break;
            case Const.METHOD_SEARCH_GOE_CODE: {
                String address = call.argument("address");
                String city = call.argument("city");
                geocoderResult = result;
                GeocodeQuery query = new GeocodeQuery(address, city);// 第一个参数表示地址，第二个参数表示查询城市，中文或者中文全拼，citycode、adcode，
                geocoderSearch.getFromLocationNameAsyn(query);// 设置同步地理编码请求
            }
            break;

            case Const.METHOD_SEARCH_REGOE_CODE:
                List<Double> latLng = (List<Double>) call.arguments;

                double lat = latLng.get(0);
                double lng = latLng.get(1);

                reGeocoderResult = result;
                LatLonPoint latLonPoint = new LatLonPoint(lat, lng);
                RegeocodeQuery query = new RegeocodeQuery(latLonPoint, 200,
                        GeocodeSearch.AMAP);// 第一个参数表示一个Latlng，第二参数表示范围多少米，第三个参数表示是火系坐标系还是GPS原生坐标系
                geocoderSearch.getFromLocationAsyn(query);
                break;
            default:
                LogUtil.w(CLASS_NAME, "onMethodCall not find methodId:" + call.method);
                break;
        }

    }

    @Override
    public void onMapLoaded() {
        LogUtil.i(CLASS_NAME, "onMapLoaded==>");
        try {
            mapLoaded = true;
            if (null != mapReadyResult) {
                mapReadyResult.success(null);
                mapReadyResult = null;
            }
        } catch (Throwable e) {
            LogUtil.e(CLASS_NAME, "onMapLoaded", e);
        }
    }

    @Override
    public void setCamera(CameraPosition camera) {
        amap.moveCamera(CameraUpdateFactory.newCameraPosition(camera));
    }

    @Override
    public void setMapType(int mapType) {
        amap.setMapType(mapType);
    }

    @Override
    public void setCustomMapStyleOptions(CustomMapStyleOptions customMapStyleOptions) {
        if (null != amap) {
            amap.setCustomMapStyle(customMapStyleOptions);
        }
    }

    private boolean myLocationShowing = false;

    @Override
    public void setMyLocationStyle(MyLocationStyle myLocationStyle) {
        if (null != amap) {
            myLocationShowing = myLocationStyle.isMyLocationShowing();
            amap.setMyLocationEnabled(myLocationShowing);
            amap.setMyLocationStyle(myLocationStyle);
        }
    }

    @Override
    public void setScreenAnchor(float x, float y) {
        amap.setPointToCenter(Float.valueOf(mapView.getWidth() * x).intValue(), Float.valueOf(mapView.getHeight() * y).intValue());
    }

    @Override
    public void setMinZoomLevel(float minZoomLevel) {
        amap.setMinZoomLevel(minZoomLevel);
    }

    @Override
    public void setMaxZoomLevel(float maxZoomLevel) {
        amap.setMaxZoomLevel(maxZoomLevel);
    }

    @Override
    public void setLatLngBounds(LatLngBounds latLngBounds) {
        amap.setMapStatusLimits(latLngBounds);
    }

    @Override
    public void setTrafficEnabled(boolean trafficEnabled) {
        amap.setTrafficEnabled(trafficEnabled);
    }

    @Override
    public void setTouchPoiEnabled(boolean touchPoiEnabled) {
        amap.setTouchPoiEnable(touchPoiEnabled);
    }

    @Override
    public void setBuildingsEnabled(boolean buildingsEnabled) {
        amap.showBuildings(buildingsEnabled);
    }

    @Override
    public void setLabelsEnabled(boolean labelsEnabled) {
        amap.showMapText(labelsEnabled);
    }

    @Override
    public void setCompassEnabled(boolean compassEnabled) {
        amap.getUiSettings().setCompassEnabled(compassEnabled);
    }

    @Override
    public void setScaleEnabled(boolean scaleEnabled) {
        amap.getUiSettings().setScaleControlsEnabled(scaleEnabled);
    }

    @Override
    public void setZoomGesturesEnabled(boolean zoomGesturesEnabled) {
        amap.getUiSettings().setZoomGesturesEnabled(zoomGesturesEnabled);
    }

    @Override
    public void setScrollGesturesEnabled(boolean scrollGesturesEnabled) {
        amap.getUiSettings().setScrollGesturesEnabled(scrollGesturesEnabled);
    }

    @Override
    public void setRotateGesturesEnabled(boolean rotateGesturesEnabled) {
        amap.getUiSettings().setRotateGesturesEnabled(rotateGesturesEnabled);
    }

    @Override
    public void setTiltGesturesEnabled(boolean tiltGesturesEnabled) {
        amap.getUiSettings().setTiltGesturesEnabled(tiltGesturesEnabled);
    }

    private CameraPosition getCameraPosition() {
        if (null != amap) {
            return amap.getCameraPosition();
        }
        return null;
    }

    @Override
    public void onMyLocationChange(Location location) {
        if (null != methodChannel && myLocationShowing) {
            final Map<String, Object> arguments = new HashMap<String, Object>(2);
            arguments.put("location", ConvertUtil.location2Map(location));
            methodChannel.invokeMethod("location#changed", arguments);
            LogUtil.i(CLASS_NAME, "onMyLocationChange===>" + arguments);
        }
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        if (null != methodChannel) {
            final Map<String, Object> arguments = new HashMap<String, Object>(2);
            arguments.put("position", ConvertUtil.cameraPositionToMap(cameraPosition));
            methodChannel.invokeMethod("camera#onMove", arguments);
            LogUtil.i(CLASS_NAME, "onCameraChange===>" + arguments);
        }
    }

    @Override
    public void onCameraChangeFinish(CameraPosition cameraPosition) {
        if (null != methodChannel) {
            final Map<String, Object> arguments = new HashMap<String, Object>(2);
            arguments.put("position", ConvertUtil.cameraPositionToMap(cameraPosition));
            methodChannel.invokeMethod("camera#onMoveEnd", arguments);
            LogUtil.i(CLASS_NAME, "onCameraChangeFinish===>" + arguments);

            VisibleRegion visibleRegion = amap.getProjection().getVisibleRegion();
            LatLngBounds latLngBounds = visibleRegion.latLngBounds; //由可视区域的四个顶点形成的经纬度范围
            LatLng southwest = latLngBounds.southwest; //西南角坐标
            LatLng northeast = latLngBounds.northeast; //东北角坐标
            Map<String, Object> arguments2 = new HashMap<>(2);
            arguments2.put("southwest", ConvertUtil.latLngToList(southwest));
            arguments2.put("northeast", ConvertUtil.latLngToList(northeast));//
            final Map<String, Object> mapData = new HashMap<>(1);
            mapData.put("region", arguments2);
            System.out.println(southwest.latitude + " " + southwest.longitude);
            methodChannel.invokeMethod("camera#onVisiableRegionMoveEnd", mapData);
            LogUtil.i(CLASS_NAME, "onVisiableRegionMoveEnd==>" + mapData.get("region"));

        }
    }


    @Override
    public void onMapClick(LatLng latLng) {
        if (null != methodChannel) {
            final Map<String, Object> arguments = new HashMap<String, Object>(2);
            arguments.put("latLng", ConvertUtil.latLngToList(latLng));
            methodChannel.invokeMethod("map#onTap", arguments);
            LogUtil.i(CLASS_NAME, "onMapClick===>" + arguments);
        }
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        if (null != methodChannel) {
            final Map<String, Object> arguments = new HashMap<String, Object>(2);
            arguments.put("latLng", ConvertUtil.latLngToList(latLng));
            methodChannel.invokeMethod("map#onLongPress", arguments);
            LogUtil.i(CLASS_NAME, "onMapLongClick===>" + arguments);
        }
    }

    @Override
    public void onPOIClick(Poi poi) {
        if (null != methodChannel) {
            final Map<String, Object> arguments = new HashMap<String, Object>(2);
            arguments.put("poi", ConvertUtil.poiToMap(poi));
            methodChannel.invokeMethod("map#onPoiTouched", arguments);
            LogUtil.i(CLASS_NAME, "onPOIClick===>" + arguments);
        }
    }

    private void moveCamera(CameraUpdate cameraUpdate, Object animatedObject, Object durationObject) {
        boolean animated = false;
        long duration = 250;
        if (null != animatedObject) {
            animated = (Boolean) animatedObject;
        }
        if (null != durationObject) {
            duration = ((Number) durationObject).intValue();
        }
        if (null != amap) {
            if (animated) {
                amap.animateCamera(cameraUpdate, duration, null);
            } else {
                amap.moveCamera(cameraUpdate);
            }
        }
    }

    @Override
    public void setInitialMarkers(Object initialMarkers) {
        //不实现
    }

    @Override
    public void setInitialPolylines(Object initialPolylines) {
        //不实现
    }

    @Override
    public void setInitialPolygons(Object polygonsObject) {
        //不实现
    }

    @Override
    public void onRegeocodeSearched(RegeocodeResult result, int rCode) {
        if (rCode == AMapException.CODE_AMAP_SUCCESS) {
            if (result != null && result.getRegeocodeAddress() != null
                    && result.getRegeocodeAddress().getFormatAddress() != null) {
                RegeocodeAddress regeocodeAddress = result.getRegeocodeAddress();
                String addressName = regeocodeAddress.getFormatAddress();

                Log.w("xxxx", "addressName= " + addressName);


//                Map<String, Object> r = new Map();
//                reGeocoderResult.success(r);
//                aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
//                        AMapUtil.convertToLatLng(latLonPoint), 15));
                //regeoMarker.setPosition(AMapUtil.convertToLatLng(latLonPoint));
                //ToastUtil.show(ReGeocoderActivity.this, addressName);
//                final Map<String, Object> arguments = new HashMap<String, Object>();
//                arguments.put("country", regeocodeAddress.getCountry());
//                arguments.put("countryCode", regeocodeAddress.getCountryCode());
//
//                arguments.put("province", regeocodeAddress.getProvince());
//
//                arguments.put("city", regeocodeAddress.getCity());
//                arguments.put("cityCode", regeocodeAddress.getCityCode());
//
//                arguments.put("district", regeocodeAddress.getDistrict());
//                arguments.put("adCode", regeocodeAddress.getAdCode());
//
//                arguments.put("address", regeocodeAddress.getFormatAddress());

                reGeocoderResult.success(addressName);
            } else {
                reGeocoderResult.error("-1", "未找到地址", "没有找到地址对于的坐标");
            }
        } else {
            reGeocoderResult.error(String.valueOf(rCode), "错误", "错误代码 : " + rCode);
        }

        reGeocoderResult = null;
    }

    @Override
    public void onGeocodeSearched(GeocodeResult result, int rCode) {
        if (rCode == AMapException.CODE_AMAP_SUCCESS) {
            if (result != null && result.getGeocodeAddressList() != null
                    && result.getGeocodeAddressList().size() > 0) {
                GeocodeAddress address = result.getGeocodeAddressList().get(0);
                Log.w("xxxx", "addressName= " + address);
                if (address != null) {
//                    aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
//                            AMapUtil.convertToLatLng(address.getLatLonPoint()), 15));
//                    geoMarker.setPosition(AMapUtil.convertToLatLng(address
//                            .getLatLonPoint()));
                    LatLonPoint point = address.getLatLonPoint();
                    String addressName = "经纬度值:" + point + "\n位置描述:"
                            + address.getFormatAddress();

//                    final Map<String, Object> arguments = new HashMap<String, Object>(10);
//                    arguments.put("lat", point.getLatitude());
//                    arguments.put("lng", point.getLongitude());
//                    arguments.put("address", addressName);
                    double[] r = {point.getLatitude(), point.getLongitude()};

                    geocoderResult.success(r);
                }
            } else {
                geocoderResult.error("-1", "未找到地址", "没有找到坐标对于的地址");
            }
        } else {
            geocoderResult.error(String.valueOf(rCode), "错误", "错误代码 : " + rCode);
        }
        geocoderResult = null;
    }
}
