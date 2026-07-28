package easy.shop.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SystemController {

    private final ApplicationContext context;

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
            int exitCode = SpringApplication.exit(context, () -> 0);
            System.exit(exitCode);
        }).start();

        return ResponseEntity.ok().build();
    }
}
