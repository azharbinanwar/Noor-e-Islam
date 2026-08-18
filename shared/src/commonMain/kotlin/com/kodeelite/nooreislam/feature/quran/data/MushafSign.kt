package com.kodeelite.nooreislam.feature.quran.data

import androidx.compose.runtime.Composable
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.a_full_stop_the_commonest_sign_in_indopak_mushafs
import com.kodeelite.nooreislam.resources.a_verse_of_prostration_reciting_it_calls_for_a_sajdah
import com.kodeelite.nooreislam.resources.absolute_stop
import com.kodeelite.nooreislam.resources.before_a_ba_the_nun_is_pronounced_as_a_meem
import com.kodeelite.nooreislam.resources.both_are_allowed_but_continuing_is_preferred
import com.kodeelite.nooreislam.resources.both_are_allowed_but_stopping_is_preferred
import com.kodeelite.nooreislam.resources.brief_pause
import com.kodeelite.nooreislam.resources.carry_on_the_meaning_is_not_complete_yet
import com.kodeelite.nooreislam.resources.compulsory_stop
import com.kodeelite.nooreislam.resources.continuing_is_better
import com.kodeelite.nooreislam.resources.do_not_stop
import com.kodeelite.nooreislam.resources.hold_the_vowel_longer_than_usual
import com.kodeelite.nooreislam.resources.in_other_mushafs
import com.kodeelite.nooreislam.resources.lengthen_the_vowel
import com.kodeelite.nooreislam.resources.marks_a_quarter_of_a_hizb_it_does_not_affect_recitation
import com.kodeelite.nooreislam.resources.never_pronounced
import com.kodeelite.nooreislam.resources.nun_becomes_meem
import com.kodeelite.nooreislam.resources.optional_stop
import com.kodeelite.nooreislam.resources.page_markers
import com.kodeelite.nooreislam.resources.pause_marks
import com.kodeelite.nooreislam.resources.pause_without_taking_a_breath_then_continue
import com.kodeelite.nooreislam.resources.permitted_stop
import com.kodeelite.nooreislam.resources.prostration
import com.kodeelite.nooreislam.resources.quarter_marker
import com.kodeelite.nooreislam.resources.recitation_marks
import com.kodeelite.nooreislam.resources.silent_when_joining
import com.kodeelite.nooreislam.resources.skip_it_when_continuing_and_sound_it_when_you_stop
import com.kodeelite.nooreislam.resources.stop_at_one_of_the_pair
import com.kodeelite.nooreislam.resources.stop_here_reading_straight_on_changes_the_meaning
import com.kodeelite.nooreislam.resources.stop_or_carry_on_both_are_equally_fine
import com.kodeelite.nooreislam.resources.stopping_is_allowed_though_joining_reads_better
import com.kodeelite.nooreislam.resources.stopping_is_better
import com.kodeelite.nooreislam.resources.the_letter_is_written_but_never_sounded
import com.kodeelite.nooreislam.resources.these_marks_are_not_printed_in_this_qurans_text
import com.kodeelite.nooreislam.resources.this_mark_comes_in_twos_stop_at_either_one_never_at_both
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/** A real place the mark appears, so the guide can show it in context and jump there. */
class SignExample(val surah: Int, val ayah: Int, val text: String) {
    val reference: String get() = "$surah:$ayah"
}

/** The families a mark can belong to. [noteRes] warns when a family is reference only. */
enum class MushafSignGroup(val labelRes: StringResource, val noteRes: StringResource? = null) {
    Pause(Res.string.pause_marks),
    Page(Res.string.page_markers),
    Recitation(Res.string.recitation_marks, Res.string.these_marks_are_not_printed_in_this_qurans_text),
    Other(Res.string.in_other_mushafs, Res.string.these_marks_are_not_printed_in_this_qurans_text);

    val label: String @Composable get() = stringResource(labelRes)
}

/**
 * Marks a reader meets in a mushaf. Pause and page families are carried by the Tanzil text we ship and
 * each names a real ayah; the other two are reference only, so they have no example to open.
 *
 * [inkHeightEm] and [inkCentreEm] are the mark's own outline measured off tanzil_scheherazade, the specimen
 * face: how tall its ink is, and how far its centre sits above the baseline. The marks are cut at wildly
 * different sizes and heights, so the card sizes and positions each one from these rather than sharing a
 * font size, which leaves the small ones as specks and the tall ones cropped.
 */
enum class MushafSign(
    val mark: Char,
    val group: MushafSignGroup,
    val arabicName: String,
    val labelRes: StringResource,
    val meaningRes: StringResource,
    val inkHeightEm: Float,
    val inkCentreEm: Float,
    val example: SignExample? = null,
) {
    Lazim(
        'ۘ', MushafSignGroup.Pause, "وقف لازم",
        Res.string.compulsory_stop,
        Res.string.stop_here_reading_straight_on_changes_the_meaning,
        0.112f, 0.537f,
        SignExample(54, 6, "فَتَوَلَّ عَنْهُمْ ۘ يَوْمَ يَدْعُ الدَّاعِ"),
    ),
    Mamnu(
        'ۙ', MushafSignGroup.Pause, "وقف ممنوع",
        Res.string.do_not_stop,
        Res.string.carry_on_the_meaning_is_not_complete_yet,
        0.280f, 0.627f,
        SignExample(15, 60, "إِلَّا امْرَأَتَهُ قَدَّرْنَا ۙ إِنَّهَا لَمِنَ الْغَابِرِينَ"),
    ),
    WaqfAwla(
        'ۗ', MushafSignGroup.Pause, "الوقف أولى",
        Res.string.stopping_is_better,
        Res.string.both_are_allowed_but_stopping_is_preferred,
        0.336f, 0.669f,
        SignExample(37, 138, "وَبِاللَّيْلِ ۗ أَفَلَا تَعْقِلُونَ"),
    ),
    WaslAwla(
        'ۖ', MushafSignGroup.Pause, "الوصل أولى",
        Res.string.continuing_is_better,
        Res.string.both_are_allowed_but_continuing_is_preferred,
        0.336f, 0.669f,
        SignExample(70, 15, "كَلَّا ۖ إِنَّهَا لَظَىٰ"),
    ),
    Jaiz(
        'ۚ', MushafSignGroup.Pause, "وقف جائز",
        Res.string.optional_stop,
        Res.string.stop_or_carry_on_both_are_equally_fine,
        0.274f, 0.624f,
        SignExample(26, 11, "قَوْمَ فِرْعَوْنَ ۚ أَلَا يَتَّقُونَ"),
    ),
    Muanaqah(
        'ۛ', MushafSignGroup.Pause, "وقف المعانقة",
        Res.string.stop_at_one_of_the_pair,
        Res.string.this_mark_comes_in_twos_stop_at_either_one_never_at_both,
        0.087f, 0.540f,
        SignExample(2, 2, "لَا رَيْبَ ۛ فِيهِ ۛ هُدًى لِّلْمُتَّقِينَ"),
    ),
    Sakta(
        'ۜ', MushafSignGroup.Pause, "سكتة",
        Res.string.brief_pause,
        Res.string.pause_without_taking_a_breath_then_continue,
        0.221f, 0.593f,
        SignExample(75, 27, "وَقِيلَ مَنْ ۜ رَاقٍ"),
    ),

    RubElHizb(
        '۞', MushafSignGroup.Page, "ربع الحزب",
        Res.string.quarter_marker,
        Res.string.marks_a_quarter_of_a_hizb_it_does_not_affect_recitation,
        0.855f, 0.217f,
        SignExample(70, 19, "۞ إِنَّ الْإِنسَانَ خُلِقَ هَلُوعًا"),
    ),
    Sajda(
        '۩', MushafSignGroup.Page, "سجدة",
        Res.string.prostration,
        Res.string.a_verse_of_prostration_reciting_it_calls_for_a_sajdah,
        0.537f, 0.269f,
        SignExample(53, 62, "فَاسْجُدُوا لِلَّهِ وَاعْبُدُوا ۩"),
    ),

    Iqlab(
        'ۢ', MushafSignGroup.Recitation, "إقلاب",
        Res.string.nun_becomes_meem,
        Res.string.before_a_ba_the_nun_is_pronounced_as_a_meem,
        0.234f, 0.592f,
    ),
    SilentAlways(
        '۟', MushafSignGroup.Recitation, "صفر مستدير",
        Res.string.never_pronounced,
        Res.string.the_letter_is_written_but_never_sounded,
        0.071f, 0.527f,
    ),
    SilentJoined(
        '۠', MushafSignGroup.Recitation, "صفر مستطيل",
        Res.string.silent_when_joining,
        Res.string.skip_it_when_continuing_and_sound_it_when_you_stop,
        0.067f, 0.526f,
    ),
    Madda(
        'ۤ', MushafSignGroup.Recitation, "مد",
        Res.string.lengthen_the_vowel,
        Res.string.hold_the_vowel_longer_than_usual,
        0.055f, 0.562f,
    ),

    Mutlaq(
        'ؕ', MushafSignGroup.Other, "وقف مطلق",
        Res.string.absolute_stop,
        Res.string.a_full_stop_the_commonest_sign_in_indopak_mushafs,
        0.249f, 0.680f,
    ),
    Mujawwaz(
        'ؗ', MushafSignGroup.Other, "وقف مجوز",
        Res.string.permitted_stop,
        Res.string.stopping_is_allowed_though_joining_reads_better,
        0.262f, 0.616f,
    );

    // combining marks need a base to sit on, the way a printed mushaf key prints them over a tatweel.
    // The page markers are standalone symbols and take no base.
    val glyph: String get() = if (group == MushafSignGroup.Page) "$mark" else "ـ$mark"

    val label: String @Composable get() = stringResource(labelRes)
    val meaning: String @Composable get() = stringResource(meaningRes)
}
