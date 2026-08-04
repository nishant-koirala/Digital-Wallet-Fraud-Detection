package dev.nishanta.wallet.dev;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dev")
public class DevSeedController {

    private final SeedService seedService;

    public DevSeedController(SeedService seedService) {
        this.seedService = seedService;
    }

    @PostMapping("/seed")
    public SeedResponse seed() {
        return seedService.seed();
    }
}
