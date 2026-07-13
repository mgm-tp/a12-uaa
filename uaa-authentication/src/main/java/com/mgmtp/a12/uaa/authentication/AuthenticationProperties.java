/*
 * SPDX-License-Identifier: EUPL-1.2 OR LicenseRef-commercial
 *
 * Copyright (c) 2012-2026 mgm technology partners GmbH
 *
 * Dual License
 * ------------
 * This source file is part of the mgm A12 Platform and available under
 * a choice of two different licenses:
 *
 * 1. Open-Source License – EUPL v1.2
 *    You may redistribute and/or modify this file under the terms of the
 *    European Union Public License, version 1.2 - see https://eupl.eu/.
 *
 * 2. Commercial License
 *    Alternatively, you may obtain a commercial license from
 *    mgm technology partners GmbH, that permits use of this software
 *    under different terms (including support and maintenance services).
 *
 *    Please contact a12-license@mgm-tp.com for more information.
 *
 * You must select and comply with exactly one of the above license options.
 *
 * Warranty Disclaimer (applies to either option)
 * ----------------------------------------------
 * THIS SOFTWARE IS PROVIDED “AS IS” AND WITHOUT WARRANTY OF ANY KIND,
 * WHETHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NON-INFRINGEMENT, EXCEPT WHERE SUCH DISCLAIMERS ARE HELD TO BE
 * LEGALLY INVALID. SEE THE RESPECTIVE LICENSE TEXT FOR DETAILS.
 */
package com.mgmtp.a12.uaa.authentication;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;

import com.mgmtp.a12.uaa.authentication.config.client.properties.ClientProperties;
import com.mgmtp.a12.uaa.authentication.config.common.EnabledProperty;
import com.mgmtp.a12.uaa.authentication.config.common.UrlProperty;
import com.mgmtp.a12.uaa.authentication.internal.validation.EmptySecretValue;
import com.mgmtp.a12.uaa.authentication.internal.validation.NotNullForAuthenticationType;
import com.mgmtp.a12.uaa.authentication.internal.validation.ProperSecretValue;
import com.mgmtp.a12.uaa.authentication.security.login.internal.UAAAuthenticationSuccessHandler;

@Validated
public class AuthenticationProperties {

	@NotNull
	private Collection<AuthenticationType> types = Arrays.asList(AuthenticationType.LOCAL);
	private Urls unsecured = new Urls();
	@NotNullForAuthenticationType(authenticationTypes = AuthenticationType.ACTIVE_DIRECTORY_LDAP,
		message = "Missing configuration for Active directory LDAP")
	private LdapProperties ldap = new LdapProperties();
	@NotNullForAuthenticationType(authenticationTypes = AuthenticationType.SAML,
		message = "Missing configuration for SAML")
	@Valid
	private SamlProperties saml = new SamlProperties();
	@NotNullForAuthenticationType(authenticationTypes = { AuthenticationType.SAML, AuthenticationType.LOCAL, AuthenticationType.ACTIVE_DIRECTORY_LDAP,
		AuthenticationType.UAA_ACCESS_TOKEN },
		message = "Missing configuration for JWT token")
	@Valid
	private JwtProperties jwt = new JwtProperties();
	@NotNullForAuthenticationType(authenticationTypes = AuthenticationType.OAUTH2, message = "Provide OAUTH2 configuration")
	private Oauth2Properties oauth2 = new Oauth2Properties();
	private ClientProperties clientSelfconfiguration = new ClientProperties();
	private String contextPath = "/";
	private RedirectHolder logout = new RedirectHolder();
	private RedirectHolder login = new RedirectHolder();
	private List<String> securedContexts = new LinkedList<>();
	@NotNull
	private Cors cors = new Cors();
	@NotNullForAuthenticationType(authenticationTypes = AuthenticationType.ANONYMOUS, message = "Missing configuration for Anonymous")
	private Anonymous anonymous = new Anonymous();
	private EnabledProperty cachedTokenStorage = new EnabledProperty(false);
	private int unauthorizedCode = HttpStatus.UNAUTHORIZED.value();
	private HeaderConfiguration headerConfiguration = new HeaderConfiguration();
	private List<Resource> apiKeyAuthorityResources;
	private List<String> apiKeyWhiteListAccessUrlPatterns = new ArrayList<>();
	private List<String> certificateWhiteListAccessUrlPatterns = new ArrayList<>();
	private Cookie cookie = new Cookie();

	public List<String> getCertificateWhiteListAccessUrlPatterns() {
		return certificateWhiteListAccessUrlPatterns;
	}

	public void setCertificateWhiteListAccessUrlPatterns(List<String> certificateWhiteListAccessUrlPatterns) {
		this.certificateWhiteListAccessUrlPatterns = certificateWhiteListAccessUrlPatterns;
	}

	public List<String> getApiKeyWhiteListAccessUrlPatterns() {
		return apiKeyWhiteListAccessUrlPatterns;
	}

	public void setApiKeyWhiteListAccessUrlPatterns(List<String> apiKeyWhiteListAccessUrlPatterns) {
		this.apiKeyWhiteListAccessUrlPatterns = apiKeyWhiteListAccessUrlPatterns;
	}

	public List<Resource> getApiKeyAuthorityResources() {
		return apiKeyAuthorityResources;
	}

	public void setApiKeyAuthorityResources(List<Resource> apiKeyAuthorityResources) {
		this.apiKeyAuthorityResources = apiKeyAuthorityResources;
	}

	public HeaderConfiguration getHeaderConfiguration() {
		return headerConfiguration;
	}

	public void setHeaderConfiguration(HeaderConfiguration headerConfiguration) {
		this.headerConfiguration = headerConfiguration;
	}

	public Cors getCors() {
		return cors;
	}

	public void setCors(Cors cors) {
		this.cors = cors;
	}

	public List<String> getSecuredContexts() {
		return securedContexts;
	}

	public void setSecuredContexts(List<String> securedContexts) {
		this.securedContexts = securedContexts;
	}

	public String getContextPath() {
		return StringUtils.removeEnd(contextPath, "/");
	}

	public void setContextPath(String contextPath) {
		this.contextPath = contextPath;
	}

	public Urls getUnsecured() {
		return unsecured;
	}

	public void setUnsecured(Urls unsecured) {
		this.unsecured = unsecured;
	}

	public JwtProperties getJwt() {
		return jwt;
	}

	public void setJwt(JwtProperties jwt) {
		this.jwt = jwt;
	}

	public Collection<AuthenticationType> getTypes() {
		return types;
	}

	public void setTypes(Collection<AuthenticationType> types) {
		this.types = types;
	}

	public LdapProperties getLdap() {
		return ldap;
	}

	public void setLdap(LdapProperties ldap) {
		this.ldap = ldap;
	}

	public SamlProperties getSaml() {
		return saml;
	}

	public void setSaml(SamlProperties saml) {
		this.saml = saml;
	}

	public Oauth2Properties getOauth2() {
		return oauth2;
	}

	public void setOauth2(Oauth2Properties oauth2) {
		this.oauth2 = oauth2;
	}

	public Anonymous getAnonymous() {
		return anonymous;
	}

	public void setAnonymous(Anonymous anonymous) {
		this.anonymous = anonymous;
	}

	public ClientProperties getClientSelfconfiguration() {
		return clientSelfconfiguration;
	}

	public void setClientSelfconfiguration(ClientProperties clientSelfconfiguration) {
		this.clientSelfconfiguration = clientSelfconfiguration;
	}

	public EnabledProperty getCachedTokenStorage() {
		return cachedTokenStorage;
	}

	public void setCachedTokenStorage(EnabledProperty cachedTokenStorage) {
		this.cachedTokenStorage = cachedTokenStorage;
	}

	public int getUnauthorizedCode() {
		return unauthorizedCode;
	}

	public void setUnauthorizedCode(int unauthorizedCode) {
		this.unauthorizedCode = unauthorizedCode;
	}

	public RedirectHolder getLogout() {
		return logout;
	}

	public void setLogout(RedirectHolder logout) {
		this.logout = logout;
	}

	public RedirectHolder getLogin() {
		return login;
	}

	public void setLogin(RedirectHolder login) {
		this.login = login;
	}

	public Cookie getCookie() {
		return cookie;
	}

	public void setCookie(Cookie cookie) {
		this.cookie = cookie;
	}

	public static class SamlProperties {

		@NotNullForAuthenticationType(authenticationTypes = AuthenticationType.SAML, message = "Provide login URL for SAML")
		private UrlProperty login;
		private UrlProperty signingAlgorithm = new UrlProperty();
		private EnabledProperty forceAuth = new EnabledProperty(false);
		private List<String> uniqueElementXpaths = Arrays.asList("//Response/Status");
		private List<String> sameValueElementXpaths = Arrays.asList("substring-after(/Response/Signature/SignedInfo/Reference/@URI, \"#\")##/Response/@ID");
		private EnabledProperty idpLogout = new EnabledProperty(false);
		private Integer authorizationCodeExpirationSeconds = 5;
		private int assertionLifetimeMinutes = 5;

		public List<String> getUniqueElementXpaths() {
			return uniqueElementXpaths;
		}

		public void setUniqueElementXpaths(List<String> uniqueElementXpaths) {
			this.uniqueElementXpaths = uniqueElementXpaths;
		}

		public List<String> getSameValueElementXpaths() {
			return sameValueElementXpaths;
		}

		public void setSameValueElementXpaths(List<String> sameValueElementXpaths) {
			this.sameValueElementXpaths = sameValueElementXpaths;
		}

		public EnabledProperty getForceAuth() {
			return forceAuth;
		}

		public void setForceAuth(EnabledProperty forceAuth) {
			this.forceAuth = forceAuth;
		}

		public UrlProperty getSigningAlgorithm() {
			return signingAlgorithm;
		}

		public void setSigningAlgorithm(UrlProperty signingAlgorithm) {
			this.signingAlgorithm = signingAlgorithm;
		}

		public UrlProperty getLogin() {
			return login;
		}

		public void setLogin(UrlProperty login) {
			this.login = login;
		}

		public EnabledProperty getIdpLogout() {
			return idpLogout;
		}

		public void setIdpLogout(EnabledProperty idpLogout) {
			this.idpLogout = idpLogout;
		}

		public Integer getAuthorizationCodeExpirationSeconds() {
			return authorizationCodeExpirationSeconds;
		}

		public void setAuthorizationCodeExpirationSeconds(Integer authorizationCodeExpirationSeconds) {
			this.authorizationCodeExpirationSeconds = authorizationCodeExpirationSeconds;
		}

		public int getAssertionLifetimeMinutes() {
			return assertionLifetimeMinutes;
		}

		public void setAssertionLifetimeMinutes(int assertionLifetimeMinutes) {
			this.assertionLifetimeMinutes = assertionLifetimeMinutes;
		}

	}

	public static class LdapProperties {

		private String domain = "sambaad.local";
		private String url = "ldap://localhost:389/";
		private String rootDn = "DC=sambaad,DC=local";
		private String searchFilter = "(&(objectClass=user)(sAMAccountName={1}))";

		public String getDomain() {
			return domain;
		}

		public void setDomain(String domain) {
			this.domain = domain;
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public String getRootDn() {
			return rootDn;
		}

		public void setRootDn(String rootDn) {
			this.rootDn = rootDn;
		}

		public String getSearchFilter() {
			return searchFilter;
		}

		public void setSearchFilter(String searchFilter) {
			this.searchFilter = searchFilter;
		}
	}

	public static class JwtProperties {
		private String headerName = "Authorization";
		@EmptySecretValue(message = "Property is empty")
		@ProperSecretValue(message = "Property decoded value [{decodedSecret}] is in wrong length. Must be 32 bytes long")
		private String secret;
		private Integer expirationSeconds = 1800;
		private Integer tokenRenewThresholdInSeconds = 15;
		private EnabledProperty storeUserInToken = new EnabledProperty(false);
		private EnabledProperty compressUser = new EnabledProperty(false);
		private Integer userLifetimeSeconds;
		private Resource privateKeyLocation;
		private Resource publicKeyLocation;
		private EnabledProperty tokenEndpoints = new EnabledProperty(true);
		private EnabledProperty tokenSignature = new EnabledProperty(true);

		public EnabledProperty getStoreUserInToken() {
			return storeUserInToken;
		}

		public void setStoreUserInToken(EnabledProperty storeUserInToken) {
			this.storeUserInToken = storeUserInToken;
		}

		public EnabledProperty getCompressUser() {
			return compressUser;
		}

		public void setCompressUser(EnabledProperty compressUser) {
			this.compressUser = compressUser;
		}

		public String getHeaderName() {
			return headerName;
		}

		public void setHeaderName(String header) {
			this.headerName = header;
		}

		public String getSecret() {
			return secret;
		}

		public void setSecret(String secret) {
			this.secret = secret;
		}

		public Integer getExpirationSeconds() {
			return expirationSeconds;
		}

		public void setExpirationSeconds(Integer expiration) {
			this.expirationSeconds = expiration;
		}

		public Integer getTokenRenewThresholdInSeconds() {
			return tokenRenewThresholdInSeconds;
		}

		public void setTokenRenewThresholdInSeconds(Integer tokenRenewThresholdInSeconds) {
			this.tokenRenewThresholdInSeconds = tokenRenewThresholdInSeconds;
		}

		public Integer getUserLifetimeSeconds() {
			return userLifetimeSeconds;
		}

		public void setUserLifetimeSeconds(Integer maximumUserLifetime) {
			this.userLifetimeSeconds = maximumUserLifetime;
		}

		public void setPrivateKeyLocation(Resource privateKeyLocation) {
			this.privateKeyLocation = privateKeyLocation;
		}

		public void setPublicKeyLocation(Resource publicKeyLocation) {
			this.publicKeyLocation = publicKeyLocation;
		}

		public Resource getPrivateKeyLocation() {
			return privateKeyLocation;
		}

		public Resource getPublicKeyLocation() {
			return publicKeyLocation;
		}

		public EnabledProperty getTokenEndpoints() {
			return tokenEndpoints;
		}

		public void setTokenEndpoints(EnabledProperty tokenEndpoints) {
			this.tokenEndpoints = tokenEndpoints;
		}

		public EnabledProperty getTokenSignature() {
			return tokenSignature;
		}

		public void setTokenSignature(EnabledProperty tokenSignature) {
			this.tokenSignature = tokenSignature;
		}
	}

	public static class Cors {
		private List<String> allowedOrigins = Arrays.asList("http://localhost:3000");
		private List<String> allowedMethods = Arrays.asList("GET", "POST", "OPTIONS", "DELETE", "PUT", "PATCH");
		private List<String> allowedHeaders = Arrays.asList("X-Requested-With", "Origin", "Content-Type", "Accept", "Authorization");
		private List<String> exposedHeaders = Arrays.asList(UAAAuthenticationSuccessHandler.TOKEN_KEY,
			UAAAuthenticationSuccessHandler.TOKEN_RENEW_IN_SECONDS, UAAAuthenticationSuccessHandler.TOKEN_EXPIRATION_IN_SECONDS);
		private Boolean allowCredentials;
		private boolean enabled = true;

		public boolean isEnabled() {
			return enabled;
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}

		public List<String> getAllowedOrigins() {
			return allowedOrigins;
		}

		public void setAllowedOrigins(List<String> origins) {
			this.allowedOrigins = origins;
		}

		public List<String> getAllowedMethods() {
			return allowedMethods;
		}

		public void setAllowedMethods(List<String> allowedMethods) {
			this.allowedMethods = allowedMethods;
		}

		public List<String> getAllowedHeaders() {
			return allowedHeaders;
		}

		public void setAllowedHeaders(List<String> allowedHeaders) {
			this.allowedHeaders = allowedHeaders;
		}

		public List<String> getExposedHeaders() {
			return exposedHeaders;
		}

		public void setExposedHeaders(List<String> exposedHeaders) {
			this.exposedHeaders = exposedHeaders;
		}

		public Boolean getAllowCredentials() {
			return allowCredentials;
		}

		public void setAllowCredentials(Boolean allowCredentials) {
			this.allowCredentials = allowCredentials;
		}

		@Override
		public String toString() {
			return "Cors{" +
				"allowedOrigins=" + allowedOrigins +
				", allowedMethods=" + allowedMethods +
				", allowedHeaders=" + allowedHeaders +
				", exposedHeaders=" + exposedHeaders +
				", allowCredentials=" + allowCredentials +
				", enabled=" + enabled +
				'}';
		}
	}

	public static class Anonymous {
		private String username = "anonymousUser";
		private Urls access = new Urls();
		private List<String> roles = Arrays.asList("ROLE_ANONYMOUS");

		public Urls getAccess() {
			return access;
		}

		public void setAccess(Urls access) {
			this.access = access;
		}

		public List<String> getRoles() {
			return roles;
		}

		public void setRoles(List<String> roles) {
			this.roles = roles;
		}

		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
		}

		@Override
		public String toString() {
			return "Anonymous{" +
				"username='" + username + '\'' +
				", access=" + access +
				", roles=" + roles +
				'}';
		}
	}

	public static class Oauth2Properties {
		private EnabledProperty idpLogout = new EnabledProperty(true);
		private UrlProperty login = new UrlProperty();
		private UrlProperty postLogout = new UrlProperty("http://localhost:8080/");

		private final ResourceServer resourceserver = new ResourceServer();

		public ResourceServer getResourceserver() {
			return resourceserver;
		}

		public EnabledProperty getIdpLogout() {
			return idpLogout;
		}

		public void setIdpLogout(EnabledProperty idpLogout) {
			this.idpLogout = idpLogout;
		}

		public UrlProperty getLogin() {
			return login;
		}

		public void setLogin(UrlProperty login) {
			this.login = login;
		}

		public UrlProperty getPostLogout() {
			return postLogout;
		}

		public void setPostLogout(UrlProperty postLogout) {
			this.postLogout = postLogout;
		}

		public static class ResourceServer {
			private final List<Tenant> tenants = new ArrayList<>();

			public List<Tenant> getTenants() {
				return tenants;
			}

			public static class Tenant {
				private final Jwt jwt = new Jwt();
				private final OpaqueToken opaquetoken = new OpaqueToken();

				public Jwt getJwt() {
					return jwt;
				}

				public OpaqueToken getOpaquetoken() {
					return opaquetoken;
				}

				public static class Jwt {
					private String issuerUri;
					private String jwkSetUri;
					private List<String> jwsAlgorithms = Arrays.asList("RS256");
					private Resource publicKeyLocation;
					private List<String> audiences;

					public String getIssuerUri() {
						return issuerUri;
					}

					public void setIssuerUri(String issuerUri) {
						this.issuerUri = issuerUri;
					}

					public String getJwkSetUri() {
						return jwkSetUri;
					}

					public void setJwkSetUri(String jwkSetUri) {
						this.jwkSetUri = jwkSetUri;
					}

					public List<String> getJwsAlgorithms() {
						return jwsAlgorithms;
					}

					public void setJwsAlgorithms(List<String> jwsAlgorithms) {
						this.jwsAlgorithms = jwsAlgorithms;
					}

					public Resource getPublicKeyLocation() {
						return publicKeyLocation;
					}

					public void setPublicKeyLocation(Resource publicKeyLocation) {
						this.publicKeyLocation = publicKeyLocation;
					}

					public List<String> getAudiences() {
						return audiences;
					}

					public void setAudiences(List<String> audiences) {
						this.audiences = audiences;
					}
				}

				public static class OpaqueToken {
					private String clientId;
					private String clientSecret;
					private String introspectionUri;

					public String getClientId() {
						return this.clientId;
					}

					public void setClientId(String clientId) {
						this.clientId = clientId;
					}

					public String getClientSecret() {
						return this.clientSecret;
					}

					public void setClientSecret(String clientSecret) {
						this.clientSecret = clientSecret;
					}

					public String getIntrospectionUri() {
						return this.introspectionUri;
					}

					public void setIntrospectionUri(String introspectionUri) {
						this.introspectionUri = introspectionUri;
					}
				}
			}
		}
	}

	public static class HeaderConfiguration {
		private String xContentType = "nosniff";
		private String xFrameOptions = "DENY";
		private String[] contentSecurityPolicySources;

		public String getxContentType() {
			return xContentType;
		}

		public void setxContentType(String xContentType) {
			this.xContentType = xContentType;
		}

		public String getxFrameOptions() {
			return xFrameOptions;
		}

		public void setxFrameOptions(String xFrameOptions) {
			this.xFrameOptions = xFrameOptions;
		}

		public String[] getContentSecurityPolicySources() {
			return contentSecurityPolicySources;
		}

		public void setContentSecurityPolicySources(String[] contentSecurityPolicySources) {
			this.contentSecurityPolicySources = contentSecurityPolicySources;
		}
	}

	public static class Urls {
		private List<String> urls = Collections.emptyList();

		public List<String> getUrls() {
			return urls;
		}

		public void setUrls(List<String> urls) {
			this.urls = urls;
		}
	}

	public static class RedirectHolder {

		private Redirect redirect = new Redirect();

		public Redirect getRedirect() {
			return redirect;
		}

		public void setRedirect(Redirect redirect) {
			this.redirect = redirect;
		}

	}

	public static class Redirect {

		private String urlPattern;
		private UrlProperty success = new UrlProperty();
		private UrlProperty failure = new UrlProperty();

		public String getUrlPattern() {
			return urlPattern;
		}

		public void setUrlPattern(String urlPattern) {
			this.urlPattern = urlPattern;
		}

		public UrlProperty getSuccess() {
			return success;
		}

		public void setSuccess(UrlProperty success) {
			this.success = success;
		}

		public UrlProperty getFailure() {
			return failure;
		}

		public void setFailure(UrlProperty failure) {
			this.failure = failure;
		}

	}

	public static class Cookie {

		private EnabledProperty httpOnly = new EnabledProperty(false);
		private SameSite sameSite = SameSite.UNSET;
		private EnabledProperty secured = new EnabledProperty(false);
		private Integer lifetimeSeconds = 180;

		public EnabledProperty getHttpOnly() {
			return httpOnly;
		}

		public void setHttpOnly(EnabledProperty httpOnly) {
			this.httpOnly = httpOnly;
		}

		public SameSite getSameSite() {
			return sameSite;
		}

		public void setSameSite(SameSite sameSite) {
			this.sameSite = sameSite;
		}

		public EnabledProperty getSecured() {
			return secured;
		}

		public void setSecured(EnabledProperty secured) {
			this.secured = secured;
		}

		public Integer getLifetimeSeconds() {
			return lifetimeSeconds;
		}

		public void setLifetimeSeconds(Integer lifetimeSeconds) {
			this.lifetimeSeconds = lifetimeSeconds;
		}

		public static enum SameSite {
			NONE,
			UNSET,
			LAX,
			STRICT
		}
	}

}
