package br.com.basis.abaco.web.rest;

import br.com.basis.abaco.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.PrintWriter;
import java.io.StringWriter;

@RestController
@RequestMapping("/api")
public class DebugResource {

    private final Logger log = LoggerFactory.getLogger(DebugResource.class);
    private final UserRepository userRepository;

    public DebugResource(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/debug/db-check")
    public String checkDatabase() {
        try {
            long count = userRepository.count();
            return "Database is connected. User count: " + count;
        } catch (Throwable e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            return sw.toString();
        }
    }
}
