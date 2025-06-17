package com.popple.kraken.api

class TaskSet(vararg tasks: Task) {
    private var taskList: MutableList<Task> = java.util.ArrayList<Task>()

    init {
        taskList.addAll(listOf(*tasks))
    }

    fun addAll(vararg tasks: Task) {
        taskList.addAll(listOf(*tasks))
    }

    fun clear() {
        taskList.clear()
    }

    fun getValidTasks(): List<Task> {
        val tasks: MutableList<Task> = mutableListOf()

        for (task in this.taskList) {
            if (task.validate()) {
                tasks.add(task)
            }
        }
        return tasks.toList()
    }
}
