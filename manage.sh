#!/bin/bash

# Простой скрипт управления Toy Zhiri

set -e

echo "======================================"
echo "    Toy Zhiri - Управление"
echo "======================================"
echo ""

# Функция для вывода цветных сообщений
print_info() {
    echo "✓ $1"
}

print_error() {
    echo "✗ $1"
}

# Проверка Docker
if ! command -v docker &> /dev/null; then
    print_error "Docker не установлен!"
    echo "Установите Docker: curl -fsSL https://get.docker.com -o get-docker.sh && sudo sh get-docker.sh"
    exit 1
fi

# Проверка .env файла
if [ ! -f .env ]; then
    print_error ".env файл не найден!"
    echo ""
    echo "Создайте .env файл:"
    echo "  cp .env.example .env"
    echo "  nano .env"
    echo ""
    exit 1
fi

# Меню
echo "Выберите действие:"
echo ""
echo "1) Запустить приложение"
echo "2) Остановить приложение"
echo "3) Перезапустить приложение"
echo "4) Посмотреть логи"
echo "5) Проверить статус"
echo "6) Обновить приложение"
echo "7) Создать бэкап БД"
echo "8) Остановить и удалить всё"
echo ""
read -p "Введите номер: " choice

case $choice in
    1)
        print_info "Запускаю приложение..."
        docker compose up -d
        echo ""
        print_info "Приложение запущено!"
        echo ""
        echo "API: http://localhost:8080"
        echo "Swagger: http://localhost:8080/swagger-ui/index.html"
        echo ""
        echo "Логи: docker compose logs -f app"
        ;;
    2)
        print_info "Останавливаю приложение..."
        docker compose down
        print_info "Приложение остановлено"
        ;;
    3)
        print_info "Перезапускаю приложение..."
        docker compose restart
        print_info "Приложение перезапущено"
        ;;
    4)
        print_info "Показываю логи (Ctrl+C для выхода)..."
        echo ""
        docker compose logs -f app
        ;;
    5)
        print_info "Статус контейнеров:"
        echo ""
        docker compose ps
        echo ""
        print_info "Использование ресурсов:"
        docker stats --no-stream
        ;;
    6)
        print_info "Обновляю приложение..."
        echo ""
        echo "Шаг 1/4: Получаю новый код..."
        git pull
        echo ""
        echo "Шаг 2/4: Останавливаю контейнеры..."
        docker compose down
        echo ""
        echo "Шаг 3/4: Пересобираю образ..."
        docker compose build app
        echo ""
        echo "Шаг 4/4: Запускаю..."
        docker compose up -d
        echo ""
        print_info "Приложение обновлено!"
        echo ""
        echo "Проверьте логи: docker compose logs -f app"
        ;;
    7)
        print_info "Создаю бэкап базы данных..."
        backup_file="backup_toyzhiri_$(date +%Y%m%d_%H%M%S).sql"
        docker exec toyzhiri-postgres pg_dump -U postgres toyzhiri > "$backup_file"
        print_info "Бэкап создан: $backup_file"

        # Сжимаем
        gzip "$backup_file"
        print_info "Бэкап сжат: ${backup_file}.gz"
        ;;
    8)
        echo ""
        print_error "ВНИМАНИЕ! Это удалит ВСЕ данные (включая БД)!"
        read -p "Вы уверены? Введите 'yes' для подтверждения: " confirm
        if [ "$confirm" = "yes" ]; then
            print_info "Останавливаю и удаляю всё..."
            docker compose down -v
            print_info "Всё удалено"
        else
            print_info "Отменено"
        fi
        ;;
    *)
        print_error "Неверный выбор"
        exit 1
        ;;
esac

echo ""
print_info "Готово!"