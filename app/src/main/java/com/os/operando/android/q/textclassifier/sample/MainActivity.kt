package com.os.operando.android.q.textclassifier.sample

import android.icu.util.ULocale
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.textclassifier.*
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.text_selection).setOnClickListener {
            GlobalScope.launch(Dispatchers.Main) {
                val tcm = getSystemService(TextClassificationManager::class.java)
                val ts: TextSelection = async {
                    tcm.textClassifier.suggestSelection(
                        TextSelection.Request.Builder(
                            "GoogleのサイトURL:https://google.com",
                            21,
                            32
                        ).build()
                    )
                }.await()
                findViewById<TextView>(R.id.result).text = ts.toString()
            }
        }

        findViewById<EditText>(R.id.edit).addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                findViewById<TextView>(R.id.input).text = p0.toString()
                GlobalScope.launch(Dispatchers.Main) {
                    val tcm = getSystemService(TextClassificationManager::class.java)
                    val r = TextLanguage.Request.Builder(p0!!).build()
                    val textLanguage = async { tcm.textClassifier.detectLanguage(r) }.await()
                    //Log.d("log", "text=$p0 result=$textLanguage")
                    val s = textLanguage.getConfidenceScore(ULocale.JAPANESE)
                    Log.d("log", "$s")
                    val c = textLanguage.localeHypothesisCount
                    //Log.d("log", "$c")
                    findViewById<TextView>(R.id.result).text = textLanguage.toString()

                    val tlr = TextLinks.Request.Builder(p0).build()
                    val tl: TextLinks = async { tcm.textClassifier.generateLinks(tlr) }.await()
                    Log.d("log", tl.toString())

                    val tcr = TextClassification.Request.Builder(p0, (0), p0.length).build()
                    val tc: TextClassification = async { tcm.textClassifier.classifyText(tcr) }.await()
                    Log.d("log", tc.toString())

                    tc.actions
//                        .map { "${it.contentDescription}:${it.isEnabled}\n" }
                        .map { "${it.actionIntent}" }
                        .forEach { Log.d("log", it) }

                    // 実機のPixel 3なら動いたけど結果は空
                    // Emulatorで動かすとnative crashする
//                    val tom = Person.Builder()
//                        .setName("tom")
//                        .setIcon(null)
//                        .build()
//
//                    val tim = Person.Builder()
//                        .setName("tim")
//                        .setIcon(null)
//                        .build()
//
//                    val l = mutableListOf<ConversationActions.Message>().apply {
//                        add(ConversationActions.Message.Builder(tom).setText("Hi!").build())
//                        add(ConversationActions.Message.Builder(tim).setText("Hi!").build())
//                        add(ConversationActions.Message.Builder(tom).setText("How about sushi?").build())
//                    }
//                    val ca = ConversationActions.Request.Builder(l).build()
//                    val sca = async { tcm.textClassifier.suggestConversationActions(ca) }.await()
//
//                    Log.d("log", sca.conversationActions.toString())
                }
            }

        })
    }
}