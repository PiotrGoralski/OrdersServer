INSERT INTO public."APP_USER" (id, username) VALUES ('29c2ec3b-cf6e-40b7-ac9f-738289e3af77', 'User 1');
INSERT INTO public."APP_USER" (id, username) VALUES ('95292452-9d45-4bbd-94a5-486aa08d6b0a', 'User 2');
INSERT INTO public."APP_USER" (id, username) VALUES ('75ecdb98-a3d1-4cfd-bf99-5f8331b319be', 'User 3');

INSERT INTO public."APP_ORDER" (id, creation_date, status, description, id_user) VALUES ('fce71902-6380-4b5c-9f41-215e7d0239d2', '2025-06-06 10:00:00.000000', 'CREATED', 'Order 1', '29c2ec3b-cf6e-40b7-ac9f-738289e3af77');
INSERT INTO public."APP_ORDER" (id, creation_date, status, description, id_user) VALUES ('5c21077b-08d3-4823-9c9c-5565c818443a', '2025-06-06 21:10:00.000000', 'CREATED', 'Order 2', '95292452-9d45-4bbd-94a5-486aa08d6b0a');
INSERT INTO public."APP_ORDER" (id, creation_date, status, description, id_user) VALUES ('077b1207-47c2-415d-8b88-25ad2d4c8e82', '2025-06-07 22:30:00.000000', 'CREATED', 'Order 3', '75ecdb98-a3d1-4cfd-bf99-5f8331b319be');
INSERT INTO public."APP_ORDER" (id, creation_date, status, description, id_user) VALUES ('68fe79cd-9e01-4d18-8eaa-0733c3aac5e1', '2025-06-05 12:00:00.000000', 'CLOSED', 'Order 4 closed', '29c2ec3b-cf6e-40b7-ac9f-738289e3af77');
INSERT INTO public."APP_ORDER" (id, creation_date, status, description, id_user) VALUES ('a14eaa90-0d84-4a7f-87e8-5d55cdb6dcb2', '2025-06-06 15:00:00.000000', 'CLOSED', 'Order 5 closed', '29c2ec3b-cf6e-40b7-ac9f-738289e3af77');
INSERT INTO public."APP_ORDER" (id, creation_date, status, description, id_user) VALUES ('785c610e-3f47-4f76-b5cc-056bced529f4', '2025-06-07 21:00:00.000000', 'CLOSED', 'Order 6 closed', '29c2ec3b-cf6e-40b7-ac9f-738289e3af77');
