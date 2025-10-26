INSERT INTO users (id, email, password, fullname, phone, role, city, emailverified)
VALUES (
           gen_random_uuid(),
           'admin@toyzhiri.kz',
           '$2a$10$s3bN9PLZ21MdOrG3uYDWSuoPFLUT5Oz2jlcWXFt1HIUVEyRiYVDsO',
           'Администратор',
           '77086204330',
           'ADMIN',
           'Астана',
           true
       );