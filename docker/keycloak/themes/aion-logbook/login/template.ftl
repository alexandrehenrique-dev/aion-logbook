<#macro registrationLayout bodyClass="" displayInfo=false displayMessage=true displayRequiredFields=false>
<!DOCTYPE html>
<html class="aion-html" <#if realm.internationalizationEnabled> lang="${locale.currentLanguageTag}" dir="${(locale.rtl)?then('rtl','ltr')}"</#if>>
<head>
  <meta charset="utf-8">
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1.0, viewport-fit=cover"/>
  <meta name="robots" content="noindex, nofollow">

  <#if properties.meta?has_content>
    <#list properties.meta?split(' ') as meta>
      <meta name="${meta?split('==')[0]}" content="${meta?split('==')[1]}"/>
    </#list>
  </#if>

  <title>${msg("loginTitle",(realm.displayName!''))}</title>
  <link rel="icon" href="${url.resourcesPath}/img/aion-symbol.svg"/>

  <#if properties.stylesCommon?has_content>
    <#list properties.stylesCommon?split(' ') as style>
      <link href="${url.resourcesCommonPath}/${style}" rel="stylesheet"/>
    </#list>
  </#if>
  <#if properties.styles?has_content>
    <#list properties.styles?split(' ') as style>
      <link href="${url.resourcesPath}/${style}" rel="stylesheet"/>
    </#list>
  </#if>
  <#if properties.scripts?has_content>
    <#list properties.scripts?split(' ') as script>
      <script src="${url.resourcesPath}/${script}" type="text/javascript"></script>
    </#list>
  </#if>

  <script type="importmap">
    {"imports":{"rfc4648":"${url.resourcesCommonPath}/vendor/rfc4648/rfc4648.js"}}
  </script>
  <script src="${url.resourcesPath}/js/menu-button-links.js" type="module"></script>
  <#if scripts??>
    <#list scripts as script>
      <script src="${script}" type="text/javascript"></script>
    </#list>
  </#if>
  <script type="module">
    import { startSessionPolling } from "${url.resourcesPath}/js/authChecker.js";
    startSessionPolling("${url.ssoLoginInOtherTabsUrl?no_esc}");
  </script>
</head>

<body class="aion-body">
<div class="aion-page">

  <!-- ════════════════════════════════════════
       HERO — seção esquerda (identidade)
  ════════════════════════════════════════ -->
  <aside class="aion-hero" aria-hidden="true">
    <div class="aion-hero-bg">
      <div class="aion-hero-glow aion-hero-glow-1"></div>
      <div class="aion-hero-glow aion-hero-glow-2"></div>
      <div class="aion-hero-grid"></div>
    </div>
    <div class="aion-hero-inner">
      <div class="aion-hero-symbol-wrap">
        <img src="${url.resourcesPath}/img/aion-compass.svg" alt="" class="aion-hero-symbol" aria-hidden="true"/>
      </div>
      <div class="aion-hero-copy">
        <p class="aion-hero-eyebrow">Sistema de direção pessoal</p>
        <h1 class="aion-hero-title">
          <span class="aion-hero-title-aion">Aion</span><span class="aion-hero-title-logbook">Logbook</span>
        </h1>
        <p class="aion-hero-subtitle">Direção antes de velocidade.</p>
        <p class="aion-hero-body">
          Um espaço para transformar intenção em direção,<br>
          direção em execução e execução em memória.
        </p>
      </div>
    </div>
  </aside>

  <!-- ════════════════════════════════════════
       LOGIN — seção direita (formulário)
  ════════════════════════════════════════ -->
  <main class="aion-main" role="main">
    <div class="aion-card-wrap">

      <!-- Logo mobile-only (visível apenas em telas pequenas) -->
      <div class="aion-mobile-brand">
        <img src="${url.resourcesPath}/img/aion-logo.svg" alt="Aion Logbook" class="aion-mobile-logo"/>
      </div>

      <div class="aion-card">

        <!-- Locale switcher -->
        <#if realm.internationalizationEnabled && locale.supported?size gt 1>
          <div class="aion-locale" id="kc-locale">
            <div id="kc-locale-wrapper" class="${properties.kcLocaleWrapperClass!}">
              <div id="kc-locale-dropdown" class="menu-button-links ${properties.kcLocaleDropDownClass!}">
                <button tabindex="1" id="kc-current-locale-link"
                        aria-label="${msg('languages')}" aria-haspopup="true"
                        aria-expanded="false" aria-controls="language-switch1">
                  ${locale.current}
                </button>
                <ul role="menu" tabindex="-1" aria-labelledby="kc-current-locale-link"
                    id="language-switch1" class="${properties.kcLocaleListClass!}">
                  <#assign li = 1>
                  <#list locale.supported as l>
                    <li class="${properties.kcLocaleListItemClass!}" role="none">
                      <a role="menuitem" id="language-${li}" class="${properties.kcLocaleItemClass!}" href="${l.url}">${l.label}</a>
                    </li>
                    <#assign li++>
                  </#list>
                </ul>
              </div>
            </div>
          </div>
        </#if>

        <!-- Username display (fluxos MFA) -->
        <#if auth?has_content && auth.showUsername() && !auth.showResetCredentials()>
          <#nested "show-username">
          <div id="kc-username" class="aion-username-display">
            <span id="kc-attempted-username">${auth.attemptedUsername}</span>
            <a id="reset-login" href="${url.loginRestartFlowUrl}"
               aria-label="${msg('restartLoginTooltip')}" class="aion-link-muted">
              <i class="${properties.kcResetFlowIcon!}"></i>
            </a>
          </div>
        </#if>

        <!-- Alerta global (erros, avisos, sucesso) -->
        <#if displayMessage && message?has_content && (message.type != 'warning' || !isAppInitiatedAction??)>
          <div class="aion-alert aion-alert-${message.type}" role="alert">
            ${kcSanitize(message.summary)?no_esc}
          </div>
        </#if>

        <!-- Conteúdo do formulário (vem de login.ftl #nested "form") -->
        <#nested "form">

        <!-- Link "tentar de outra forma" (MFA) -->
        <#if auth?has_content && auth.showTryAnotherWayLink()>
          <form id="kc-select-try-another-way-form" action="${url.loginAction}" method="post">
            <input type="hidden" name="tryAnotherWay" value="on"/>
            <a href="#" class="aion-link-muted"
               onclick="document.getElementById('kc-select-try-another-way-form').requestSubmit();return false;">
              ${msg("doTryAnotherWay")}
            </a>
          </form>
        </#if>

        <!-- Provedores sociais -->
        <#nested "socialProviders">

        <!-- Registro / info extra -->
        <#if displayInfo>
          <div class="aion-info-section">
            <#nested "info">
          </div>
        </#if>

      </div>
    </div>
  </main>

</div>
</body>
</html>
</#macro>
