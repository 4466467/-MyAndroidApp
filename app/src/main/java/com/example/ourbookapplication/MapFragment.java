package com.example.ourbookapplication;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.core.LatLonPoint;

import java.util.List;

public class MapFragment extends Fragment {
    private static final String TAG = "MapFragment";
    // 独立的地图控件和对象，与原代码隔离
    private MapView fragmentMapView;
    private AMap fragmentAMap;
    private GeocodeSearch fragmentGeocodeSearch;
    private ManualClusterManager fragmentClusterManager;
    private OnMapInteractionListener interactionListener;



    // 接口用于与宿主Activity通信
    public interface OnMapInteractionListener {
        // 注意参数名和类型（例如 latLng 而非 latLng1 等）
        void onMapClick(LatLng latLng);
        void onMarkerClick(Book book);
    }

    private OnMapInteractionListener mListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnMapInteractionListener) {
            mListener = (OnMapInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnMapInteractionListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        // 初始化Fragment专属地图控件（与原MapView ID不同）
        fragmentMapView = view.findViewById(R.id.fragment_map_view);
        fragmentMapView.onCreate(savedInstanceState);
        initFragmentMap();

        return view;
    }

    // 核心：添加newInstance()静态创建方法（MainActivity调用的关键）
    public static MapFragment newInstance() {
        MapFragment fragment = new MapFragment();
        // 若需要传递参数，可通过Bundle设置（示例）
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    // 初始化Fragment独立的地图实例
    private void initFragmentMap() {
        if (fragmentAMap == null) {
            fragmentAMap = fragmentMapView.getMap();
            setupFragmentMapSettings();
            setupFragmentClusterManager();
            setupFragmentMapListeners();
            initGeocodeSearch(); // 初始化独立的地理编码
        }
    }

    // 初始化地理编码（独立于MapPickerActivity）
    private void initGeocodeSearch() {
        try {
            fragmentGeocodeSearch = new GeocodeSearch(requireContext());
            fragmentGeocodeSearch.setOnGeocodeSearchListener(new GeocodeSearch.OnGeocodeSearchListener() {
                @Override
                public void onGeocodeSearched(com.amap.api.services.geocoder.GeocodeResult result, int resultCode) {
                    // 空实现，如需使用可添加
                }

                @Override
                public void onRegeocodeSearched(com.amap.api.services.geocoder.RegeocodeResult result, int resultCode) {
                    if (resultCode == 1000 && result != null && result.getRegeocodeAddress() != null) {
                        String address = result.getRegeocodeAddress().getFormatAddress();
                        Log.d(TAG, "Fragment解析到的地址: " + address);
                        Toast.makeText(getContext(), "选中位置: " + address, Toast.LENGTH_LONG).show();
                    } else {
                        Log.e(TAG, "Fragment逆地理编码失败，错误码: " + resultCode);
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Fragment地理编码初始化失败", e);
        }
    }

    private void setupFragmentMapSettings() {
        // 设置独立的初始位置（与原代码相同但独立生效）
        LatLng yunnanUniversity = new LatLng(24.8333, 102.8519);
        fragmentAMap.moveCamera(CameraUpdateFactory.newLatLngZoom(yunnanUniversity, 15f));
        // 独立配置地图UI
        fragmentAMap.getUiSettings().setCompassEnabled(true);
    }

    // 初始化独立的聚合管理器
    private void setupFragmentClusterManager() {
        fragmentClusterManager = new ManualClusterManager(requireContext(), fragmentAMap);
        fragmentAMap.setOnMarkerClickListener(marker -> fragmentClusterManager.onMarkerClick(marker));
    }

    // 设置Fragment独立的地图监听器
    private void setupFragmentMapListeners() {
        // 地图点击事件（独立于MainActivity的点击逻辑）
        fragmentAMap.setOnMapClickListener(latLng -> {
            if (interactionListener != null) {
                interactionListener.onMapClick(latLng);
            }
            // 执行逆地理编码（独立于MapPickerActivity）
            if (fragmentGeocodeSearch != null) {
                LatLonPoint point = new LatLonPoint(latLng.latitude, latLng.longitude);
                RegeocodeQuery query = new RegeocodeQuery(point, 200, GeocodeSearch.AMAP);
                fragmentGeocodeSearch.getFromLocationAsyn(query);
            }
        });

        // 标记点击事件（独立回调）
        // 在MapFragment的setupFragmentMapListeners方法中
        fragmentAMap.setOnMarkerClickListener(marker -> {
            Object object = marker.getObject();
            if (object instanceof Book) {
                Book book = (Book) object;
                if (mListener != null) {
                    mListener.onMarkerClick(book);
                    marker.showInfoWindow();
                }
                // 跳转到详情页
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).navigateToBookDetail(book);
                }
                return true;
            } else if (object instanceof ClusterPoint) {
                ClusterPoint cluster = (ClusterPoint) object;
                if (cluster.getSize() == 1) {
                    Book book = cluster.getFirstBook();
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).navigateToBookDetail(book);
                    }
                    return true;
                }
            }
            return false;
        });
    }

    // 独立添加书籍标记（不影响原代码的标记）
    public void addBooksToFragmentMap(List<Book> books) {
        if (fragmentClusterManager != null) {
            fragmentClusterManager.addBooks(books);
        } else {
            Log.e(TAG, "Fragment ClusterManager is not initialized");
        }
    }

    // 清除Fragment内的标记（不影响原地图）
    public void clearFragmentMapMarkers() {
        if (fragmentClusterManager != null) {
            fragmentClusterManager.clear();
        }
    }

    // 独立管理Fragment内地图的生命周期
    @Override
    public void onResume() {
        super.onResume();
        if (fragmentMapView != null) {
            fragmentMapView.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (fragmentMapView != null) {
            fragmentMapView.onPause();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (fragmentMapView != null) {
            fragmentMapView.onDestroy();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (fragmentMapView != null) {
            fragmentMapView.onSaveInstanceState(outState);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        interactionListener = null;
    }


}