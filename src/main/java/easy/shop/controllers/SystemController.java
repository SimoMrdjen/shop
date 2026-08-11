package easy.shop.controllers;

import easy.shop.config.AppLifecycleService;
import easy.shop.config.HeartbeatMonitorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SystemController {

    private final AppLifecycleService lifecycleService;
    private final HeartbeatMonitorService heartbeatMonitorService;

    /**
     * Frontend redovno zove ovaj endpoint dok god je stranica otvorena (i pre
     * i posle prijave). Ako signal predugo izostane, HeartbeatMonitorService
     * pouzdano zaključuje da je prozor zatvoren i gasi backend - nezavisno od
     * toga da li je Chrome-ov proces i dalje "tehnicki" ziv u pozadini.
     */
    @PostMapping("/api/system/heartbeat")
    public ResponseEntity<Void> heartbeat() {
        heartbeatMonitorService.recordHeartbeat();
        return ResponseEntity.ok().build();
    }

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
