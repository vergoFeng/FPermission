package com.vergo.fpermission.lib.dialog

import android.Manifest
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import com.vergo.fpermission.lib.R
import com.vergo.fpermission.lib.allSpecialPermissions
import com.vergo.fpermission.lib.databinding.FpermissionDefaultDialogBinding
import com.vergo.fpermission.lib.databinding.FpermissionPermissionItemBinding
import com.vergo.fpermission.lib.permissionMapOnQ
import com.vergo.fpermission.lib.permissionMapOnR

class DefaultDialog(
        context: Context,
        private val permissions: List<String>,
        private val message: String,
        private val positiveText: String,
        private val negativeText: String?,
) : BaseDialog(context) {

    private lateinit var binding: FpermissionDefaultDialogBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FpermissionDefaultDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupWindow()
        initDialog()
    }

    override val positiveButton: View
        get() = binding.positiveBtn

    override val negativeButton: View?
        get() = negativeText?.let {
            binding.negativeBtn
        }

    override val permissionsToRequest: List<String>
        get() = permissions

    /**
     * 显示设置对话框窗口。
     * 在纵向和横向模式下控制不同的窗口大小。
     */
    private fun setupWindow() {
        val width = context.resources.displayMetrics.widthPixels
        val height = context.resources.displayMetrics.heightPixels
        if (width < height) {
            // 竖屏
            window?.let {
                val param = it.attributes
                it.setGravity(Gravity.CENTER)
                param.width = (width * 0.86).toInt()
                it.attributes = param
            }
        } else {
            // 横屏
            window?.let {
                val param = it.attributes
                it.setGravity(Gravity.CENTER)
                param.width = (width * 0.6).toInt()
                it.attributes = param
            }
        }
    }

    private fun initDialog() {
        binding.messageTv.text = message
        binding.positiveBtn.text = positiveText
        if (negativeText != null) {
            binding.negativeLayout.visibility = View.VISIBLE
            binding.negativeBtn.text = negativeText
        } else {
            binding.negativeLayout.visibility = View.GONE
        }

        buildPermissionsLayout()
    }

    /**
     * 在对话框中添加需要解释请求原因的每个权限。
     * 但是我们只需要添加权限组。因此，如果有两个权限属于一个组，则仅一项将添加到对话框中。
     */
    private fun buildPermissionsLayout() {
        val tempSet = HashSet<String>()
        val currentVersion = Build.VERSION.SDK_INT
        for (permission in permissions) {
            val permissionGroup = when(currentVersion) {
                Build.VERSION_CODES.Q -> permissionMapOnQ[permission]
                Build.VERSION_CODES.R -> permissionMapOnR[permission]
                else -> {
                    val permissionInfo = context.packageManager.getPermissionInfo(permission, 0)
                    permissionInfo.group
                }
            }
            if ((permission in allSpecialPermissions && !tempSet.contains(permission))
                    || (permissionGroup != null && !tempSet.contains(permissionGroup))) {
                val itemBinding = FpermissionPermissionItemBinding.inflate(layoutInflater, binding.permissionsLayout, false)
                when(permission) {
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION -> {
                        itemBinding.permissionNameTv.text = context.getString(R.string.fpermission_access_background_location)
                        itemBinding.permissionIconIv.setImageResource(R.drawable.fpermission_location_ic)
                    }
                    Manifest.permission.SYSTEM_ALERT_WINDOW -> {
                        itemBinding.permissionNameTv.text = context.getString(R.string.fpermission_system_alert_window)
                        itemBinding.permissionIconIv.setImageResource(R.drawable.fpermission_alert_ic)
                    }
                    Manifest.permission.WRITE_SETTINGS -> {
                        itemBinding.permissionNameTv.text = context.getString(R.string.fpermission_write_settings)
                        itemBinding.permissionIconIv.setImageResource(R.drawable.fpermission_setting_ic)
                    }
                    Manifest.permission.MANAGE_EXTERNAL_STORAGE -> {
                        itemBinding.permissionNameTv
                        itemBinding.permissionIconIv.setImageResource(R.drawable.fpermission_storage_ic)
                    }
                    else -> {
                        itemBinding.permissionNameTv.text = context.getString(context.packageManager.getPermissionGroupInfo(permissionGroup!!, 0).labelRes)
                        itemBinding.permissionIconIv.setImageResource(context.packageManager.getPermissionGroupInfo(permissionGroup, 0).icon)
                    }
                }
                binding.permissionsLayout.addView(itemBinding.root)
                tempSet.add(permissionGroup ?: permission)
            }
        }
    }
}