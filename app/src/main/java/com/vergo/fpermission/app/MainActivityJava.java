package com.vergo.fpermission.app;

import android.Manifest;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.vergo.fpermission.lib.FPermission;
import com.vergo.fpermission.lib.callback.RequestCallback;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MainActivityJava extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FPermission.init(this).permissions(Manifest.permission.CAMERA)
//                .explainReasonBeforeRequest()
                .onExplainRequestReason((expainPrompt, deniedList, beforeRequest) -> {
                    expainPrompt.showExplainDialog(deniedList, "FPermission需要以下权限才能继续");
                })
                .onForwardToSetting((forwardPrompt, deniedList) -> {
                    forwardPrompt.showForwardDialog(deniedList, "请在设置中允许以下权限");
                })
                .request((allGranted, grantedList, deniedList) -> {
                    if (allGranted) {
                        Toast.makeText(MainActivityJava.this, "All permissions are granted", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivityJava.this, "The following permissions are denied"+deniedList, Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
