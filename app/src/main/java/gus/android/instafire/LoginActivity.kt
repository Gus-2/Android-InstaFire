package gus.android.instafire

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

        btnLogin.setOnClickListener {
            val email = etLoginEmail.text.toString()
            val password = etLoginPassword.text.toString()

            if (email.isBlank() || password.isBlank()) {
                Toast.makeText(this, "Email/Password cannot be empty !", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Firebase authentication check
            val auth = FirebaseAuth.getInstance()
            // These methods are asynchronous so we need to listen to them
            // to be notified when the task is done
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
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
    }
}