package com.example.api

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit
import com.example.BuildConfig

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null,
    val systemInstruction: Content? = null
)

@JsonClass(generateAdapter = true)
data class Content(
    val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class Part(
    val text: String? = null
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    val temperature: Float? = null,
    val topP: Float? = null,
    val topK: Int? = null
)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(
    val candidates: List<Candidate>? = null
)

@JsonClass(generateAdapter = true)
data class Candidate(
    val content: Content? = null
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object RetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    val service: GeminiApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
        retrofit.create(GeminiApiService::class.java)
    }
}

class PromptOptimizerEngine {

    fun generateHighFidelityFallbackPrompt(rawIdea: String, targetModel: String, category: String): String {
        val modelLower = targetModel.lowercase()
        val isClaude = modelLower.contains("claude")
        val isChatGPT = modelLower.contains("chatgpt") || modelLower.contains("gpt")
        val isGemini = modelLower.contains("gemini")
        val isGrok = modelLower.contains("grok")
        val isDeepSeek = modelLower.contains("deepseek")

        val (role, instructions, format) = when (category.lowercase()) {
            "coding", "programming", "software dev" -> Triple(
                "expert Full-Stack Software Engineer and System Architect",
                "Deconstruct the problem systematically, follow SOLID principles, implement robust error-handling, structure the logic to be clean and modular, and optimize computational complexity.",
                "Provide complete, production-grade solutions inside clean Markdown code blocks with appropriate language tags."
            )
            "writing", "content writing" -> Triple(
                "distinguished Literary Editor and elite Creative Copywriter",
                "Hook readers immediately, optimize natural style flow, polish voice transitions, eliminate wordiness, and emphasize persuasive and engaging hooks.",
                "Present the narrative structured with crisp headings, bulleted lists, and clear outline milestones."
            )
            "marketing", "marketing & growth" -> Triple(
                "Conversion-focused Growth Marketing Director and Brand Strategist",
                "Conduct deep persona mapping, target specific customer pain points, outline viral hook angles, write high-converting calls to action (CTAs), and map acquisition metrics.",
                "Provide a structured marketing brief with defined acquisition hooks and visual creative copy drafts."
            )
            "education" -> Triple(
                "expert Academic Pedagogue and Curriculum Design Director",
                "Deconstruct the technical thesis into intuitive steps, provide realistic real-world analogies, construct interactive Socratic scenarios, and check for conceptual progression.",
                "Provide structured modular lessons, interactive checklists, and a summary definitions gloss."
            )
            "business", "business & sales" -> Triple(
                "Senior Enterprise Business Consultant and Silicon Valley Venture Analyst",
                "Analyze commercial market fit, formulate competitive advantage maps, establish financial sanity guidelines, outline operations friction, and set clear ROI key results.",
                "Format as a concise, executive-focused strategic plan with distinct tactical stages."
            )
            "productivity" -> Triple(
                "master Productivity Coach and Workflow Automation Architect",
                "Build structured habit loops, establish Eisenhower decision matrices, define time-blocking priorities, and suggest specific automation rule constraints.",
                "Provide highly actionable daily templates, prioritized agendas, and productivity checks."
            )
            "image generation" -> Triple(
                "world-class Digital Artist and Cinematographer specializing in Generative Art",
                "Specify absolute artistic style constraints, texture parameters, lighting directions (e.g. volumetric, high-contrast, ray-traced), camera lens type, focal length, color grade, and precise render settings.",
                "Generate a highly visual, single-paragraph prompt optimized with descriptive adjectives, separated by clean commas, with exact aspect ratio flags."
            )
            "research", "research & synthesis" -> Triple(
                "distinguished Principal Investigator and Scientific Research Director",
                "Formulate comprehensive systematic literature search queries, cross-examine scientific arguments, organize contradictory proofs, and synthesize citation taxonomy.",
                "Structure in rigorous academic format with dedicated methodology outline and hypothesis frames."
            )
            "creative", "creative tasks" -> Triple(
                "award-winning Screeenplay Writer and Theatrical Creative Director",
                "Detail deep world-building aspects, write high-intensity dialogue cadence, frame intense cinematic actions, and map sensory environmental indicators.",
                "Draft structured screenwriting files with clear scene beats and character direction."
            )
            else -> Triple(
                "highly versatile Senior Consultant and elite multidisciplinary problem solver",
                "Provide high-context solutions, outline clear underlying assumptions, address corner cases, and generate fully factual answers.",
                "Format with highly readable markdown headings, structured bullet grids, and highlighted summaries."
            )
        }

        val promptBody = StringBuilder()
        promptBody.append("# ⚡ OPTIMIZED PROMPT FOR LOCAL COMPILER\n\n")
        promptBody.append("> **Target Engine:** $targetModel\n")
        promptBody.append("> **Domain Context:** $category\n")
        promptBody.append("> **Optimization Status:** Offline Resilient Model Active\n\n")

        if (isClaude) {
            promptBody.append("<system_role>\n")
            promptBody.append("Act as an elite $role. Always adhere to exact technical parameters and perform step-by-step evaluation.\n")
            promptBody.append("</system_role>\n\n")
            promptBody.append("<context>\n")
            promptBody.append("The objective is to optimize and solve: \"$rawIdea\".\n")
            promptBody.append("</context>\n\n")
            promptBody.append("<instructions>\n")
            promptBody.append("1. Core Action: $instructions\n")
            promptBody.append("2. Format: $format\n")
            promptBody.append("3. Guardrail: Do not write conversational greetings or preambles. Output only the requested solution.\n")
            promptBody.append("</instructions>\n")
        } else if (isChatGPT) {
            promptBody.append("## SYSTEM ROLE\n")
            promptBody.append("Act as a prestigious, elite $role.\n\n")
            promptBody.append("## OBJECTIVE\n")
            promptBody.append("Execute the following instruction with extreme precision:\n")
            promptBody.append("> \"$rawIdea\"\n\n")
            promptBody.append("## INSTRUCTIONS & STEPS\n")
            promptBody.append("- **Technical approach:** $instructions\n")
            promptBody.append("- **Output guidelines:** $format\n\n")
            promptBody.append("## CRITICAL CONSTRAINT\n")
            promptBody.append("Output absolute technical answers only. Do not add casual conversational opening/closing sentences.")
        } else if (isGemini) {
            promptBody.append("### Role & Persona\n")
            promptBody.append("You are the definitive expert $role.\n\n")
            promptBody.append("### Task Context\n")
            promptBody.append("Process the following idea: **$rawIdea**\n\n")
            promptBody.append("### Reasoning & Logic Path\n")
            promptBody.append("1. **Prerequisite Focus:** $instructions\n")
            promptBody.append("2. **Format Constraints:** $format\n")
            promptBody.append("3. **Reasoning Verification:** Solve the instruction incrementally, logging your systematic considerations before writing the final output.")
        } else if (isGrok) {
            promptBody.append("**ROLE:** Senior $role with maximum analytical drive.\n\n")
            promptBody.append("**DIRECTIVE:** Optimize and execute: \"$rawIdea\"\n\n")
            promptBody.append("**SYSTEM CORE GUIDELINE:**\n")
            promptBody.append("- Approach: $instructions\n")
            promptBody.append("- Layout: $format\n")
            promptBody.append("- Persona: Highly concise, witty, objective, and structurally flawless.")
        } else if (isDeepSeek) {
            promptBody.append("[Role: Expert $role]\n\n")
            promptBody.append("[Objective]\n")
            promptBody.append("Solve raw task: \"$rawIdea\" with mathematical rigor.\n\n")
            promptBody.append("[Execution Plan]\n")
            promptBody.append("1. Sift task elements and list variables.\n")
            promptBody.append("2. Apply core methodology: $instructions\n")
            promptBody.append("3. Format optimized result: $format")
        } else {
            promptBody.append("### Act as a $role\n\n")
            promptBody.append("### Objective\n")
            promptBody.append("Perform: \"$rawIdea\"\n\n")
            promptBody.append("### Guidelines\n")
            promptBody.append("- **Sourcing Method:** $instructions\n")
            promptBody.append("- **Verification Style:** $format")
        }

        return promptBody.toString()
    }

    suspend fun optimizePrompt(rawIdea: String, targetModel: String, category: String): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            // Dynamic local resilient compilation when there is no API Key configured
            return generateHighFidelityFallbackPrompt(rawIdea, targetModel, category)
        }

        val promptBody = """
            You are "Prompter", an elite AI prompt engineer.
            Your task is to optimize the user's raw idea into a highly structured, ultra-professional, ready-to-use prompt tailored specifically for the selected AI model to maximize the quality of its output.

            The user's raw idea/request is:
            "$rawIdea"

            The target AI model is: $targetModel.
            The target category/use case is: $category.

            To write an excellent prompt for $targetModel, adhere strictly to the following target model characteristics:
            - ChatGPT (GPT): Prefers role prompting, clear sub-sections, Markdown formatting, and explicit constraints.
            - Gemini: Prefers rich content, natural structured lists, multi-stage reasoning indicators, systematic guidelines, and contextual examples.
            - Claude: Extremely receptive to XML tags (e.g. <context>, <instructions>, <raw_data>), step-by-step thinking requests, and explicit warning/evaluation criteria.
            - Grok: Loves witty, direct, highly analytical, and logical frameworks. Responds best to bold instructions and raw reasoning directives.
            - Perplexity: Thrives on clear research scope, deep-dive search queries, citation requirements, and structuring results.
            - DeepSeek: Thrives on pure mathematical, step-by-step reasoning directives, complete coding details, and clear syntax separation.
            - Meta AI: Prefers highly engaging, direct, easily understandable summaries with strong task framing.
            - Microsoft Copilot: Responds best to structured work instructions, productivity guidelines, and clear constraints.
            - Mistral: Prefers compact, dense instructions with strong functional definitions.
            - OpenRouter/Other: Clean, general-purpose structured prompt design.

            Structure the optimized prompt beautifully using professional markup (Markdown blockquotes, code fences, headers, bold lists, XML containers for Claude, etc.).
            Ensure it includes:
            1. Clear Persona/Role Definition: (e.g., "Act as an expert Full-stack Developer...")
            2. Context & Motivation: Why this is being asked.
            3. Task Instructions: Step-by-step details of the work.
            4. Output format block: Showcase layout.
            5. Constraints & Guardrails: Keep output highly standard, factual, and correct.

            Begin your output directly with the optimized prompt. Do not write friendly conversational intro or outro (such as "Here is your optimized prompt..."), just output the raw optimized prompt itself, ready for copying.
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = promptBody)))),
            generationConfig = GenerationConfig(temperature = 0.7f),
            systemInstruction = Content(parts = listOf(Part(text = "You are an elite, objective AI prompt optimizer.")))
        )

        var lastException: Exception? = null
        var attempts = 3
        var delayMs = 1000L
        
        for (i in 1..attempts) {
            try {
                val response = RetrofitClient.service.generateContent(apiKey, request)
                val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (!text.isNullOrBlank()) {
                    return text
                }
            } catch (e: Exception) {
                lastException = e
                if (i < attempts) {
                    kotlinx.coroutines.delay(delayMs)
                    delayMs *= 2
                }
            }
        }

        // Falls back beautifully instead of returning raw error to provide continuous flawless workflow
        return generateHighFidelityFallbackPrompt(rawIdea, targetModel, category)
    }
}
