package easy.shop.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.awt.Desktop;
import java.io.File;
import java.net.URI;

@Component
@RequiredArgsConstructor
public class BrowserLauncher {

    private final AppLifecycleService lifecycleService;

    @Value("${server.port:8080}")
    private String port;

    private static final String[] CHROME_PATHS = {
            "C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe",
            "C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe",
            System.getenv("LOCALAPPDATA") + "\\Google\\Chrome\\Application\\chrome.exe",
    };

    /**
     * Minimalno vreme (ms) da proces mora da bude živ da bismo ga smatrali
     * "korisnik je stvarno koristio i zatvorio prozor", umesto neuspelog
     * pokretanja (npr. zauzet profil) - da ne bismo pogrešno ugasili server.
     */
    private static final long MIN_ALIVE_MS = 3000;

    @EventListener(ApplicationReadyEvent.class)
    public void openBrowser() {
        String url = "http://localhost:" + port + "/";
        if (openChromeAppMode(url)) {
            return;
        }
        openDefaultBrowser(url);
    }

    /**
     * Otvara Chrome u "app modu" (bez adresne trake), koristeći NAMENSKI,
     * izolovan profil (--user-data-dir). Bez ovoga, ako korisnik već ima
     * Chrome otvoren za nešto drugo, novi proces se odmah gasi i "preda"
     * prozor postojećoj instanci - a mi onda ne bismo mogli pouzdano da
     * pratimo kad je BAŠ OVAJ prozor zatvoren.
     *
     * Sa izolovanim profilom, pokrenuti proces živi tačno onoliko koliko
     * živi taj konkretan prozor - kad korisnik klikne X, proces se gasi,
     * mi to primetimo i ugasimo ceo backend automatski (korisnik ne mora
     * da zna da mora posebno da "zatvori aplikaciju" - zatvaranje prozora
     * je dovoljno, kao kod svake druge desktop aplikacije).
     */
    private boolean openChromeAppMode(String url) {
        for (String path : CHROME_PATHS) {
            if (path != null && new File(path).isFile()) {
                try {
                    String profileDir = System.getProperty("java.io.tmpdir") + File.separator + "easyshop-chrome-profile";
                    Process process = new ProcessBuilder(
                            path,
                            "--user-data-dir=" + profileDir,
                            "--app=" + url
                    ).start();

                    watchWindowAndShutdownOnClose(process);
                    return true;
                } catch (Exception ignored) {
                    // probaj sledecu putanju / fallback na podrazumevani browser
                }
            }
        }
        return false;
    }

    private void watchWindowAndShutdownOnClose(Process process) {
        long startedAt = System.currentTimeMillis();
        Thread watcher = new Thread(() -> {
            try {
                process.waitFor();
            } catch (InterruptedException e) {
                return;
            }
            long aliveMs = System.currentTimeMillis() - startedAt;
            if (aliveMs >= MIN_ALIVE_MS) {
                lifecycleService.shutdown();
            }
            // ako je proces umro prebrzo, verovatno pokretanje nije uspelo -
            // ne gasimo server zbog toga
        }, "easyshop-window-watcher");
        watcher.setDaemon(true);
        watcher.start();
    }

    private void openDefaultBrowser(String url) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
            }
        } catch (Exception ignored) {
            // Aplikacija i dalje radi i dostupna je na http://localhost:<port>/
            // i ako automatsko otvaranje browsera ne uspe (npr. bez grafickog okruzenja).
            // NAPOMENA: u ovom fallback slucaju (podrazumevani browser umesto Chrome-a)
            // ne postoji nacin da pratimo zivotni vek prozora, pa se zatvaranje prozora
            // NE gasi backend automatski - korisnik i dalje mora da koristi
            // "Zatvori aplikaciju" iz menija ili Task Manager.
        }
    }
}
