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
      // 2. Salvar token no localStorage
      localStorage.setItem('authToken', token);
      console.log('Token salvo no localStorage');

      // Mostrar mensagem de sucesso
      successDiv.textContent = 'Login realizado com sucesso! Redirecionando...';
      successDiv.style.display = 'block';

      // 3. Fazer requisição para /templates/love-calendar com Authorization header
      const authHeader = `Bearer ${token}`;
      fetch('/templates/love-calendar', {
        method: 'GET',
        headers: {
          'Content-Type': 'text/html',
          'Authorization': authHeader
        }
      })
      .then(async (resp) => {
        console.log('Status da resposta:', resp.status, resp.statusText);
        console.log('response.ok:', resp.ok);
        
        if (resp.status === 401) {
          const errorBody = await resp.text();
          console.error('Erro 401 - Detalhes:', errorBody);
          throw new Error('Token inválido ou expirado: ' + errorBody);
        }
        
        if (!resp.ok) {
          const errorBody = await resp.text();
          console.error('Erro na resposta:', resp.status, errorBody);
          throw new Error(`Erro ${resp.status}: ${errorBody}`);
        }
        
        console.log('Redirecionando para /templates/love-calendar');
        window.location.href = '/templates/love-calendar';
      })
      .catch((err) => {
        console.error('Erro no fetch:', err.message);
        errorDiv.textContent = '❌ ' + err.message;
        errorDiv.style.display = 'block';
        localStorage.removeItem('authToken');
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


