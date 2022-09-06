@file:OptIn(ExperimentalCoroutinesApi::class)

package com.absut.todo.ui.task

import androidx.lifecycle.*
import com.absut.todo.data.PreferenceManager
import com.absut.todo.data.SortOrder
import com.absut.todo.data.Task
import com.absut.todo.data.TaskRepository
import com.absut.todo.util.Constant.ADD_TASK_RESULT_OK
import com.absut.todo.util.Constant.EDIT_TASK_RESULT_OK
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val preferenceManager: PreferenceManager,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    val searchQuery = savedStateHandle.getLiveData("searchQuery", "")
    val preferenceFlow = preferenceManager.preferencesFlow

    private val _tasksEvent = MutableSharedFlow<TasksEvent>()
    val tasksEvent: SharedFlow<TasksEvent> = _tasksEvent


    private val taskFlow = combine(
        searchQuery.asFlow(),
        preferenceFlow
    ) { query, preference ->
        Pair(query, preference)
    }.flatMapLatest { (query, filterPreferences) ->
        taskRepository.getTasks(query, filterPreferences.sortOrder, filterPreferences.hideCompleted)
    }

    val tasks = taskFlow.asLiveData()

    fun onSortOrderSelected(sortOrder: SortOrder) = viewModelScope.launch {
        preferenceManager.updateSortOrder(sortOrder)
    }

    fun onHideCompletedClick(hideCompleted: Boolean) = viewModelScope.launch {
        preferenceManager.updateHideCompleted(hideCompleted)
    }

    fun onTaskCheckedChanged(task: Task, isChecked: Boolean) = viewModelScope.launch {
        taskRepository.updateTask(task.copy(completed = isChecked))
    }

    fun onTaskSwiped(task: Task) = viewModelScope.launch {
        taskRepository.deleteTask(task)
        _tasksEvent.emit(TasksEvent.ShowUndoDeleteTaskMessage(task))
    }

    fun onUndoDeleteClick(task: Task) = viewModelScope.launch {
        taskRepository.insertTask(task)
    }

    fun onAddEditResult(result: Int) {
        when (result) {
            ADD_TASK_RESULT_OK -> showTaskSavedConfirmationMessage("Task added")
            EDIT_TASK_RESULT_OK -> showTaskSavedConfirmationMessage("Task updated")
        }
    }

    private fun showTaskSavedConfirmationMessage(text: String) = viewModelScope.launch {
        _tasksEvent.emit(TasksEvent.ShowTaskSavedConfirmationMessage(text))
    }

    fun deleteAllCompletedTask() = viewModelScope.launch {
        taskRepository.deleteCompletedTasks()
    }


    sealed class TasksEvent {
        data class ShowUndoDeleteTaskMessage(val task: Task) : TasksEvent()
        data class ShowTaskSavedConfirmationMessage(val msg: String) : TasksEvent()
    }

}