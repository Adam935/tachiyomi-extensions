package eu.kanade.tachiyomi.extension.fr.flamescansfr

import android.util.Log
import eu.kanade.tachiyomi.multisrc.mangathemesia.MangaThemesia
import eu.kanade.tachiyomi.source.model.SManga
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.text.SimpleDateFormat
import java.util.Locale

class LegacyScans : MangaThemesia("Legacy Scans", "https://legacy-scans.com", "fr", dateFormat = SimpleDateFormat("MMMM d, yyyy", Locale.FRENCH)) {
    override val id = 8947802555328550956

    override fun searchMangaSelector() = ".utao .uta .imgu, .listupd .bs .bsx, .listo .bs .bsx"

    override fun searchMangaFromElement(element: Element) = SManga.create().apply {
        thumbnail_url = element.select("img").attr("src")
        title = element.select("a").attr("title")
        setUrlWithoutDomain(element.select("a").attr("href"))
    }

    private fun String?.removeEmptyPlaceholder(): String? {
        return if (this.isNullOrBlank() || this == "-" || this == "N/A") null else this
    }

    override fun mangaDetailsParse(document: Document) = SManga.create().apply {
        document.selectFirst(seriesDetailsSelector)?.let { seriesDetails ->
            title = seriesDetails.selectFirst(seriesTitleSelector)?.text().orEmpty()
            artist = seriesDetails.selectFirst(seriesArtistSelector)?.ownText().removeEmptyPlaceholder()
            author = seriesDetails.selectFirst(seriesAuthorSelector)?.ownText().removeEmptyPlaceholder()
            description = seriesDetails.select(seriesDescriptionSelector).joinToString("\n") { it.text() }.trim()
            // Add alternative name to manga description
            val altName = seriesDetails.selectFirst(seriesAltNameSelector)?.ownText().takeIf { it.isNullOrBlank().not() }
            altName?.let {
                description = "$description\n\n$altNamePrefix$altName".trim()
            }
            val genres = seriesDetails.select(seriesGenreSelector).map { it.text() }.toMutableList()
            // Add series type (manga/manhwa/manhua/other) to genre
            seriesDetails.selectFirst(seriesTypeSelector)?.ownText().takeIf { it.isNullOrBlank().not() }?.let { genres.add(it) }
            genre = genres.map { genre ->
                genre.lowercase(Locale.forLanguageTag(lang)).replaceFirstChar { char ->
                    if (char.isLowerCase()) {
                        char.titlecase(Locale.forLanguageTag(lang))
                    } else {
                        char.toString()
                    }
                }
            }
                .joinToString { it.trim() }

            status = seriesDetails.selectFirst(seriesStatusSelector)?.text().parseStatus()
            thumbnail_url = seriesDetails.select(seriesThumbnailSelector).attr("src")
        }
    }
}
