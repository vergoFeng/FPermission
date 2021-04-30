package com.vergo.fpermission.lib.request

/**
 * 组装请求权限任务链
 *
 * @author fhj
 * @since 2021/04/27
 */
class RequestChain {

    /**
     * 第一项任务，权限请求从此处开始。
     */
    private var headTask: BaseTask? = null

    /**
     * 最后一个任务，权限请求到此结束。
     */
    private var tailTask: BaseTask? = null

    /**
     * 将任务添加到任务链。
     */
    fun addTask(task: BaseTask) {
        if(headTask == null) {
            headTask = task
        }
        tailTask?.setNext(task)
        tailTask = task
    }

    /**
     * 从第一项任务开始运行此任务连
     */
    fun runTask() {
        headTask?.request()
    }
}