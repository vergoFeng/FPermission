package com.vergo.fpermission.lib

import android.os.Build
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.vergo.fpermission.lib.request.PermissionBuilder
import com.vergo.fpermission.lib.request.RequestBackgroundLocationPermission

/**
 * 权限请求调节类，支持在Activity、Fragment中请求权限；
 * 将请求权限分为了特殊权限类和普通权限类
 *
 * @author fhj
 * @since 2021/04/26
 */
class PermissionMediator {

    private var activity: FragmentActivity? = null
    private var fragment: Fragment? = null

    constructor(activity: FragmentActivity) {
        this.activity = activity
    }

    constructor(fragment: Fragment) {
        this.fragment = fragment
    }

    /**
     * 您要请求的所有权限.
     *
     * @param permissions 权限列表.
     * @return PermissionBuilder.
     */
    fun permissions(vararg permissions: String): PermissionBuilder {
        return permissions(listOf(*permissions))
    }

    fun permissions(permissions: List<String>): PermissionBuilder {
        // 普通权限列表
        val normalPermissionSet = LinkedHashSet<String>()
        // 特殊权限列表
        val specialPermissionSet = LinkedHashSet<String>()
        permissions.forEach {
            if(it in allSpecialPermissions) {
                specialPermissionSet.add(it)
            } else {
                normalPermissionSet.add(it)
            }
        }

        // 获取当前targetSdkVersion值
        val targetSdkVersion = when {
            activity != null -> activity!!.applicationInfo.targetSdkVersion
            fragment != null -> fragment!!.context!!.applicationInfo.targetSdkVersion
            else -> -1
        }
        // 处理是否要请求 ACCESS_BACKGROUND_LOCATION 权限
        if(RequestBackgroundLocationPermission.ACCESS_BACKGROUND_LOCATION in specialPermissionSet) {
            // 在Q或R上请求ACCESS_BACKGROUND_LOCATION权限
            // 但如果targetSdkVersion值在R之下的，则不需要特别请求，只需作为常规权限即可。
            if(Build.VERSION.SDK_INT == Build.VERSION_CODES.Q ||
                (Build.VERSION.SDK_INT == Build.VERSION_CODES.R && targetSdkVersion > 0 && targetSdkVersion < Build.VERSION_CODES.R)) {
                specialPermissionSet.remove(RequestBackgroundLocationPermission.ACCESS_BACKGROUND_LOCATION)
                normalPermissionSet.add(RequestBackgroundLocationPermission.ACCESS_BACKGROUND_LOCATION)
            }
        }
        return PermissionBuilder(activity, fragment, normalPermissionSet, specialPermissionSet)
    }
}