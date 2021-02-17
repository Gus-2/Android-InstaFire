package gus.android.instafire

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

private const val TAG = "LoginActivity"

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Firebase authentication check
        val auth = FirebaseAuth.getInstance()

        // If there is already someone connected
        if(auth.currentUser != null) {
            goPostActivity()
        }

        btnLogin.setOnClickListener {

            btnLogin.isEnabled = false

            val email = etLoginEmail.text.toString()
            val password = etLoginPassword.text.toString()

            if (email.isBlank() || password.isBlank()) {
                Toast.makeText(this, "Email/Password cannot be empty !", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // These methods are asynchronous so we need to listen to them
            // to be notified when the task is done
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    btnLogin.isEnabled = true
                    if(task.isSuccessful) {
                        Toast.makeText(this, "Success !", Toast.LENGTH_SHORT).show()
                        goPostActivity()
                    } else {
                        Log.e(TAG, "signInWithEmail", task.exception)
                        Toast.makeText(this, "Authentication failed !", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun goPostActivity() {
        Log.i(TAG, "goPostActivity")
        val intent = Intent(this, PostActivity::class.java)
        startActivity(intent)
        // It allows that when you're in the second activity, it will disable the come back to the LoginActivity
        finish()
    }
}