package com.example.auctionkingdom

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment

class ProfileImageFragment : Fragment() {

    companion object {
        private const val ARG_IMAGE_RES = "image_res"

        fun newInstance(imageRes: Int): ProfileImageFragment {
            val fragment = ProfileImageFragment()
            val args = Bundle()
            args.putInt(ARG_IMAGE_RES, imageRes)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile_image, container, false)
        val imageView: ImageView = view.findViewById(R.id.profile_image)
        val imageRes = arguments?.getInt(ARG_IMAGE_RES) ?: R.drawable.profile_image_1
        imageView.setImageResource(imageRes)
        return view
    }
}
