package com.example.proyectomoviles.ui.users

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.proyectomoviles.R
import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AlertDialog
import android.content.Intent
import android.widget.Button
import android.widget.EditText
import android.widget.TextView

import com.google.firebase.auth.FirebaseAuth
import android.widget.Toast
import com.example.proyectomoviles.MainActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date

const val valorIntentSignup = 1
const val RC_SIGN_IN = 9001

class LoginActivity : AppCompatActivity() {

    var auth = FirebaseAuth.getInstance()

    private lateinit var btnAutenticar: Button
    private lateinit var txtEmail: EditText
    private lateinit var txtContra: EditText
    private lateinit var txtRegister: TextView
    private lateinit var btnLoginWithGoogle: Button
    var db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        btnAutenticar = findViewById(R.id.btnAutenticar)
        txtEmail = findViewById(R.id.txtName)
        txtContra = findViewById(R.id.txtSchoolGrade)
        txtRegister = findViewById(R.id.txtRegister)
        btnLoginWithGoogle = findViewById(R.id.btnAddStudent)

        txtRegister.setOnClickListener {
            goToSignup()
        }
        btnLoginWithGoogle.setOnClickListener {
            signInWithGoogle()
        }

        btnAutenticar.setOnClickListener {
            if(txtEmail.text.isNotEmpty() && txtContra.text.isNotEmpty()){
                auth.signInWithEmailAndPassword(txtEmail.text.toString(), txtContra.text.toString()).addOnCompleteListener{
                    if (it.isSuccessful){
                        val dt: Date = Date()

                        val user = hashMapOf(
                            "ultAcceso" to dt.toString()
                        )

                        db.collection("datosUsuarios").whereEqualTo("idemp", it.result?.user?.uid.toString()).get()
                            .addOnSuccessListener { documentReference ->
                                documentReference.forEach { document ->
                                    db.collection("datosUsuarios").document(document.id).update(user as Map<String, Any>)
                                }
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this,"Error updating user data", Toast.LENGTH_SHORT).show()
                            }

                        //Register the data into the local storage
                        val prefe = this.getSharedPreferences("appData", Context.MODE_PRIVATE)

                        //Create editor object for write app data
                        val editor = prefe.edit()

                        //Set editor fields with the new values
                        editor.putString("email", txtEmail.text.toString())
                        editor.putString("contra", txtContra.text.toString())

                        //Write app data
                        editor.commit()

                        // call back to main activity
                        Intent().let {
                            setResult(Activity.RESULT_OK)
                            finish()
                        }
                    }else{
                        showAlert("Error","When authenticating the user")
                    }
                }
            }else{
                showAlert("Error","Email and password cannot be empty")
            }
        }
    }

    private fun goToSignup() {
        val intent = Intent(this, SignupActivity::class.java)
        startActivityForResult(intent, valorIntentSignup)
    }

    private fun showAlert(titu:String, mssg: String){
        val diagMessage = AlertDialog.Builder(this)
        diagMessage.setTitle(titu)
        diagMessage.setMessage(mssg)
        diagMessage.setPositiveButton("Accept", null)

        val diagVentana: AlertDialog = diagMessage.create()
        diagVentana.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // validate control variables
        if(resultCode == Activity.RESULT_OK){
            // call back to main activity
            Intent().let {
                setResult(Activity.RESULT_OK)
                finish()
            }
        }
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                checkUserInFirebase(account.id!!)
            } catch (e: ApiException) {
                Toast.makeText(this, "Google sign in failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun signInWithGoogle() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(this, gso)
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun checkUserInFirebase(userId: String) {
        auth.fetchSignInMethodsForEmail("$userId@gmail.com")
            .addOnCompleteListener { result ->
                if (result.isSuccessful) {
                    val signInMethods = result.result?.signInMethods
                    if (signInMethods.isNullOrEmpty()) {
                        // El usuario no está registrado, crea el usuario en Firebase
                        createUserInFirebase(userId)
                    } else {
                        // El usuario ya está registrado, redirige al MainActivity
                        redirectToMainActivity()
                    }
                } else {
                    Toast.makeText(this, "Error checking user in Firebase", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun createUserInFirebase(userId: String) {
        // Implementa la lógica para crear el usuario en Firebase
        // ...

        // Después de crear el usuario, redirigimos al MainActivity
        redirectToMainActivity()
    }

    private fun redirectToMainActivity() {
        val loginIntent = Intent(this, MainActivity::class.java)
        startActivity(loginIntent)
        finish()
    }
}