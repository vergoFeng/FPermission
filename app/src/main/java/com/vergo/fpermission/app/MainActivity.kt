package com.vergo.fpermission.app

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.vergo.fpermission.lib.FPermission

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        FPermission.init(this)
            .permissions(Manifest.permission.CAMERA)
            .explainReasonBeforeRequest()
    }
}