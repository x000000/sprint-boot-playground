package com.x0.sbootplayground.rest;

import com.x0.sbootplayground.rest.DummyController.ResponseBean;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DummyController.class)
@AutoConfigureJsonTesters
class DummyControllerTests {

    private static final String BASE_URI = "/api/echo";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JacksonTester<ResponseBean> json;

    @Test
    void testQueryEndpoint1() throws Exception {
        testRequest(BASE_URI + "?text={0}", "some text");
    }

    @Test
    void testQueryEndpoint2() throws Exception {
        testRequest(BASE_URI + "?text={0}", "some\\text");
    }

    @Test
    void testQueryEndpoint3() throws Exception {
        mvc.perform(get(BASE_URI).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testPathEndpoint() throws Exception {
        testRequest(BASE_URI + "/{0}", "some text");
    }

    @Test
    void testFallbackEndpoint() throws Exception {
        testRequest(BASE_URI + "/{0}", "some/text");
    }

    private void testRequest(String uriTemplate, String text) throws Exception {
        mvc.perform(get(uriTemplate, text).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(json.write(new ResponseBean(text)).getJson()));
    }
}
