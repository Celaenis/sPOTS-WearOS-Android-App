package com.example.tutorial.data.mapper

import com.example.tutorial.com.example.tutorial.domain.model.Article
import com.google.firebase.firestore.DocumentSnapshot

internal fun DocumentSnapshot.toArticle() = Article(
    id = id,
    title = getString("title") ?: "",
    summary = getString("summary") ?: "",
    content = getString("content") ?: ""
)