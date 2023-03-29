package eu.kanade.tachiyomi.extension.fr.scanfr


import eu.kanade.tachiyomi.multisrc.mmrcms.MMRCMS
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.util.asJsoup
import okhttp3.Response

class ScanFR : MMRCMS("Scan FR", "https://www.scan-fr.org", "fr") {

    override fun mangaDetailsParse(response: Response): SManga = SManga.create().apply {
        val document = response.asJsoup()

        document.select("h2.listmanga-header, h2.widget-title").firstOrNull()?.text()?.trim()?.let {
            title = it.removePrefix("Manga ")
        }
        thumbnail_url = coverGuess(document.select(".row [class^=img-responsive]").firstOrNull()?.attr("abs:src"), document.location())
        description = document.select(".row .well p").text().trim()

        for (element in document.select(".row .dl-horizontal dt")) {
            when (element.text().trim().lowercase().removeSuffix(":")) {
                "auteur(s)" -> author = element.nextElementSibling()!!.text()
                "artiste(s)" -> artist = element.nextElementSibling()!!.text()
                "catégories" -> genre = element.nextElementSibling()!!.select("a").joinToString {
                    it.text().trim()
                }
                "statut" -> status = when (element.nextElementSibling()!!.text().trim().lowercase()) {
                    "complete" -> SManga.COMPLETED
                    "en cours" -> SManga.ONGOING
                    else -> SManga.UNKNOWN
                }
            }
        }
    }

    companion object {
        private const val MANGA_SUFFIX = "Manga"
    }
}
