package org.example.dingdongserver.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.extern.slf4j.Slf4j;
import java.util.ArrayList;

@RestController
@Slf4j
@RequestMapping("/user")
public class UserController {
    @GetMapping
    public ResponseEntity<ArrayList<String>> getUser() {
        ArrayList<String> list = new ArrayList<>();

        log.info("로그가 잘 찍히는 지 확인");
        return ResponseEntity.ok(list);
    }

}
