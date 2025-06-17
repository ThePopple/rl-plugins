package com.popple.runecrafter

class TaskSet(vararg tasks: Task) {
    private var taskList: MutableList<Task> = arrayListOf()

    val size: Int
        get() {
            return taskList.size
        }

    init {
        taskList.addAll(tasks)
    }

    fun addAll(vararg tasks: Task) {
        taskList.addAll(tasks)
    }

    fun clear() {
        taskList.clear()
    }

    val validTask: Task?
        /**
         * Iterates through all the tasks in the set and returns
         * the highest priority valid task.
         *
         * @return The first valid task from the task list or null if no valid task.
         */
        get() {
            for (task in this.taskList) {
                if (task.validate()) {
                    return task
                }
            }
            return null
        }
}
