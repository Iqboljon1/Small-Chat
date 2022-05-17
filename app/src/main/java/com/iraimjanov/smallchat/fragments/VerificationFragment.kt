package com.iraimjanov.smallchat.fragments

import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.database.*
import com.iraimjanov.smallchat.data.NetworkHelper
import com.iraimjanov.smallchat.data.PublicData
import com.iraimjanov.smallchat.data.PublicData.resendToken
import com.iraimjanov.smallchat.data.PublicData.sentCode
import com.iraimjanov.smallchat.data.PublicData.storedVerificationId
import com.iraimjanov.smallchat.databinding.FragmentVerificationBinding
import com.iraimjanov.smallchat.models.Users
import com.iraimjanov.smallchat.R
import java.util.concurrent.TimeUnit

class VerificationFragment : Fragment() {
    private lateinit var binding: FragmentVerificationBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var firebaseReference: DatabaseReference
    private lateinit var networkHelper: NetworkHelper
    private val timerInvalidCode = object : CountDownTimer(10000, 1000) {
        override fun onTick(p0: Long) {}
        override fun onFinish() {
            binding.tvFailureDescription.setText(R.string.code_received)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentVerificationBinding.inflate(layoutInflater)
        networkHelper = NetworkHelper(requireActivity())

        if (networkHelper.isNetworkConnected()){
            connection()
        }else{
            findNavController().navigate(R.id.action_verificationFragment_to_noInternetFragment)
        }

        return binding.root
    }

    private fun connection() {
        auth = FirebaseAuth.getInstance()
        auth.setLanguageCode("en")
        firebaseDatabase = FirebaseDatabase.getInstance()
        firebaseReference = firebaseDatabase.getReference("users")
        binding.tvNumber.text = PublicData.number
        binding.swipeRefreshLayout.isEnabled = false
        binding.imageBack.setOnClickListener {
            findNavController().popBackStack()
        }

        writeCode()

        sentNumberFirebase()

        binding.tvFailureDescription.setOnClickListener {
            if (binding.tvFailureDescription.text == "Code not receiver?") {
                sentNumberFirebase()
            }
        }
    }

    private fun sentNumberFirebase() {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(PublicData.number)       // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(requireActivity())                 // Activity (for callback binding)
            .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            // This callback will be invoked in two situations:
            // 1 - Instant verification. In some cases the phone number can be instantly
            //     verified without needing to send or enter a verification code.
            // 2 - Auto-retrieval. On some devices Google Play services can automatically
            //     detect the incoming verification SMS and perform verification without
            //     user action.
            Log.d(TAG, "onVerificationCompleted:$credential")
            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            Log.w(TAG, "onVerificationFailed", e)
            binding.tvFailureDescription.setText(R.string.code_received)

            if (e is FirebaseAuthInvalidCredentialsException) {

            } else if (e is FirebaseTooManyRequestsException) {
                buildDailyLimitOverDialog()
            }
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken,
        ) {
            Log.d(TAG, "onCodeSent:$verificationId")
            binding.tvFailureDescription.setText(R.string.code_sent)
            storedVerificationId = verificationId
            resendToken = token
        }
    }

    private fun buildDailyLimitOverDialog() {
        val builder = AlertDialog.Builder(requireActivity())
        builder.setMessage("Today’s limit of authentication is over. Try tomorrow or enter another number")
            .setCancelable(false)
            .setPositiveButton("Ok") { _, _ ->
                findNavController().popBackStack()
            }
        val alert: AlertDialog = builder.create()
        alert.show()
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        binding.swipeRefreshLayout.isRefreshing = true
        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    firebaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            binding.swipeRefreshLayout.isRefreshing = false
                            if (checkUserAddedFirebaseDatabase(snapshot)) {
                                findNavController().navigate(R.id.action_verificationFragment_to_homeFragment)
                            } else {
                                findNavController().navigate(R.id.action_verificationFragment_to_createProfileFragment)
                            }
                        }
                        override fun onCancelled(error: DatabaseError) {}
                    })
                } else {
                    binding.swipeRefreshLayout.isRefreshing = false
                    binding.tvFailureDescription.setText(R.string.invalid_code)
                    timerInvalidCode.start()
                    binding.tv1.text = ""
                    binding.tv2.text = ""
                    binding.tv3.text = ""
                    binding.tv4.text = ""
                    binding.tv5.text = ""
                    binding.tv6.text = ""
                    sentCode = ""
                }
            }
    }

    private fun writeCode() {
        binding.apply {
            btn0.setOnClickListener {
                addLetterToSentCode("0")
            }
            btn1.setOnClickListener {
                addLetterToSentCode("1")
            }
            btn2.setOnClickListener {
                addLetterToSentCode("2")
            }
            btn3.setOnClickListener {
                addLetterToSentCode("3")
            }
            btn4.setOnClickListener {
                addLetterToSentCode("4")
            }
            btn5.setOnClickListener {
                addLetterToSentCode("5")
            }
            btn6.setOnClickListener {
                addLetterToSentCode("6")
            }
            btn7.setOnClickListener {
                addLetterToSentCode("7")
            }
            btn8.setOnClickListener {
                addLetterToSentCode("8")
            }
            btn9.setOnClickListener {
                addLetterToSentCode("9")
            }
            btnBackspace.setOnClickListener {
                if (sentCode.isNotEmpty()) {
                    deleteLetterToSentCode()
                }
            }
        }
    }

    private fun addLetterToSentCode(letter: String) {
        if (sentCode.length != 6) {
            sentCode += letter
            addTextToTextView(sentCode.length)
            if (sentCode.length == 6) {
                val credential = PhoneAuthProvider.getCredential(storedVerificationId, sentCode)
                callbacks.onVerificationCompleted(credential)
            }
        }
    }

    private fun deleteLetterToSentCode() {
        sentCode = sentCode.substring(0, sentCode.length - 1)
        deleteTextToTextView(sentCode.length)
    }

    private fun addTextToTextView(length: Int) {
        when (length) {
            1 -> {
                binding.tv1.text = sentCode[sentCode.length - 1].toString()
            }
            2 -> {
                binding.tv2.text = sentCode[sentCode.length - 1].toString()
            }
            3 -> {
                binding.tv3.text = sentCode[sentCode.length - 1].toString()
            }
            4 -> {
                binding.tv4.text = sentCode[sentCode.length - 1].toString()
            }
            5 -> {
                binding.tv5.text = sentCode[sentCode.length - 1].toString()
            }
            6 -> {
                binding.tv6.text = sentCode[sentCode.length - 1].toString()
            }
        }
    }

    private fun deleteTextToTextView(length: Int) {
        when (length) {
            0 -> {
                binding.tv1.text = ""
            }
            1 -> {
                binding.tv2.text = ""
            }
            2 -> {
                binding.tv3.text = ""
            }
            3 -> {
                binding.tv4.text = ""
            }
            4 -> {
                binding.tv5.text = ""
            }
            5 -> {
                binding.tv6.text = ""
            }
        }
    }

    private fun checkUserAddedFirebaseDatabase(snapshot: DataSnapshot): Boolean {
        var boolean = false
        for (child in snapshot.children) {
            val users = child.getValue(Users::class.java)
            if (users != null) {
                if (users.uid == auth.uid) {
                    boolean = true
                    break
                } else {
                    boolean = false
                }
            }
        }
        return boolean
    }

}
