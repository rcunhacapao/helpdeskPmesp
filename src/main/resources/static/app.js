const loginForm = document.querySelector('#formulario-login');
const loginMessage = document.querySelector('#mensagem-login');
const loginPage = document.querySelector('#pagina-login');
const appPage = document.querySelector('#pagina-app');
const routes = document.querySelectorAll('[data-route]');
const views = document.querySelectorAll('[data-view]');
const navigationLinks = document.querySelectorAll('.nav-link, .mobile-nav-link');
const ticketForm = document.querySelector('#formulario-chamado');
const ticketMessage = document.querySelector('#mensagem-chamado');
const mikeForm = document.querySelector('#formulario-mike');
const mikeQuestion = document.querySelector('#pergunta-mike');
const mikeAnswer = document.querySelector('#resposta-mike');
const mikeReply = document.querySelector('#retorno-mike');

function showRoute(route) {
    views.forEach((view) => {
        const isCurrentView = view.id === route;
        view.hidden = !isCurrentView;
        view.classList.toggle('is-visible', isCurrentView);
    });

    navigationLinks.forEach((link) => {
        const isActive = link.dataset.route === route;
        link.classList.toggle('is-active', isActive);
        link.toggleAttribute('aria-current', isActive);
    });
}

loginForm?.addEventListener('submit', (event) => {
    event.preventDefault();

    loginMessage.classList.remove('is-error');
    loginMessage.textContent = '';
    loginPage.hidden = true;
    appPage.hidden = false;
    showRoute('visao-geral');
});

routes.forEach((route) => {
    route.addEventListener('click', () => showRoute(route.dataset.route));
});

ticketForm?.addEventListener('submit', (event) => {
    event.preventDefault();

    if (!ticketForm.checkValidity()) {
        ticketMessage.classList.add('is-error');
        ticketMessage.textContent = 'Preencha os campos obrigatórios para continuar na demonstração.';
        ticketForm.reportValidity();
        return;
    }

    ticketMessage.classList.remove('is-error');
    ticketMessage.textContent = 'Chamado simulado com sucesso. O envio real será conectado em uma próxima etapa.';
    ticketForm.reset();
});

mikeForm?.addEventListener('submit', (event) => {
    event.preventDefault();
    const message = mikeQuestion.value.trim();

    if (!message) return;

    mikeAnswer.querySelector('p').textContent = message;
    mikeAnswer.hidden = false;
    mikeReply.hidden = false;
    mikeQuestion.value = '';
});

document.querySelector('#sair')?.addEventListener('click', () => {
    appPage.hidden = true;
    loginPage.hidden = false;
    loginForm.reset();
    loginMessage.textContent = '';
});
