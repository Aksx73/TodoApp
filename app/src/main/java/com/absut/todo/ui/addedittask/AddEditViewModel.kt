package com.absut.todo.ui.addedittask

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.absut.todo.data.Task
import com.absut.todo.data.TaskRepository
import com.absut.todo.util.Constant.ADD_TASK_RESULT_OK
import com.absut.todo.util.Constant.EDIT_TASK_RESULT_OK
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEditViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _addEditTaskEvent = MutableSharedFlow<AddEditTaskEvent>()
    val addEditTaskEvent : SharedFlow<AddEditTaskEvent> = _addEditTaskEvent


    val task = savedStateHandle.get<Task>("task")

    var taskName = savedStateHandle.get<String>("taskName") ?: task?.name ?: ""
        set(value) {
            field = value
            savedStateHandle.set("taskName", value)
        }

    var taskImportance = savedStateHandle.get<Boolean>("taskImportance") ?: task?.important ?: false
        set(value) {
            field = value
            savedStateHandle.set("taskImportance", value)
        }

    fun onSaveClick() {
        if (taskName.isBlank()) {
            showInvalidInputMessage("Name cannot be empty")
            return
        }

        if (task != null) {
            val updatedTask = task.copy(name = taskName, important = taskImportance)
            updateTask(updatedTask)
        } else {
            val newTask = Task(name = taskName, important = taskImportance)
            createTask(newTask)
        }
    }

    fun isTaskValid(): Boolean {
        if (taskName.isBlank()) {
            return false
        }
        return true
    }

    private fun createTask(task: Task) = viewModelScope.launch {
        taskRepository.insertTask(task)
        _addEditTaskEvent.emit(AddEditTaskEvent.NavigateBackWithResult(ADD_TASK_RESULT_OK))
    }

    private fun updateTask(task: Task) = viewModelScope.launch {
        taskRepository.updateTask(task)
        _addEditTaskEvent.emit(AddEditTaskEvent.NavigateBackWithResult(EDIT_TASK_RESULT_OK))
    }

    private fun showInvalidInputMessage(msg: String) = viewModelScope.launch {
        _addEditTaskEvent.emit(AddEditTaskEvent.ShowInvalidInputMessage(msg))
    }

    sealed class AddEditTaskEvent {
        data class ShowInvalidInputMessage(val msg: String) : AddEditTaskEvent()
        data class NavigateBackWithResult(val result: Int) : AddEditTaskEvent()
    }

}