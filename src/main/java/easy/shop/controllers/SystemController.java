package easy.shop.controllers;

import easy.shop.config.AppLifecycleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SystemController {

    private final AppLifecycleService lifecycleService;

    /**
     * Potpuno gasi backend server (ne samo browser prozor). Koristi se iz
     * desktop verzije aplikacije da bi korisnik mogao stvarno da zatvori app
     * iz menija, bez potrebe da ubija proces preko Task Manager-a.
     */
    @PostMapping("/api/system/shutdown")
    public ResponseEntity<Void> shutdown() {
        new Thread(() -> {
            try {
                Thread.sleep(500); // ostavi vremena da HTTP odgovor stigne do klijenta
            } catch (InterruptedException ignored) {
            }
            lifecycleService.shutdown();
        }).start();

        return ResponseEntity.ok().build();
    }
}
