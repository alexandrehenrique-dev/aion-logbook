package br.com.byop.aionlogbook.shared.web;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Profile("!local")
@Controller
public class SpaController {

    @GetMapping(value = {
            "/",
            "/dashboard",
            "/directions/**",
            "/plans/**",
            "/sessions/**",
            "/calendar/**",
            "/analytics/**",
            "/settings/**",
            "/journal",
            "/onboarding",
            "/login"
    })
    public String forwardSpaRoutes() {
        return "forward:/index.html";
    }
}