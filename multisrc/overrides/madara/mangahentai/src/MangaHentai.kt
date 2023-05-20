package eu.kanade.tachiyomi.extension.en.mangahentai

import android.graphics.Insets.add
import eu.kanade.tachiyomi.multisrc.madara.Madara
import eu.kanade.tachiyomi.network.POST
import okhttp3.CacheControl
import okhttp3.FormBody
import okhttp3.Headers
import okhttp3.Request

class MangaHentai : Madara("Manga Hentai", "https://mangahentai.me", "en") {

    open fun formBuilder(page: Int, popular: Boolean) = FormBody.Builder().apply {
        add("action", "madara_load_more")
        add("page", (page - 1).toString())
        add("template", "madara-core/content/content-archive")
        add("vars[orderby]", "meta_value_num")
        add("vars[paged]", "1")
        add("vars[posts_per_page]", "20")
        add("vars[post_type]", "wp-manga")
        add("vars[post_status]", "publish")
        add("vars[meta_key]", if (popular) "_wp_manga_views" else "_latest_update")
        add("vars[order]", "desc")
        add("vars[sidebar]", if (popular) "full" else "right")
        add("vars[manga_archives_item_layout]", "big_thumbnail")

        if (filterNonMangaItems) {
            add("vars[meta_query][0][key]", "_wp_manga_chapter_type")
            add("vars[meta_query][0][value]", "manga")
        }
    }

    open val formHeaders: Headers by lazy { headersBuilder().build() }

    override fun popularMangaSelector() = "div.page-item-detail:not(:has(a[href*='bilibilicomics.com']))"
    override fun popularMangaNextPageSelector(): String? = "body:not(:has(.no-posts))"
    override fun popularMangaRequest(page: Int): Request {
        return POST(
            "$baseUrl/wp-admin/admin-ajax.php",
            formHeaders,
            formBuilder(page, true).build(),
            CacheControl.FORCE_NETWORK,
        )
    }
    override fun latestUpdatesRequest(page: Int): Request {
        return POST("$baseUrl/wp-admin/admin-ajax.php", formHeaders, formBuilder(page, false).build(), CacheControl.FORCE_NETWORK)
    }

    override val useNewChapterEndpoint: Boolean = true
}

