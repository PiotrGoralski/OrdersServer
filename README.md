Aplikacja do tworzenia zamówień z możliwością oznaczania ich jako zamknięte.

-- BACKEND --

REST API zawiera endpointy do:
- Pobierania wszystkich aktualnych zamówień. (GET /orders/active)
- Dodawania nowego zamówienia (POST /orders {description: String, userId: Long})
- Pobierania pełnej listy zamówień (GET /orders)
- Pobierania przefiltrowanej listy zamówień względem daty początkowej i końcowej (GET /orders?dateFrom=2025-06-06T20:00:00&dateTo=2025-06-07T21:00:00)
- Zamykania zamówienia (DELETE /orders/{orderId})

Plik "Orders REST API" zawiera kolekcje postmanową do przetestowania REST API.

Server wysyła wiadomości na kafkę otworzoną na porcie 9092.
Nowa wiadomość jest wysyłana na topic created-orders przy tworzeniu nowego zamówienia.
Nowa wiadomość jest wysyłana na topic closed-orders przy zamykaniu zamówienia. 

W KafkaMessageConsumer są 2 konsumery które nasłuchują na topicach w celu potwierdzenia poprawnego wysłania wiadomości.

ExceptionHandlerAdvice pomaga nam obsługiwać błędy po stronie serwera. Można go rozbudować w razie potrzeby.

Klasa OrderServiceIntegrationTest zawiera testy integracyjne. Kafka jest mockowana.
Klasa OrderServiceUnitTest zawiera unit testy. JUnit5

-- FRONTEND -- 

Aplikacja w Lit korzystająca z komponentów Lion

W aplikacji są 3 główne komponenty:
- my-element jako zbiorczy element. Pobiera dane o zamówieniach i jest odpowiedzialny za ich magazynowanie.
- orders-table jest elementem wyświetlającym tabele ze wszystkimi zamówieniami.
- create-order-form jest elementem pozwalającym utworzyć nowe zamówienie. 


