package com.project.sentuhadvance;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.project.sentuhadvance.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity implements FloatingService.ServiceCallback {
    private ActivityMainBinding binding;
    private FloatingService floatingService;
    private boolean isBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.btnStartService.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, FloatingService.class);
            startService(intent);
            bindService(intent, connection, Context.BIND_AUTO_CREATE);
        });
    }

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            FloatingService.LocalBinder binder = (FloatingService.LocalBinder) service;
            floatingService = binder.getService();
            floatingService.setCallback(MainActivity.this);
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            floatingService = null;
            isBound = false;
        }
    };

    @Override
    public void onMessageReceived(String message) {
        runOnUiThread(() -> binding.tvMessage.setText(message));
    }

    @Override
    public void onCloseActivity() {
        finish();
    }

    @Override
    public void onStopService() {
        if (isBound) {
            unbindService(connection);
            isBound = false;
        }
        stopService(new Intent(this, FloatingService.class));
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isBound) {
            unbindService(connection);
            isBound = false;
        }
    }
}