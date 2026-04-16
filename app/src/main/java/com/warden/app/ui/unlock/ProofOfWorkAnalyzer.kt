package com.warden.app.ui.unlock

import android.graphics.Bitmap
import android.util.Base64
import com.warden.app.data.repository.SecurePreferences
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

class ProofOfWorkAnalyzer(private val securePrefs: SecurePreferences) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    data class AnalysisResult(
        val isAccepted: Boolean,
        val message: String,
        val breakMinutes: Int = 5
    )

    suspend fun analyze(bitmap: Bitmap): AnalysisResult {
        val apiKey = securePrefs.getApiKey()
        if (apiKey.isBlank()) {
            return AnalysisResult(false, "NO_API_KEY")
        }

        return try {
            val base64Image = bitmapToBase64(bitmap)
            val baseUrl = securePrefs.getApiBaseUrl().trimEnd('/')

            val requestBody = JSONObject().apply {
                put("model", "gpt-4o-mini")
                put("max_tokens", 150)
                put("messages", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", JSONArray().apply {
                            put(JSONObject().apply {
                                put("type", "text")
                                put("text", """
                                    You are a strict study enforcer. Analyze this image and determine if it shows genuine handwritten study notes.
                                    
                                    Criteria for ACCEPTANCE:
                                    - Clear handwritten text visible (not printed/typed)
                                    - Content appears to be study material (notes, formulas, diagrams, outlines)
                                    - At least half a page of content
                                    
                                    Respond with ONLY a JSON object:
                                    {"accepted": true/false, "reason": "brief reason", "break_minutes": 5}
                                    
                                    If accepted, break_minutes should be 5. If rejected, break_minutes should be 0.
                                """.trimIndent())
                            })
                            put(JSONObject().apply {
                                put("type", "image_url")
                                put("image_url", JSONObject().apply {
                                    put("url", "data:image/jpeg;base64,$base64Image")
                                    put("detail", "low")
                                })
                            })
                        })
                    })
                })
            }

            val request = Request.Builder()
                .url("$baseUrl/chat/completions")
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("Content-Type", "application/json")
                .post(requestBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return AnalysisResult(false, "API_ERROR: ${response.code}")
            }

            parseResponse(responseBody)
        } catch (e: Exception) {
            AnalysisResult(false, "ERROR: ${e.message}")
        }
    }

    private fun parseResponse(responseBody: String): AnalysisResult {
        return try {
            val json = JSONObject(responseBody)
            val content = json
                .getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")
                .trim()

            // Extract JSON from response
            val jsonStart = content.indexOf('{')
            val jsonEnd = content.lastIndexOf('}')
            if (jsonStart == -1 || jsonEnd == -1) {
                return AnalysisResult(false, "INVALID_RESPONSE")
            }

            val resultJson = JSONObject(content.substring(jsonStart, jsonEnd + 1))
            val accepted = resultJson.optBoolean("accepted", false)
            val reason = resultJson.optString("reason", "")
            val breakMinutes = resultJson.optInt("break_minutes", if (accepted) 5 else 0)

            AnalysisResult(accepted, reason, breakMinutes)
        } catch (e: Exception) {
            AnalysisResult(false, "PARSE_ERROR: ${e.message}")
        }
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        // Compress to reduce API payload size
        val scaledBitmap = if (bitmap.width > 1024 || bitmap.height > 1024) {
            val scale = minOf(1024f / bitmap.width, 1024f / bitmap.height)
            Bitmap.createScaledBitmap(
                bitmap,
                (bitmap.width * scale).toInt(),
                (bitmap.height * scale).toInt(),
                true
            )
        } else bitmap

        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }
}
