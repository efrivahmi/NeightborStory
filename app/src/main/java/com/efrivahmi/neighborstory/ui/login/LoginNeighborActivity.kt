package com.efrivahmi.neighborstory.ui.login

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.efrivahmi.neighborstory.data.model.NeighborModel
import com.efrivahmi.neighborstory.databinding.ActivityLoginBinding
import com.efrivahmi.neighborstory.ui.main.MainActivity
import com.efrivahmi.neighborstory.utils.NeighborFactory
import com.efrivahmi.neighborstory.utils.Result

class LoginNeighborActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var factory: NeighborFactory
    private val loginViewModel: LoginViewModel by viewModels { factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        super.onCreate(savedInstanceState)
        clickButton()

        factory = NeighborFactory.getInstance(this)

        supportActionBar?.title = barTitle
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun clickButton() {
        binding.apply {
            val email = binding.tiEmail.text.toString().trim()
            val password = binding.tiPass.text.toString().trim()
            btLogin.setOnClickListener {
                if (email.isEmpty() && password.isEmpty() && isValidEmail(email)) {
                    tiEmail.error = FILL_EMAIL
                    tiPass.error = FILL_PASSWORD
                } else {
                    showLoading()
                    uploadData()
                    loginViewModel.login()
                    moveAction()
                    showToast()
                }
            }
        }
        binding.seePassword.setOnClickListener {
            if (binding.seePassword.isChecked) {
                binding.tiPass.transformationMethod = HideReturnsTransformationMethod.getInstance()
            } else {
                binding.tiPass.transformationMethod = PasswordTransformationMethod.getInstance()
            }
        }
    }

    private fun showLoading() {
        loginViewModel.isLoading.observe(this) { result ->
            when (result) {
                is Result.Success -> {
                    val isLoading = result.data
                    binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
                }
                is Result.Error -> {
                    val errorMessage = result.error
                    showErrorDialog(errorMessage)
                    binding.progressBar.visibility = View.GONE
                }
                Result.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun showErrorDialog(errorMessage: String) {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setTitle("Error")
        dialogBuilder.setMessage(errorMessage)
        dialogBuilder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }
        val dialog = dialogBuilder.create()
        dialog.show()
    }

    private fun uploadData() {
        val email = binding.tiEmail.text.toString()
        val password = binding.tiPass.text.toString()

        if (email.isNotEmpty() && password.isNotEmpty()) {
            loginViewModel.uploadLoginData(email, password)
            loginViewModel.login.observe(this) { response ->
                when (response) {
                    is Result.Success -> {
                        val loginResult = response.data?.loginResult
                        if (loginResult != null) {
                            saveSession(NeighborModel(loginResult.name, "Bearer ${loginResult.token}", true))
                        }
                    }
                    is Result.Error -> {
                        val errorMessage = response.error
                        showErrorDialog(errorMessage)
                    }
                    Result.Loading -> {
                        showLoading()
                    }
                }
            }
        } else {
            val errorMessage = "Please enter both email and password"
            showErrorDialog(errorMessage)
        }
    }


    private fun showToast() {
        loginViewModel.toast.observe(this) {
            it.getContentIfNotHandled()?.let { toastText ->
                Toast.makeText(
                    this, toastText, Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun moveAction() {
        loginViewModel.login.observe(this@LoginNeighborActivity) { response ->
            when (response) {
                is Result.Success -> {
                    val intent = Intent(this@LoginNeighborActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                is Result.Error -> {
                    val errorMessage = response.error
                    showErrorDialog(errorMessage)
                }
                Result.Loading -> {
                    showLoading()
                }
            }
        }
    }

    private fun isValidEmail(email: CharSequence): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun saveSession(session: NeighborModel){
        loginViewModel.saveSession(session)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    companion object {
        private const val FILL_PASSWORD = "Have to fill password first"
        private const val FILL_EMAIL = "Have to fill email first"
        private const val barTitle = "Login Account"
    }
}