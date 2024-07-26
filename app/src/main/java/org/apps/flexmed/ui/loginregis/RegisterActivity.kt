package org.apps.flexmed.ui.loginregis

import android.app.Activity
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.apps.flexmed.R
import org.apps.flexmed.model.User
import org.apps.flexmed.databinding.ActivityRegisterBinding
import org.apps.flexmed.ui.MainActivity

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseStore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseStore = FirebaseFirestore.getInstance()

        setupBinding()
    }

    private fun setupBinding(){
        binding.apply {
            etPassword.addTextChangedListener(object : TextWatcher {
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
                val resultIntent = Intent()
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            }

            btnRegister.setOnClickListener {
                validateRegister()
            }
        }
    }

    private fun validateRegister(){
        binding.apply {
            val email = etEmail.text.toString()
            val displayName = etDisplayName.text.toString()
            val userName = etUsername.text.toString().lowercase()
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

                displayName.isEmpty() -> {
                    etDisplayName.error = "Display Name Harus Diisi"
                    etDisplayName.requestFocus()
                }

                userName.isEmpty() -> {
                    etUsername.error = "Username Harus Diisi"
                    etUsername.requestFocus()
                }

                userName.contains(" ") -> {
                    etUsername.error = "Username tidak boleh mengandung spasi"
                    etUsername.requestFocus()
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
                    registerUser(email, displayName, userName, password)
                }
            }
        }
    }

    private fun registerUser(email: String, displayName: String, userName: String, password: String) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful){
                    val userId = firebaseAuth.currentUser?.uid
                    if (userId != null){
                        val user = User(
                            id = userId,
                            email = email,
                            displayName = displayName,
                            username = userName,
                            password = password
                        )
                        firebaseStore.collection("users").document(userId).set(user)
                            .addOnCompleteListener{
                                Toast.makeText(this, "Registrasi Berhasil",Toast.LENGTH_SHORT).show()
                                Handler(Looper.getMainLooper()).postDelayed({
                                    startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                                    showProgressbar(false)
                                }, 2000)
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Registrasi Gagal",Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    Toast.makeText(this, "Register Error", Toast.LENGTH_SHORT).show()
                }
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