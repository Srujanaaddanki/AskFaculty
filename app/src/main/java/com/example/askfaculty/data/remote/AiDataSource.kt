package com.example.askfaculty.data.remote

import com.example.askfaculty.core.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Calls secure Cloud Functions for AI features.
 * No API keys are stored in the APK — all AI processing happens server-side.
 */
@Singleton
class AiDataSource @Inject constructor() {

    suspend fun getSuggestedResponse(question: String, context: String): Result<String> = runCatching {
        withContext(Dispatchers.IO) {
            val payload = JSONObject().apply {
                put("question", question)
                put("context", context)
            }
            post("${Constants.AI_FUNCTION_BASE_URL}/suggestResponse", payload.toString())
                .let { JSONObject(it).optString("response", "") }
        }
    }

    suspend fun getQuestionSummary(question: String): Result<String> = runCatching {
        withContext(Dispatchers.IO) {
            val payload = JSONObject().apply { put("question", question) }
            post("${Constants.AI_FUNCTION_BASE_URL}/summarizeQuestion", payload.toString())
                .let { JSONObject(it).optString("summary", "") }
        }
    }

    suspend fun getSimilarDoubts(topic: String, facultyId: String): Result<Int> = runCatching {
        withContext(Dispatchers.IO) {
            val payload = JSONObject().apply {
                put("topic", topic)
                put("facultyId", facultyId)
            }
            post("${Constants.AI_FUNCTION_BASE_URL}/similarDoubts", payload.toString())
                .let { JSONObject(it).optInt("count", 0) }
        }
    }

    private fun post(urlString: String, body: String): String {
        val url = URL(urlString)
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        conn.doOutput = true
        conn.connectTimeout = 10_000
        conn.readTimeout = 15_000
        conn.outputStream.use { it.write(body.toByteArray()) }
        return conn.inputStream.bufferedReader().readText()
    }
}
