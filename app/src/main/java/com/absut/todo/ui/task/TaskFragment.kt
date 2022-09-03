package com.absut.todo.ui.task

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.absut.todo.R
import com.absut.todo.databinding.FragmentTaskBinding

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class TaskFragment : Fragment(R.layout.fragment_task) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentTaskBinding.bind(view)

    }

    override fun onDestroyView() {
        super.onDestroyView()

    }
}