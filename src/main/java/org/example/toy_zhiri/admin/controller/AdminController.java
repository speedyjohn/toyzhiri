package org.example.toy_zhiri.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.toy_zhiri.partner.dto.PartnerApprovalRequest;
import org.example.toy_zhiri.partner.dto.PartnerResponse;
import org.example.toy_zhiri.partner.enums.PartnerStatus;
import org.example.toy_zhiri.partner.service.PartnerService;
import org.example.toy_zhiri.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin", description = "API для администраторов")
public class AdminController {
    private final PartnerService partnerService;
    private final UserService userService;

    @GetMapping("/partners/pending")
    @Operation(
        summary = "Получить заявки на рассмотрении",
        description = "Список всех заявок со статусом PENDING",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<List<PartnerResponse>> getPendingPartners() {
        List<PartnerResponse> partners = partnerService.getPendingPartners();
        return ResponseEntity.ok(partners);
    }

    @GetMapping("/partners/status/{status}")
    @Operation(
        summary = "Получить заявки по статусу",
        description = "Фильтр заявок по статусу: PENDING, APPROVED, REJECTED",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<List<PartnerResponse>> getPartnersByStatus(
        @PathVariable PartnerStatus status) {
        List<PartnerResponse> partners = partnerService.getAllPartnersByStatus(status);
        return ResponseEntity.ok(partners);
    }

    @PostMapping("/partners/{partnerId}/approve")
    @Operation(
        summary = "Одобрить или отклонить заявку",
        description = "Администратор может одобрить (approved=true) или отклонить (approved=false) заявку",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<PartnerResponse> approvePartner(
        @PathVariable UUID partnerId,
        @Valid @RequestBody PartnerApprovalRequest request,
        @AuthenticationPrincipal UserDetails userDetails) {
        UUID adminId = userService.getIdByEmail(userDetails.getUsername());

        PartnerResponse response = partnerService.approvePartner(partnerId, adminId, request);
        return ResponseEntity.ok(response);
    }
}