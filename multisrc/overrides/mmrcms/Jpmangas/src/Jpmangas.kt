package eu.kanade.tachiyomi.extension.fr.jpmangas

import eu.kanade.tachiyomi.multisrc.mmrcms.MMRCMS
import eu.kanade.tachiyomi.source.model.SChapter
import org.jsoup.nodes.Element
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class Jpmangas : MMRCMS("Jpmangas", "https://jpmangas.cc", "fr") {

    private fun parseDate(dateText: String): Long {
        return try {
            DATE_FORMAT.parse(dateText)?.time ?: 0
        } catch (e: ParseException) {
            0L
        }
    }
    override fun chapterListSelector() = "ul[class^=chapters] > li:not(.btn)"

    override fun nullableChapterFromElement(element: Element): SChapter? {
        val chapter = SChapter.create()

        val titleWrapper = element.select("[class^=chapter-title-rtl]").first()!!
        // Some websites add characters after "..-rtl" thus the need of checking classes that starts with that
        val url = titleWrapper.getElementsByTag("a")
            .attr("href")

        chapter.url = getUrlWithoutBaseUrl(url)
        chapter.name = titleWrapper.text()

        // Parse date
        val dateText = element.getElementsByClass("date-chapter-title-rtl").text().trim()
        chapter.date_upload = parseDate(dateText)

        return chapter
    }
    companion object {
        private val DATE_FORMAT = SimpleDateFormat("d MMM. yyyy", Locale.US)
    }
}
