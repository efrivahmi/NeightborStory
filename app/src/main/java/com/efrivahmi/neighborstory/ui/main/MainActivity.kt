package com.efrivahmi.neighborstory.ui.main

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.efrivahmi.neighborstory.R
import com.efrivahmi.neighborstory.data.response.ListStoryItem
import com.efrivahmi.neighborstory.databinding.ActivityMainBinding
import com.efrivahmi.neighborstory.paging.LoadingStateAdapter
import com.efrivahmi.neighborstory.ui.add.AddNeighborActivity
import com.efrivahmi.neighborstory.ui.maps.MapsActivity
import com.efrivahmi.neighborstory.ui.welcome.WelcomeNeighborActivity
import com.efrivahmi.neighborstory.utils.NeighborFactory
import com.efrivahmi.neighborstory.utils.Result


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var rvNeighbor: RecyclerView
    private lateinit var factory: NeighborFactory
    private lateinit var adapterNeighbor: NeighborListAdapter
    private val mainViewModel: MainViewModel by viewModels { factory }
    private var token = ""
    private lateinit var neighbor: ArrayList<ListStoryItem>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        factory = NeighborFactory.getInstance(this)

        val layoutManager = LinearLayoutManager(this)
        binding.rvNeighbor.layoutManager = layoutManager
        val itemDecoration = DividerItemDecoration(this, layoutManager.orientation)
        binding.rvNeighbor.addItemDecoration(itemDecoration)

        rvNeighbor = findViewById(R.id.rv_neighbor)
        binding.rvNeighbor.setHasFixedSize(true)

        ourAdapter()
        createSetup()
        isLogin()

        mainViewModel.listNeighbor.observe(this) { result ->
            when (result) {
                is Result.Success -> {
                    neighbor = result.data?.listStory as ArrayList<ListStoryItem>
                    Log.d("story", neighbor.toString())
                }
                is Result.Error -> {
                    showErrorToast(result.error)
                }
                else -> {}
            }
        }
    }

    private fun showErrorToast(errorMessage: String) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return try {
            when (item.itemId) {
                R.id.menu_language -> {
                    startActivity(Intent(Settings.ACTION_LOCALE_SETTINGS))
                    true
                }
                R.id.menu_logout -> {
                    mainViewModel.neighborLogout()
                    true
                }
                R.id.menu_location -> {
                    val i = Intent(this, MapsActivity::class.java)
                    i.putParcelableArrayListExtra("location", neighbor)
                    startActivity(i)
                    return true
                }
                else -> super.onOptionsItemSelected(item)
            }
        } catch (e: Exception) {
            false
        }
    }

    private fun isLogin() {
        showLoading(true)
        mainViewModel.getNeighbor().observe(this@MainActivity) { result ->
            when (result) {
                is Result.Success -> {
                    token = result.data.token
                    if (!result.data.isLogin) {
                        moveActivity()
                    } else {
                        showLoading(false)
                        mainViewModel.createStory(token)
                    }
                }
                is Result.Error -> {
                    Log.e("MainActivity", "Error: ${result.error}")
                    showErrorToast(result.error)
                }
                else -> {}
            }
        }
    }

    private fun ourAdapter() {
        adapterNeighbor = NeighborListAdapter()
        binding.rvNeighbor.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = adapterNeighbor.withLoadStateFooter(
                footer = LoadingStateAdapter {
                    adapterNeighbor.retry()
                }
            )
        }
        mainViewModel.getListStories.observe(this) { pagingData ->
            adapterNeighbor.submitData(lifecycle, pagingData)
        }
        mainViewModel.getListStories
    }

    private fun createSetup() {
        binding.createStory.setOnClickListener {
            startActivity(Intent(this, AddNeighborActivity::class.java))
        }
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBar3.visibility = View.VISIBLE
        } else {
            binding.progressBar3.visibility = View.GONE
        }
    }

    private fun moveActivity() {
        startActivity(Intent(this, WelcomeNeighborActivity::class.java))
        finish()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_first, menu)
        return true
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }
}
