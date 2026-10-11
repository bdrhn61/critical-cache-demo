package critical_cache_demo.com.example.critical_cache_demo.controller;

import critical_cache_demo.com.example.critical_cache_demo.service.DltReplayService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final DltReplayService dltReplayService;

    public AdminController(DltReplayService dltReplayService) {
        this.dltReplayService = dltReplayService;
    }

    @PostMapping("/dlt/replay")
    public ResponseEntity<String> replayDlt() {

        dltReplayService.replay();

        return ResponseEntity.ok(
                "DLT mesajları replay edildi."
        );
    }
}