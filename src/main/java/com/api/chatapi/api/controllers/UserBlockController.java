package com.api.chatapi.api.controllers;

import com.api.chatapi.application.services.UserBlockService;
import com.api.chatapi.domain.dtos.userBlock.CreateUserBlockRequest;
import com.api.chatapi.domain.dtos.userBlock.UserBlockResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "userBlocks")
@RequiredArgsConstructor
public class UserBlockController {

    private final UserBlockService userBlockService;

    @GetMapping("/{appContext}")
    public ResponseEntity<List<UserBlockResponse>> getUserBlocks(@PathVariable String appContext) {
        List<UserBlockResponse> userBlockResponses = userBlockService.getUserBlocks(appContext);
        return ResponseEntity.ok(userBlockResponses);
    }

    @PostMapping()
    public ResponseEntity<UserBlockResponse> createUserBlock(@RequestBody CreateUserBlockRequest userBlockRequest){
        UserBlockResponse userBlock = userBlockService.createUserBlock(userBlockRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(userBlock);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUserBlock(@PathVariable Long id) {
        userBlockService.deleteUserBlock(id);
        return ResponseEntity.noContent().build();
    }
}
