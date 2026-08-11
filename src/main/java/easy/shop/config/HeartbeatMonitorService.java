package easy.shop.config;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Nezavisna, pouzdana zaštita da backend ne ostane da radi u pozadini kad
 * korisnik zatvori prozor aplikacije.
 *
 * Praćenje samog OS procesa browsera (BrowserLauncher) nije dovoljno pouzdano
 * samo po sebi - Chrome po difoltu može da nastavi da radi u pozadini i posle
 * zatvaranja svih prozora (opcija "Continue running background apps"), pa se
 * proces ponekad NE gasi kad korisnik klikne X.
 *
 * Ovaj mehanizam je nezavisan od toga: frontend (dok god je stranica otvorena
 * u browseru, bez obzira na login status) šalje redovan "heartbeat" signal.
 * Ako signal predugo izostane, to pouzdano znači da je prozor stvarno zatvoren
 * (ili je racunar ugasen/hibernisan) - i backend se sam gasi.
 */
@Service
@RequiredArgsConstructor
public class HeartbeatMonitorService {

    private static final long TIMEOUT_MS = 60_000;

    private final AppLifecycleService lifecycleService;
    private volatile long lastHeartbeatAt = System.currentTimeMillis();

    public void recordHeartbeat() {
        lastHeartbeatAt = System.currentTimeMillis();
    }

    @Scheduled(fixedDelay = 15_000)
    public void checkHeartbeat() {
        if (System.currentTimeMillis() - lastHeartbeatAt > TIMEOUT_MS) {
            // Gašenje se MORA pokrenuti na posebnom thread-u, van scheduler pool-a.
            // Spring gasi taskScheduler bean kao deo shutdown-a, a ako bi upravo
            // taj scheduler thread pozvao SpringApplication.exit(...) direktno,
            // scheduler bi cekao da se sopstveni zadatak zavrsi pre nego sto moze
            // da se ugasi - sto izaziva ~30s blokadu i "Interrupted" gresku u logu.
            Thread shutdownThread = new Thread(lifecycleService::shutdown, "easyshop-heartbeat-shutdown");
            shutdownThread.setDaemon(true);
            shutdownThread.start();
        }
    }
}
