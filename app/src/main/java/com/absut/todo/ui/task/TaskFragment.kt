package com.absut.todo.ui.task

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.absut.todo.R
import com.absut.todo.data.SortOrder
import com.absut.todo.data.Task
import com.absut.todo.databinding.FragmentTaskBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
@AndroidEntryPoint
class TaskFragment : Fragment(R.layout.fragment_task), TaskAdapter.OnItemClickListener,
    MenuProvider {

    private val viewModel by viewModels<TaskViewModel>()
    private lateinit var searchView: SearchView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentTaskBinding.bind(view)

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

        val taskAdapter = TaskAdapter(this)
        binding.apply {
            recyclerView.apply {
                adapter = taskAdapter
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
            }

            ItemTouchHelper(object :
                ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val task = taskAdapter.currentList[viewHolder.adapterPosition]
                    viewModel.onTaskSwiped(task)
                }
            }).attachToRecyclerView(recyclerView)

            fab.setOnClickListener {
                val action = TaskFragmentDirections.actionHomeFragmentToAddEditFragment(null)
                findNavController().navigate(action)

            }
        }

        setFragmentResultListener("add_edit_request") { _, bundle ->
            val result = bundle.getInt("add_edit_result")

            viewModel.onAddEditResult(result)
            Log.d("TAG", "onViewCreated: $result")
        }


        viewModel.tasks.observe(viewLifecycleOwner) {
            taskAdapter.submitList(it)
            binding.emptyView.isVisible = it.isEmpty()
        }

        observeEvents()

    }

    private fun observeEvents() {
        lifecycleScope.launchWhenStarted {
            viewModel.tasksEvent.collectLatest { event ->
                when (event) {
                    is TaskViewModel.TasksEvent.ShowTaskSavedConfirmationMessage -> {
                        Log.d("TAG", "observeEvents: ${event.msg}")
                        Snackbar.make(requireView(), event.msg, Snackbar.LENGTH_SHORT).show()
                        Log.d("TAG", "observeEvents: ${event.msg}")
                    }
                    is TaskViewModel.TasksEvent.ShowUndoDeleteTaskMessage -> {
                        Snackbar.make(requireView(), "Task deleted", Snackbar.LENGTH_LONG)
                            .setAction("Undo") {
                                viewModel.onUndoDeleteClick(event.task)
                            }.show()
                    }
                }
            }
        }
    }


    override fun onItemClick(task: Task) {
        val action = TaskFragmentDirections.actionHomeFragmentToAddEditFragment(task, "Edit Task")
        findNavController().navigate(action)
    }

    override fun onCheckBoxClick(task: Task, isChecked: Boolean) {
        viewModel.onTaskCheckedChanged(task, isChecked)
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.menu_main, menu)

        val searchItem = menu.findItem(R.id.action_search)
        searchView = searchItem.actionView as SearchView

        val pendingQuery = viewModel.searchQuery.value
        if (pendingQuery != null && pendingQuery.isNotEmpty()) {
            searchItem.expandActionView()
            searchView.setQuery(pendingQuery, false)
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.searchQuery.value = newText
                return true
            }
        })

        viewLifecycleOwner.lifecycleScope.launch {
            menu.findItem(R.id.action_hide_completed).isChecked =
                viewModel.preferenceFlow.first().hideCompleted
        }

    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.action_sort_by_name -> {
                viewModel.onSortOrderSelected(SortOrder.BY_NAME)
                true
            }
            R.id.action_sort_by_date_created -> {
                viewModel.onSortOrderSelected(SortOrder.BY_DATE)
                true
            }
            R.id.action_hide_completed -> {
                menuItem.isChecked = !menuItem.isChecked
                viewModel.onHideCompletedClick(menuItem.isChecked)
                true
            }
            R.id.action_delete_all_completed -> {
                deleteAllCompletedTask()
                true
            }
            else -> false
        }
    }

    private fun deleteAllCompletedTask() {
        MaterialAlertDialogBuilder(requireActivity())
            .setTitle("Delete all completed task?")
            .setMessage("This action cannot be undone")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteAllCompletedTask()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        searchView.setOnQueryTextListener(null)
    }


}