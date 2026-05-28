package com.tranvanluan.backend.controller;

import com.tranvanluan.backend.entity.Contact;
import com.tranvanluan.backend.service.ContactService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contacts")
@RequiredArgsConstructor
public class ContactController {
    
    private final ContactService contactService;

    // Dùng cho Khách hàng gửi liên hệ
    @PostMapping
    public ResponseEntity<Contact> createContact(@RequestBody Contact contact) {
        return ResponseEntity.ok(contactService.createContact(contact));
    }

    // Dùng cho Admin lấy danh sách liên hệ
    @GetMapping
    public ResponseEntity<List<Contact>> getAllContacts() {
        return ResponseEntity.ok(contactService.getAllContacts());
    }

    // Dùng cho Admin cập nhật trạng thái
    @PutMapping("/{id}/status")
    public ResponseEntity<Contact> updateStatus(@PathVariable Long id, @RequestParam String status) {
        return ResponseEntity.ok(contactService.updateContactStatus(id, status));
    }
}
