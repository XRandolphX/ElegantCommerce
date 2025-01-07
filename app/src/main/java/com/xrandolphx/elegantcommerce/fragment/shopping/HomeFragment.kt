package com.xrandolphx.elegantcommerce.fragment.shopping

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayoutMediator
import com.xrandolphx.elegantcommerce.R
import com.xrandolphx.elegantcommerce.adapter.HomeViewpagerAdapter
import com.xrandolphx.elegantcommerce.databinding.FragmentHomeBinding
import com.xrandolphx.elegantcommerce.fragment.categories.MainCategoryFragment
import com.xrandolphx.elegantcommerce.fragment.categories.PantsFragment
import com.xrandolphx.elegantcommerce.fragment.categories.ShirtFragment
import com.xrandolphx.elegantcommerce.fragment.categories.ShoesFragment
import com.xrandolphx.elegantcommerce.fragment.categories.TieFragment


class HomeFragment : Fragment(R.layout.fragment_home) {
    private lateinit var binding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val categoriesFragments = arrayListOf<Fragment>(
            MainCategoryFragment(),
            ShirtFragment(),
            PantsFragment(),
            ShoesFragment(),
            TieFragment(),
        )

        binding.viewpagerHome.isUserInputEnabled = false

        val viewPager2Adapter =
            HomeViewpagerAdapter(categoriesFragments, childFragmentManager, lifecycle)
        binding.viewpagerHome.adapter = viewPager2Adapter
        TabLayoutMediator(binding.tabLayout, binding.viewpagerHome) { tab, position ->
            when (position) {
                0 -> tab.text = "Principal"
                1 -> tab.text = "Camisas"
                2 -> tab.text = "Pantalones"
                3 -> tab.text = "Zapatos"
                4 -> tab.text = "Corbatas"
                5 -> tab.text = "Ternos"

            }
        }.attach()
    }
}