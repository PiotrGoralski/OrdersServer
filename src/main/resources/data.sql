INSERT INTO public."APP_USER" (username) VALUES ('User 1');
INSERT INTO public."APP_USER" (username) VALUES ('User 2');
INSERT INTO public."APP_USER" (username) VALUES ('User 3');

INSERT INTO public."APP_ORDER" (creation_date, status, description, id_user) VALUES ('2025-06-06 10:00:00.000000', 'CREATED', 'Order 1', 1);
INSERT INTO public."APP_ORDER" (creation_date, status, description, id_user) VALUES ('2025-06-06 21:10:00.000000', 'CREATED', 'Order 2', 2);
INSERT INTO public."APP_ORDER" (creation_date, status, description, id_user) VALUES ('2025-06-07 22:30:00.000000', 'CREATED', 'Order 3', 3);
INSERT INTO public."APP_ORDER" (creation_date, status, description, id_user) VALUES ('2025-06-05 12:00:00.000000', 'CLOSED', 'Order 4 closed', 1);
INSERT INTO public."APP_ORDER" (creation_date, status, description, id_user) VALUES ('2025-06-06 15:00:00.000000', 'CLOSED', 'Order 5 closed', 1);
INSERT INTO public."APP_ORDER" (creation_date, status, description, id_user) VALUES ('2025-06-07 21:00:00.000000', 'CLOSED', 'Order 6 closed', 1);
