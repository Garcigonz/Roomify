TRUNCATE permissions, roles, role_hierarchy, role_permissions CASCADE;

INSERT INTO permissions(id, resource, action) VALUES
                                                  ('users:read', 'users', 'read'),
                                                  ('users:create', 'users', 'create'),
                                                  ('users:update', 'users', 'update'),
                                                  ('users:delete', 'users', 'delete'),
                                                  ('books:read', 'books', 'read'),
                                                  ('books:create', 'books', 'create'),
                                                  ('books:update', 'books', 'update'),
                                                  ('books:delete', 'books', 'delete');

INSERT INTO roles(rolename) VALUES
                                ('ADMIN'),
                                ('USER');

INSERT INTO role_hierarchy(role, includes) VALUES
    ('ADMIN', 'USER');

INSERT INTO role_permissions(role, permission) VALUES
                                                   ('USER', 'users:read'),
                                                   ('USER', 'books:read'),
                                                   ('ADMIN', 'users:create'),
                                                   ('ADMIN', 'users:update'),
                                                   ('ADMIN', 'users:delete'),
                                                   ('ADMIN', 'books:create'),
                                                   ('ADMIN', 'books:update'),
                                                   ('ADMIN', 'books:delete');