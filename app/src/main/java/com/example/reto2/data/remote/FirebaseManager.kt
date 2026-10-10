
package com.example.reto2.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object FirebaseManager {
    val auth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    val firestore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }
}
