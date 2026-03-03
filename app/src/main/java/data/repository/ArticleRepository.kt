package com.example.tutorial.data.repository

import com.example.tutorial.com.example.tutorial.domain.model.Article
import com.example.tutorial.data.mapper.toArticle
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class ArticleRepository @Inject constructor(
    private val fs: FirebaseFirestore
) {


    suspend fun getAll(): List<Article> =
        runCatching {
            ensureSeeded()
            fs.collection(COL).get().await().documents.map { it.toArticle() }
        }.getOrElse { mockArticles }

    suspend fun findById(id: String): Article? =
        runCatching {
            ensureSeeded()
            fs.collection(COL).document(id).get().await().toArticle()
        }.getOrElse { mockArticles.firstOrNull { it.id == id } }


    private suspend fun ensureSeeded() {
        val snap = fs.collection(COL).limit(1).get().await()
        if (!snap.isEmpty) return

        mockArticles.forEach { art ->
            fs.collection(COL).document(art.id).set(
                mapOf(
                    "title" to art.title,
                    "summary" to art.summary,
                    "content" to art.content
                )
            ).await()
        }
    }

    companion object {
        private const val COL = "articles"
    }

    private val mockArticles = listOf(
        Article(
            id = "basics",
            title = "What Is POTS? Understanding the Basics",
            summary = "Core diagnostic criteria, typical symptoms and why early detection matters.",
            content = """
Postural Orthostatic Tachycardia Syndrome (**POTS**) is defined as …
- **Heart-rate rise ≥ 30 bpm** (40 bpm in teens) within 10 minutes of standing.
- Accompanied by symptoms such as dizziness, brain-fog, palpitations.

Unlike classical orthostatic hypotension, blood pressure usually stays normal.

**Why does it happen?**  
Current evidence points to autonomic dysregulation, often after viral illnesses, autoimmune activation or prolonged de-conditioning.

> _Key takeaway_: if your heart races when you stand and you feel light-headed, track it and discuss the pattern with a physician.
""".trimIndent()
        ),
        Article(
            id = "tilt_test",
            title = "Preparing for a Tilt-Table Test",
            summary = "A step-by-step guide so you know what to expect on testing day.",
            content = """
The tilt-table test is considered the gold-standard for confirming POTS…

**24 h before the test**
1. Continue prescribed medication **unless** your doctor says otherwise.
2. Avoid caffeine and nicotine.

**During the test**
- Straps keep you safely on the table.
- Phases: baseline (supine) → 60–70° tilt for up to 10 min.
- Heart-rate and blood-pressure are recorded continuously.

Possible sensations: warmth, nausea, tunnel vision. Tell the nurse if you feel faint.
""".trimIndent()
        ),
        Article(
            id = "hydration",
            title = "Hydration & Salt - Friends, Not Foes",
            summary = "Why extra fluids and sodium are first-line, evidence-based interventions.",
            content = """
Increasing blood-volume is an inexpensive, side-effect-sparing intervention.

- **Fluids:** aim for 2.5–3 L / day  
- **Salt:** 3–10 g NaCl / day (5–15 g table salt)

Practical tips
* Carry a 1 L bottle and refill twice.
* Add electrolyte tablets to one of the bottles.
""".trimIndent()
        ),
        Article(
            id = "exercise",
            title = "Exercise That Actually Helps",
            summary = "Recumbent cardio & resistance training shown to improve orthostatic tolerance.",
            content = """
Bed-cycle, rowing machine or swimming allow cardiovascular conditioning **without** provoking upright tachycardia.

**Starter protocol**
- 3 x week, 25 min recumbent bike at 60 % max-HR
- Add light resistance bands for lower-body after 2 weeks

Progress gradually to upright jogging over 3-6 months.
""".trimIndent()
        ),
        Article(
            id = "red_flags",
            title = "Red Flags — When to Seek Immediate Care",
            summary = "Not every episode is benign. Know the warning signs.",
            content = """
Call emergency services if you experience:
* New-onset chest pain or pressure
* Black-out (complete loss of consciousness)
* Shortness of breath at rest
* Heart-rate sustained > 150 bpm while lying down

These may indicate arrhythmia or another diagnosis requiring urgent evaluation.
""".trimIndent()
        )
    )
}