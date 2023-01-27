package com.example.ecoroute.ui.user

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.ecoroute.R
import com.example.ecoroute.ui.home.HomeFragment
import com.example.ecoroute.ui.home.HomeViewModel
import com.example.ecoroute.utils.ApplicationUtils
import com.example.ecoroute.utils.UiUtils

@SuppressLint("LogNotTimber", "StringFormatInvalid", "SetTextI18n")
class UserFragment : Fragment() {

    private val TAG = HomeFragment::class.java.simpleName
    private lateinit var root: View
    private val uiUtilInstance = UiUtils()
    private val viewModel: UserViewModel by viewModels()
    private lateinit var pb: ProgressBar
    private val ctx = ApplicationUtils.getContext()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.fragment_user, container, false)

        return root
    }
}
