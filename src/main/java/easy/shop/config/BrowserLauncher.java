package easy.shop.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.awt.Desktop;
import java.io.File;
import java.net.URI;

@Component
public class BrowserLauncher {

    @Value("${server.port:8080}")
    private String port;

    private static final String[] CHROME_PATHS = {
            "C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe",
            "C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe",
            System.getenv("LOCALAPPDATA") + "\\Google\\Chrome\\Application\\chrome.exe",
    };

    @EventListener(ApplicationReadyEvent.class)
    public void openBrowser() {
        String url = "http://localhost:" + port + "/";
        if (openChromeAppMode(url)) {
            return;
        }
        openDefaultBrowser(url);
    }

    private boolean openChromeAppMode(String url) {
        for (String path : CHROME_PATHS) {
            if (path != null && new File(path).isFile()) {
                try {
                    new ProcessBuilder(path, "--app=" + url).start();
                    return true;
                } catch (Exception ignored) {
                    // probaj sledecu putanju / fallback na podrazumevani browser
                }
            }
        }
        return false;
    }

    private void openDefaultBrowser(String url) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
            }
        } catch (Exception ignored) {
            // Aplikacija i dalje radi i dostupna je na http://localhost:<port>/
            // i ako automatsko otvaranje browsera ne uspe (npr. bez grafickog okruzenja).
        }
    }
}
