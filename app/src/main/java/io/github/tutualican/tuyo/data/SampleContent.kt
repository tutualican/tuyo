package io.github.tutualican.tuyo.data

import io.github.tutualican.tuyo.domain.model.Confusion
import io.github.tutualican.tuyo.domain.model.ErrorKind
import io.github.tutualican.tuyo.domain.model.EvidenceLevel
import io.github.tutualican.tuyo.domain.model.ExamNet
import io.github.tutualican.tuyo.domain.model.Mistake
import io.github.tutualican.tuyo.domain.model.StudyData
import io.github.tutualican.tuyo.domain.model.Subject
import io.github.tutualican.tuyo.domain.model.Tactic
import io.github.tutualican.tuyo.domain.model.Topic
import java.time.LocalDate

/**
 * Fixture content for the first runnable slice. It is shaped exactly like the
 * bundled JSON will be, so replacing this object with a repository is a
 * mechanical change rather than a rewrite.
 */
object SampleContent {

    fun studyData(today: LocalDate = LocalDate.now()): StudyData {
        val topics = listOf(
            Topic(
                id = "turev",
                subject = Subject.MATEMATIK,
                title = "Türev",
                summary = "Değişim oranı, teğet eğimi ve zincir kuralı.",
                keyIdea = "Zincir kuralı: dış fonksiyonun türevi × iç fonksiyonun türevi. " +
                    "İç türevi atlamak, AYT'de en pahalı tek hatadır.",
                confusions = listOf(
                    Confusion(
                        question = "İç fonksiyonun türevini neden çarpıyoruz?",
                        answer = "Çünkü x değişirken iç fonksiyon da değişir. " +
                            "Zincir kuralı, iki değişimin oranını üst üste bindirir: " +
                            "dy/dx = dy/du · du/dx.",
                    ),
                    Confusion(
                        question = "f(g(x)) ile f(x)·g(x) aynı şey mi?",
                        answer = "Hayır. f(g(x)) bileşke, f(x)·g(x) çarpımdır. " +
                            "Türevde ilki zincir, ikincisi çarpım kuralı ister.",
                    ),
                ),
                mastery = 2,
            ),
            Topic(
                id = "limit",
                subject = Subject.MATEMATIK,
                title = "Limit",
                summary = "Noktaya yaklaşırken davranış; sürekliliğin temeli.",
                keyIdea = "Limit, noktadaki değer değil, noktaya yaklaşırkenki davranıştır. " +
                    "0/0 bir sonuç değil, bir belirsizliktir; sadeleştirince yol açılır.",
                confusions = listOf(
                    Confusion(
                        question = "0/0 her zaman tanımsız mı?",
                        answer = "İfadenin kendisi tanımsızdır ama limiti var olabilir. " +
                            "Önce çarpanlara ayır, sonra sadeleştir.",
                    ),
                    Confusion(
                        question = "Süreklilik limiti garanti eder mi?",
                        answer = "Süreklilik limitin var ve fonksiyon değerine eşit olmasını gerektirir. " +
                            "Limit varken fonksiyon o noktada tanımsız olabilir.",
                    ),
                ),
                mastery = 1,
            ),
            Topic(
                id = "newton",
                subject = Subject.FIZIK,
                title = "Newton hareket yasaları",
                summary = "Kuvvet, kütle ve ivme; serbest cisim diyagramı.",
                keyIdea = "Önce serbest cisim diyagramı. Her kuvveti ayrı çiz, " +
                    "sonra bileşenlere ayır. Sürtünme her zaman harekete zıttır.",
                confusions = listOf(
                    Confusion(
                        question = "Net kuvvet sıfırsa cisim durur mu?",
                        answer = "Hayır, sabit hızla hareket edebilir. " +
                            "Sıfır net kuvvet ivmenin sıfır olduğunu söyler, hızın değil.",
                    ),
                    Confusion(
                        question = "Etki-tepki kuvvetleri neden birbirini götürmez?",
                        answer = "Çünkü farklı cisimlere etki ederler. " +
                            "Aynı cismin diyagramına iki kuvveti birlikte yazma.",
                    ),
                ),
                mastery = 3,
            ),
            Topic(
                id = "paragraf",
                subject = Subject.TURKCE,
                title = "Paragraf",
                summary = "Ana fikir, yardımcı fikir ve yazarın amacı.",
                keyIdea = "Ana fikir, paragrafın tamamını kapsayan tek cümledir. " +
                    "Bir örnek ya da ayrıntı ana fikir olamaz.",
                confusions = listOf(
                    Confusion(
                        question = "Ana fikir ile konu arasındaki fark ne?",
                        answer = "Konu 'ne anlatılıyor?', ana fikir 'ne söyleniyor?' sorusunun cevabıdır. " +
                            "Ana fikir yargı bildirir.",
                    ),
                ),
                mastery = 4,
            ),
            Topic(
                id = "mol",
                subject = Subject.KIMYA,
                title = "Mol",
                summary = "Tanecik sayısı, kütle ve hacim arasındaki köprü.",
                keyIdea = "1 mol = 6,02·10²³ tanecik. Mol kütlesi g/mol'dür; " +
                    "atom kütlesi ile mol kütlesini karıştırma.",
                confusions = listOf(
                    Confusion(
                        question = "Mol kütlesi neden periyodik tablodaki sayıya eşit?",
                        answer = "Çünkü 1 mol atomun gram cinsinden kütlesi, " +
                            "atomik kütle birimindeki sayıya sayısal olarak eşittir.",
                    ),
                ),
                mastery = 2,
            ),
            Topic(
                id = "elektrik",
                subject = Subject.FIZIK,
                title = "Elektrik devreleri",
                summary = "Akım, gerilim, direnç ve eşdeğer direnç.",
                keyIdea = "Seride akım aynı, gerilim paylaşılır. Paralelde gerilim aynı, " +
                    "akım paylaşılır. Eşdeğer direnç paralelde küçülür.",
                confusions = listOf(
                    Confusion(
                        question = "Paralel dirençte eşdeğer direnç neden küçülür?",
                        answer = "Akıma daha fazla yol açılır. Toplam iletkenlik artar, " +
                            "direnç azalır.",
                    ),
                ),
                mastery = 1,
            ),
        )

        val mistakes = listOf(
            Mistake(
                id = "m1",
                subject = Subject.MATEMATIK,
                topicId = "turev",
                topicTitle = "Türev · Zincir kuralı",
                questionRef = "2024 AYT · 14. soru",
                note = "İç fonksiyonun türevini çarpmayı atladım.",
                errorKind = ErrorKind.BILGI,
                createdOn = today.minusDays(2),
                dueOn = today,
                mastery = 1,
            ),
            Mistake(
                id = "m2",
                subject = Subject.FIZIK,
                topicId = "newton",
                topicTitle = "Newton · Sürtünme yönü",
                questionRef = "Deneme 6 · 8. soru",
                note = "Sürtünmeyi hareketle aynı yönde aldım.",
                errorKind = ErrorKind.DIKKATSIZLIK,
                createdOn = today.minusDays(1),
                dueOn = today,
                mastery = 2,
            ),
            Mistake(
                id = "m3",
                subject = Subject.MATEMATIK,
                topicId = "limit",
                topicTitle = "Limit · 0/0 belirsizliği",
                questionRef = "Deneme 5 · 3. soru",
                note = "Sadeleştirmeyi görmeden türev kuralına geçtim.",
                errorKind = ErrorKind.BILGI,
                createdOn = today.minusDays(3),
                dueOn = today.plusDays(1),
                mastery = 0,
            ),
            Mistake(
                id = "m4",
                subject = Subject.TURKCE,
                topicId = "paragraf",
                topicTitle = "Paragraf · Ana fikir",
                questionRef = "Deneme 4 · 22. soru",
                note = "Yardımcı fikri ana fikir sandım.",
                errorKind = ErrorKind.SORU_KOKU,
                createdOn = today.minusDays(4),
                dueOn = today.plusDays(3),
                mastery = 3,
            ),
            Mistake(
                id = "m5",
                subject = Subject.KIMYA,
                topicId = "mol",
                topicTitle = "Mol · Kütle hesabı",
                questionRef = "Deneme 3 · 11. soru",
                note = "Mol kütlesi yerine atom kütlesi kullandım.",
                errorKind = ErrorKind.ISLEM,
                createdOn = today.minusDays(5),
                dueOn = today.plusDays(5),
                mastery = 2,
            ),
            Mistake(
                id = "m6",
                subject = Subject.MATEMATIK,
                topicId = "turev",
                topicTitle = "Türev · Çarpım kuralı",
                questionRef = "Deneme 2 · 19. soru",
                note = "Çarpım kuralında ikinci terimi unuttum.",
                errorKind = ErrorKind.BILGI,
                createdOn = today.minusDays(8),
                dueOn = today.plusDays(7),
                mastery = 4,
            ),
            Mistake(
                id = "m7",
                subject = Subject.FIZIK,
                topicId = "elektrik",
                topicTitle = "Elektrik · Eşdeğer direnç",
                questionRef = "Deneme 1 · 12. soru",
                note = "Seri ve paralel formüllerini karıştırdım.",
                errorKind = ErrorKind.BILGI,
                createdOn = today.minusDays(2),
                dueOn = today,
                mastery = 1,
            ),
        )

        val tactics = listOf(
            Tactic(
                id = "hata-gunlugu",
                title = "Hata günlüğü",
                summary = "Her yanlışı üç satırda kaydet: ne oldu, neden oldu, ne yapacaksın.",
                steps = listOf(
                    "Yanlışı çözdükten hemen sonra kaydet; üzerinden gün geçmesin.",
                    "Hatayı türüne ayır: bilgi, dikkat, soru kökü, işlem, zaman.",
                    "Aynı konudan üç benzer soruyu 48 saat içinde çöz.",
                ),
                evidence = EvidenceLevel.RESEARCH,
                minutes = 10,
                topicIds = listOf("turev", "limit", "newton"),
            ),
            Tactic(
                id = "aktif-hatirlama",
                title = "Aktif hatırlama",
                summary = "Notu tekrar okumak yerine kapatıp anlat; boşlukları orada yakala.",
                steps = listOf(
                    "Konu kartını oku, sonra kapat.",
                    "Boş kâğıda bildiğin her şeyi yaz.",
                    "Eksikleri kaynağa dönerek tamamla, tekrar kapat ve anlat.",
                ),
                evidence = EvidenceLevel.RESEARCH,
                minutes = 20,
                topicIds = listOf("turev", "mol", "paragraf"),
            ),
            Tactic(
                id = "aralikli-tekrar",
                title = "Aralıklı tekrar",
                summary = "Aynı konuyu artan aralıklarla geri getir: 1, 3, 7, 14, 30 gün.",
                steps = listOf(
                    "Bugün öğrendiğin konuyu yarın 5 dakikada yokla.",
                    "3. gün üç soru, 7. gün bir deneme sorusu çöz.",
                    "Takıldığın adımı sıfırla, kalanları ilerlet.",
                ),
                evidence = EvidenceLevel.RESEARCH,
                minutes = 15,
                topicIds = listOf("limit", "elektrik"),
            ),
            Tactic(
                id = "blurting",
                title = "Blurting",
                summary = "Konuyu kapatıp hafızadan dök, sonra kaynakla karşılaştır.",
                steps = listOf(
                    "Beş dakika boyunca durmadan yaz.",
                    "Kaynağı aç ve eksikleri işaretle.",
                    "Aynı sayfayı 24 saat sonra yeniden doldur.",
                ),
                evidence = EvidenceLevel.ANECDOTE,
                minutes = 25,
                topicIds = listOf("newton", "paragraf"),
            ),
            Tactic(
                id = "odak-bloku",
                title = "25/5 odak bloğu",
                summary = "Tek konu, tek ekran, 25 dakika; sonra 5 dakika gerçek mola.",
                steps = listOf(
                    "Bloğun tek hedefini bir cümleyle yaz.",
                    "Telefonu başka odaya bırak, zamanlayıcıyı başlat.",
                    "Molada ekrana değil, pencereye bak.",
                ),
                evidence = EvidenceLevel.DEBATED,
                minutes = 25,
                topicIds = listOf("turev", "elektrik"),
            ),
        )

        return StudyData(
            learnerName = "Deniz",
            streakDays = 12,
            nets = listOf(
                ExamNet(today.minusDays(18), 28.5),
                ExamNet(today.minusDays(15), 31.0),
                ExamNet(today.minusDays(12), 30.2),
                ExamNet(today.minusDays(9), 34.5),
                ExamNet(today.minusDays(6), 36.0),
                ExamNet(today.minusDays(3), 35.2),
                ExamNet(today, 38.4),
            ),
            mistakes = mistakes,
            topics = topics,
            tactics = tactics,
            featuredTacticId = "hata-gunlugu",
        )
    }
}
