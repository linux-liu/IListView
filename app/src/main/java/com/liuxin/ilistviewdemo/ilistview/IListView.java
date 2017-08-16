package com.liuxin.ilistviewdemo.ilistview;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.LayoutRes;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.AbsListView;
import android.widget.Adapter;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.liuxin.ilistviewdemo.R;


/**
 * 刘信 仿写IRecycelView
 * Created by Finley on 2017/8/5.
 */

public class IListView extends ListView {
    private static final int DEFAULT_STATUS=0;
    private static final int SWIPING_TO_REFRESH_STATUS=1;
    private static final int RELEASE_TO_REFRESH_STATUS=2;
    private static final int REFRESHING_STATUS=3;
    private int mStatus;


    private boolean refreshEnable;
    private boolean loadMoreEnable;
    private RefreshHeaderLayout refreshHeadLayout;
    private LinearLayout loadMoreLayout;
    private View loadMoreView;
    private View refreshHeadView;

    public OnScrollListener externalListener;

    public onLoadMoreListener onLoadMoreListener;

    public onRefreshListener onRefreshListener;

    private int mScrollState=0;
    private int firstvisible;
    private int lastvisibleCount;
    private boolean mIsAutoRefreshing;

    public IListView(Context context) {
        super(context);
        init(context,null,0);
    }

    public IListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context,attrs,0);
    }

    public IListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context,attrs,defStyleAttr);
    }
    private void init(Context context,AttributeSet attrs,int defStyleAttrs){
        TypedArray typedArray=context.obtainStyledAttributes(attrs, R.styleable.IListView,defStyleAttrs,0);
        int refreshHeadResId=-1;
        int loadmoreResId=-1;
        boolean refreshEnable=false;
        boolean loadmoreEnable=false;
        try {
            refreshHeadResId=typedArray.getResourceId(R.styleable.IListView_refreshHeaderlayout,-1);
            loadmoreResId=typedArray.getResourceId(R.styleable.IListView_loadMoreFooterlayout,-1);
            refreshEnable=typedArray.getBoolean(R.styleable.IListView_refreshenable,false);
            loadmoreEnable=typedArray.getBoolean(R.styleable.IListView_loadMoreenable,false);
       }catch (Exception e){

       }finally {
           typedArray.recycle();
       }

        setRefreshEnable(refreshEnable);
        setLoadMoreEnable(loadmoreEnable);
        if(refreshHeadResId!=-1){
            setRefreshHeadView(refreshHeadResId);
        }

         if(loadmoreResId!=-1){
             setLoadMoreView(loadmoreResId);
         }

         enSureRefreshHeadLayout();
         ensurenLoadMoreLayout();
         setOnScrollListener(new onScrollListener());
         setFooterDividersEnabled(false);
         addHeaderView(refreshHeadLayout);
         addFooterView(loadMoreLayout);
    }

    public void setRefreshing(boolean refreshing) {
        if (mStatus == DEFAULT_STATUS && refreshing) {
            this.mIsAutoRefreshing = true;
            setStatus(SWIPING_TO_REFRESH_STATUS);
            startScrollDefaultStatusToRefreshingStatus();
        } else if (mStatus == REFRESHING_STATUS && !refreshing) {
            this.mIsAutoRefreshing = false;
            startScrollRefreshingStatusToDefaultStatus();
        } else {
            this.mIsAutoRefreshing = false;

        }
    }

    /**
     * 设置是否可以上拉刷新
     * @param isRefreshEnable
     */
    public void setRefreshEnable(boolean isRefreshEnable){
        this.refreshEnable=isRefreshEnable;

    }

    public void setOnLoadMoreListener(onLoadMoreListener onloadMoreListener){
        this.onLoadMoreListener=onloadMoreListener;
    }


    public void setOnRefreshListener(onRefreshListener onrefreshlistener){
        this.onRefreshListener=onrefreshlistener;
    }

    /**
     * 设置是否可以下来加载更多
     * @param isLoadMoreEnable
     */
    public void setLoadMoreEnable(boolean isLoadMoreEnable){
        this.loadMoreEnable=isLoadMoreEnable;

    }

    private void setStatus(int status){
        this.mStatus=status;
    }

    /**
     * 获取loadmoreview
     * @return
     */
    public View getLoadMoreView(){
        return loadMoreView;
    }


    private class onScrollListener implements OnScrollListener{

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
             mScrollState=scrollState;
            if(scrollState==OnScrollListener.SCROLL_STATE_IDLE&&loadMoreEnable
                    &&lastvisibleCount==getAdapter().getCount()-1
                    &&onLoadMoreListener!=null&&mStatus==DEFAULT_STATUS){

                onLoadMoreListener.onLoadMore();
            }
            if(externalListener!=null){
                externalListener.onScrollStateChanged(view,scrollState);
            }

        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
               firstvisible=firstVisibleItem;
               lastvisibleCount=firstVisibleItem+visibleItemCount-1;
            if(externalListener!=null){
                externalListener.onScroll(view,firstVisibleItem,visibleItemCount,totalItemCount);
            }
        }
    }



    public void setExternalListener(OnScrollListener onScrollListener){

        this.externalListener=onScrollListener;
    }


    private void setRefreshHeadView(@LayoutRes int layoutResId){
        enSureRefreshHeadLayout();
        final  View refreshHeadView=LayoutInflater.from(getContext()).inflate(layoutResId,refreshHeadLayout,false);
        if(refreshHeadView!=null){
           setRefreshHeadView(refreshHeadView);
        }
    }

     private int lastpointid;
     private int touchX;
     private int touchY;
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int action= MotionEventCompat.getActionMasked(ev);
        int pointindex= MotionEventCompat.getActionIndex(ev);
        switch (action){
            case MotionEvent.ACTION_DOWN:
                lastpointid= MotionEventCompat.getPointerId(ev,0);
                touchX=getMotionEventX(ev,pointindex);
                touchY=getMotionEventY(ev,pointindex);
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                lastpointid= MotionEventCompat.getPointerId(ev,pointindex);
                touchX=getMotionEventX(ev,pointindex);
                touchY=getMotionEventY(ev,pointindex);
                break;
            case MotionEvent.ACTION_POINTER_UP:
                onPointUp(ev);
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        int action= MotionEventCompat.getActionMasked(ev);
        switch (action){
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                int lastindex= MotionEventCompat.findPointerIndex(ev,lastpointid);
                int x=getMotionEventX(ev,lastindex);
                int y=getMotionEventY(ev,lastindex);
                int dx=x-touchX;
                int dy=y-touchY;
                touchX=x;
                touchY=y;
               boolean iscan=isEnabled()&&refreshEnable&&refreshHeadView!=null&&
                       isStopScroll()&&istop();
                if(iscan){
                    final int refreshHeaderContainerHeight = refreshHeadLayout.getMeasuredHeight();
                    final int refreshHeaderViewHeight = refreshHeadView.getMeasuredHeight();

                    if (dy > 0 && mStatus == DEFAULT_STATUS) {
                        setStatus(SWIPING_TO_REFRESH_STATUS);
                        mRefreshTrigger.onStart(false, refreshHeaderViewHeight, 0);
                    } else if (dy < 0) {
                        if (mStatus == SWIPING_TO_REFRESH_STATUS && refreshHeaderContainerHeight <= 0) {
                            setStatus(DEFAULT_STATUS);
                        }
                        if (mStatus == DEFAULT_STATUS) {
                            break;
                        }
                    }

                    if (mStatus == SWIPING_TO_REFRESH_STATUS || mStatus == RELEASE_TO_REFRESH_STATUS) {
                        if (refreshHeaderContainerHeight >= refreshHeaderViewHeight) {
                            setStatus(RELEASE_TO_REFRESH_STATUS);
                        } else {
                            setStatus(SWIPING_TO_REFRESH_STATUS);
                        }
                        fingerMove(dy);
                        return true;
                    }
                }
                break;
            case MotionEventCompat.ACTION_POINTER_DOWN: {
                Log.d("lx","onTouchEvent_ACTION_POINTER_DOWN");
                final int index = MotionEventCompat.getActionIndex(ev);
                lastpointid = MotionEventCompat.getPointerId(ev, index);
                touchX = getMotionEventX(ev, index);
                touchY = getMotionEventY(ev, index);
            }
            break;
            case MotionEventCompat.ACTION_POINTER_UP: {
                Log.d("lx","onTouchEvent_ACTION_POINTER_UP");
                onPointUp(ev);
            }
            break;

            case MotionEvent.ACTION_UP: {
                Log.d("lx","onTouchEvent_ACTION_UP");
                onFingerUpStartAnimating();
            }
            break;

            case MotionEvent.ACTION_CANCEL: {
                Log.i("lx","onTouchEvent_ACTION_CANCEL");
                onFingerUpStartAnimating();
            }
            break;

        }

        return super.onTouchEvent(ev);
    }

    private void onFingerUpStartAnimating() {
        if (mStatus == RELEASE_TO_REFRESH_STATUS) {
            startScrollReleaseStatusToRefreshingStatus();
        } else if (mStatus == SWIPING_TO_REFRESH_STATUS) {
            startScrollSwipingToRefreshStatusToDefaultStatus();
        }
    }


    private void fingerMove(int dy) {
        int ratioDy = (int) (dy * 0.5f + 0.5f);
        int offset = refreshHeadLayout.getMeasuredHeight();


        int nextOffset = offset + ratioDy;


        if (nextOffset < 0) {
            ratioDy = -offset;
        }
        move(ratioDy);
    }

    private void move(int dy) {
        if (dy != 0) {
            int height = refreshHeadLayout.getMeasuredHeight() + dy;
            setRefreshHeaderContainerHeight(height);
            mRefreshTrigger.onMove(false, false, height);
        }
    }

    private void startScrollReleaseStatusToRefreshingStatus() {
        mRefreshTrigger.onRelease();

        final int targetHeight = refreshHeadView.getMeasuredHeight();
        final int currentHeight = refreshHeadLayout.getMeasuredHeight();
        startScrollAnimation(300, new DecelerateInterpolator(), currentHeight, targetHeight);
    }
    private void startScrollSwipingToRefreshStatusToDefaultStatus() {
        final int targetHeight = 0;
        final int currentHeight = refreshHeadLayout.getMeasuredHeight();
        startScrollAnimation(300, new DecelerateInterpolator(), currentHeight, targetHeight);
    }

    private void startScrollDefaultStatusToRefreshingStatus() {
        mRefreshTrigger.onStart(true, refreshHeadView.getMeasuredHeight(),0);

        int targetHeight = refreshHeadView.getMeasuredHeight();
        int currentHeight = refreshHeadLayout.getMeasuredHeight();
        startScrollAnimation(400, new AccelerateInterpolator(), currentHeight, targetHeight);
    }

    private void startScrollRefreshingStatusToDefaultStatus() {
        mRefreshTrigger.onComplete();

        final int targetHeight = 0;
        final int currentHeight = refreshHeadLayout.getMeasuredHeight();
        startScrollAnimation(400, new DecelerateInterpolator(), currentHeight, targetHeight);
    }

    private ValueAnimator mScrollAnimator;

    private void startScrollAnimation(final int time, final Interpolator interpolator, int value, int toValue) {
        if (mScrollAnimator == null) {
            mScrollAnimator = new ValueAnimator();
        }

        //cancel
        mScrollAnimator.removeAllUpdateListeners();
        mScrollAnimator.removeAllListeners();
        mScrollAnimator.cancel();

        //reset new value
        mScrollAnimator.setIntValues(value, toValue);
        mScrollAnimator.setDuration(time);
        mScrollAnimator.setInterpolator(interpolator);
        mScrollAnimator.addUpdateListener(mAnimatorUpdateListener);
        mScrollAnimator.addListener(mAnimationListener);
        mScrollAnimator.start();
    }

    private ValueAnimator.AnimatorUpdateListener mAnimatorUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            final int height = (Integer) animation.getAnimatedValue();
            setRefreshHeaderContainerHeight(height);
            switch (mStatus) {
                case SWIPING_TO_REFRESH_STATUS: {
                    mRefreshTrigger.onMove(false, true, height);
                }
                break;

                case RELEASE_TO_REFRESH_STATUS: {
                    mRefreshTrigger.onMove(false, true, height);
                }
                break;

                case REFRESHING_STATUS: {
                    mRefreshTrigger.onMove(true, true, height);
                }
                break;
            }

        }
    };

    private void setRefreshHeaderContainerHeight(int height) {
        refreshHeadLayout.getLayoutParams().height = height;
        refreshHeadLayout.requestLayout();
    }

    private Animator.AnimatorListener mAnimationListener = new SimpleAnimatorListener() {
        @Override
        public void onAnimationEnd(Animator animation) {
            int lastStatus = mStatus;

            switch (mStatus) {
                case SWIPING_TO_REFRESH_STATUS: {
                    if (mIsAutoRefreshing) {
                        refreshHeadLayout.getLayoutParams().height = refreshHeadView.getMeasuredHeight();
                        refreshHeadLayout.requestLayout();
                        setStatus(REFRESHING_STATUS);
                        if (onRefreshListener != null) {
                            onRefreshListener.onRefresh();
                            mRefreshTrigger.onRefresh();
                        }
                    } else {
                        refreshHeadLayout.getLayoutParams().height = 0;
                        refreshHeadLayout.requestLayout();
                        setStatus(DEFAULT_STATUS);
                    }
                }
                break;

                case RELEASE_TO_REFRESH_STATUS: {
                    refreshHeadLayout.getLayoutParams().height = refreshHeadView.getMeasuredHeight();
                    refreshHeadLayout.requestLayout();
                    setStatus(REFRESHING_STATUS);
                    if (onRefreshListener != null) {
                        onRefreshListener.onRefresh();
                        mRefreshTrigger.onRefresh();
                    }
                }
                break;

                case REFRESHING_STATUS: {
                    mIsAutoRefreshing = false;
                    refreshHeadLayout.getLayoutParams().height = 0;
                    refreshHeadLayout.requestLayout();
                    setStatus(DEFAULT_STATUS);
                    mRefreshTrigger.onReset();
                }
                break;
            }

        }
    };


    private RefreshTrigger mRefreshTrigger = new RefreshTrigger() {
        @Override
        public void onStart(boolean automatic, int headerHeight, int finalHeight) {
            if (refreshHeadView != null && refreshHeadView instanceof RefreshTrigger) {
                RefreshTrigger trigger = (RefreshTrigger) refreshHeadView;
                trigger.onStart(automatic, headerHeight, finalHeight);
            }
        }

        @Override
        public void onMove(boolean finished, boolean automatic, int moved) {
            if (refreshHeadView != null && refreshHeadView instanceof RefreshTrigger) {
                RefreshTrigger trigger = (RefreshTrigger) refreshHeadView;
                trigger.onMove(finished, automatic, moved);
            }
        }

        @Override
        public void onRefresh() {
            if (refreshHeadView != null && refreshHeadView instanceof RefreshTrigger) {
                RefreshTrigger trigger = (RefreshTrigger) refreshHeadView;
                trigger.onRefresh();
            }
        }

        @Override
        public void onRelease() {
            if (refreshHeadView != null && refreshHeadView instanceof RefreshTrigger) {
                RefreshTrigger trigger = (RefreshTrigger) refreshHeadView;
                trigger.onRelease();
            }
        }

        @Override
        public void onComplete() {
            if (refreshHeadView != null && refreshHeadView instanceof RefreshTrigger) {
                RefreshTrigger trigger = (RefreshTrigger) refreshHeadView;
                trigger.onComplete();
            }
        }

        @Override
        public void onReset() {
            if (refreshHeadView != null && refreshHeadView instanceof RefreshTrigger) {
                RefreshTrigger trigger = (RefreshTrigger) refreshHeadView;
                trigger.onReset();
            }
        }
    };

    /**
     * 是否listView在停止滑动的状态
     * @return
     */
    private boolean isStopScroll(){
       return mScrollState==OnScrollListener.SCROLL_STATE_IDLE;
    }

    /**
     * 是否listView滑到了顶部
     * @return
     */
    private boolean istop(){
        final Adapter adapter=getAdapter();
        if(adapter==null ||adapter.getCount()<=0){
            return false;
        }
        View firstView=getChildAt(firstvisible);
        if(firstView!=null&&firstView.getTop()==refreshHeadLayout.getTop()){
            return true;
        }
        return false;
    }


    private void onPointUp(MotionEvent event){
        int pointindex= MotionEventCompat.getActionIndex(event);
        int pointid= MotionEventCompat.getPointerId(event,pointindex);
        if(pointid==lastpointid){
            int newpointindex=pointindex==0?1:0;
            lastpointid= MotionEventCompat.getPointerId(event,newpointindex);
            touchX=getMotionEventX(event,pointid);
            touchY=getMotionEventY(event,pointid);
        }
    }


    private int getMotionEventX(MotionEvent ev,int pointIndex){
        return (int) (MotionEventCompat.getX(ev,pointIndex)+0.5f);
    }

    private int getMotionEventY(MotionEvent ev,int pointIndex){
        return (int) (MotionEventCompat.getY(ev,pointIndex)+0.5f);
    }

    /**
     * 设置下拉刷新的View
     *
     */
    public void setRefreshHeadView(View view){

        if (!(view instanceof RefreshTrigger)) {
            throw new ClassCastException("Refresh header view must be an implement of RefreshTrigger");
        }
          if(refreshHeadView!=null){
             removeRefreshView();
          }

            if(view !=refreshHeadView){
                refreshHeadView=view;
                enSureRefreshHeadLayout();
                refreshHeadLayout.addView(view);

        }
    }

    private void removeRefreshView(){
        if(refreshHeadLayout!=null){
            refreshHeadLayout.removeView(refreshHeadView);
        }
    }

    private void enSureRefreshHeadLayout(){
        if(refreshHeadLayout==null){
            refreshHeadLayout=new RefreshHeaderLayout(getContext());
            refreshHeadLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,0));
        }
    }


    private void setLoadMoreView(@LayoutRes int layoutResId){
        ensurenLoadMoreLayout();
        final View loadMoreView=LayoutInflater.from(getContext()).inflate(layoutResId,loadMoreLayout,false);
        if(loadMoreView!=null){
            setLoadMoreView(loadMoreView);
        }

    }

    private void ensurenLoadMoreLayout(){
        if(loadMoreLayout==null){
            loadMoreLayout=new LinearLayout(getContext());
            loadMoreLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        }
    }

    /**
     * 设置loadmoreView
     * @param view
     */
    public void setLoadMoreView(View view){
        if(loadMoreView!=null){
          removeLoadMoreView();
        }
        if(loadMoreView!=view){
            ensurenLoadMoreLayout();
            this.loadMoreView=view;
            loadMoreLayout.addView(view);
        }

          this.loadMoreView=view;

    }

    private void removeLoadMoreView(){
        if(loadMoreLayout!=null){
            loadMoreLayout.removeView(loadMoreView);
        }
    }


    public interface onLoadMoreListener{
        void onLoadMore();
    }

    public interface onRefreshListener{
        void onRefresh();
    }

}
