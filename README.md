# Bank System - REST API

## 📌 Описание проекта
**Bank System** - это RESTful API для управления банковскими картами с системой аутентификации и авторизации. Проект предоставляет функционал для администраторов и пользователей по управлению картами, переводам между картами и просмотру информации.

### 🔧 Основные возможности:
- **👨💻 Для администраторов**:
  - Создание/блокировка/активация/удаление карт
  - Управление пользователями
  - Просмотр всех карт в системе

- **👤 Для пользователей**:
  - Просмотр своих карт с пагинацией
  - Запрос на блокировку карты
  - Переводы между своими картами
  - Просмотр баланса

## 🛠 Технологии
| Технология         | Версия       |
|--------------------|-------------|
| Java               | 21          |
| Spring Boot        | 3.5.3       |
| PostgreSQL         | 15-alpine   |
| Liquibase          | 4.25.0      |
| Springdoc OpenAPI  | 2.8.9       |

## ⚠️ Настройка окружения (.env)

Перед первым запуском необходимо создать файл `.env` в корне проекта со следующими переменными:

```ini
# JWT Настройки
JWT_SECRET=your-256-bit-secret-key-here
JWT_ACCESS_TTL=15m    # Время жизни access токена (например 15 минут)
JWT_REFRESH_TTL=7d    # Время жизни refresh токена (например 7 дней)

# Учетные данные суперпользователя в БД
ALL_PRIVILEGES_USER_LOGIN=postgres
ALL_PRIVILEGES_USER_PASSWORD=admin
```
Образец можно взять по `.env.example`.

## 🚀 Запуск проекта с помощью Docker

### 📋 Предварительные требования
- Docker 20.10.0+
- Docker Compose 2.0.0+
- 2 ГБ свободной памяти
- Порт 8080 свободен

### ⚡ Быстрый старт
```bash
git clone https://github.com/dankotyt/Bank_REST.git
cd Bank_REST
docker compose up --build -d
```

## 📚 Доступ к Swagger UI

После успешного запуска приложения интерактивная документация API будет доступна по адресу:

🔗 [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui/index.html)

![Swagger UI Preview](/docs/swagger-preview.png)

> **Tip**
> Для авторизации в Swagger используйте кнопку "Authorize" и введите JWT-токен

## ⁉️ Поддержка и обратная связь

### 📧 Email для связи:
[danilkotlarov1@gmail.com](mailto:danilkotlarov1@gmail.com)
### 📲 Telegram:
[@eximun](https://t.me/eximun)

