package com.example.miqatapp.feature.studio.presentation

import androidx.compose.animation.AnimatedVisibility
import com.example.miqatapp.core.constants.defaults.StudioDefaults
import com.example.miqatapp.feature.studio.data.GradientStore
import com.example.miqatapp.feature.studio.data.ImageStore
import com.example.miqatapp.feature.studio.presentation.panels.CardPanel
import com.example.miqatapp.feature.studio.presentation.panels.EffectsPanel
import com.example.miqatapp.feature.studio.presentation.panels.PresetsPanel
import com.example.miqatapp.feature.studio.presentation.panels.TextSizePanel
import com.example.miqatapp.feature.studio.presentation.panels.TextStylePanel
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.composables.icons.lucide.AlignLeft
import com.composables.icons.lucide.BookOpen
import com.composables.icons.lucide.Bookmark
import com.composables.icons.lucide.BoxSelect
import com.composables.icons.lucide.CalendarDays
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.ChevronLeft
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.GalleryThumbnails
import com.composables.icons.lucide.Image
import com.composables.icons.lucide.Info
import com.composables.icons.lucide.Layers
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Maximize
import com.composables.icons.lucide.Download
import com.composables.icons.lucide.LayoutTemplate
import androidx.compose.foundation.layout.width
import com.composables.icons.lucide.Palette
import com.composables.icons.lucide.Pencil
import com.composables.icons.lucide.Redo
import com.composables.icons.lucide.Share
import com.composables.icons.lucide.Sparkles
import com.composables.icons.lucide.Stamp
import com.composables.icons.lucide.Star
import com.composables.icons.lucide.TextQuote
import com.composables.icons.lucide.Type
import com.composables.icons.lucide.Undo
import com.example.miqatapp.config.theme.AppTheme
import com.example.miqatapp.core.components.AppBottomSheet
import com.example.miqatapp.core.components.AppSwitch
import androidx.compose.runtime.rememberCoroutineScope
import org.koin.compose.koinInject
import com.example.miqatapp.feature.studio.data.StudioCreationRepository
import com.example.miqatapp.core.datetime.currentDate
import com.example.miqatapp.core.datetime.toHijri
import com.example.miqatapp.core.locale.tr
import com.example.miqatapp.core.navigation.LocalAppNavigator
import com.example.miqatapp.core.util.ShareService
import com.example.miqatapp.feature.studio.data.LogoCorner
import com.example.miqatapp.feature.studio.data.PlacedSticker
import com.example.miqatapp.feature.studio.data.StudioAspectRatio
import com.example.miqatapp.feature.quran.data.QuranSymbols
import com.example.miqatapp.feature.quran.data.Ayah
import com.example.miqatapp.feature.studio.data.StudioConfig
import com.example.miqatapp.feature.studio.data.StudioGradient
import com.example.miqatapp.feature.studio.presentation.components.AlignmentToggle
import com.example.miqatapp.feature.studio.presentation.components.AspectRatioStrip
import com.example.miqatapp.feature.studio.presentation.components.FontPicker
import com.example.miqatapp.feature.studio.presentation.components.GradientStripPicker
import com.example.miqatapp.feature.studio.presentation.components.ImageGridPicker
import com.example.miqatapp.feature.studio.presentation.components.StickerPicker
import com.example.miqatapp.feature.studio.presentation.components.TemplatePicker
import com.example.miqatapp.feature.studio.presentation.panels.BrandingPanel
import com.example.miqatapp.feature.studio.presentation.panels.ContentPanel
import com.example.miqatapp.feature.studio.presentation.panels.DatesPanel
import com.example.miqatapp.core.util.toSurahKey
import com.example.miqatapp.resources.Res
import com.example.miqatapp.resources.miqat_logo
import com.example.miqatapp.resources.quran_juz
import com.example.miqatapp.resources.quran_surah_name
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.painterResource
import kotlin.math.roundToInt

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
            val isFull = config.aspectRatio == StudioAspectRatio.Full
            val screenRatio = maxWidth / maxHeight
            val activeRatio = when (config.aspectRatio) {
                StudioAspectRatio.Full -> screenRatio
                StudioAspectRatio.Original -> imageRatio ?: screenRatio   // image's own ratio once loaded
                else -> config.aspectRatio.ratio ?: 0.64f
            }

            // --- WORKSPACE ---
            Box(Modifier.fillMaxSize().padding(bottom = if (isEditing) 180.dp else 0.dp), contentAlignment = Alignment.Center) {
                val animatedScale by animateFloatAsState(if (isEditing) 0.85f else 1f)
                val animatedOffset by animateFloatAsState(if (isEditing) 40f else 0f)

                // Artboard Scaling Engine
                Box(
                    Modifier.fillMaxSize()
                        .pointerInput(isEditing) { if (isEditing) detectTapGestures { toolsVisible = !toolsVisible } },
                    contentAlignment = Alignment.Center,
                ) {
                    val currentMaxWidth = this@BoxWithConstraints.maxWidth
                    val actualArtWidth = if (isFull && !isEditing) currentMaxWidth else (currentMaxWidth * animatedScale)
                    val internalScale = (actualArtWidth / CANVAS_BASE_WIDTH)

                    Box(
                        Modifier
                            .graphicsLayer {
                                translationY = animatedOffset
                                scaleX = internalScale
                                scaleY = internalScale
                            }
                            .requiredWidth(CANVAS_BASE_WIDTH)
                            .aspectRatio(activeRatio)
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

                        StudioMode.Stickers -> StickerPicker { type ->
                            updateConfig(config.copy(stickers = config.stickers + PlacedSticker(config.stickers.size, type, 0.5f, 0.5f)))
                        }

                        StudioMode.Card -> CardPanel(config, cardSwatches) { updateConfig(it) }
                        StudioMode.Effects -> EffectsPanel(config) { updateConfig(it) }

                        StudioMode.Templates -> TemplatePicker(config) { updateConfig(it) } // fixed looks + a Generate section
                        StudioMode.Presets -> PresetsPanel(onReset = { updateConfig(initialConfig) }, onSave = { saveCurrent() })

                        StudioMode.Dates -> DatesPanel(config) { updateConfig(it) }

                        StudioMode.Branding -> BrandingPanel(config) { updateConfig(it) }
                    }
                }
            } else if (!isEditing) {
                StudioDoneBar(
                    onEdit = { isEditing = true },
                    onSaveToGallery = { /* placeholder: save to gallery */ },
                    onShare = {
                        val text =
                            "${config.ayahs.joinToString("\n") { it.text }}\n\n— (${config.ayahs.first().surah}:${
                                config.ayahs.joinToString(",") { it.ayah.toString() }
                            })"
                        ShareService.shareText(text)
                        saveCurrent()
                    },
                    onExport = { /* placeholder: export sizes + preview */ },
                )
            }

            // tools hidden → floating pen (our style) to bring the panel back
            if (isEditing && !toolsVisible) {
                Box(Modifier.align(Alignment.BottomEnd).navigationBarsPadding().padding(20.dp)) {
                    StudioButton(icon = Lucide.Pencil, onClick = { toolsVisible = true })
                }
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


