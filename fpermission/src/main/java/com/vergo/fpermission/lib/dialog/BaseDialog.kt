package com.vergo.fpermission.lib.dialog

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.view.View

abstract class BaseDialog : Dialog {
    constructor(context: Context) : super(context)
    constructor(context: Context, themeResId: Int) : super(context, themeResId)
    constructor(
        context: Context,
        cancelable: Boolean,
        cancelListener: DialogInterface.OnCancelListener?
    ) : super(context, cancelable, cancelListener)

    /**
     * 返回对话框上肯定按钮的实例。您的对话框必须有一个肯定按钮才能继续请求。
     *
     * @return 对话框上肯定按钮的实例。
     */
    abstract val positiveButton: View

    /**
     * 返回对话框上的否定按钮实例。
     * 如果您请求的权限是强制性的，则对话框中不能有否定按钮。
     * 在这种情况下，您可以简单地返回null。
     *
     * @return 对话框上肯定按钮的实例；如果对话框没有否定按钮，则为null。
     */
    abstract val negativeButton: View?

    /**
     * 提供请求权限。这些权限应该是您在基本原理对话框上显示的权限。
     *
     * @return 请求的权限列表。
     */
    abstract val permissionsToRequest: List<String>
}