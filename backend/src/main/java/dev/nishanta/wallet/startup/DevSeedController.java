package dev.nishanta.wallet.startup;

import dev.nishanta.wallet.constant.ApiConstants;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.nishanta.wallet.common.constant.ApiRoutes;

@RestController
@RequestMapping(ApiRoutes.DEV_BASE)
public class DevSeedController {

    private final SeedService seedService;

    public DevSeedController(SeedService seedService) {
        this.seedService = seedService;
    }

    @PostMapping(ApiRoutes.DEV_SEED)
    public SeedResponse seed() {
        return seedService.seed();
    }
}
