package com.vergo.fpermission.lib.request

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.vergo.fpermission.lib.callback.ExplainCallback
import com.vergo.fpermission.lib.callback.ForwardToSettingCallback
import com.vergo.fpermission.lib.callback.RequestCallback
import com.vergo.fpermission.lib.dialog.BaseDialog
import com.vergo.fpermission.lib.dialog.DefaultDialog
import java.util.*

/**
 * 开发人员可以使用更多API来控制FPermission函数。
 *
 * @author fhj
 * @since 2021/04/26
 */
class PermissionBuilder(
    var activity: FragmentActivity?,
    private val fragment: Fragment?,
    /**
     * 应用程序要请求的正常运行时权限
     */
    val normalPermissions: MutableSet<String>,
    /**
     * 需要通过特殊情况处理的特殊权限。如SYSTEM_ALERT_WINDOW，WRITE_SETTINGS和MANAGE_EXTERNAL_STORAGE。
     */
    val specialPermissions: MutableSet<String>
) {
    internal var currentDialog: BaseDialog? = null

    /**
     * 查找和创建的PermissionFragment的TAG
     */
    private val fragmentTag = "PermissionFragment"

    /**
     * 请求权限列表中允许的权限集合
     */
    internal val grantedPermissions: MutableSet<String> = LinkedHashSet()

    /**
     * 请求权限集合中未允许的权限集合
     */
    internal val deniedPermissions: MutableSet<String> = LinkedHashSet()

    /**
     * 请求权限集合中被永久拒绝的权限集合。 （拒绝，再也不问了）
     */
    internal val permanentDeniedPermissions: MutableSet<String> = LinkedHashSet()

    /**
     * 一些不应请求的权限将存储在此处。并在请求完成时通知用户
     */
    internal val permissionsWontRequest: MutableSet<String> = LinkedHashSet()

    /**
     * 应转发给“设置”以允许它们的权限列表。
     * 并非所有永久拒绝的权限都应转发到“设置”。只有那些认为必要的才应该这样做。
     */
    internal val forwardPermissions: MutableSet<String> = LinkedHashSet()

    /**
     * request()方法的回调。不能为null.
     */
    internal lateinit var requestCallback: RequestCallback
    internal var explainReasonBeforeRequest: Boolean = false
    internal var explainCallback: ExplainCallback? = null
    internal var forwardToSettingCallback: ForwardToSettingCallback? = null

    internal var showDialogCalled: Boolean = false

    init {
        // activity 和 fragment 不能同时为null
        if (activity == null && fragment != null) {
            activity = fragment.activity
        }
    }

    /**
     * 如果需要在请求权限前显示原因，在请求链中调用此方法，则 onExplainRequestReason 方法会在请求权限前执行
     */
    fun explainReasonBeforeRequest(): PermissionBuilder {
        this.explainReasonBeforeRequest = true
        return this
    }

    /**
     * 在权限需要说明请求原因时调用。
     * 通常，每次用户拒绝您的请求时，都会调用此方法。
     * 如果设置了explainReasonBeforeRequest()，则在权限请求之前会先运行一次。
     * beforeRequest参数会告诉您此方法当前在权限请求之前或之后。
     */
    fun onExplainRequestReason(explainCallback: ExplainCallback?): PermissionBuilder {
        this.explainCallback = explainCallback
        return this
    }

    /**
     * 当权限需要转发到“设置”以允许时调用。
     * 通常，在用户会拒绝权限，并且选中“不再询问”会调用此方法。
     * onExplainRequestReason 始终在此方法之前。
     * 如果调用了 onExplainRequestReason ，则不会在相同的请求时间内调用此方法。
     */
    fun onForwardToSetting(forwardToSettingCallback: ForwardToSettingCallback?): PermissionBuilder {
        this.forwardToSettingCallback = forwardToSettingCallback
        return this
    }

    /**
     * 执行任务链，设置请求回调
     */
    fun request(callback: RequestCallback) {
        requestCallback = callback
        val requestChain = RequestChain()
        requestChain.addTask(RequestNormalPermissions(this))
        requestChain.addTask(RequestBackgroundLocationPermission(this))
        requestChain.addTask(RequestSystemAlertWindowPermission(this))
        requestChain.addTask(RequestWriteSettingsPermission(this))
        requestChain.addTask(RequestManageExternalStoragePermission(this))
        requestChain.runTask()
    }

    /**
     * 立即请求权限
     *
     * @param permissions 要请求的权限。
     * @param task        当前任务的实例。
     */
    internal fun requestNow(permissions: Set<String>, task: BaseTask) {
        getPermissionFragment().requestNow(permissions, this, task)
    }

    internal fun requestAccessBackgroundLocationNow(task: BaseTask) {
        getPermissionFragment().requestAccessBackgroundLocationNow(this, task)
    }

    /**
     * 立即请求 SYSTEM_ALERT_WINDOW 权限
     *
     * @param task 当前任务的实例。
     */
    internal fun requestSystemAlertWindowPermissionNow(task: BaseTask) {
        getPermissionFragment().requestSystemAlertWindowPermissionNow(this, task)
    }

    /**
     * 立即请求 WRITE_SETTINGS 权限
     *
     * @param task 当前任务的实例。
     */
    internal fun requestWriteSettingsPermissionNow(task: BaseTask) {
        getPermissionFragment().requestWriteSettingsPermissionNow(this, task)
    }

    /**
     * 立即请求 MANAGE_EXTERNAL_STORAGE 权限
     *
     * @param task 当前任务的实例。
     */
    internal fun requestManageExternalStoragePermissionNow(task: BaseTask) {
        getPermissionFragment().requestManageExternalStoragePermissionNow(this, task)
    }

    /**
     * 获取请求权限Fragment
     */
    private fun getPermissionFragment(): PermissionFragment {
        val fragmentManager = getFragmentManager()
        val fragment = fragmentManager.findFragmentByTag(fragmentTag)
        return if (fragment != null) {
            fragment as PermissionFragment
        } else {
            val permissionFragment = PermissionFragment()
            fragmentManager.beginTransaction().add(permissionFragment, fragmentTag)
                .commitNowAllowingStateLoss()
            permissionFragment
        }
    }

    /**
     * 获取FragmentManager
     * 如果初始化传入的是 FragmentActivity，则为 FragmentManager；
     * 如果初始化传入的是 Fragment，则为 ChildFragmentManager
     */
    private fun getFragmentManager(): FragmentManager {
        return fragment?.childFragmentManager ?: activity!!.supportFragmentManager
    }

    /**
     * 是否应该请求ACCESS_BACKGROUND_LOCATION权限。
     *
     * @return 如果specialPermissions包含ACCESS_BACKGROUND_LOCATION权限，则为true，否则为false。
     */
    internal fun shouldRequestBackgroundLocationPermission(): Boolean {
        return specialPermissions.contains(RequestBackgroundLocationPermission.ACCESS_BACKGROUND_LOCATION)
    }

    /**
     * 是否应该请求SYSTEM_ALERT_WINDOW权限。
     *
     * @return 如果specialPermissions包含SYSTEM_ALERT_WINDOW权限，则为true，否则为false。
     */
    internal fun shouldRequestSystemAlertWindowPermission(): Boolean {
        return specialPermissions.contains(Manifest.permission.SYSTEM_ALERT_WINDOW)
    }

    /**
     * 是否应该请求WRITE_SETTINGS权限。
     *
     * @return 如果specialPermissions包含WRITE_SETTINGS权限，则为true，否则为false。
     */
    internal fun shouldRequestWriteSettingsPermission(): Boolean {
        return specialPermissions.contains(Manifest.permission.WRITE_SETTINGS)
    }

    /**
     * 是否应该请求MANAGE_EXTERNAL_STORAGE权限。
     *
     * @return 如果specialPermissions包含MANAGE_EXTERNAL_STORAGE权限，则为true，否则为false。
     */
    internal fun shouldRequestManageExternalStoragePermission(): Boolean {
        return specialPermissions.contains(RequestManageExternalStoragePermission.MANAGE_EXTERNAL_STORAGE)
    }

    /**
     * 向用户显示一个对话框，并解释为什么需要这些权限。
     *
     * @param baseTask               当前任务实例
     * @param showReasonOrGoSettings 判断显示解释原因或转至“设置”，true：显示解释原因；false：跳转至"设置"
     * @param permissions            再次请求的权限
     * @param message                向用户解释为什么需要这些权限的消息。
     * @param positiveText           肯定按钮文字，可再次请求。
     * @param negativeText           否定按钮文字。如果不应该取消此对话框，则可为null。
     */
    internal fun showHandlerPermissionDialog(
        baseTask: BaseTask,
        showReasonOrGoSettings: Boolean,
        permissions: List<String>,
        message: String,
        positiveText: String,
        negativeText: String?
    ) {
        val defaultDialog = DefaultDialog(
            activity!!,
            permissions,
            message,
            positiveText,
            negativeText
        )
        showHandlerPermissionDialog(baseTask, showReasonOrGoSettings, defaultDialog)
    }

    /**
     * 向用户显示一个对话框，并解释为什么需要这些权限。
     *
     * @param baseTask               当前任务实例
     * @param showReasonOrGoSettings 判断显示解释原因或转至“设置”，true：显示解释原因；false：跳转至"设置"
     * @param baseDialog             对话框，向用户解释为什么需要这些权限。
     */
    internal fun showHandlerPermissionDialog(
        baseTask: BaseTask,
        showReasonOrGoSettings: Boolean,
        baseDialog: BaseDialog
    ) {
        showDialogCalled = true
        val permissions = baseDialog.permissionsToRequest
        if(permissions.isEmpty()) {
            baseTask.finish()
            return
        }
        currentDialog = baseDialog
        baseDialog.show()
        val positiveButton: View = baseDialog.positiveButton
        val negativeButton: View? = baseDialog.negativeButton
        baseDialog.setCancelable(false)
        baseDialog.setCanceledOnTouchOutside(false)
        positiveButton.isClickable = true
        positiveButton.setOnClickListener {
            baseDialog.dismiss()
            if (showReasonOrGoSettings) {
                baseTask.requestAgain(permissions)
            } else {
                forwardToSettings(permissions)
            }
        }
        if (negativeButton != null) {
            negativeButton.isClickable = true
            negativeButton.setOnClickListener {
                baseDialog.dismiss()
                baseTask.finish()
            }
        }
        currentDialog!!.setOnDismissListener { currentDialog = null }
    }

    /**
     * 转到您应用的“设置”页面，以允许用户打开必要的权限。
     *
     * @param permissions 必要的权限。
     */
    private fun forwardToSettings(permissions: List<String>) {
        forwardPermissions.clear()
        forwardPermissions.addAll(permissions)
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", activity!!.packageName, null)
        intent.data = uri
        getPermissionFragment().startActivityForResult(
            intent,
            PermissionFragment.FORWARD_TO_SETTINGS
        )
    }
}