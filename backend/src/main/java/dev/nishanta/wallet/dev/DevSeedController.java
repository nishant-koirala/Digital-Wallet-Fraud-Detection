package dev.nishanta.wallet.dev;

import dev.nishanta.wallet.constant.ApiConstants;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiConstants.Dev.BASE)
public class DevSeedController {

    private final SeedService seedService;

    public DevSeedController(SeedService seedService) {
        this.seedService = seedService;
    }

    @PostMapping(ApiConstants.Dev.SEED)
    public SeedResponse seed() {
        return seedService.seed();
    }
}
