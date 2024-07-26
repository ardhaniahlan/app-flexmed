package org.apps.flexmed.ui.loginregis

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Patterns
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.play.integrity.internal.s
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.apps.flexmed.ui.MainActivity
import org.apps.flexmed.PreferenceManager
import org.apps.flexmed.R
import org.apps.flexmed.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseStore: FirebaseFirestore
    private lateinit var preferenceManager: PreferenceManager

    override fun onStart() {
        super.onStart()
        if (preferenceManager.isLoggedIn()){
            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseStore = FirebaseFirestore.getInstance()
        preferenceManager = PreferenceManager(this)

        setupBinding()
    }

    private fun setupBinding(){
        binding.apply {
            etPassword.addTextChangedListener(object : TextWatcher{
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    if (p0.isNullOrEmpty()) {
                        passwordToggle.visibility = ImageView.GONE
                        etPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                        passwordToggle.setImageResource(R.drawable.ic_eye_inactive)
                    } else {
                        passwordToggle.visibility = ImageView.VISIBLE
                    }
                }

                override fun afterTextChanged(p0: Editable?) {}

            })

            passwordToggle.setOnClickListener {
                if (etPassword.transformationMethod is PasswordTransformationMethod) {
                    etPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                    passwordToggle.setImageResource(R.drawable.ic_eye_active)
                } else {
                    etPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                    passwordToggle.setImageResource(R.drawable.ic_eye_inactive)
                }
                etPassword.setSelection(etPassword.text.length)
            }

            btnLogin.setOnClickListener {
                validateLogin()
            }

            btnRegister.setOnClickListener {
                startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
            }
        }
    }

    private fun validateLogin() {
        binding.apply {

            val email = etEmail.text.toString()
            val password = etPassword.text.toString()

            when {
                email.isEmpty() -> {
                    etEmail.error = "Email Harus Diisi"
                    etEmail.requestFocus()
                }

                email.contains(" ") -> {
                    etEmail.error = "Email tidak boleh mengandung spasi"
                    etEmail.requestFocus()
                }

                !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                    etEmail.error = "Email Tidak Valid"
                    etEmail.requestFocus()
                }

                password.isEmpty() -> {
                    etPassword.error = "Password Harus Diisi"
                    etPassword.requestFocus()
                }

                password.length < 6 -> {
                    etPassword.error = "Password minimal 6 digit"
                    etPassword.requestFocus()
                }

                password.contains(" ") -> {
                    etPassword.error = "Password tidak boleh mengandung spasi"
                    etPassword.requestFocus()
                }

                else -> {
                    userLogin(email, password)
                }
            }
        }
    }

    private fun userLogin(email: String, password: String) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Toast.makeText(
                        this@LoginActivity,
                        "Selamat Datang",
                        Toast.LENGTH_SHORT
                    ).show()

                    preferenceManager.setLoggedIn(true)

                    showProgressbar(true)
                    Handler(Looper.getMainLooper()).postDelayed({
                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        showProgressbar(false)
                    }, 2000)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this@LoginActivity,
                    "Periksa Email atau Password",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun showProgressbar(isLoading: Boolean){
        if (isLoading) {
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.progressBar.visibility = View.GONE
        }
    }
}