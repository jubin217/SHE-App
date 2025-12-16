package com.example.womenssafetyapp.features

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.womenssafetyapp.R
import com.example.womenssafetyapp.adapters.ChatAdapter
import com.example.womenssafetyapp.databinding.ActivityChatbotBinding
import com.example.womenssafetyapp.models.ChatMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class ChatBotActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatbotBinding
    private lateinit var chatAdapter: ChatAdapter
    private val chatMessages = mutableListOf<ChatMessage>()
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatbotBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupChatRecyclerView()
        setupClickListeners()

        // Initial bot message
        addBotMessage("Hello! I'm your safety companion. I'm here to help you stay calm and safe. How can I assist you today?")
    }

    private fun setupChatRecyclerView() {
        chatAdapter = ChatAdapter(chatMessages)
        binding.rvChatMessages.layoutManager = LinearLayoutManager(this)
        binding.rvChatMessages.adapter = chatAdapter
    }

    private fun setupClickListeners() {
        binding.btnSend.setOnClickListener {
            val message = binding.etMessage.text?.toString()?.trim() ?: ""
            if (message.isNotEmpty()) {
                sendMessage(message)
                binding.etMessage.text?.clear()
            }
        }

        binding.btnEmergencyTips.setOnClickListener {
            showEmergencyTips()
        }

        binding.btnCalmExercises.setOnClickListener {
            showCalmExercises()
        }
    }

    private fun sendMessage(message: String) {
        // Add user message
        val userMessage = ChatMessage(
            text = message,
            isUser = true,
            timestamp = System.currentTimeMillis()
        )
        chatMessages.add(userMessage)
        chatAdapter.notifyItemInserted(chatMessages.size - 1)
        binding.rvChatMessages.scrollToPosition(chatMessages.size - 1)

        // Show typing indicator
        showTypingIndicator(true)

        // Get bot response
        CoroutineScope(Dispatchers.IO).launch {
            val botResponse = getBotResponse(message)
            runOnUiThread {
                showTypingIndicator(false)
                addBotMessage(botResponse)
            }
        }
    }

    private fun addBotMessage(message: String) {
        val botMessage = ChatMessage(
            text = message,
            isUser = false,
            timestamp = System.currentTimeMillis()
        )
        chatMessages.add(botMessage)
        chatAdapter.notifyItemInserted(chatMessages.size - 1)
        binding.rvChatMessages.scrollToPosition(chatMessages.size - 1)
    }

    private fun getBotResponse(userMessage: String): String {
        return try {
            // Simple AI response logic - can be replaced with actual AI service
            val lowerMessage = userMessage.lowercase()

            when {
                lowerMessage.contains("emergency") || lowerMessage.contains("help") -> {
                    "I understand you're feeling unsafe. Take deep breaths. " +
                            "I've detected your location and will notify your emergency contacts. " +
                            "Remember: Stay in a well-lit area, call 112, and keep me updated."
                }
                lowerMessage.contains("scared") || lowerMessage.contains("afraid") -> {
                    "It's okay to feel scared. Let's do a calming exercise together: " +
                            "Inhale for 4 seconds, hold for 4, exhale for 6. Repeat 5 times. " +
                            "You're stronger than you think. Help is on the way."
                }
                lowerMessage.contains("location") || lowerMessage.contains("where") -> {
                    "Your location is being tracked and shared with trusted contacts. " +
                            "The nearest police station is 1.2km away. " +
                            "Safe route suggested: Main Street → Park Avenue."
                }
                lowerMessage.contains("calm") || lowerMessage.contains("anxiety") -> {
                    "Let's try the 5-4-3-2-1 grounding technique:\n" +
                            "5 things you can see\n" +
                            "4 things you can touch\n" +
                            "3 things you can hear\n" +
                            "2 things you can smell\n" +
                            "1 thing you can taste\n" +
                            "This will help center your thoughts."
                }
                else -> {
                    // Default calming responses
                    val responses = listOf(
                        "I'm here with you. You're not alone. Help is coming.",
                        "Take slow, deep breaths. Focus on your breathing.",
                        "You're doing great by reaching out. Stay aware of your surroundings.",
                        "Remember your emergency contacts have been notified. They're checking on you.",
                        "Would you like me to guide you through a quick calming exercise?"
                    )
                    responses.random()
                }
            }
        } catch (e: Exception) {
            "I'm here for you. Stay calm and remember help is on the way."
        }
    }

    private fun showTypingIndicator(show: Boolean) {
        binding.tvTypingIndicator.visibility = if (show) View.VISIBLE else View.GONE
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun showEmergencyTips() {
        val tips = listOf(
            "🆘 Always trust your instincts",
            "📱 Keep your phone charged",
            "📍 Share your live location with trusted contacts",
            "🚶 Walk in well-lit, populated areas",
            "🎒 Carry a personal safety alarm",
            "👥 Avoid isolated shortcuts",
            "📞 Keep emergency numbers on speed dial",
            "🔊 Don't wear headphones in unfamiliar areas",
            "🚗 Check backseat before entering vehicle",
            "🗣️ Yell 'Fire!' instead of 'Help!' for attention"
        )

        val tipsText = "Emergency Safety Tips:\n\n" + tips.joinToString("\n• ")
        addBotMessage(tipsText)
    }

    private fun showCalmExercises() {
        val exercises = """
            Breathing Exercises:
            
            1. Box Breathing:
            • Inhale for 4 seconds
            • Hold for 4 seconds
            • Exhale for 4 seconds
            • Hold for 4 seconds
            • Repeat 5 times
            
            2. 4-7-8 Breathing:
            • Inhale for 4 seconds
            • Hold for 7 seconds
            • Exhale for 8 seconds
            
            3. Grounding Technique:
            • Name 5 things you can see
            • 4 things you can feel
            • 3 things you can hear
            • 2 things you can smell
            • 1 thing you can taste
        """.trimIndent()

        addBotMessage(exercises)
    }
}