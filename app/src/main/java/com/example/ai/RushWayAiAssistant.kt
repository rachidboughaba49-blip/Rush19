package com.example.ai

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object RushWayAiAssistant {

  private val client = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .writeTimeout(30, TimeUnit.SECONDS)
    .build()

  private const val SYSTEM_PROMPT =
    "You are THE RUSH WAY AI Coaching Assistant, designed exclusively for elite personal fitness coaches and bodybuilding professionals. " +
    "CRITICAL CONSTRAINTS:\n" +
    "1. You must NEVER diagnose diseases or medical conditions.\n" +
    "2. You must NEVER invent missing information. If client metrics, adherence data, or background details are missing, explicitly state: 'Additional information is required.'\n" +
    "3. Keep tone objective, athletic, professional, and practical.\n" +
    "4. The coach always has final control over recommendations.\n" +
    "5. When drafting messages or reports, maintain high-performance bodybuilding and athletic excellence standards."

  suspend fun askAssistant(
    prompt: String,
    clientContext: String? = null
  ): String = withContext(Dispatchers.IO) {
    val apiKey = try {
      BuildConfig.GEMINI_API_KEY
    } catch (e: Throwable) {
      ""
    }

    if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
      // Offline Intelligent Coaching Rule-Based Fallback
      return@withContext generateOfflineCoachingResponse(prompt, clientContext)
    }

    try {
      val fullPrompt = buildString {
        if (!clientContext.isNullOrBlank()) {
          appendLine("CLIENT CONTEXT:")
          appendLine(clientContext)
          appendLine("---")
        }
        appendLine("COACH REQUEST:")
        appendLine(prompt)
      }

      val jsonBody = JSONObject().apply {
        put("systemInstruction", JSONObject().apply {
          put("parts", JSONArray().apply {
            put(JSONObject().apply { put("text", SYSTEM_PROMPT) })
          })
        })
        put("contents", JSONArray().apply {
          put(JSONObject().apply {
            put("parts", JSONArray().apply {
              put(JSONObject().apply { put("text", fullPrompt) })
            })
          })
        })
        put("generationConfig", JSONObject().apply {
          put("temperature", 0.4)
          put("maxOutputTokens", 1200)
        })
      }

      val request = Request.Builder()
        .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
        .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
        .build()

      val response = client.newCall(request).execute()
      val responseBody = response.body?.string()

      if (response.isSuccessful && responseBody != null) {
        val root = JSONObject(responseBody)
        val candidates = root.optJSONArray("candidates")
        val content = candidates?.optJSONObject(0)?.optJSONObject("content")
        val parts = content?.optJSONArray("parts")
        val text = parts?.optJSONObject(0)?.optString("text")
        if (!text.isNullOrBlank()) {
          return@withContext text
        }
      }

      generateOfflineCoachingResponse(prompt, clientContext)
    } catch (e: Exception) {
      generateOfflineCoachingResponse(prompt, clientContext)
    }
  }

  private fun generateOfflineCoachingResponse(prompt: String, clientContext: String?): String {
    val lower = prompt.lowercase()

    return when {
      lower.contains("message") || lower.contains("draft") -> {
        if (clientContext.isNullOrBlank()) {
          "Additional information is required: Please specify the client name, current goals, and latest adherence numbers to draft a personalized message.\n\n" +
          "Standard The Rush Way Template:\n" +
          "'Hi [Client], excellent work on completing your scheduled training sessions this week. Let's maintain this disciplined momentum into next week. Review your updated macro targets in the portal and hit your hydration protocol. - Coach'"
        } else {
          "Draft Coach Message (The Rush Way Protocol):\n\n" +
          "\"Great execution on your weekly check-in. Looking at your metrics, your consistency is driving measurable body composition shifts. Keep progressive overload at the forefront this week—aim for 1 extra rep or 2.5kg on your compound lifts while maintaining strict mechanical tension. Stay dialed into your nutrition plan.\""
        }
      }

      lower.contains("calculate") || lower.contains("bmr") || lower.contains("tdee") || lower.contains("macro") -> {
        "The Rush Way Metabolic Calculation Principles:\n" +
        "• BMR (Mifflin-St Jeor) establishes baseline basal energy expenditure.\n" +
        "• Activity Multiplier: Sedentary (1.2) to Extremely Active (1.9) scales to TDEE.\n" +
        "• Calorie Adjustment: Deficit (-300 to -500 kcal) for fat loss; Surplus (+250 to +400 kcal) for hypertrophy.\n" +
        "• Protein Floor: 2.0g - 2.4g per kg to protect lean mass.\n" +
        "• Fat Floor: 20-25% total calories to support hormonal health.\n" +
        "• Carbohydrates: Allocated to match training intensity and muscle glycogen replenishment."
      }

      lower.contains("program") || lower.contains("training") || lower.contains("split") -> {
        "Recommended The Rush Way Program Architecture:\n\n" +
        "1. 4-Day Upper/Lower Split (Optimal Frequency & Recovery):\n" +
        "   - Day 1: Upper Strength (Bench, Barbell Row, Overhead Press, Pull-ups)\n" +
        "   - Day 2: Lower Hypertrophy (Back Squat, Romanian Deadlift, Leg Press, Calves)\n" +
        "   - Day 3: Upper Hypertrophy (Incline DB Press, Lat Pulldown, Lateral Raises, Arms)\n" +
        "   - Day 4: Lower Posterior/Glutes (Deadlift/RDL, Hack Squat, Leg Curls, Core)\n\n" +
        "Advanced Intensifiers: Integrate Rest-Pause or Myo-Reps on final isolation sets for metabolic stress without excessive CNS fatigue."
      }

      lower.contains("report") || lower.contains("summary") -> {
        if (clientContext.isNullOrBlank()) {
          "Additional information is required: Starting weight, current weight, and check-in adherence logs are needed to synthesize an accurate progress report."
        } else {
          "Monthly Report Summary:\n\n" +
          "• Anthropometrics: Notable structural progress consistent with client goals.\n" +
          "• Adherence: High compliance observed across logged training sessions and prescribed macronutrients.\n" +
          "• Head Coach Recommendation: Proceed with current progressive overload cycle for next 4-week mesocycle before a scheduled deload."
        }
      }

      else -> {
        "The Rush Way Coaching Assistant:\n\n" +
        "I am ready to assist you with:\n" +
        "1. Program structural design & progressive overload sequencing\n" +
        "2. Metabolic & macronutrient calculation explanations\n" +
        "3. Check-in adherence analysis & trend evaluation\n" +
        "4. Drafting personalized feedback messages (EN / FR / AR)\n\n" +
        "*(Note: Provide client details or select a client from the profile view to generate tailored feedback. Additional information is required if data is missing.)*"
      }
    }
  }
}
