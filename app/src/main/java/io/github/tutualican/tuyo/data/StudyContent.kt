package io.github.tutualican.tuyo.data

import io.github.tutualican.tuyo.domain.currentStreak
import io.github.tutualican.tuyo.domain.model.Confusion
import io.github.tutualican.tuyo.domain.model.ErrorKind
import io.github.tutualican.tuyo.domain.model.EvidenceLevel
import io.github.tutualican.tuyo.domain.model.ExamComment
import io.github.tutualican.tuyo.domain.model.ExamOverview
import io.github.tutualican.tuyo.domain.model.ExamSection
import io.github.tutualican.tuyo.domain.model.ExamStat
import io.github.tutualican.tuyo.domain.model.MotivationNote
import io.github.tutualican.tuyo.domain.model.MotivationTheme
import io.github.tutualican.tuyo.domain.model.Source
import io.github.tutualican.tuyo.domain.model.StudyData
import io.github.tutualican.tuyo.domain.model.Subject
import io.github.tutualican.tuyo.domain.model.Tactic
import io.github.tutualican.tuyo.domain.model.TacticCategory
import io.github.tutualican.tuyo.domain.model.Topic
import io.github.tutualican.tuyo.domain.model.UserData
import java.time.LocalDate

/**
 * Static reference content. It contains no learner name, net, mistake, streak or
 * mastery value: every personal number in the app is derived from [UserData].
 *
 * The study techniques are annotated with an evidence level. "Araştırma" is
 * reserved for techniques with direct experimental support; exam-specific
 * tactics are marked as experience-based because their value depends on the
 * student and the paper.
 */
object StudyContent {

    val topics: List<Topic> = listOf(
        Topic(
            id = "paragraf",
            subject = Subject.TURKCE,
            title = "Paragraf",
            summary = "Ana fikir, yardımcı fikir, yazarın amacı ve anlatım biçimi.",
            keyIdea = "Ana fikir, paragrafın tamamını kapsayan tek yargıdır. " +
                "Bir örnek, ayrıntı veya karşılaştırma ana fikir olamaz; " +
                "önce soru kökünü, sonra paragrafın bütününü oku.",
            confusions = listOf(
                Confusion(
                    question = "Ana fikir ile konu arasındaki fark ne?",
                    answer = "Konu 'ne anlatılıyor?', ana fikir 'ne söyleniyor?' sorusunun cevabıdır. " +
                        "Ana fikir yargı bildirir; konu tek başına yargı bildirmez.",
                ),
                Confusion(
                    question = "Paragrafın ilk cümlesi her zaman ana fikir mi?",
                    answer = "Hayır. Ana fikir bazen sonda, bazen paragrafın genelinden çıkarılır. " +
                        "İlk cümle bir giriş veya örnek olabilir.",
                ),
            ),
        ),
        Topic(
            id = "dil-bilgisi",
            subject = Subject.TURKCE,
            title = "Dil bilgisi",
            summary = "Cümlenin ögeleri, fiil çatısı, anlatım bozuklukları.",
            keyIdea = "Ögeleri bulurken önce yüklemi, sonra yüklemin çektiği ögeleri ara. " +
                "Soruya 'kim, ne, nereye, nasıl?' diye sormak, ezberden daha güvenlidir.",
            confusions = listOf(
                Confusion(
                    question = "Nesne ile dolaylı tümleç nasıl ayrılır?",
                    answer = "Nesne 'neyi/kimi?' sorusuna, dolaylı tümleç 'nereye/neden/kime?' sorusuna cevap verir. " +
                        "Yükleme sorulan soru, ek alıp almadığından daha belirleyicidir.",
                ),
            ),
        ),
        Topic(
            id = "turev",
            subject = Subject.MATEMATIK,
            title = "Türev",
            summary = "Değişim oranı, teğet eğimi, zincir ve çarpım kuralı.",
            keyIdea = "Zincir kuralı: dış fonksiyonun türevi × iç fonksiyonun türevi. " +
                "İç türevi atlamak, AYT'de en pahalı tek hatadır.",
            confusions = listOf(
                Confusion(
                    question = "İç fonksiyonun türevini neden çarpıyoruz?",
                    answer = "Çünkü x değişirken iç fonksiyon da değişir. " +
                        "Zincir kuralı iki değişimin oranını üst üste bindirir: dy/dx = dy/du · du/dx.",
                ),
                Confusion(
                    question = "f(g(x)) ile f(x)·g(x) aynı şey mi?",
                    answer = "Hayır. İlki bileşke, ikincisi çarpımdır. " +
                        "Türevde ilki zincir, ikincisi çarpım kuralı ister.",
                ),
            ),
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
                    answer = "Süreklilik, limitin var ve fonksiyon değerine eşit olmasını gerektirir. " +
                        "Limit varken fonksiyon o noktada tanımsız olabilir.",
                ),
            ),
        ),
        Topic(
            id = "problemler",
            subject = Subject.MATEMATIK,
            title = "Problemler",
            summary = "Denklem kurma, oran-orantı, hız, işçi ve karışım.",
            keyIdea = "Problemi çözmeden önce bilinmeyeni tek harfle yaz ve birimleri aynı dile getir. " +
                "Denklemi kurduktan sonra 'bu cevap mantıklı mı?' diye kontrol et.",
            confusions = listOf(
                Confusion(
                    question = "Hız problemlerinde neden birimleri değiştiriyoruz?",
                    answer = "Saat/dakika veya km/m aynı denklemde karışırsa sonuç yanlış çıkar. " +
                        "Önce tüm süreleri aynı birime çevir.",
                ),
            ),
        ),
        Topic(
            id = "ucgenler",
            subject = Subject.GEOMETRI,
            title = "Üçgenler",
            summary = "Açı, kenar, benzerlik, alan ve çevrel-İç teğet çember.",
            keyIdea = "Şekli sen çiz, verilenleri şekle taşı ve yardımcı çizgiyi ancak bir teoremi " +
                "hatırlattığında ekle. Her çizgi bir amaca hizmet etmeli.",
            confusions = listOf(
                Confusion(
                    question = "Benzerlik oranı alanı neden kareyle etkiler?",
                    answer = "Benzerlikte uzunluklar k katına çıkarsa alan k² katına çıkar. " +
                        "Alan oranı ile kenar oranını karıştırma.",
                ),
            ),
        ),
        Topic(
            id = "newton",
            subject = Subject.FIZIK,
            title = "Newton hareket yasaları",
            summary = "Kuvvet, kütle, ivme ve serbest cisim diyagramı.",
            keyIdea = "Önce serbest cisim diyagramı. Her kuvveti ayrı çiz, sonra bileşenlere ayır. " +
                "Sürtünme, hareket yönüne zıt yönde etki eder.",
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
        ),
        Topic(
            id = "elektrik",
            subject = Subject.FIZIK,
            title = "Elektrik devreleri",
            summary = "Akım, gerilim, direnç, seri-paralel ve eşdeğer direnç.",
            keyIdea = "Seride akım aynı, gerilim paylaşılır. Paralelde gerilim aynı, " +
                "akım paylaşılır. Eşdeğer direnç paralelde küçülür.",
            confusions = listOf(
                Confusion(
                    question = "Paralel dirençte eşdeğer direnç neden küçülür?",
                    answer = "Akıma daha fazla yol açılır. Toplam iletkenlik artar, direnç azalır.",
                ),
            ),
        ),
        Topic(
            id = "mol",
            subject = Subject.KIMYA,
            title = "Mol",
            summary = "Tanecik sayısı, kütle, hacim ve mol kütlesi arasındaki köprü.",
            keyIdea = "1 mol = 6,02·10²³ tanecik. Mol kütlesi g/mol'dür; " +
                "atom kütlesi ile mol kütlesini karıştırma.",
            confusions = listOf(
                Confusion(
                    question = "Mol kütlesi neden periyodik tablodaki sayıya eşit?",
                    answer = "1 mol atomun gram cinsinden kütlesi, atomik kütle birimindeki sayıya " +
                        "sayısal olarak eşittir.",
                ),
            ),
        ),
        Topic(
            id = "denge",
            subject = Subject.KIMYA,
            title = "Kimyasal denge",
            summary = "Denge sabiti, Le Chatelier ve derişim değişimleri.",
            keyIdea = "Denge, tepkimenin durduğu değil, ileri ve geri hızların eşitlendiği andır. " +
                "Sıcaklık denge sabitini değiştirir; derişim ve basınç yalnızca dengeyi kaydırır.",
            confusions = listOf(
                Confusion(
                    question = "Katalizör dengeyi kaydırır mı?",
                    answer = "Hayır. Katalizör ileri ve geri tepkimeyi aynı oranda hızlandırır; " +
                        "dengeye ulaşmayı hızlandırır, denge konumunu değiştirmez.",
                ),
            ),
        ),
        Topic(
            id = "genetik",
            subject = Subject.BIYOLOJI,
            title = "Genetik",
            summary = "Kalıtım, çaprazlama, eş baskınlık ve soy ağaçları.",
            keyIdea = "Önce genotipleri harflerle yaz, sonra olası gametleri çaprazla. " +
                "Fenotip oranını soru istiyorsa genotip oranından ayrı hesapla.",
            confusions = listOf(
                Confusion(
                    question = "Eş baskınlık ile eksik baskınlık aynı mı?",
                    answer = "Hayır. Eş baskınlıkta iki özellik aynı anda görünür; " +
                        "eksik baskınlıkta ara bir fenotip oluşur.",
                ),
            ),
        ),
        Topic(
            id = "osmanli",
            subject = Subject.TARIH,
            title = "Osmanlı tarihi",
            summary = "Kuruluş, yükselme, duraklama ve modernleşme dönemleri.",
            keyIdea = "Tarih sorularında olayı değil, olayın neden-sonuç ilişkisini kur. " +
                "Dönem özelliklerini birbirine karıştırmamak için kronoloji şeridi çıkar.",
            confusions = listOf(
                Confusion(
                    question = "Duraklama ile gerileme aynı dönem mi?",
                    answer = "Hayır. Duraklama, ilerlemenin yavaşladığı; gerileme, toprak kayıplarının " +
                        "ve iç sorunların arttığı dönemdir.",
                ),
            ),
        ),
        Topic(
            id = "iklim",
            subject = Subject.COGRAFYA,
            title = "İklim ve hava olayları",
            summary = "Sıcaklık, basınç, rüzgâr, yağış ve iklim tipleri.",
            keyIdea = "Haritayı okurken enlem, yükselti, denizellik ve karasallığı birlikte düşün. " +
                "Tek bir faktörle açıklanan sonuç genellikle eksiktir.",
            confusions = listOf(
                Confusion(
                    question = "Aynı enlemde sıcaklık neden farklı olabilir?",
                    answer = "Yükselti, denizden uzaklık, okyanus akıntıları ve yer şekilleri " +
                        "enlemin etkisini değiştirebilir.",
                ),
            ),
        ),
        Topic(
            id = "siir",
            subject = Subject.EDEBIYAT,
            title = "Şiir bilgisi",
            summary = "Ölçü, uyak, sanatlar, dönem ve akım özellikleri.",
            keyIdea = "Şiiri yalnızca sanatlar üzerinden değil, dönemin dünya görüşü üzerinden oku. " +
                "Bir dizede kullanılan sanat, şairin ait olduğu akımı tek başına kanıtlamaz.",
            confusions = listOf(
                Confusion(
                    question = "Serbest şiirde ölçü yok mu?",
                    answer = "Klasik anlamda düzenli ölçü yoktur; ama ritim, duraklama ve ses tekrarları " +
                        "şiirin yapısını kurar.",
                ),
            ),
        ),
    )

    val tactics: List<Tactic> = listOf(
        Tactic(
            id = "hata-gunlugu",
            title = "Hata günlüğü",
            summary = "Her yanlışı üç satırda kaydet: ne oldu, neden oldu, ne yapacaksın.",
            steps = listOf(
                "Yanlışı çözdükten hemen sonra kaydet; üzerinden gün geçmesin.",
                "Hatayı türüne ayır: bilgi, dikkat, soru kökü, işlem, zaman.",
                "Aynı konudan üç benzer soruyu 48 saat içinde çöz.",
                "Tekrar sonrası notu güncelle: hata gerçekten kapandı mı?",
            ),
            evidence = EvidenceLevel.ANECDOTE,
            minutes = 10,
            category = TacticCategory.CALISMA,
            topicIds = listOf("turev", "limit", "newton", "paragraf"),
            sourceNote = "Geri bildirim ve hata analizi pratiği; doğrudan etki büyüklüğü kişiye göre değişir.",
        ),
        Tactic(
            id = "aktif-hatirlama",
            title = "Aktif hatırlama",
            summary = "Notu tekrar okumak yerine kapatıp anlat; boşlukları orada yakala.",
            steps = listOf(
                "Konu kartını oku, sonra kapat.",
                "Boş kâğıda bildiğin her şeyi yaz.",
                "Eksikleri kaynağa dönerek tamamla, tekrar kapat ve anlat.",
                "Aynı sayfayı 24 saat sonra yeniden doldur.",
            ),
            evidence = EvidenceLevel.RESEARCH,
            minutes = 20,
            category = TacticCategory.CALISMA,
            topicIds = listOf("turev", "mol", "paragraf", "genetik"),
            sourceNote = "Roediger & Karpicke (2006); Dunlosky vd. (2013) retrieval practice.",
        ),
        Tactic(
            id = "aralikli-tekrar",
            title = "Aralıklı tekrar",
            summary = "Aynı konuyu artan aralıklarla geri getir: 1, 3, 7, 14, 30 gün.",
            steps = listOf(
                "Bugün öğrendiğin konuyu yarın 5 dakikada yokla.",
                "3. gün üç soru, 7. gün bir deneme sorusu çöz.",
                "Takıldığın adımı sıfırla, kalanları ilerlet.",
                "Tekrarı takvime değil, unutmaya başladığın ana göre planla.",
            ),
            evidence = EvidenceLevel.RESEARCH,
            minutes = 15,
            category = TacticCategory.CALISMA,
            topicIds = listOf("limit", "elektrik", "denge"),
            sourceNote = "Dunlosky vd. (2013) distributed practice; aralık etkisi.",
        ),
        Tactic(
            id = "deneme-analizi",
            title = "Denemeyi üç geçişte analiz et",
            summary = "Yanlış, boş ve şüpheli doğruları ayır; her biri için farklı karar ver.",
            steps = listOf(
                "Yanlışları çözümden bağımsız olarak yeniden çöz.",
                "Boşları neden boş bıraktığını yaz: bilgi mi, süre mi, korku mu?",
                "Şüpheli doğruları da incele; doğru cevap yanlış gerekçeyle gelmiş olabilir.",
                "Aynı konudan üç soruyu 48 saat içinde tekrar çöz.",
            ),
            evidence = EvidenceLevel.RESEARCH,
            minutes = 40,
            category = TacticCategory.SINAV_STRATEJISI,
            sourceNote = "Practice testing ve geri bildirim araştırmaları; deneme analizi bu ilkeleri uygular.",
        ),
        Tactic(
            id = "tyt-turkce",
            title = "TYT Türkçe: önce soru kökü",
            summary = "Paragrafı okumadan önce ne istendiğini bil; ayrıntıya boğulma.",
            steps = listOf(
                "Soru kökünü oku ve altını çiz: ana fikir, çıkarım, anlatım biçimi?",
                "Paragrafta geçiş ifadelerini işaretle: ama, ancak, bu yüzden.",
                "Bütün seçenekleri değil, soru köküne uymayanları hızla ele.",
                "Takıldığın soruyu işaretle ve dönüşte tekrar oku; ilk okumada ısrar etme.",
            ),
            evidence = EvidenceLevel.ANECDOTE,
            minutes = 25,
            category = TacticCategory.TYT,
            topicIds = listOf("paragraf", "dil-bilgisi"),
            sourceNote = "Sınav formatı ve aday deneyimlerine dayalı strateji; her aday için aynı değildir.",
        ),
        Tactic(
            id = "tyt-matematik",
            title = "TYT matematikte iki tur",
            summary = "İlk turda tanıdığını al, ikinci turda zorlayıcı sorulara dön.",
            steps = listOf(
                "İlk 60 saniyede çözüm yolu görünmüyorsa işaretle ve geç.",
                "İlk turda kolay netleri topla; moral değil, süre kazan.",
                "İkinci turda işaretli sorulara dön ve 90 saniye sınırı koy.",
                "İşlemleri tek satırda yaz; kafadan işlem yapma.",
            ),
            evidence = EvidenceLevel.ANECDOTE,
            minutes = 30,
            category = TacticCategory.TYT,
            topicIds = listOf("problemler", "turev", "limit"),
            sourceNote = "Zaman yönetimi stratejisi; net etkisi deneme pratiğiyle ölçülmelidir.",
        ),
        Tactic(
            id = "tyt-fen",
            title = "TYT fen: kavram mı, işlem mi?",
            summary = "Soruyu okurken önce hangi kavramı sorduğunu belirle; formülü sonra seç.",
            steps = listOf(
                "Verilenleri ve isteneni iki ayrı sütuna yaz.",
                "Olayın hangi yasaya ait olduğunu tek cümleyle söyle.",
                "Birimleri dönüştür, sonra işlemi kur.",
                "Sonucun büyüklüğünü tahmin et; mantıksızsa işlemi kontrol et.",
            ),
            evidence = EvidenceLevel.ANECDOTE,
            minutes = 20,
            category = TacticCategory.TYT,
            topicIds = listOf("newton", "elektrik", "mol", "genetik"),
            sourceNote = "Fen sorularında kavram seçimi ve birim kontrolü için sınav stratejisi.",
        ),
        Tactic(
            id = "ayt-matematik",
            title = "AYT matematik: denklemi kur, işlemi yavaşlat",
            summary = "Zor soru çoğu zaman uzun işlem değil, yanlış kurulmuş denklemdir.",
            steps = listOf(
                "Soruyu okuduktan sonra bilinmeyeni ve kısıtları yaz.",
                "Denklemi kurmadan işleme başlama.",
                "Her satırda bir işlem yap; atladığın satırı geri dönüşte bulamazsın.",
                "Cevabı yerine koyarak sağlamasını yap.",
            ),
            evidence = EvidenceLevel.ANECDOTE,
            minutes = 35,
            category = TacticCategory.AYT,
            topicIds = listOf("turev", "limit", "problemler"),
            sourceNote = "2025 AYT aday yorumlarında matematik için zaman ve işlem yükü öne çıktı.",
        ),
        Tactic(
            id = "ayt-edebiyat",
            title = "AYT edebiyat: dönem-akım-eser üçlüsü",
            summary = "Ezber listesi yerine dönemlerin dünya görüşünü kur.",
            steps = listOf(
                "Her dönem için üç anahtar kelime ve iki temsilci eser yaz.",
                "Eserin türünü, dönemini ve akımını aynı satırda eşleştir.",
                "Yanlış yaptığın eşleştirmeyi deftere ekle, sadece doğruyu tekrar etme.",
                "Haftada bir 20 soruluk edebiyat denemesi çöz ve hataları türlere ayır.",
            ),
            evidence = EvidenceLevel.ANECDOTE,
            minutes = 30,
            category = TacticCategory.AYT,
            topicIds = listOf("siir"),
            sourceNote = "Edebiyat bilgisi, bağlam kurma ve aktif hatırlama üzerine çalışma stratejisi.",
        ),
        Tactic(
            id = "ayt-fen",
            title = "AYT fen: çeldiriciyi değil, yasayı oku",
            summary = "Uzun soruda önce olayı, sonra verilenleri, en son seçenekleri oku.",
            steps = listOf(
                "Sorunun son cümlesini oku: ne isteniyor?",
                "Şekil veya grafik varsa eksenleri ve birimleri işaretle.",
                "Bilinen yasayı yaz; seçeneklere bakarak yasa seçme.",
                "İki seçenek arasında kaldıysan, hangi varsayımı ihlal ettiğini kontrol et.",
            ),
            evidence = EvidenceLevel.ANECDOTE,
            minutes = 25,
            category = TacticCategory.AYT,
            topicIds = listOf("newton", "elektrik", "denge", "genetik"),
            sourceNote = "Fen sorularında kavram ve çeldirici yönetimi için deneme pratiği.",
        ),
        Tactic(
            id = "sinav-gunu",
            title = "Sınav günü için küçük ritüel",
            summary = "Yeni bir şey öğrenme; bildiğini sakin ve düzenli kullan.",
            steps = listOf(
                "Bir gün önce çanta, kimlik, su ve saat gibi malzemeleri hazırla.",
                "Sabah ağır olmayan bir kahvaltı yap; yeni yiyecek deneme.",
                "Salonda oturunca üç kez yavaş nefes al, omuzlarını gevşet.",
                "Optik forma her test sonunda kodlama yap; sona bırakma.",
            ),
            evidence = EvidenceLevel.ANECDOTE,
            minutes = 10,
            category = TacticCategory.SINAV_GUNU,
            sourceNote = "MEB sınav kaygısı önerileri ve sınav günü rutini deneyimi.",
        ),
        Tactic(
            id = "kaygi-ani",
            title = "Kaygı anında ana dön",
            summary = "Panik yükselirken beyni gelecekten bugüne getiren kısa bir çapa kur.",
            steps = listOf(
                "Dört saniye al, altı saniye ver; üç tur nefes al.",
                "Çevrendeki beş nesneyi, dört sesi ve üç dokuyu fark et.",
                "Şu an yapabileceğin tek soruyu seç ve sadece onu oku.",
                "Kaygı sürerse sınav görevlisinden su veya kısa bir mola iste; bu bir zayıflık değildir.",
            ),
            evidence = EvidenceLevel.ANECDOTE,
            minutes = 3,
            category = TacticCategory.IYI_HAL,
            sourceNote = "5-4-3-2-1 duyusal çapa ve nefes egzersizi; klinik tedavi yerine geçmez.",
        ),
        Tactic(
            id = "uyku",
            title = "Uykuyu çalışma programına al",
            summary = "Son gece çalışması yerine düzenli uyku, öğrenmenin pekişmesini sağlar.",
            steps = listOf(
                "Her gün aynı saatte yatıp kalkmaya çalış.",
                "Yatmadan 45 dakika önce ekranı ve yoğun soru çözmeyi bırak.",
                "Son tekrarı yatmadan hemen önce değil, akşam erken saatte yap.",
                "Sınav saatine yakın bir uyanma düzeni kur; son hafta ani değişiklik yapma.",
            ),
            evidence = EvidenceLevel.RESEARCH,
            minutes = 8,
            category = TacticCategory.CALISMA,
            sourceNote = "Diekelmann & Born (2010) uyku ve bellek pekiştirme.",
        ),
        Tactic(
            id = "haftalik-plan",
            title = "Haftalık planı üç soruyla kur",
            summary = "Her hafta yeni bir 40 maddelik listeye değil, üç karara ihtiyacın var.",
            steps = listOf(
                "Bu hafta en zayıf olduğum iki konu ne?",
                "Hangi günler deneme, hangi günler tekrar için ayrılacak?",
                "Kaç saat değil, kaç soru ve kaç tekrar hedefim var?",
                "Cuma günü planı 10 dakikada gözden geçir; yapılmayanı cezalandırma, nedenini yaz.",
            ),
            evidence = EvidenceLevel.ANECDOTE,
            minutes = 15,
            category = TacticCategory.CALISMA,
            sourceNote = "Öz düzenleme ve planlama pratiği; katı program yerine esnek hedef.",
        ),
        Tactic(
            id = "sosyal-medya",
            title = "Dikkatini koruyan telefon ayarı",
            summary = "İradeye güvenmek yerine ortamı, telefonu senden uzaklaştıracak şekilde kur.",
            steps = listOf(
                "Çalışırken telefonu başka odada veya çekmecede tut.",
                "Bildirimleri kapat; sadece arama ve takvim açık kalsın.",
                "Mola süresini zamanlayıcıyla sınırla; sosyal medya molası 5 dakikada kalsın.",
                "Haftada bir, hangi uygulamanın en çok zaman aldığını kontrol et.",
            ),
            evidence = EvidenceLevel.DEBATED,
            minutes = 5,
            category = TacticCategory.CALISMA,
            sourceNote = "Ortam düzenlemesi ve dikkat araştırmaları; etkisi kişiye göre değişir.",
        ),
        Tactic(
            id = "yardim-iste",
            title = "Takıldığında yardım istemeyi planla",
            summary = "Bir konuda 20 dakikadır aynı yerdeysen, bu artık çalışma değil; strateji değiştir.",
            steps = listOf(
                "Soruyu çözemiyorsan 20 dakika sınırı koy.",
                "Nerede takıldığını tek cümleyle yaz: kavram, işlem, soru kökü?",
                "Öğretmenine veya arkadaşına bu cümleyi götür; 'bu soru nasıl çözülür?' demek yeterli.",
                "Çözümü aldıktan sonra soruyu kapatıp kendin yeniden çöz.",
            ),
            evidence = EvidenceLevel.ANECDOTE,
            minutes = 20,
            category = TacticCategory.IYI_HAL,
            sourceNote = "Yardım arama ve geri bildirim alma davranışı; sınav kaygısını da azaltabilir.",
        ),
        Tactic(
            id = "odak-bloku",
            title = "25/5 odak bloğu",
            summary = "Tek konu, tek ekran, 25 dakika; sonra 5 dakika gerçek mola.",
            steps = listOf(
                "Bloğun tek hedefini bir cümleyle yaz.",
                "Telefonu başka odaya bırak, zamanlayıcıyı başlat.",
                "Molada ekrana değil, pencereye bak; su iç ve kısa yürü.",
                "Dört bloktan sonra 20-30 dakika daha uzun bir mola ver.",
            ),
            evidence = EvidenceLevel.DEBATED,
            minutes = 25,
            category = TacticCategory.CALISMA,
            topicIds = listOf("turev", "elektrik", "problemler"),
            sourceNote = "Pomodoro popüler bir çerçeve; 25/5 sürelerinin evrensel üstünlüğü kanıtlanmış değildir.",
        ),
        Tactic(
            id = "blurting",
            title = "Blurting",
            summary = "Konuyu kapatıp hafızadan dök, sonra kaynakla karşılaştır.",
            steps = listOf(
                "Beş dakika boyunca durmadan yaz.",
                "Kaynağı aç ve eksikleri işaretle.",
                "Aynı sayfayı 24 saat sonra yeniden doldur.",
                "Eksik kalan başlığı aktif hatırlama kartına dönüştür.",
            ),
            evidence = EvidenceLevel.ANECDOTE,
            minutes = 25,
            category = TacticCategory.CALISMA,
            topicIds = listOf("newton", "paragraf", "osmanli"),
            sourceNote = "Aktif hatırlamanın serbest yazma biçimi; doğrudan 'blurting' literatürü sınırlıdır.",
        ),
    )

    val motivation: List<MotivationNote> = listOf(
        MotivationNote(
            "m01",
            "Bugün her şeyi bitirmek zorunda değilsin. Sadece sıradaki yanlışı aç.",
            MotivationTheme.BASLANGIC,
        ),
        MotivationNote(
            "m02",
            "Netler bir günde değil, tekrar tekrar döndüğün yerlerde yükselir.",
            MotivationTheme.DEVAM,
        ),
        MotivationNote(
            "m03",
            "Kötü bir deneme, kötü bir öğrenci demek değildir. Sadece bir sonraki hamlenin verisidir.",
            MotivationTheme.TOPARLANMA,
        ),
        MotivationNote(
            "m04",
            "Kaygı, hazırlanmadığının kanıtı değil; önemsediğinin işareti.",
            MotivationTheme.SINAV_KAYGISI,
        ),
        MotivationNote(
            "m05",
            "Bugün 20 dakika, yarın 0 dakikadan iyidir.",
            MotivationTheme.BASLANGIC,
        ),
        MotivationNote(
            "m06",
            "Hızını başkasının takvimiyle ölçme. Kendi verin, kendi yolun.",
            MotivationTheme.OZ_SEFKAT,
        ),
        MotivationNote(
            "m07",
            "Bir konuyu anlamadığında 'aptalım' değil, 'bu anlatım bana uymadı' de.",
            MotivationTheme.OZ_SEFKAT,
        ),
        MotivationNote(
            "m08",
            "Deneme, aynayı kırmadan yüzüne bakmaktır.",
            MotivationTheme.DEVAM,
        ),
        MotivationNote(
            "m09",
            "Her yanlış, doğruyu nerede arayacağını söyler.",
            MotivationTheme.TOPARLANMA,
        ),
        MotivationNote(
            "m10",
            "Bugünün işi bugünün; yarının kaygısını yarın taşı.",
            MotivationTheme.SINAV_KAYGISI,
        ),
        MotivationNote(
            "m11",
            "Mola, çalışmanın rakibi değil; parçası.",
            MotivationTheme.DEVAM,
        ),
        MotivationNote(
            "m12",
            "Uyku, son gece çalışmasından daha çok net getirebilir.",
            MotivationTheme.DEVAM,
        ),
        MotivationNote(
            "m13",
            "Bir gün kaçırdıysan seri bitti demek değil; yarın yeniden başlar.",
            MotivationTheme.TOPARLANMA,
        ),
        MotivationNote(
            "m14",
            "Kendine, arkadaşına konuştuğun gibi konuş.",
            MotivationTheme.OZ_SEFKAT,
        ),
        MotivationNote(
            "m15",
            "Sınav senin değerini ölçmez; sadece bugünkü bilgi düzeyini gösterir.",
            MotivationTheme.SINAV_KAYGISI,
        ),
        MotivationNote(
            "m16",
            "Büyük hedefi küçük bir bugüne böl.",
            MotivationTheme.BASLANGIC,
        ),
        MotivationNote(
            "m17",
            "Zorlandığın yer, öğrenmenin tam olarak başladığı yerdir.",
            MotivationTheme.DEVAM,
        ),
        MotivationNote(
            "m18",
            "Herkesin yolu farklı; sen kendi verinle yarış.",
            MotivationTheme.OZ_SEFKAT,
        ),
        MotivationNote(
            "m19",
            "Nefes al. Soruya dön. Bir sonraki adım yeter.",
            MotivationTheme.SINAV_KAYGISI,
        ),
        MotivationNote(
            "m20",
            "Bugün kendine bir iyilik yap: en zayıf konudan beş soru çöz.",
            MotivationTheme.TOPARLANMA,
        ),
    )

    val examOverviews: List<ExamOverview> = listOf(
        ExamOverview(
            id = "tyt",
            title = "TYT",
            meta = "120 soru · 165 dakika",
            summary = "Temel Yeterlilik Testi, tüm adayların girdiği ilk oturum. " +
                "Türkçe ve matematik ağırlığı yüksek; süre yönetimi belirleyici.",
            sections = listOf(
                ExamSection("Türkçe", "40", "Paragraf, dil bilgisi, anlam bilgisi"),
                ExamSection("Sosyal Bilimler", "20", "Tarih 5 · Coğrafya 5 · Felsefe 5 · Din/ek felsefe 5"),
                ExamSection("Temel Matematik", "40", "Temel kavramlar, problemler, geometri"),
                ExamSection("Fen Bilimleri", "20", "Fizik 7 · Kimya 7 · Biyoloji 6"),
            ),
            note = "TYT puanı tek başına bazı önlisans programları için kullanılır; " +
                "lisans yerleştirmesinde AYT/YDT ile birlikte değerlendirilir.",
        ),
        ExamOverview(
            id = "ayt",
            title = "AYT",
            meta = "160 soru · 180 dakika",
            summary = "Alan Yeterlilik Testi, seçtiğin alana göre daha derin bilgi ölçer. " +
                "Dört testin tamamı kitapçıkta bulunur; sen alanına uygun olanları çözersin.",
            sections = listOf(
                ExamSection("Matematik", "40", "Matematik ve geometri"),
                ExamSection(
                    "Fen Bilimleri",
                    "40",
                    "Fizik 14 · Kimya 13 · Biyoloji 13",
                ),
                ExamSection(
                    "Türk Dili ve Edebiyatı–Sosyal 1",
                    "40",
                    "Edebiyat 24 · Tarih 10 · Coğrafya 6",
                ),
                ExamSection(
                    "Sosyal Bilimler 2",
                    "40",
                    "Tarih 11 · Coğrafya 11 · Felsefe 12 · Din/ek felsefe 6",
                ),
            ),
            note = "Sayısal, eşit ağırlık, sözel ve dil puan türlerinde farklı testlerin ağırlığı vardır; " +
                "kesin ağırlıklar için ÖSYM kılavuzunu esas al.",
        ),
        ExamOverview(
            id = "ydt",
            title = "YDT",
            meta = "80 soru · 120 dakika",
            summary = "Yabancı Dil Testi, dil puanıyla yerleşmek isteyen adaylar için. " +
                "Kelime, dil bilgisi, paragraf ve çeviri becerilerini birlikte ölçer.",
            sections = listOf(
                ExamSection("Yabancı dil", "80", "Kelime, dil bilgisi, okuma, çeviri, diyalog"),
            ),
            note = "YDT'de İngilizce, Almanca, Fransızca, Arapça ve Rusça gibi dillerden sınav yapılır.",
        ),
    )

    val examStats: List<ExamStat> = listOf(
        ExamStat("TYT Türkçe", "20,1", "40"),
        ExamStat("TYT Sosyal", "10,0", "20"),
        ExamStat("TYT Temel Matematik", "6,4", "40"),
        ExamStat("TYT Fen", "3,9", "20"),
        ExamStat("AYT Matematik", "7,1", "39*"),
        ExamStat("AYT Fizik", "2,6", "14"),
        ExamStat("AYT Kimya", "2,1", "13"),
        ExamStat("AYT Biyoloji", "3,0", "13"),
        ExamStat("AYT Edebiyat", "6,5", "23*"),
        ExamStat("YDT İngilizce", "37,6", "80"),
    )

    val examComments: List<ExamComment> = listOf(
        ExamComment(
            id = "tyt-genel",
            label = "TYT",
            title = "Süre kadar soru seçimi de belirleyici",
            body = "2025 aday yorumlarında Türkçe ve sosyalin görece rahat, matematik ve fenin seçici " +
                "olduğu öne çıktı. 2026 ÖSYM verisi de matematikte ortalama 6,4/40, fende 3,9/20 ile " +
                "bu tabloyu destekliyor. Ancak sınav sonrası yorumlar kişisel deneyimdir; kendi deneme " +
                "verin daha güvenilir bir yol göstericidir.",
            takeaway = "TYT'de önce hızlı net kazandığın testi al; matematikte seçici davran, " +
                "tek soruda ısrar etme.",
        ),
        ExamComment(
            id = "ayt-genel",
            label = "AYT",
            title = "Bilgi yükü ve işlem disiplini",
            body = "2025 AYT yorumlarında matematik zaman alıcı, edebiyat görece kolay, fen çeldirici " +
                "bulundu. 2026 ortalamaları AYT matematikte 7,1/39, fizikte 2,6/14, kimyada 2,1/13; " +
                "yani düzenli çalışan bir aday için bu alanlarda fark açmak mümkün.",
            takeaway = "AYT'de hız değil, doğru denklem ve işlem disiplini kazandırır. " +
                "Her soruda önce kurulumu kontrol et.",
        ),
        ExamComment(
            id = "kolay-zor",
            label = "Yorum",
            title = "Kolay/zor söylentisine göre plan yapma",
            body = "Sınav sonrası yorumlar sınavın nesnel zorluğunu değil, o günkü deneyimi anlatır. " +
                "Aynı sınav bir aday için kolay, diğeri için zor olabilir. Kararını kendi deneme " +
                "netlerin ve ÖSYM'nin resmî ortalamalarıyla ver.",
            takeaway = "Bir yorumu değil, üç denemenin ortalamasını ve tekrar eden hata türlerini takip et.",
        ),
        ExamComment(
            id = "ne-yapmali",
            label = "Plan",
            title = "Bu veriyi nasıl kullanacaksın?",
            body = "2026 verisi sayısal testlerde ortalamanın düşük olduğunu gösteriyor. Bu, " +
                "'herkes yapamıyorsa ben de yapamam' demek değil; düzenli tekrarın fark yaratacağı " +
                "alanları işaret ediyor. Haftada bir deneme, iki gün içinde analiz, üç gün sonra aynı " +
                "konudan üç soru.",
            takeaway = "Hedefini net sayısı değil, kapatılacak yanlış sayısı olarak kur.",
        ),
    )

    val sources: List<Source> = listOf(
        Source(
            title = "2026-YKS Sayısal Bilgiler",
            publisher = "ÖSYM",
            url = "https://cdn.osym.gov.tr/pdfdokuman/2026/YKS/SB/sayisal_ykdd21072026.pdf",
        ),
        Source(
            title = "2026 YKS Kılavuzu ve Konu Dağılımı",
            publisher = "MEB",
            url = "https://ttkb.meb.gov.tr/meb_iys_dosyalar/2025_11/26164023_2026_yks.pdf",
        ),
        Source(
            title = "2025 AYT sınav yorumları",
            publisher = "Hürriyet",
            url = "https://www.hurriyet.com.tr/bilgi/galeri/ayt-sinav-yorumlari-2025-yks-ayt-kolay-miydi-zor-muydu-42848769",
        ),
        Source(
            title = "Verimli Ders Çalışma Yöntemleri",
            publisher = "MEB",
            url = "https://yenimahalle.meb.gov.tr/meb_iys_dosyalar/2025_12/05121922_verimliderscalismayontemleri.pdf",
        ),
        Source(
            title = "Improving Students' Learning With Effective Learning Techniques",
            publisher = "Dunlosky vd. (2013)",
            url = "https://doi.org/10.1177/1529100612453266",
        ),
        Source(
            title = "Test-enhanced learning: Taking memory tests improves long-term retention",
            publisher = "Roediger & Karpicke (2006)",
            url = "https://doi.org/10.1111/j.1467-9280.2006.01693.x",
        ),
        Source(
            title = "The memory function of sleep",
            publisher = "Diekelmann & Born (2010)",
            url = "https://doi.org/10.1038/nrn2762",
        ),
    )

    fun studyData(user: UserData): StudyData {
        val weakestTopicId = user.mistakes.minByOrNull { it.mastery }?.topicId
        val featured = weakestTopicId
            ?.let { id -> tactics.firstOrNull { id in it.topicIds }?.id }
            ?: "deneme-analizi"
        return StudyData(
            profile = user.profile,
            streakDays = user.currentStreak(LocalDate.now()),
            reviewsCompleted = user.reviewsCompleted,
            nets = user.nets.sortedBy { it.date },
            mistakes = user.mistakes.sortedByDescending { it.createdOn },
            topics = topics,
            tactics = tactics,
            motivation = motivation,
            examOverviews = examOverviews,
            examStats = examStats,
            examComments = examComments,
            sources = sources,
            featuredTacticId = featured,
        )
    }
}
