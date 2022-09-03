package com.absut.todo.ui.addedittask

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.absut.todo.R
import com.absut.todo.databinding.FragmentAddEditBinding

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class AddEditFragment : Fragment(R.layout.fragment_add_edit) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentAddEditBinding.bind(view)


    }

}