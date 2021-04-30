package com.vergo.fpermission.lib

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity

/**
 * 权限请求初始化类
 * 简单用法:
 * <pre>
 *   FPermission.init(activity)
 *      .permissions(Manifest.permission.READ_CONTACTS, Manifest.permission.CAMERA)
 *      .request { allGranted, grantedList, deniedList ->
 *          // TODO 处理逻辑
 *      }
 * </pre>
 *
 * @author fhj
 * @since 2021/04/26
 */
class FPermission {
    companion object {
        @JvmStatic
        fun init(activity: FragmentActivity): PermissionMediator {
            return PermissionMediator(activity)
        }

        @JvmStatic
        fun init(fragment: Fragment): PermissionMediator {
            return PermissionMediator(fragment)
        }

        /**
         * 辅助功能判断权限是否同意
         */
        @JvmStatic
        fun isGranted(context: Context, permission: String): Boolean {
            return ContextCompat.checkSelfPermission(
                context,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
}