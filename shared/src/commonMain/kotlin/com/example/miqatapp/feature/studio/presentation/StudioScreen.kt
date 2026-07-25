package com.example.miqatapp.feature.studio.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Pencil
import com.example.miqatapp.config.theme.AppTheme
import com.example.miqatapp.core.components.AppBottomSheet
import com.example.miqatapp.core.constants.defaults.StudioDefaults
import com.example.miqatapp.core.navigation.LocalAppNavigator
import com.example.miqatapp.core.util.GalleryService
import com.example.miqatapp.core.util.ShareService
import com.example.miqatapp.core.util.toPngBytes
import com.example.miqatapp.feature.quran.data.Ayah
import com.example.miqatapp.feature.studio.data.GradientStore
import com.example.miqatapp.feature.studio.data.ImageStore
import com.example.miqatapp.feature.studio.data.StudioAspectRatio
import com.example.miqatapp.feature.studio.data.StudioConfig
import com.example.miqatapp.feature.studio.data.StudioCreationRepository
import com.example.miqatapp.feature.studio.presentation.components.AlignmentToggle
import com.example.miqatapp.feature.studio.presentation.components.AspectRatioStrip
import com.example.miqatapp.feature.studio.presentation.components.FontPicker
import com.example.miqatapp.feature.studio.presentation.components.GradientStripPicker
import com.example.miqatapp.feature.studio.presentation.components.ImageGridPicker
import com.example.miqatapp.feature.studio.presentation.components.TemplatePicker
import com.example.miqatapp.feature.studio.presentation.panels.BrandingPanel
import com.example.miqatapp.feature.studio.presentation.panels.CardPanel
import com.example.miqatapp.feature.studio.presentation.panels.ContentPanel
import com.example.miqatapp.feature.studio.presentation.panels.DatesPanel
import com.example.miqatapp.feature.studio.presentation.panels.EffectsPanel
import com.example.miqatapp.feature.studio.presentation.panels.TextSizePanel
import com.example.miqatapp.feature.studio.presentation.panels.TextStylePanel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject

// StudioMode moved to StudioMode.kt

// STUDIO_IMAGES → ImageStore, STUDIO_GRADIENTS → GradientStore

internal val CANVAS_BASE_WIDTH = 380.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudioScreen(
    ayahs: List<Ayah>,
) {

    val fullText = ayahs.joinToString(" ") { it.text }
    val initialConfig = remember(fullText) {
        val size = when {
            fullText.length < StudioDefaults.SHORT_LEN -> StudioDefaults.FONT_SHORT
            fullText.length < StudioDefaults.MEDIUM_LEN -> StudioDefaults.FONT_MEDIUM
            else -> StudioDefaults.FONT_LONG
        }
        StudioConfig.default(ayahs).copy(fontSize = size)
    }

    // all editing state + history now lives in the holder; the screen delegates to it (read + write)
    val repo = koinInject<StudioCreationRepository>()
    val scope = rememberCoroutineScope()
    val store = remember { StudioStore(initialConfig, repo, scope) }
    var config by store.configState
    var isEditing by store.isEditingState
    var studioMode by store.studioModeState
    var galleryOpen by store.galleryOpenState
    var savedCreations by store.savedCreationsState
    var draft by store.draftState
    var showSavedHint by store.showSavedHintState
    var toolsVisible by remember { mutableStateOf(true) }   // tap canvas to hide the tools panel (top bar stays)
    var imageRatio by remember { mutableStateOf<Float?>(null) }   // loaded photo w/h, drives the "Original" layout
    // suggested colors from the current background — image or gradient palette (empty on solid)
    val bgImage = ImageStore.byUrl(config.bgImageUrl)
    val palColors = bgImage?.colors ?: config.bgGradient?.colors ?: emptyList()
    val palOnColors = bgImage?.onColors ?: config.bgGradient?.onColors ?: emptyList()
    val textSwatches = palOnColors + palColors // onColor first for text
    val cardSwatches = palColors + palOnColors // base color first for card bg

    val colors = AppTheme.colors

    // captures the artboard at its native resolution (unscaled by the preview zoom) for share / export
    val captureLayer = rememberGraphicsLayer()
    var sharing by remember { mutableStateOf(false) }   // spinner on Share while we capture + encode
    var savingToGallery by remember { mutableStateOf(false) }   // spinner on the Download button
    var galleryHint by remember { mutableStateOf<String?>(null) }   // transient "Saved to gallery" pill
    var shareSheetOpen by remember { mutableStateOf(false) }   // review/edit caption before sharing

    fun updateConfig(newConfig: StudioConfig) = store.update(newConfig)
    fun undo() = store.undo()
    fun redo() = store.redo()
    fun saveCurrent() = store.saveCurrent()

    LaunchedEffect(showSavedHint) {
        if (showSavedHint) {
            delay(1500); showSavedHint = false
        }
    }

    // auto-save the in-progress design as a draft once editing settles (only after real edits)
    LaunchedEffect(config) {
        if (config != initialConfig) {
            delay(800); store.saveDraft()
        }
    }

    val nav = LocalAppNavigator.current


    Scaffold(containerColor = colors.background) { _ ->

        BoxWithConstraints(Modifier.fillMaxSize()) {
            val screenRatio = maxWidth / maxHeight
            val activeRatio = when (config.aspectRatio) {
                StudioAspectRatio.Full -> screenRatio
                StudioAspectRatio.Original -> imageRatio ?: screenRatio   // image's own ratio once loaded
                else -> config.aspectRatio.ratio ?: 0.64f
            }

            // --- WORKSPACE ---
            // compact = editing WITH the tools panel up: the canvas fits the area above the panel. Panel hidden
            // (tap) or done → it fits the whole screen. Always contain-fit + centered, no dead margins.
            val compact = isEditing && toolsVisible
            Box(Modifier.fillMaxSize().padding(bottom = if (compact) 180.dp else 0.dp), contentAlignment = Alignment.Center) {
                Box(
                    Modifier.fillMaxSize()
                        .pointerInput(isEditing) { if (isEditing) detectTapGestures { toolsVisible = !toolsVisible } },
                    contentAlignment = Alignment.Center,
                ) {
                    // contain-fit: largest artboard of ratio `activeRatio` that fits the free area (whole screen,
                    // minus the panel's room when the tools are up). Bound by width or height, whichever binds first.
                    val availW = this@BoxWithConstraints.maxWidth
                    val availH = this@BoxWithConstraints.maxHeight - (if (compact) 180.dp else 0.dp)
                    val fittedWidth = minOf(availW, availH * activeRatio)
                    val artWidth by animateDpAsState(fittedWidth)
                    val internalScale = artWidth / CANVAS_BASE_WIDTH

                    Box(
                        Modifier
                            .graphicsLayer { scaleX = internalScale; scaleY = internalScale }
                            .requiredWidth(CANVAS_BASE_WIDTH)
                            .aspectRatio(activeRatio)
                            // record the design (at native res, before the preview scale) into the capture layer
                            .drawWithContent {
                                captureLayer.record { this@drawWithContent.drawContent() }
                                drawLayer(captureLayer)
                            }
                    ) {
                        DesignCanvas(config, Modifier.fillMaxSize(), isEditing, onImageRatio = { imageRatio = it }) { store.updateLive(it) }
                    }
                }
            }

            // Top Buttons
            AnimatedVisibility(visible = isEditing, enter = fadeIn(), exit = fadeOut()) {
                StudioTopBar(
                    savedHint = showSavedHint,
                    onBack = { nav.back() },
                    onUndo = { undo() },
                    onRedo = { redo() },
                    onSave = { saveCurrent() },
                    onGallery = { galleryOpen = true },
                    onDone = { isEditing = false },
                )
            }

            if (isEditing && toolsVisible) {
                StudioPanel(studioMode, onSelectMode = { studioMode = it }) {
                    when (studioMode) {
                        StudioMode.Layout -> AspectRatioStrip(config.aspectRatio) { updateConfig(config.copy(aspectRatio = it)) }
                        StudioMode.BgImage -> ImageGridPicker(ImageStore.urls, config.bgImageUrl) {
                            // new photo → clear any prior zoom/pan so it doesn't inherit the old crop
                            updateConfig(config.copy(bgImageUrl = it, bgGradient = null, bgImageScale = 1f, bgImageOffsetX = 0f, bgImageOffsetY = 0f))
                        }

                        StudioMode.BgGradient -> GradientStripPicker(
                            config.bgGradient,
                            { updateConfig(config.copy(bgGradient = it, bgImageUrl = null)) },
                            GradientStore.presets
                        )

                        StudioMode.Fonts -> FontPicker(config.fontFamily) { updateConfig(config.copy(fontFamily = it)) }
                        StudioMode.TextSize -> TextSizePanel(config) { updateConfig(it) }
                        StudioMode.TextStyle -> TextStylePanel(config, textSwatches) { updateConfig(it) }

                        StudioMode.Align -> AlignmentToggle(config.textAlign) { updateConfig(config.copy(textAlign = it)) }
                        StudioMode.Content -> ContentPanel(config) { updateConfig(it) }

                        // TODO(studio): stickers not ready — re-enable with StudioMode.Stickers
                        // StudioMode.Stickers -> StickerPicker { type ->
                        //     updateConfig(config.copy(stickers = config.stickers + PlacedSticker(config.stickers.size, type, 0.5f, 0.5f)))
                        // }

                        StudioMode.Card -> CardPanel(config, cardSwatches) { updateConfig(it) }
                        StudioMode.Effects -> EffectsPanel(config) { updateConfig(it) }

                        StudioMode.Templates -> TemplatePicker(config) { updateConfig(it) } // fixed looks + a Generate section
                        // TODO(studio): presets not ready — re-enable with StudioMode.Presets
                        // StudioMode.Presets -> PresetsPanel(onReset = { updateConfig(initialConfig) }, onSave = { saveCurrent() })

                        StudioMode.Dates -> DatesPanel(config) { updateConfig(it) }

                        StudioMode.Branding -> BrandingPanel(config) { updateConfig(it) }
                    }
                }
            } else if (!isEditing) {
                StudioDoneBar(
                    onEdit = { isEditing = true },
                    savingToGallery = savingToGallery,
                    onSaveToGallery = {
                        if (!savingToGallery) {
                            savingToGallery = true
                            scope.launch {
                                try {
                                    val bitmap = captureLayer.toImageBitmap()
                                    val bytes = withContext(Dispatchers.Default) { bitmap.toPngBytes() }
                                    galleryHint = if (GalleryService.saveImage(bytes, "miqat_ayah_${config.ayahs.first().surah}.png"))
                                        "Saved to gallery" else "Couldn't save"
                                } finally {
                                    savingToGallery = false
                                }
                            }
                        }
                    },
                    sharing = sharing,
                    onShare = { if (!sharing) shareSheetOpen = true },
                    onExport = { /* placeholder: export sizes + preview */ },
                )

                // transient confirmation for save-to-gallery (share has the system sheet as its own signal)
                galleryHint?.let {
                    Box(Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = 132.dp)) {
                        Text(
                            it,
                            color = colors.onSurface,
                            fontSize = 12.sp,
                            modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(colors.surface.copy(alpha = 0.9f))
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                        )
                    }
                    LaunchedEffect(it) { delay(1600); galleryHint = null }
                }
            }

            // tools hidden → floating pen (our style) to bring the panel back
            if (isEditing && !toolsVisible) {
                Box(Modifier.align(Alignment.BottomEnd).navigationBarsPadding().padding(20.dp)) {
                    StudioButton(icon = Lucide.Pencil, onClick = { toolsVisible = true })
                }
            }

            if (shareSheetOpen) {
                ShareSheet(
                    // ayah text + its reference travel together — hiding the ayah hides the number too
                    ayahText = "${config.ayahs.joinToString("\n") { it.text }}\n\n" +
                            "(${config.ayahs.first().surah}:${config.ayahs.joinToString(",") { it.ayah.toString() }})",
                    otherText = "Shared with Miqat",
                    onDismiss = { shareSheetOpen = false },
                    onShare = { caption ->
                        shareSheetOpen = false
                        sharing = true
                        scope.launch {
                            try {
                                val bitmap = captureLayer.toImageBitmap()
                                val bytes = withContext(Dispatchers.Default) { bitmap.toPngBytes() }
                                ShareService.shareImage(bytes, "miqat_ayah.png", caption)
                            } finally {
                                sharing = false
                            }
                        }
                        saveCurrent()
                    },
                )
            }

            if (galleryOpen) {
                AppBottomSheet(onDismiss = { galleryOpen = false }, title = "My Creations") {
                    CreationsGrid(
                        savedCreations,
                        draft = draft,
                        onSelect = { config = it; galleryOpen = false; isEditing = false },
                        onDelete = { store.delete(it) },
                        onResumeDraft = { draft?.let { config = it; galleryOpen = false; isEditing = true } },
                    )
                }
            }
        }
    }
}


