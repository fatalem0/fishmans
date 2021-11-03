package com.example.hseobshaga.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.ArrayAdapter
import android.widget.SpinnerAdapter
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.example.hseobshaga.R
import com.example.hseobshaga.data.User
import com.example.hseobshaga.databinding.ActivityRegistrationBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.FirebaseAuthUserCollisionException

import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException

import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import java.lang.Exception


class RegistrationActivity : AppCompatActivity() {

    var data = ArrayList<String>()
    private lateinit var binding: ActivityRegistrationBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrationBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        setRoomsData()
        auth = FirebaseAuth.getInstance()
    }

    private fun setRoomsData(){ Firebase.database.getReference("").child("rooms").get()
        .addOnCompleteListener{
            it.result.children.forEach{
                data.add("Room " + it.key.toString())
            }
            val adapter = ArrayAdapter(this, R.layout.spinner_item,R.id.roomId,data)
            binding.spinner.adapter = adapter
        }
    }
    override fun onStart() {
        super.onStart()
        binding.registritationBtn.setOnClickListener {
            Log.d("lol","HelloFromOnCreate")
            checkLoginInfoAndRegister()
        }
    }
    private fun createUser(mail : String, password : String){
        auth.createUserWithEmailAndPassword(mail, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val userAuth = auth.currentUser
                    updateUserInfo(userAuth!!)
                    updateUI(userAuth)
                } else {
                    try {
                        throw task.exception!!
                    }
                    catch (existEmail: FirebaseAuthUserCollisionException) {
                        binding.mail.error = "Этот электронный адрес уже используется"
                        binding.mail.requestFocus()
                    }
                    catch (e :Exception){
                        Toast.makeText(this,"Что-то пошло не так", Toast.LENGTH_SHORT).show()
                    }

                }
            }

    }

    private fun updateUserInfo(userAuth: FirebaseUser){
        FirebaseDatabase.getInstance().getReference("users")
            .child(userAuth.uid).setValue(User(
                binding.firstName.text.toString(),
                binding.secondName.text.toString(),
                binding.mail.text.toString(),
                binding.spinner.selectedItem.toString()
            ))
        FirebaseDatabase.getInstance().getReference("rooms")
            .child(binding.spinner.selectedItem.toString().substring(5))
            .child(userAuth.uid)
            .setValue(
                binding.firstName.text.toString() + " " + binding.secondName.text.toString()
            )
    }

    private fun updateUI(user: FirebaseUser?){
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
    }

    private fun checkLoginInfoAndRegister(){
        val firstName = binding.firstName.text.toString()
        val secondName = binding.secondName.text.toString()
        val mail = binding.mail.text.toString()
        val password = binding.password.text.toString()

        if (firstName.isEmpty()){
            binding.firstName.error = "Введите имя"
            binding.firstName.requestFocus()
            return
        }
        if (secondName.isEmpty()){
            binding.firstName.error = "Введите фамилию"
            binding.firstName.requestFocus()
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(mail).matches()){
            binding.mail.error = "Невверный email"
            binding.mail.requestFocus()
            return
        }
        if (password.isEmpty()){
            binding.password.error = "Введите пароль"
            binding.password.requestFocus()
            return
        }

        if (password.length < 6){
            binding.password.error = "Слишком короткий пароль"
            binding.password.requestFocus()
            return
        }
        createUser(mail,password)
    }
}