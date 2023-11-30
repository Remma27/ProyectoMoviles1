package com.example.proyectomoviles

import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.proyectomoviles.databinding.ActivityMainBinding

import android.content.Context
import android.content.Intent
import android.widget.Button
import android.widget.Toast
import com.example.proyectomoviles.ui.users.LoginActivity
import com.google.firebase.auth.FirebaseAuth

const val valorIntentLogin = 1


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding


    var auth = FirebaseAuth.getInstance()
    var email: String? = null
    var contra: String? = null

    private lateinit var btnLogOut: Button
    private lateinit var btnAboutUs: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // intenta obtener el token del usuario del local storage, sino llama a la ventana de registro
        val prefe = getSharedPreferences("appData", Context.MODE_PRIVATE)
        email = prefe.getString("email","")
        contra = prefe.getString("contra","")

        if(email.toString().trim { it <= ' ' }.length == 0){
            val intent = Intent(this, LoginActivity::class.java)
            startActivityForResult(intent, valorIntentLogin)
        }else {
            val uid: String = auth.uid.toString()
            if (uid == "null"){
                auth.signInWithEmailAndPassword(email.toString(), contra.toString()).addOnCompleteListener {
                    if (it.isSuccessful) {
                        Toast.makeText(this,"Successful Authentication", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            obtenerDatos()
        }

        btnLogOut = findViewById(R.id.btnLogOut)
        btnAboutUs = findViewById(R.id.btnAboutUS)

        btnLogOut.setOnClickListener {
            LogOut()
        }

        btnAboutUs.setOnClickListener {
            AboutUs()
        }

    }

    private fun obtenerDatos() {
        Toast.makeText(this,"Hoping to do something important", Toast.LENGTH_LONG).show()
    }

    private fun LogOut() {
        auth.signOut()
        Toast.makeText(this, "Cerrar sesión exitosa", Toast.LENGTH_SHORT).show()

        // Redirige a la pantalla de inicio de sesión
        val intent = Intent(this, LoginActivity::class.java)
        startActivityForResult(intent, valorIntentLogin)
    }

    private fun AboutUs(){
        val intent = Intent(this, about::class.java)
        startActivity(intent)
    }

}