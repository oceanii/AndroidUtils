package com.example.xzy.androidutils.baseactivity;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.xzy.androidutils.R;

/**
 * Created by xzy on 2019/12/1.
 */

public class TitleBar extends RelativeLayout implements View.OnClickListener{
    private static final String TAG = "TitleBar";
    private RelativeLayout mBackLayout;
    private TextView mCenterText;
    private TextView mRightText;
    private RelativeLayout mForwardLayout;

    private OnTitleBarClickListener mOnTitleBarClickListener;

    public interface OnTitleBarClickListener{
        void onBackImageClick();
        void onCenterTextClick();
        void onRightTextClick();
    }

    public void setOnTitleBarClickListener(OnTitleBarClickListener onTitleBarClickListener){
        mOnTitleBarClickListener = onTitleBarClickListener;
    }

    public TitleBar(Context context) {
        this(context, null);
        Log.d(TAG, "TitleBar1: ");
    }

    public TitleBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
        Log.d(TAG, "TitleBar2: ");
    }

    public TitleBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
        Log.d(TAG, "TitleBar3: ");
    }

    private void init(Context context){
        LayoutInflater inflater = LayoutInflater.from(context);
        View viewParent = inflater.inflate(R.layout.view_titlebar, null);
        addView(viewParent);
        mBackLayout = (RelativeLayout)viewParent.findViewById(R.id.rl_back);
        mBackLayout.setOnClickListener(this);
        mCenterText = (TextView)viewParent.findViewById(R.id.tv_center);
        mCenterText.setOnClickListener(this);
        mRightText = (TextView)viewParent.findViewById(R.id.tv_right);
        mForwardLayout = (RelativeLayout)viewParent.findViewById(R.id.rl_forward);
        mForwardLayout.setOnClickListener(this);
    }

    public void setBackImageVisible(int visible){
        mBackLayout.setVisibility(visible);
        if(visible == View.VISIBLE){
            mBackLayout.setClickable(true);
        }else{
            mBackLayout.setClickable(false);
        }
    }

    public void setCenterText(String text){
        mCenterText.setText(text);
    }

    public void setCenterText(@StringRes int resId){
        mCenterText.setText(getContext().getResources().getText(resId));
    }

    public void setRightText(String text){
        mRightText.setText(text);
    }

    public void setRightText(@StringRes int resId){
        mRightText.setText(getContext().getResources().getText(resId));
    }

    public void setRightTextVisible(int visible){
        mRightText.setVisibility(visible);
        if(visible == View.VISIBLE){
            mForwardLayout.setClickable(true);
        }else{
            mForwardLayout.setClickable(false);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.rl_back:
                if(mOnTitleBarClickListener != null){
                    mOnTitleBarClickListener.onBackImageClick();
                }
                //ActivityManager.getInstance().finishActivity();
                break;
            case R.id.tv_center:
                if(mOnTitleBarClickListener != null){
                    mOnTitleBarClickListener.onCenterTextClick();
                }
                break;
            case R.id.rl_forward:
                if(mOnTitleBarClickListener != null){
                    mOnTitleBarClickListener.onRightTextClick();
                }
                break;
            default:
                break;
        }
    }
}
