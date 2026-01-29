package org.example.toy_zhiri.service.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.toy_zhiri.admin.dto.MessageResponse;
import org.example.toy_zhiri.service.dto.AddToCartRequest;
import org.example.toy_zhiri.service.dto.CartResponse;
import org.example.toy_zhiri.service.dto.ServiceResponse;
import org.example.toy_zhiri.service.entity.CartItem;
import org.example.toy_zhiri.service.entity.Service;
import org.example.toy_zhiri.service.repository.CartItemRepository;
import org.example.toy_zhiri.service.repository.ServiceRepository;
import org.example.toy_zhiri.user.entity.User;
import org.example.toy_zhiri.user.repository.UserRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class CartService {
    private final CartItemRepository cartItemRepository;
    private final ServiceRepository serviceRepository;
    private final UserRepository userRepository;
    private final ServiceService serviceService;

    @Transactional
    public MessageResponse addToCart(UUID userId, AddToCartRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        Service service = serviceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new RuntimeException("Услуга не найдена"));

        if (!service.getIsActive() || !service.getIsApproved()) {
            throw new RuntimeException("Услуга недоступна");
        }

        CartItem cartItem = cartItemRepository.findByUserIdAndServiceId(userId, request.getServiceId())
                .orElse(CartItem.builder()
                        .user(user)
                        .service(service)
                        .build());

        cartItem.setQuantity(request.getQuantity());
        cartItem.setEventDate(request.getEventDate());
        cartItem.setNotes(request.getNotes());

        cartItemRepository.save(cartItem);

        return MessageResponse.builder()
                .message("Услуга добавлена в корзину")
                .build();
    }

    @Transactional
    public MessageResponse removeFromCart(UUID userId, UUID serviceId) {
        if (cartItemRepository.findByUserIdAndServiceId(userId, serviceId).isEmpty()) {
            throw new RuntimeException("Услуга не найдена в корзине");
        }

        cartItemRepository.deleteByUserIdAndServiceId(userId, serviceId);

        return MessageResponse.builder()
                .message("Услуга удалена из корзины")
                .build();
    }

    @Transactional
    public MessageResponse clearCart(UUID userId) {
        cartItemRepository.deleteByUserId(userId);

        return MessageResponse.builder()
                .message("Корзина очищена")
                .build();
    }

    public CartResponse getCart(UUID userId) {
        List<CartItem> items = cartItemRepository.findByUserId(userId);

        List<CartResponse.CartItemResponse> cartItems = items.stream()
                .map(item -> {
                    ServiceResponse serviceResp = serviceService.getServiceById(item.getService().getId(), userId);
                    BigDecimal price = serviceResp.getPriceFrom() != null ?
                            serviceResp.getPriceFrom() : BigDecimal.ZERO;
                    BigDecimal itemTotal = price.multiply(BigDecimal.valueOf(item.getQuantity()));

                    return CartResponse.CartItemResponse.builder()
                            .cartItemId(item.getId().toString())
                            .service(serviceResp)
                            .quantity(item.getQuantity())
                            .eventDate(item.getEventDate())
                            .notes(item.getNotes())
                            .itemTotal(itemTotal)
                            .build();
                })
                .collect(Collectors.toList());

        BigDecimal totalPrice = cartItems.stream()
                .map(CartResponse.CartItemResponse::getItemTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponse.builder()
                .items(cartItems)
                .totalItems(cartItems.size())
                .totalPrice(totalPrice)
                .build();
    }
}