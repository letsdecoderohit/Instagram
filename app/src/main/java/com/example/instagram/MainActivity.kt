package com.example.instagram

import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.nav_home -> {
                //moveToFragment(HomeFragment())
                message.text = "Home"
                return@OnNavigationItemSelectedListener true
            }
            R.id.nav_search -> {
                //moveToFragment(SearchFragment())
                message.text = "Search"
                return@OnNavigationItemSelectedListener true
            }
            R.id.nav_add_post -> {
                message.text = "Add_Post"
                return@OnNavigationItemSelectedListener true
            }
            R.id.nav_notifications -> {
                message.text = "Notifications"
                //moveToFragment(NotificationsFragment())
                return@OnNavigationItemSelectedListener true
            }
            R.id.nav_profile -> {
                //moveToFragment(ProfileFragment())
                message.text = "Profile"
                return@OnNavigationItemSelectedListener true
            }
        }

        false
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        navView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)

    }
}