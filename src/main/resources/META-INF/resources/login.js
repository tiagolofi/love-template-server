// Toggle de visibilidade da senha
const toggleButton = document.getElementById('togglePassword');
const passwordInput = document.getElementById('senha');

toggleButton.addEventListener('click', (e) => {
  e.preventDefault();
  const isPassword = passwordInput.type === 'password';
  passwordInput.type = isPassword ? 'text' : 'password';
  toggleButton.querySelector('.eye-icon').textContent = isPassword ? '🙈' : '👁️';
});

document.getElementById('loginForm').addEventListener('submit', async (e) => {
  e.preventDefault();

  const usuario = document.getElementById('usuario').value;
  const senha = document.getElementById('senha').value;

  const errorDiv = document.getElementById('errorMessage');
  const successDiv = document.getElementById('successMessage');

  // Limpar mensagens anteriores
  errorDiv.style.display = 'none';
  successDiv.style.display = 'none';
  errorDiv.textContent = '';
  successDiv.textContent = '';

  try {
    // 1. Fazer POST para gerar token
    const response = await fetch('/templates/login', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({"usuario": usuario, "senha": senha })
    });

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(errorText || 'Falha na autenticação');
    }

    const token = await response.text();

    if (token && token.trim() !== '') {
      // 2. Salvar token no localStorage (ARMAZENAR APENAS O JWT, sem o prefixo "Bearer ")
      // Isso evita possíveis duplicações "Bearer Bearer ..." e torna o uso mais previsível.
      localStorage.setItem('authToken', token);

      // Mostrar mensagem de sucesso
      successDiv.textContent = 'Login realizado com sucesso! Redirecionando...';
      successDiv.style.display = 'block';

      // 3. Fazer uma chamada para o endpoint protegido com o header Authorization
      // (Isso vai retornar 200 ou 401 — vamos inspecionar a resposta e mostrar erro se necessário.)
      const authHeader = `Bearer ${token}`;
      fetch('/templates/love-calendar', {
        method: 'GET',
        headers: {
          'Authorization': authHeader
        }
      })
      .then(async (resp) => {
        console.log('Resposta do fetch /templates/love-calendar:', resp.status, resp.statusText);
        if (!resp.ok) {
          const body = await resp.text().catch(() => '');
          throw new Error(`Servidor retornou ${resp.status}: ${body}`);
        }
        // Se o fetch foi OK, redirecionamos o usuário — OBS: a navegação direta NÃO incluirá headers.
        window.location.href = '/templates/love-calendar';
      })
      .catch((err) => {
        console.error('Erro ao validar token no fetch antes do redirecionamento:', err);
        // Mostrar mensagem de erro detalhada ao usuário
        errorDiv.textContent = '❌ Erro de autenticação: ' + (err.message || '401');
        errorDiv.style.display = 'block';
      });
    } else {
      throw new Error('Token inválido recebido do servidor');
    }
  } catch (error) {
    console.error('Erro:', error);
    errorDiv.textContent = '❌ ' + (error.message || 'Usuário ou senha incorretos');
    errorDiv.style.display = 'block';
  }
});


