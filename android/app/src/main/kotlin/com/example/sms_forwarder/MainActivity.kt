
package com.example.sms_forwarder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.telephony.SmsMessage
import android.util.Log
import io.flutter.embedding.android.FlutterActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.pow
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.MethodCall
import io.flutter.plugins.GeneratedPluginRegistrant

class MainActivity: FlutterActivity() {
    private val smsReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "android.provider.Telephony.SMS_RECEIVED") {
                val bundle = intent.extras
                if (bundle != null) {
                    val pdus = bundle["pdus"] as Array<*>
                    for (pdu in pdus) {
                        val smsMessage = SmsMessage.createFromPdu(pdu as ByteArray)
                        val sender = smsMessage.displayOriginatingAddress
                        val messageBody = smsMessage.messageBody
                        val timestamp = smsMessage.timestampMillis

                        try {
                            // Log SMS history
                            logSmsHistory(sender, messageBody, timestamp)

                            // Apply SMS filtering
                            if (shouldForwardSms(sender, messageBody)) {
                                // Forward the SMS to the specified URLs
                                forwardSmsToMultipleWebhooks(sender, messageBody, timestamp)
                            }

                            // Send notification
                            sendNotification(sender, messageBody)
                        } catch (e: Exception) {
                            Log.e("SMSForwarder", "Error processing SMS: ${e.message}")
                            sendErrorNotification(e.message ?: "Unknown error")
                        }
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Register the SMS receiver
        val filter = IntentFilter("android.provider.Telephony.SMS_RECEIVED")
        registerReceiver(smsReceiver, filter)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Unregister the SMS receiver
        unregisterReceiver(smsReceiver)
    }

    private fun shouldForwardSms(sender: String, messageBody: String): Boolean {
        val allowedSenders = listOf("12345", "67890") // Customize this list
        val keywords = listOf("important", "urgent") // Customize this list

        if (allowedSenders.contains(sender)) {
            return true
        }

        for (keyword in keywords) {
            if (messageBody.contains(keyword, ignoreCase = true)) {
                return true
            }
        }

        return false
    }

    private fun forwardSmsToMultipleWebhooks(sender: String, messageBody: String, timestamp: Long) {
        val webhookUrls = listOf(
            "https://yourwebsite.com/path1",
            "https://yourwebsite.com/path2"
        ) // Customize this list

        for (webhookUrl in webhookUrls) {
            forwardSms(sender, messageBody, timestamp, webhookUrl)
        }
    }

    private fun forwardSms(sender: String, messageBody: String, timestamp: Long, webhookUrl: String) {
        CoroutineScope(Dispatchers.IO).launch {
            if (!webhookUrl.contains("webhook.site")) {
                Log.e("SMSForwarder", "Invalid webhook URL. Only URLs from webhook.site are allowed.")
                sendErrorNotification("Invalid webhook URL. Only URLs from webhook.site are allowed.")
                return@launch
            }

            val url = URL(webhookUrl)
            val jsonPayload = """
                {
                    "from": "$sender",
                    "text": "$messageBody",
                    "sentStamp": "$timestamp",
                    "receivedStamp": "${System.currentTimeMillis()}",
                    "sim": "SIM1"
                }
            """.trimIndent()

            var attempts = 0
            val maxAttempts = 10 // Customize this value
            val initialRetryInterval = 10000L // 10 seconds, customize this value

            while (attempts < maxAttempts) {
                try {
                    with(url.openConnection() as HttpURLConnection) {
                        requestMethod = "POST"
                        setRequestProperty("Content-Type", "application/json; charset=utf-8")
                        doOutput = true
                        outputStream.write(jsonPayload.toByteArray())
                        outputStream.flush()
                        outputStream.close()

                        val responseCode = responseCode
                        Log.d("SMSForwarder", "Response Code: $responseCode")

                        if (responseCode in 200..299) {
                            Log.d("SMSForwarder", "SMS forwarded successfully.")
                            break
                        } else {
                            Log.e("SMSForwarder", "Failed to forward SMS. Response Code: $responseCode")
                            sendErrorNotification("Failed to forward SMS. Response Code: $responseCode")
                        }
                    }
                } catch (e: Exception) {
                    Log.e("SMSForwarder", "Error forwarding SMS: ${e.message}")
                    sendErrorNotification("Error forwarding SMS: ${e.message}")
                }

                attempts++
                val retryInterval = initialRetryInterval * (2.0.pow(attempts.toDouble())).toLong()
                delay(retryInterval)
            }
        }
    }

    private fun logSmsHistory(sender: String, messageBody: String, timestamp: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            val dbHelper = DatabaseHelper()
            val smsHistory = mapOf(
                "sender" to sender,
                "messageBody" to messageBody,
                "timestamp" to timestamp
            )
            dbHelper.insertSmsHistory(smsHistory)
        }
    }

    private fun sendNotification(sender: String, messageBody: String) {
        val androidPlatformChannelSpecifics = AndroidNotificationDetails(
            'your_channel_id',
            'your_channel_name',
            'your_channel_description',
            importance: Importance.max,
            priority: Priority.high,
            showWhen: false
        )
        val platformChannelSpecifics = NotificationDetails(android: androidPlatformChannelSpecifics)
        flutterLocalNotificationsPlugin.show(
            0,
            'New SMS from $sender',
            messageBody,
            platformChannelSpecifics,
            payload: 'item x'
        )
    }

    private fun sendErrorNotification(errorMessage: String) {
        val androidPlatformChannelSpecifics = AndroidNotificationDetails(
            'error_channel_id',
            'Error Notifications',
            'Notifications for errors',
            importance: Importance.max,
            priority: Priority.high,
            showWhen: false
        )
        val platformChannelSpecifics = NotificationDetails(android: androidPlatformChannelSpecifics)
        flutterLocalNotificationsPlugin.show(
            1,
            'Error',
            errorMessage,
            platformChannelSpecifics,
            payload: 'error'
        )
    }
}
