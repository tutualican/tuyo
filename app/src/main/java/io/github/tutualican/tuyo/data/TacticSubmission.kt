package io.github.tutualican.tuyo.data

import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * Zero-cost moderation: the app never uploads anything. It prepares a structured
 * text that the student can send through their own share target (GitHub app,
 * e-mail, messaging). A maintainer then reviews it with the repository's issue
 * template before it is bundled in a release.
 */
object TacticSubmission {
    fun share(context: Context) {
        val text = buildString {
            appendLine("Tuyo'ya taktik önerisi")
            appendLine()
            appendLine("Başlık:")
            appendLine("Kategori (çalışma / TYT / AYT / sınav stratejisi / sınav günü / iyi hâl):")
            appendLine("Özet:")
            appendLine("Adımlar:")
            appendLine("1. ")
            appendLine("2. ")
            appendLine("3. ")
            appendLine("Kanıt veya kaynak:")
            appendLine("Neden işe yarıyor:")
            appendLine()
            appendLine("Kişisel bilgi eklemeyin. Gönderiler yayından önce incelenir.")
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Tuyo taktik önerisi")
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, "Taktik önerisini paylaş"))
    }

    fun openUrl(context: Context, url: String) {
        runCatching {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }
    }
}
