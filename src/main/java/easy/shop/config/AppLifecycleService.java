package easy.shop.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AppLifecycleService {

    private final ApplicationContext context;

    /** Potpuno gasi backend proces (koristi se i sa "Zatvori aplikaciju" dugmeta i pri zatvaranju prozora). */
    public void shutdown() {
        int exitCode = SpringApplication.exit(context, () -> 0);
        System.exit(exitCode);
    }
}
