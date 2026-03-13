ALTER TABLE bookings ADD COLUMN extra_params JSONB;

COMMENT ON COLUMN bookings.extra_params IS
    'Дополнительные параметры бронирования в зависимости от типа услуги.
     Примеры:
     Ресторан:  {"menu": "европейская кухня", "hall": "VIP зал", "decoration": true}
     Кортеж:    {"route": "ЗАГС → ресторан Астана Холл", "cars_count": 3}
     Тамада:    {"language": "русский", "style": "классический", "duration_hours": 4}
     Фото/Видео:{"format": "репортаж", "duration_hours": 6, "drone": false}
     Кейтеринг: {"menu_type": "шведский стол", "dishes_count": 10}';