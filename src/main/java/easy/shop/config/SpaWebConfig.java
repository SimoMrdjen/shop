package easy.shop.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Omogucava da Angular (client-side) rutiranje radi i posle F5/direktnog linka
 * (npr. /contracts/5) - takvi zahtevi (bez ekstenzije fajla) se prosledjuju
 * na index.html, a Angular Router preuzima dalje rutiranje u browseru.
 */
@Configuration
public class SpaWebConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/{path:[^\\.]*}").setViewName("forward:/index.html");
        registry.addViewController("/**/{path:[^\\.]*}").setViewName("forward:/index.html");
    }
}
