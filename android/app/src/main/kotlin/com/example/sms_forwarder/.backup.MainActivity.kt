
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

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

                        // Forward the SMS to the specified URL
                        forwardSms(sender, messageBody, timestamp)
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

    private fun forwardSms(sender: String, messageBody: String, timestamp: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL("https://yourwebsite.com/path")
                val jsonPayload = """
                    {
                        "from": "$sender",
                        "text": "$messageBody",
                        "sentStamp": "$timestamp",
                        "receivedStamp": "${System.currentTimeMillis()}",
                        "sim": "SIM1"
                    }
                """.trimIndent()

                with(url.openConnection() as HttpURLConnection) {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/json; charset=utf-8")
                    doOutput = true
                    outputStream.write(jsonPayload.toByteArray())
                    outputStream.flush()
                    outputStream.close()

                    val responseCode = responseCode
                    Log.d("SMSForwarder", "Response Code: $responseCode")
                }
            } catch (e: Exception) {
                Log.e("SMSForwarder", "Error forwarding SMS: ${e.message}")
            }
        }
    }
}
