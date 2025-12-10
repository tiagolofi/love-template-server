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
      console.log('Token salvo');

      // 3. Fazer fetch para /templates/love-calendar com Authorization header
      const authHeader = `Bearer ${token}`;
      const response = await fetch('/templates/love-calendar', {
        method: 'GET',
        headers: {
          'Authorization': authHeader
        }
      });

      console.log('Resposta status:', response.status);

      if (!response.ok) {
        const errorBody = await response.text();
        throw new Error(`Erro ${response.status}: ${errorBody}`);
      }

      // 4. Carregar o HTML da resposta e injetar na página
      // const html = await response.text();

      document.cookie = `Authorization=${token}; path=/; SameSite=Lax`;
      
      // Mostrar mensagem de sucesso
      successDiv.textContent = 'Login realizado com sucesso!';
      successDiv.style.display = 'block';

      window.location.href = '/templates/love-calendar';
      window.Headers.set('Authorization', token);

    //   // Aguardar um pouco e depois substituir o conteúdo
    //   setTimeout(() => {
    //     // Atualizar URL sem redirecionar
    //     window.history.pushState({}, '', '/templates/love-calendar');
        
    //     document.open();
    //     document.write(html);
    //     document.close();
    //   }, 500);
    } else {
      throw new Error('Token inválido recebido do servidor');
    }
  } catch (error) {
    console.error('Erro:', error);
    // errorDiv.textContent = '❌ ' + (error.message || 'Usuário ou senha incorretos');
    // errorDiv.style.display = 'block';
  }
});


