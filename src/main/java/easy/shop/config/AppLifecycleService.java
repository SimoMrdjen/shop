package easy.shop.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicBoolean;

@Service
@RequiredArgsConstructor
public class AppLifecycleService {

    private final ApplicationContext context;
    private final AtomicBoolean shuttingDown = new AtomicBoolean(false);

    /**
     * Potpuno gasi backend proces (koristi se i sa "Zatvori aplikaciju" dugmeta,
     * pri zatvaranju prozora, i pri isteku heartbeat-a). Bezbedno je pozvati je
     * više puta / iz više izvora istovremeno - samo prvi poziv nešto radi.
     */
    public void shutdown() {
        if (!shuttingDown.compareAndSet(false, true)) {
            return;
        }
        int exitCode = SpringApplication.exit(context, () -> 0);
        System.exit(exitCode);
    }
}
