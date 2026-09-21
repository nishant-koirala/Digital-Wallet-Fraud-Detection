package dev.nishanta.wallet.startup;

import dev.nishanta.wallet.constant.ApiConstants;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.nishanta.wallet.common.constant.ApiRoutes;

@RestController
@RequestMapping(ApiRoutes.DEV_BASE)
@Profile("dev")
public class DevSeedController {

    private final SeedService seedService;

    public DevSeedController(SeedService seedService) {
        this.seedService = seedService;
    }

    @PostMapping(ApiRoutes.DEV_SEED)
    public SeedResponse seed() {
        return seedService.seed();
    }

    @org.springframework.web.bind.annotation.GetMapping("/promote")
    public String promoteAdmin(@org.springframework.web.bind.annotation.RequestParam String email) {
        return seedService.promoteToAdmin(email);
    }
}
