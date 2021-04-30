package com.vergo.fpermission.lib.callback

/**
 * 请求权限回调接口
 *
 * @author fhj
 * @since 2021/04/27
 */
interface RequestCallback {
    /**
     * 回调请求结果。
     * @param allGranted  指示是否授予了所有权限。
     * @param grantedList 用户授予的所有权限。
     * @param deniedList  用户拒绝的所有权限。
     */
    fun onResult(allGranted: Boolean, grantedList: List<String>, deniedList: List<String>)
}