package com.x0.sbootplayground.rest;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(value = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
public class DummyController {

    private static final String ECHO_PFX_SHORT = "/echo";
    private static final String ECHO_PFX = ECHO_PFX_SHORT + '/';

    @GetMapping(value = ECHO_PFX_SHORT)
    public ResponseBean echoFromQuery(@RequestParam("text") String text) {
        return new ResponseBean(text);
    }

    @GetMapping(ECHO_PFX + "{text}")
    public ResponseBean echoFromPath(@PathVariable("text") String text) {
        return echoFromQuery(text);
    }

    @GetMapping(ECHO_PFX + "**")
    public ResponseBean echoFromPath(HttpServletRequest req) {
        String uri = req.getRequestURI();
        return echoFromPath(uri.substring(uri.indexOf(ECHO_PFX) + ECHO_PFX.length()));
    }


    public static final class ResponseBean {

        private final String data;

        public ResponseBean(String data) {
            this.data = data;
        }

        public String getData() {
            return data;
        }
    }
}