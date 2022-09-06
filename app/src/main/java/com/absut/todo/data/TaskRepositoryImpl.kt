package com.absut.todo.data

import com.absut.todo.data.local.TaskDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 *  Implementation of [TaskRepository]. Single entry point for managing tasks data.
 */
class TaskRepositoryImpl @Inject constructor(private val taskDao: TaskDao) : TaskRepository {

    override fun getTasks(
        query: String,
        sortOrder: SortOrder,
        hideCompleted: Boolean
    ): Flow<List<Task>> {
        return taskDao.getTasks(query, sortOrder, hideCompleted)
    }

    override suspend fun updateTask(task: Task) = taskDao.update(task)

    override suspend fun deleteTask(task: Task) = taskDao.delete(task)

    override suspend fun deleteCompletedTasks() = taskDao.deleteCompletedTasks()

    override suspend fun insertTask(task: Task) = taskDao.insert(task)
}