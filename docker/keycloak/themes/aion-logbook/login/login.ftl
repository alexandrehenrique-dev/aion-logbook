<#import "template.ftl" as layout>
<@layout.registrationLayout
    displayMessage=!messagesPerField.existsError('username','password')
    displayInfo=realm.password && realm.registrationAllowed && !registrationDisabled??;
    section>

  <#-- ── Título da página (usado internamente pelo KC, não exibido) ── -->
  <#if section = "header">
    ${msg("loginAccountTitle")}

  <#-- ══════════════════════════════════════
       FORMULÁRIO PRINCIPAL
  ══════════════════════════════════════ -->
  <#elseif section = "form">

    <div class="aion-form-header">
      <h2 class="aion-form-title">Bem-vindo de volta</h2>
      <p class="aion-form-subtitle">Retorne para sua jornada.</p>
    </div>

    <#if realm.password>
      <form id="kc-form-login"
            onsubmit="login.disabled = true; return true;"
            action="${url.loginAction}"
            method="post"
            class="aion-form">

        <#-- Campo: usuário / email -->
        <#if !usernameHidden??>
          <div class="aion-field">
            <label for="username" class="aion-label">
              <#if !realm.loginWithEmailAllowed>
                ${msg("username")}
              <#elseif !realm.registrationEmailAsUsername>
                ${msg("usernameOrEmail")}
              <#else>
                ${msg("email")}
              </#if>
            </label>
            <input tabindex="2"
                   id="username"
                   class="aion-input"
                   name="username"
                   value="${(login.username!'')}"
                   type="text"
                   autofocus
                   autocomplete="username"
                   dir="ltr"
                   aria-invalid="<#if messagesPerField.existsError('username','password')>true</#if>"/>
            <#if messagesPerField.existsError('username','password')>
              <span class="aion-field-error" role="alert">
                ${kcSanitize(messagesPerField.getFirstError('username','password'))?no_esc}
              </span>
            </#if>
          </div>
        </#if>

        <#-- Campo: senha (componente visual unificado) -->
        <div class="aion-field">
          <label for="password" class="aion-label">${msg("password")}</label>
          <div class="aion-input-password" dir="ltr">
            <input tabindex="3"
                   id="password"
                   class="aion-input aion-input-pw"
                   name="password"
                   type="password"
                   autocomplete="current-password"
                   aria-invalid="<#if messagesPerField.existsError('username','password')>true</#if>"/>
            <button class="aion-pw-toggle"
                    type="button"
                    aria-label="${msg('showPassword')}"
                    aria-controls="password"
                    data-password-toggle
                    tabindex="4"
                    data-icon-show="aion-icon-eye-show"
                    data-icon-hide="aion-icon-eye-hide"
                    data-label-show="${msg('showPassword')}"
                    data-label-hide="${msg('hidePassword')}">
              <i class="aion-icon-eye-show" aria-hidden="true"></i>
            </button>
          </div>
          <#if usernameHidden?? && messagesPerField.existsError('username','password')>
            <span class="aion-field-error" role="alert">
              ${kcSanitize(messagesPerField.getFirstError('username','password'))?no_esc}
            </span>
          </#if>
        </div>

        <#-- Lembrar-me + Esqueci a senha -->
        <div class="aion-form-meta">
          <#if realm.rememberMe && !usernameHidden??>
            <label class="aion-remember">
              <input tabindex="5"
                     id="rememberMe"
                     name="rememberMe"
                     type="checkbox"
                     <#if login.rememberMe??>checked</#if>>
              <span>${msg("rememberMe")}</span>
            </label>
          </#if>
          <#if realm.resetPasswordAllowed>
            <a tabindex="6" href="${url.loginResetCredentialsUrl}" class="aion-forgot-link">
              ${msg("doForgotPassword")}
            </a>
          </#if>
        </div>

        <#-- Botão entrar -->
        <div class="aion-form-actions">
          <input type="hidden" id="id-hidden-input" name="credentialId"
                 <#if auth.selectedCredential?has_content>value="${auth.selectedCredential}"</#if>/>
          <input tabindex="7"
                 type="submit"
                 name="login"
                 id="kc-login"
                 class="aion-btn-primary"
                 value="${msg("doLogIn")}"/>
        </div>

      </form>
    </#if>
    <script type="module" src="${url.resourcesPath}/js/passwordVisibility.js"></script>

  <#-- ══════════════════════════════════════
       LINK DE REGISTRO
  ══════════════════════════════════════ -->
  <#elseif section = "info">
    <#if realm.password && realm.registrationAllowed && !registrationDisabled??>
      <div class="aion-register-row">
        <span>${msg("noAccount")}</span>
        <a tabindex="8" href="${url.registrationUrl}" class="aion-link">${msg("doRegister")}</a>
      </div>
    </#if>

  <#-- ══════════════════════════════════════
       PROVEDORES SOCIAIS
  ══════════════════════════════════════ -->
  <#elseif section = "socialProviders">
    <#if realm.password && social?? && social.providers?has_content>
      <div class="aion-social-section">
        <div class="aion-social-divider">
          <span class="aion-social-divider-text">ou continue com</span>
        </div>
        <div class="aion-social-list">
          <#list social.providers as p>
            <a id="social-${p.alias}"
               href="${p.loginUrl}"
               class="aion-social-btn"
               aria-label="Continuar com ${p.displayName!}">

              <span class="aion-social-icon" aria-hidden="true">

                <#if p.alias == "google">
                  <svg width="20" height="20" viewBox="0 0 24 24">
                    <path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" fill="#4285F4"/>
                    <path d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" fill="#34A853"/>
                    <path d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z" fill="#FBBC05"/>
                    <path d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" fill="#EA4335"/>
                  </svg>

                <#elseif p.alias == "microsoft">
                  <svg width="20" height="20" viewBox="0 0 21 21">
                    <path fill="#f25022" d="M0 0h10v10H0z"/>
                    <path fill="#00a4ef" d="M11 0h10v10H11z"/>
                    <path fill="#7fba00" d="M0 11h10v10H0z"/>
                    <path fill="#ffb900" d="M11 11h10v10H11z"/>
                  </svg>

                <#elseif p.alias == "apple">
                  <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
                    <path d="M18.71 19.5c-.83 1.24-1.71 2.45-3.05 2.47-1.34.03-1.77-.79-3.29-.79-1.53 0-2 .77-3.27.82-1.31.05-2.3-1.32-3.14-2.53C4.25 17 2.94 12.45 4.7 9.39c.87-1.52 2.43-2.48 4.12-2.51 1.28-.02 2.5.87 3.29.87.78 0 2.26-1.07 3.8-.91.65.03 2.47.26 3.64 1.98-.09.06-2.17 1.28-2.15 3.81.03 3.02 2.65 4.03 2.68 4.04-.03.07-.42 1.44-1.38 2.83M13 3.5c.73-.83 1.94-1.46 2.94-1.5.13 1.17-.34 2.35-1.04 3.19-.69.85-1.83 1.51-2.95 1.42-.15-1.15.41-2.35 1.05-3.11z"/>
                  </svg>

                <#else>
                  <span class="aion-social-letter">${p.displayName?substring(0,1)}</span>
                </#if>

              </span>
              <span class="aion-social-label">Continuar com ${p.displayName!}</span>
            </a>
          </#list>
        </div>
      </div>
    </#if>
  </#if>

</@layout.registrationLayout>
