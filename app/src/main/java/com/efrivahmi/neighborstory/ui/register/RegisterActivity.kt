package com.efrivahmi.neighborstory.ui.register

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.efrivahmi.neighborstory.databinding.ActivityRegisterBinding
import com.efrivahmi.neighborstory.ui.welcome.WelcomeNeighborActivity
import com.efrivahmi.neighborstory.utils.NeighborFactory
import com.efrivahmi.neighborstory.utils.Result

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var factory: NeighborFactory
    private val registerViewModel: RegisterViewModel by viewModels { factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        factory = NeighborFactory.getInstance(this)

        supportActionBar?.title = barTitle
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        clickButton()
    }

    private fun clickButton() {
        binding.btRegister.setOnClickListener {
            val name = binding.tiName.text.toString().trim()
            val email = binding.tiEmail.text.toString().trim()
            val password = binding.tiPass.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || !isValidEmail(email)) {
                binding.tiName.error = FILL_NAME
                binding.tiEmail.error = FILL_EMAIL
                binding.tiPass.error = FILL_PASSWORD
            } else {
                showLoading()
                uploadData(name, email, password)
                showToast()
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
        registerViewModel.isLoading.observe(this) { result ->
            when (result) {
                is Result.Success -> {
                    val isLoading = result.data
                    binding.progressBar2.visibility = if (isLoading) View.VISIBLE else View.GONE
                }
                is Result.Error -> {
                    val errorMessage = result.error
                    showErrorDialog(errorMessage)
                    binding.progressBar2.visibility = View.GONE
                }
                Result.Loading -> {
                    binding.progressBar2.visibility = View.VISIBLE
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

    private fun showToast() {
        registerViewModel.toast.observe(this) {
            it.getContentIfNotHandled()?.let { toastText ->
                Toast.makeText(
                    this, toastText, Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun uploadData(name: String, email: String, password: String) {
        registerViewModel.uploadRegisData(name, email, password)
        registerViewModel.regis.observe(this) { response ->
            when (response) {
                is Result.Success -> {
                    startActivity(Intent(this, WelcomeNeighborActivity::class.java))
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

    companion object {
        private const val FILL_NAME = "Have to fill your name"
        private const val FILL_PASSWORD = "Have to fill password first"
        private const val FILL_EMAIL = "Have to fill email first"
        private const val barTitle = "Create Account"
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }
}