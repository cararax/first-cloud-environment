-- =============================================
-- TODO APP DATABASE SCHEMA
-- =============================================

-- Tabela de usuários
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Tabela de listas (para organizar tarefas)
CREATE TABLE lists (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    color VARCHAR(7) DEFAULT '#3B82F6', -- Cor em hex
    icon VARCHAR(50),
    position INTEGER DEFAULT 0, -- Para ordenação
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Tabela principal de tarefas (todos)
CREATE TABLE todos (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    list_id INTEGER REFERENCES lists(id) ON DELETE SET NULL,
    parent_id INTEGER REFERENCES todos(id) ON DELETE CASCADE, -- Para subtarefas
    title VARCHAR(255) NOT NULL,
    description TEXT,
    priority INTEGER DEFAULT 0 CHECK (priority >= 0 AND priority <= 3), -- 0=none, 1=low, 2=medium, 3=high
    is_completed BOOLEAN DEFAULT FALSE,
    completed_at TIMESTAMP WITH TIME ZONE,
    due_date TIMESTAMP WITH TIME ZONE,
    reminder_at TIMESTAMP WITH TIME ZONE,
    position INTEGER DEFAULT 0, -- Para ordenação dentro da lista
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Tabela de tags
CREATE TABLE tags (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(50) NOT NULL,
    color VARCHAR(7) DEFAULT '#6B7280', -- Cor em hex
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, name) -- Cada usuário tem tags únicas
);

-- Tabela de relacionamento todo-tags (N:N)
CREATE TABLE todo_tags (
    todo_id INTEGER NOT NULL REFERENCES todos(id) ON DELETE CASCADE,
    tag_id INTEGER NOT NULL REFERENCES tags(id) ON DELETE CASCADE,
    PRIMARY KEY (todo_id, tag_id)
);

-- =============================================
-- ÍNDICES PARA PERFORMANCE
-- =============================================

-- Índices para busca frequente
CREATE INDEX idx_todos_user_id ON todos(user_id);
CREATE INDEX idx_todos_list_id ON todos(list_id);
CREATE INDEX idx_todos_parent_id ON todos(parent_id);
CREATE INDEX idx_todos_is_completed ON todos(is_completed);
CREATE INDEX idx_todos_due_date ON todos(due_date);
CREATE INDEX idx_todos_priority ON todos(priority);

CREATE INDEX idx_lists_user_id ON lists(user_id);
CREATE INDEX idx_tags_user_id ON tags(user_id);

-- Índice composto para queries comuns
CREATE INDEX idx_todos_user_completed ON todos(user_id, is_completed);
CREATE INDEX idx_todos_user_due_date ON todos(user_id, due_date);

-- =============================================
-- TRIGGERS PARA UPDATED_AT
-- =============================================

-- Função para atualizar updated_at automaticamente
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Triggers
CREATE TRIGGER update_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_lists_updated_at
    BEFORE UPDATE ON lists
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_todos_updated_at
    BEFORE UPDATE ON todos
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- =============================================
-- VIEWS ÚTEIS
-- =============================================

-- View para tarefas pendentes com informações completas
CREATE VIEW pending_todos AS
SELECT
    t.id,
    t.title,
    t.description,
    t.priority,
    t.due_date,
    t.created_at,
    l.name as list_name,
    u.name as user_name,
    ARRAY_AGG(tg.name) FILTER (WHERE tg.name IS NOT NULL) as tags
FROM todos t
LEFT JOIN lists l ON t.list_id = l.id
LEFT JOIN users u ON t.user_id = u.id
LEFT JOIN todo_tags tt ON t.id = tt.todo_id
LEFT JOIN tags tg ON tt.tag_id = tg.id
WHERE t.is_completed = FALSE
GROUP BY t.id, l.name, u.name;

-- View para tarefas vencidas
CREATE VIEW overdue_todos AS
SELECT
    t.*,
    l.name as list_name
FROM todos t
LEFT JOIN lists l ON t.list_id = l.id
WHERE t.is_completed = FALSE
  AND t.due_date < CURRENT_TIMESTAMP;

-- =============================================
-- DADOS DE EXEMPLO (OPCIONAL)
-- =============================================

-- Inserir usuário de exemplo
INSERT INTO users (email, password_hash, name)
VALUES ('demo@example.com', 'hashed_password_here', 'Demo User');

-- Inserir listas de exemplo
INSERT INTO lists (user_id, name, description, color) VALUES
(1, 'Pessoal', 'Tarefas pessoais', '#10B981'),
(1, 'Trabalho', 'Tarefas do trabalho', '#3B82F6'),
(1, 'Compras', 'Lista de compras', '#F59E0B');

-- Inserir tags de exemplo
INSERT INTO tags (user_id, name, color) VALUES
(1, 'urgente', '#EF4444'),
(1, 'importante', '#F97316'),
(1, 'aguardando', '#8B5CF6');

-- Inserir tarefas de exemplo
INSERT INTO todos (user_id, list_id, title, description, priority, due_date) VALUES
(1, 2, 'Preparar apresentação', 'Slides para reunião de segunda', 3, CURRENT_TIMESTAMP + INTERVAL '2 days'),
(1, 1, 'Ir ao médico', 'Consulta de rotina', 2, CURRENT_TIMESTAMP + INTERVAL '7 days'),
(1, 3, 'Comprar leite', NULL, 1, NULL);

-- Associar tags
INSERT INTO todo_tags (todo_id, tag_id) VALUES
(1, 1), -- Apresentação é urgente
(1, 2); -- Apresentação é importante
