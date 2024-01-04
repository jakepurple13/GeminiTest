package com.programmersbox.geminitest

import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.SafetySetting

val safetySettings
    get() = HarmCategory.entries
        .filter { it != HarmCategory.UNKNOWN }
        .map { SafetySetting(it, BlockThreshold.NONE) }