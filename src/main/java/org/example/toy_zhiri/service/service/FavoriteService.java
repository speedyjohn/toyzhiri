package org.example.toy_zhiri.service.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.toy_zhiri.admin.dto.MessageResponse;
import org.example.toy_zhiri.service.dto.ServiceResponse;
import org.example.toy_zhiri.service.entity.Favorite;
import org.example.toy_zhiri.service.entity.Service;
import org.example.toy_zhiri.service.repository.FavoriteRepository;
import org.example.toy_zhiri.service.repository.ServiceRepository;
import org.example.toy_zhiri.user.entity.User;
import org.example.toy_zhiri.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class FavoriteService {
    private final FavoriteRepository favoriteRepository;
    private final ServiceRepository serviceRepository;
    private final UserRepository userRepository;
    private final ServiceService serviceService;

    @Transactional
    public MessageResponse addToFavorites(UUID userId, UUID serviceId) {
        if (favoriteRepository.existsByUserIdAndServiceId(userId, serviceId)) {
            throw new RuntimeException("Услуга уже в избранном");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Услуга не найдена"));

        Favorite favorite = Favorite.builder()
                .user(user)
                .service(service)
                .build();

        favoriteRepository.save(favorite);

        return MessageResponse.builder()
                .message("Услуга добавлена в избранное")
                .build();
    }

    @Transactional
    public MessageResponse removeFromFavorites(UUID userId, UUID serviceId) {
        if (!favoriteRepository.existsByUserIdAndServiceId(userId, serviceId)) {
            throw new RuntimeException("Услуга не найдена в избранном");
        }

        favoriteRepository.deleteByUserIdAndServiceId(userId, serviceId);

        return MessageResponse.builder()
                .message("Услуга удалена из избранного")
                .build();
    }

    public Page<ServiceResponse> getFavorites(UUID userId, Pageable pageable) {
        Page<Favorite> favorites = favoriteRepository.findByUserId(userId, pageable);
        return favorites.map(fav -> serviceService.getServiceById(fav.getService().getId(), userId));
    }
}