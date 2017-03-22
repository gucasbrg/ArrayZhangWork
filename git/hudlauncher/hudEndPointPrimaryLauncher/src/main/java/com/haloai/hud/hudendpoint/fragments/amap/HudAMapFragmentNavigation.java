package com.haloai.hud.hudendpoint.fragments.amap;

import android.app.Fragment;
import android.graphics.AvoidXfermode;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.OnCameraChangeListener;
import com.amap.api.maps.AMap.OnMapLoadedListener;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviView;
import com.amap.api.navi.AMapNaviViewOptions;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.NaviInfo;
import com.haloai.hud.hudendpoint.HudEndPointConstants;
import com.haloai.hud.hudendpoint.arwaylib.ARWayController;
import com.haloai.hud.hudendpoint.primarylauncher.R;
import com.haloai.hud.utils.ShareDrawables;
import com.haloai.hud.navigation.NavigationSDKAdapter;

/**
 * 导航中的界面展示
 *
 * @author 龙
 */
public class HudAMapFragmentNavigation extends Fragment implements OnMapLoadedListener, OnCameraChangeListener {

    private static final int GPS_STATUS_FINE = 0;
    private static final int GPS_STATUS_WEEK = 1;
    private static final int GPS_STATUS_BAD  = 2;


    private FrameLayout  mNaviView     = null;
    private AMapNaviView mAmapNaviView = null;

    private AMapNavi mAMapNavi          = null;
    private boolean  mMapLoaded         = false;
    private Bitmap   mCurrentCrossImage = null;
    private boolean  mCrossCanShow      = true;
    private int      mCurrentGpsStatus  = GPS_STATUS_FINE;

    private int mLastNaviIconType = 0;
    private Bitmap mNaviIconBitmap = null;
    private View arway;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mNaviView = (FrameLayout) View.inflate(getActivity(), R.layout.fragment_amap_naving, null);

        //init arway

        arway = ARWayController.getARWayAndInit(getActivity());
        if(arway.getParent()==null){
            mNaviView.addView(arway);
        }
        ARWayController.ARWayStatusUpdater.start();
        ARWayController.RouteBeanUpdater.setIsShow(true);
        ARWayController.RouteBeanUpdater.setCanDrawHudway(true);
        ARWayController.NaviInfoBeanUpdate.setIsShow(true);
        ARWayController.SpeedBeanUpdater.setIsShow(true);
        //init amap navi view1
        mAmapNaviView = (AMapNaviView) mNaviView.findViewById(R.id.amap_navi_amapnaviview);
        mAmapNaviView.onCreate(savedInstanceState);
        mAmapNaviView.getMap().setOnMapLoadedListener(this);
        mAmapNaviView.getMap().setOnCameraChangeListener(this);
        if (HudEndPointConstants.IS_DEBUG_MODE) {
            mAmapNaviView.setVisibility(View.VISIBLE);
        } else {
            mAmapNaviView.setVisibility(View.INVISIBLE);
        }

        return mNaviView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mAmapNaviView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mAmapNaviView.onPause();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (mNaviView != null) {
            if (hidden) {
                ARWayController.ARWayStatusUpdater.pause();
                //mHudwayView.destroyDrawingCache();
                //mFl_hudway.removeView(mHudwayView);

                //clear navigating bitmap when this fragment is hidden
                //mIv_cross.setImageBitmap(null);
                //TODO temp method to draw arway
                //mHudwayView.setCrossImage(null);
                //mIv_icon_normal.setImageBitmap(null);
                //mIv_icon_big.setImageBitmap(null);
                //mIv_gps_status.setImageBitmap(null);
                //mTv_distance_big.setText("");
                //mTv_distance_normal.setText("");
                //mTv_gps_status.setText("");
                //mTv_next_road.setText("");
                //mTv_time_distance.setText("");
                mCurrentCrossImage = null;
                mCrossCanShow = true;
            } else {
                showARWay();
                ARWayController.ARWayStatusUpdater.continue_();
                if (arway != null && arway.getParent() == null) {
                    mNaviView.addView(arway);
                }
            }
        }

        removeAMapNaviView();
        if (!hidden) {
            addAMapNaviView();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mAmapNaviView.onSaveInstanceState(outState);
    }

    public void setSatelliteNum(int satelliteNum) {
        //TODO 为了在室内也能正常测试使用导航功能,如果没有下面这句,则导航中会一直处于信号差不可用状态,上线记得删除
        ARWayController.RouteBeanUpdater.setGpsNumber(satelliteNum);
        satelliteNum += 10;
        if (satelliteNum <= 0) {
            //mIv_gps_status.setImageResource(R.drawable.greenline_navigation_satellite_red);
            //mTv_gps_status.setText("GPS信号弱");
            //mTv_gps_status.setVisibility(View.VISIBLE);
            //mIv_gps_status.setVisibility(View.VISIBLE);
            this.mCurrentGpsStatus = GPS_STATUS_BAD;
        } else if (satelliteNum < 5) {
            //mIv_gps_status.setImageResource(R.drawable.greenline_navigation_satellite_yellow);
            //mTv_gps_status.setText("GPS信号弱");
            //mTv_gps_status.setVisibility(View.VISIBLE);
            //mIv_gps_status.setVisibility(View.VISIBLE);
            this.mCurrentGpsStatus = GPS_STATUS_WEEK;
        } else {
            //mIv_gps_status.setImageBitmap(null);
            //mTv_gps_status.setText("");
            this.mCurrentGpsStatus = GPS_STATUS_FINE;
        }
    }

    /**
     * show cross image in image view
     *
     * @param crossimage Bitmap for show cross
     */
    public void showCrossImage(Bitmap crossimage) {
        if (mCurrentGpsStatus == GPS_STATUS_BAD) {
            ARWayController.NaviInfoBeanUpdate.setCrossBitmap(null);
            return;
        }
        if (crossimage != null) {
            mCurrentCrossImage = handleCrossImageBack(crossimage);
            if (mCrossCanShow) {
                ARWayController.NaviInfoBeanUpdate.setCrossBitmap(mCurrentCrossImage);
            } else {
                ARWayController.NaviInfoBeanUpdate.setCrossBitmap(null);
            }
        }
    }

    /**
     * 处理路口放大图，去掉灰色背景，50为颜色容忍度（左右偏差值）
     *
     * @param crossimage
     * @return
     */
    @SuppressWarnings("deprecation")
    private Bitmap handleCrossImageBack(Bitmap crossimage) {
        // start with a Bitmap bmp
        Bitmap newBmp = crossimage.copy(Bitmap.Config.ARGB_8888, true);
        Canvas c = new Canvas(newBmp);

        // get the int for the colour which needs to be removed
        Paint mPaint = new Paint();// 去锯齿
        mPaint.setAntiAlias(true);// 防抖动
        mPaint.setDither(true);// 图像过滤
        mPaint.setFilterBitmap(true);
        mPaint.setARGB(255, 0, 0, 0); // ARGB for the color to replace

        mPaint.setXfermode(new AvoidXfermode(0x616161, 50, AvoidXfermode.Mode.TARGET));
        c.drawPaint(mPaint);

        return newBmp;
    }

    /**
     * clear image view
     */
    public void hideCrossImage() {
        ARWayController.NaviInfoBeanUpdate.setCrossBitmap(null);
    }

    /**
     * update time and distance
     *
     * @param time
     * @param distance
     */
    public void updateTimeAndDistance(int time, int distance) {
        if (mCurrentGpsStatus == GPS_STATUS_BAD) {
            //mTv_time_distance.setText("");
            return;
        }
        //mTv_time_distance.setText("剩余" + (distance / 1000 + 1) + "km" + "≈" + (time / 60 + 1) + "min");
    }

    /**
     * update navi info with navi icon and current link distance
     *
     * @param naviIcon 表示诱导信息的int值
     * @param distance 当前导航信息所剩距离
     */
    public void updateRouteInfo(int naviIcon, int distance) {
        if (mCurrentGpsStatus == GPS_STATUS_BAD) {
            //mTv_distance_normal.setText("");
            //mIv_icon_normal.setImageBitmap(null);
            return;
        }
        if (mCurrentGpsStatus == GPS_STATUS_FINE) {
            //mRl_navi_icon_distance_normal.setVisibility(View.VISIBLE);
            //mRl_navi_icon_distance_big.setVisibility(View.INVISIBLE);
        } else if (mCurrentGpsStatus == GPS_STATUS_WEEK) {
            //mRl_navi_icon_distance_normal.setVisibility(View.INVISIBLE);
            //mRl_navi_icon_distance_big.setVisibility(View.VISIBLE);
        }
        if (distance >= 1000) {
            //mTv_distance_normal.setText(distance / 1000 + "." + ((distance - distance / 1000 * 1000) / 100) + "km");
            //mTv_distance_big.setText(distance / 1000 + "." + ((distance - distance / 1000 * 1000) / 100) + "km");
        } else {
            //mTv_distance_normal.setText(distance + "m");
            //mTv_distance_big.setText(distance + "m");
        }
        switch (naviIcon) {
            case 15:
                //mIv_icon_normal.setImageResource(R.drawable.greenline_direction__icon_turn_dest);
                //mIv_icon_big.setImageResource(R.drawable.greenline_direction__icon_turn_dest);
                break;
            case 13:
                //mIv_icon_normal.setImageResource(R.drawable.greenline_direction__icon_turn_service_area);
                //mIv_icon_big.setImageResource(R.drawable.greenline_direction__icon_turn_service_area);
                break;
            case 14:
                //mIv_icon_normal.setImageResource(R.drawable.greenline_direction__icon_turn_tollgate);
                //mIv_icon_big.setImageResource(R.drawable.greenline_direction__icon_turn_tollgate);
                break;
            case 16:
                //mIv_icon_normal.setImageResource(R.drawable.greenline_direction__icon_turn_tunnel);
                //mIv_icon_big.setImageResource(R.drawable.greenline_direction__icon_turn_tunnel);
                break;
            case 10:
                //mIv_icon_normal.setImageResource(R.drawable.greenline_direction__icon_turn_passing_point);
                //mIv_icon_big.setImageResource(R.drawable.greenline_direction__icon_turn_passing_point);
                break;
            case 24:
                // TODO 通过索道
                break;
            case 26:
                // TODO 通过通道、建筑物穿越通道
                break;
            case 17:
                // TODO 通过人行横道
                break;
            case 28:
                // TODO 通过游船路线
                break;
            case 1:
                // TODO 自车图标
                break;
            case 11:
                //mIv_icon_normal.setImageResource(R.drawable.greenline_direction__icon_turn_ring);
                //mIv_icon_big.setImageResource(R.drawable.greenline_direction__icon_turn_ring);
                break;
            case 31:
                // TODO 通过阶梯
                break;
            case 2:
                //mIv_icon_normal.setImageResource(R.drawable.greenline_direction__icon_turn_left);
                //mIv_icon_big.setImageResource(R.drawable.greenline_direction__icon_turn_left);
                break;
            case 6:
                //mIv_icon_normal.setImageResource(R.drawable.greenline_direction__icon_turn_left_back);
                //mIv_icon_big.setImageResource(R.drawable.greenline_direction__icon_turn_left_back);
                break;
            case 4:
                //mIv_icon_normal.setImageResource(R.drawable.greenline_direction__icon_turn_left_front);
                //mIv_icon_big.setImageResource(R.drawable.greenline_direction__icon_turn_left_front);
                break;
            case 8:
                //mIv_icon_normal.setImageResource(R.drawable.greenline_direction__icon_turn_back);
                //mIv_icon_big.setImageResource(R.drawable.greenline_direction__icon_turn_back);
                break;
            case 23:
                // TODO 通过直梯
                break;
            case 0:
                // TODO 无定义
                break;
            case 12:
                //mIv_icon_normal.setImageResource(R.drawable.greenline_direction__icon_turn_ring_out);
                //mIv_icon_big.setImageResource(R.drawable.greenline_direction__icon_turn_ring_out);
                break;
            case 18:
                // TODO 通过过街天桥
                break;
            case 21:
                // TODO 通过公园
                break;
            case 3:
                //mIv_icon_normal.setImageResource(R.drawable.greenline_direction__icon_turn_right);
                //mIv_icon_big.setImageResource(R.drawable.greenline_direction__icon_turn_right);
                break;
            case 7:
                //mIv_icon_normal.setImageResource(R.drawable.greenline_direction__icon_turn_right_back);
                //mIv_icon_big.setImageResource(R.drawable.greenline_direction__icon_turn_right_back);
                break;
            case 5:
                //mIv_icon_normal.setImageResource(R.drawable.greenline_direction__icon_turn_right_front);
                //mIv_icon_big.setImageResource(R.drawable.greenline_direction__icon_turn_right_front);
                break;
            case 29:
                // TODO 通过观光车路线
                break;
            case 25:
                // TODO 通过空中通道
                break;
            case 30:
                // TODO 通过滑道
                break;
            case 20:
                // TODO 通过广场
                break;
            case 22:
                // TODO 通过扶梯
                break;
            case 9:
                //mIv_icon_normal.setImageResource(R.drawable.greenline_direction__icon_turn_front);
                //mIv_icon_big.setImageResource(R.drawable.greenline_direction__icon_turn_front);
                break;
            case 19:
                // TODO 通过地下通道
                break;
            case 27:
                // TODO 通过行人道路
                break;
        }
    }

    /**
     * update location to show hudway
     *
     * @param location
     */
    public void updateLocation(AMapNaviLocation location) {
        // FIXME: 16/6/28 直接更新位置进去，在ARWYAN库中判断，方便根据情况处理显示
        ARWayController.RouteBeanUpdater.setCurrentLocation(location);
        /*if (mCurrentGpsStatus != GPS_STATUS_FINE) {
            HaloLogger.logE("sen_debug_location","onLocationChanged ,but gps star is 0");
            ARWayController.ARWayStatusUpdater.pause();
        } else {
            ARWayController.ARWayStatusUpdater.continue_();
            ARWayController.RouteBeanUpdater.setCurrentLocation(location);
        }*/
    }

    /**
     * update current point and step `s index in list
     *
     * @param curPoint
     * @param curStep
     */
    public void updateCurPointIndex(int curPoint, int curStep) {
        ARWayController.RouteBeanUpdater.setCurrentPoint(curPoint);
        ARWayController.RouteBeanUpdater.setCurrentStep(curStep);
    }

    /***
     * // 导航中的文字提示，可用于语音播报,
     * */
    public void updateNaviText(NavigationSDKAdapter.NavigationNotifier.NaviTextType textType, String text){
        ARWayController.NaviInfoBeanUpdate.setNaviText(text);
    }

    /**
     * 路线规划成功或者是偏航后的重新规划后调用该方法重新设置路线图
     *
     * @param aMapNavi
     */
    public void updatePath(AMapNavi aMapNavi) {
        this.mAMapNavi = aMapNavi;
        if (mMapLoaded) {
            ARWayController.RouteBeanUpdater.setPath(aMapNavi.getNaviPath());
        }
    }
    /**
     * 更新ARway中的导航数据
     *
     * @param info
     */
    public void updateNaviInfo(NaviInfo info){
        int iconResource = ShareDrawables.getNaviDirectionId(info.getIconType());//info
        Bitmap iconBitmap = null;
        // TODO: 16/6/22 应该不会内存溢出
        if(mLastNaviIconType !=iconResource &&  iconResource != 0){
            iconBitmap = BitmapFactory.decodeResource(getActivity().getResources(), iconResource);
            mNaviIconBitmap = iconBitmap;
            mLastNaviIconType = iconResource;
        }
        //update arway data
        ARWayController.NaviInfoBeanUpdate.setNaviIconDist(info.getCurStepRetainDistance());
        ARWayController.NaviInfoBeanUpdate.setNaviIcon(iconResource);
        ARWayController.NaviInfoBeanUpdate.setNaviIconBitmap(mNaviIconBitmap);
        ARWayController.NaviInfoBeanUpdate.setCurrentRoadName(info.getCurrentRoadName());
        ARWayController.NaviInfoBeanUpdate.setNextRoadName(info.getNextRoadName());
        ARWayController.NaviInfoBeanUpdate.setPathRetainDistance(info.getPathRetainDistance());
        ARWayController.NaviInfoBeanUpdate.setPathRetainTime(info.getPathRetainTime());
    }

    /**
     * 更新并显示下一条路的路名,此处需要知道路名和该路是左转,右转还是掉头.
     *
     * @param nextRoadName
     * @param nextRoadType
     */
   /* public void updateNextRoadName(String nextRoadName, RouteBean.NextRoadType nextRoadType) {
        String str = "";
        if (mCurrentGpsStatus != GPS_STATUS_BAD) {
            str = nextRoadName;
        }
        ARWayController.RouteBeanUpdater.setNextRoadName(str, nextRoadType);
    }*/

    /**
     * user said "退出导航" , will show this view with move in
     */
    public void showCancelNaviView() {
        //HudAMapAnimation.startCancelNaviShowAnimation(mRl_cancel_navi);
    }

    /**
     * user said "确定" or "取消", will hide this view with move out
     */
    public void hideCancelNaviViewDelay500() {
        //HudAMapAnimation.startCancelNaviHideAnimationDelay500(mRl_cancel_navi);
    }

    public void hideCancelNaviViewDelay0() {
        //HudAMapAnimation.startCancelNaviHideAnimationDelay0(mRl_cancel_navi);
    }

    /**
     * show the view for not network
     */
    public void showNotNetworKView() {
        //mRl_navi_not_network.setVisibility(View.VISIBLE);
    }

    /**
     * hide the view for not network
     */
    public void hideNotNetworKView() {
        //mRl_navi_not_network.setVisibility(View.INVISIBLE);
    }

    /***
     * hide the view for cross image
     */
    public void hideCross() {
        //mIv_cross.setImageBitmap(null);
        //mHudwayView.setCrossImage(null);
        mCrossCanShow = false;
    }

    /***
     * show the view for cross image
     */
    public void showCross() {
        mCrossCanShow = true;
    }

    public void initAMapNaviView() {

        AMapNaviViewOptions viewOptions = mAmapNaviView.getViewOptions();
        viewOptions.setNaviNight(true);
 //       viewOptions.setNaviViewTopic(AMapNaviViewOptions.BLUE_COLOR_TOPIC);
        viewOptions.setCrossDisplayShow(false);
        mAmapNaviView.setViewOptions(viewOptions);

        AMap aMap = this.mAmapNaviView.getMap();
        CameraPosition cameraPos = aMap.getCameraPosition();
        float curZoom = cameraPos.zoom;
        float curTilt = cameraPos.tilt;
        float maxZoomLevel = ((int) aMap.getMaxZoomLevel()) / 2 * 2;
        if (curZoom < maxZoomLevel || curTilt != 0.0) {
            cameraPos = CameraPosition.builder(cameraPos).tilt(0).zoom(maxZoomLevel).build();
            CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPos);
            aMap.moveCamera(cameraUpdate);
        }
    }

    @Override
    public void onMapLoaded() {
        mMapLoaded = true;
        mAmapNaviView.getMap().showMapText(false);
        ARWayController.RouteBeanUpdater
                .setProjection(mAmapNaviView.getMap().getProjection());
        if (mAMapNavi != null) {

            ARWayController.RouteBeanUpdater.setPath(mAMapNavi.getNaviPath());
        }
    }

    /**
     *更新偏航开始画面
     */
    public void updateYawStart(){
        ARWayController.ARWayStatusUpdater.yawStart();
        /*resetDrawHudway();
        addAMapNaviView();
        mAmapNaviView.setVisibility(View.VISIBLE);
        mAmapNaviView.setAlpha(1);
        hideARWay();*/
    }
    /**
     *
     *更新偏般结束界面
     */
    public void updateYawEnd(){
        ARWayController.ARWayStatusUpdater.start();
        ARWayController.ARWayStatusUpdater.yawEnd();
        /*mAmapNaviView.setVisibility(View.INVISIBLE);
        mAmapNaviView.setAlpha(0);
        showARWay();
        startDrawHudway();*/
    }


    /**
     * reset to draw hudway
     */
    public void resetDrawHudway() {
        ARWayController.ARWayStatusUpdater.reset();
    }


    /**
     * stop to draw hudway
     */
    public void stopDrawHudway() {
        ARWayController.ARWayStatusUpdater.stop();
    }

    /**
     * startHomeAnimator to draw hudway
     */
    public void startDrawHudway() {
        ARWayController.ARWayStatusUpdater.start();
    }

    /**
     * hide ARWay
     */
    public void hideARWay() {
       //s ARWayController.ARWayStatusUpdater.pause();
        if (arway != null && arway.getParent() != null) {
            mNaviView.removeView(arway);
        }

    }

    /**
     * show ARWay
     */
    public void showARWay() {
        ARWayController.ARWayStatusUpdater.continue_();
        if (arway != null && arway.getParent() == null) {
            mNaviView.addView(arway);
        }
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
    }

    @Override
    public void onCameraChangeFinish(CameraPosition arg0) {
        initAMapNaviView();
    }

    public void removeAMapNaviView() {
        if (mAmapNaviView != null && mAmapNaviView.getParent() != null) {
            ViewGroup parent = (ViewGroup) mAmapNaviView.getParent();
            parent.removeView(mAmapNaviView);
        }
    }

    public void addAMapNaviView() {
        if (mAmapNaviView != null) {
            removeAMapNaviView();
            mNaviView.addView(mAmapNaviView);
        }
    }

}
