package io.infinite.david.controllers

import groovy.transform.Memoized
import io.infinite.blackbox.BlackBox
import io.infinite.david.DavidApp
import io.infinite.david.conf.DavidConfiguration
import io.infinite.david.repositories.LinkRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseBody

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Controller
@BlackBox
class PushController {

    @Autowired
    LinkRepository linkRepository

    @PostMapping(value = "/david/push")
    @ResponseBody
    String post(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, @RequestBody String requestBody) {
        String path = httpServletRequest.getRequestURI()
        Binding binding = new Binding()
        binding.setVariable("httpServletRequest", httpServletRequest)
        binding.setVariable("httpServletResponse", httpServletResponse)
        binding.setVariable("requestBody", requestBody)
        binding.setVariable("linkRepository", linkRepository)
        binding.setVariable("silentSender", DavidApp.silentSender)
        return groovyScriptEngine.run("Push.groovy", binding)
    }

    @Memoized
    GroovyScriptEngine getGroovyScriptEngine() {
        return new GroovyScriptEngine(DavidConfiguration.conf.pluginsDir, "Push.groovy")
    }

}
