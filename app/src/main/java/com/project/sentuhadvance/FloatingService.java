package com.project.sentuhadvance;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Binder;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.project.sentuhadvance.databinding.LayoutFloatingButtonsBinding;

public class FloatingService extends Service {
    private final IBinder binder = new LocalBinder();
    private ServiceCallback callback;

    private WindowManager windowManager;
    private View floatView;
    private LayoutFloatingButtonsBinding binding;
    private WindowManager.LayoutParams layoutParams;
    private int initialX, initialY;
    private float initialTouchX, initialTouchY;

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class LocalBinder extends Binder {
        public FloatingService getService() {
            return FloatingService.this;
        }
    }

    public interface ServiceCallback {
        void onMessageReceived(String message);
        void onCloseActivity();
        void onStopService();
    }

    public void setCallback(ServiceCallback cb) {
        callback = cb;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        addFloatingView();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void addFloatingView() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        binding = LayoutFloatingButtonsBinding.inflate(inflater);
        floatView = binding.getRoot();

        int flag;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            flag = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            flag = WindowManager.LayoutParams.TYPE_PHONE;
        }

        layoutParams = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                flag,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        layoutParams.gravity = Gravity.TOP | Gravity.START;
        layoutParams.x = 100;
        layoutParams.y = 200;

        binding.btnA.setOnClickListener(v -> {
            if (callback != null) {
                callback.onMessageReceived("Kirim string dari service");
            }
        });

        binding.btnB.setOnClickListener(v -> {
            if (callback != null) {
                callback.onCloseActivity();
            }
            stopSelf(); 
        });

        binding.btnC.setOnClickListener(v -> {
            if (callback != null) {
              callback.onStopService();
            }
        });

        binding.btnD.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    initialX = layoutParams.x;
                    initialY = layoutParams.y;
                    initialTouchX = event.getRawX();
                    initialTouchY = event.getRawY();
                    return true;
                case MotionEvent.ACTION_MOVE:
                    layoutParams.x = initialX + (int) (event.getRawX() - initialTouchX);
                    layoutParams.y = initialY + (int) (event.getRawY() - initialTouchY);
                    windowManager.updateViewLayout(floatView, layoutParams);
                    return true;
            }
            return false;
        });

        windowManager.addView(floatView, layoutParams);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (floatView != null) {
            windowManager.removeView(floatView);
            floatView = null;
        }
    }
}
