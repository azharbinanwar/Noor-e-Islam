package com.kodeelite.nooreislam.feature.studio.data

import com.kodeelite.nooreislam.feature.studio.data.TemplateStore.generate
import com.kodeelite.nooreislam.feature.studio.data.TemplateStore.presets


/**
 * Owns the template presets + generator (the pools/logic live in StudioTemplates.kt). The studio asks
 * here for [presets] or a fresh [generate] batch — it never generates itself.
 */
object TemplateStore {
    val presets: List<StudioTemplate> get() = STUDIO_TEMPLATES

    fun generate(count: Int = 5, seed: Int): List<StudioTemplate> = generatedTemplates(count = count, seed = seed)
}
