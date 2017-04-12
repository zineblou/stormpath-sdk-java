package com.stormpath.spring.config;

import com.stormpath.sdk.account.Account;
import com.stormpath.sdk.api.ApiKey;
import com.stormpath.sdk.authc.AuthenticationResult;
import com.stormpath.sdk.authc.AuthenticationResultVisitor;
import com.stormpath.sdk.client.Client;
import com.stormpath.sdk.impl.application.okta.DefaultOktaAccessTokenResult;
import com.stormpath.sdk.oauth.AccessTokenResult;
import com.stormpath.sdk.oauth.TokenResponse;
import com.stormpath.sdk.provider.OktaProviderAccountResult;
import com.stormpath.sdk.servlet.http.Saver;
import com.stormpath.spring.security.token.JwtProviderAuthenticationToken;
import com.stormpath.spring.security.token.ProviderAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;

/**
 *
 */
public class OktaLoginSuccessHandler extends StormpathLoginSuccessHandler {

    private final Saver<AuthenticationResult> authenticationResultSaver;

    public OktaLoginSuccessHandler(Client client, Saver<AuthenticationResult> saver, String produces) {
        super(client, saver, produces);
        this.authenticationResultSaver = saver;
    }

    protected void saveAccount(HttpServletRequest request, HttpServletResponse response, final Authentication authentication) throws IOException, ServletException {

        if (authentication instanceof ProviderAuthenticationToken) {

            final Account account = getAccount(authentication);
            ProviderAuthenticationToken authenticationToken = (ProviderAuthenticationToken) authentication;
            final OktaProviderAccountResult providerAccountResult = (OktaProviderAccountResult) authenticationToken.getCredentials();

            AuthenticationResult authcResult = new DefaultOktaAccessTokenResult(providerAccountResult.getTokenResponse(), account);
            authenticationResultSaver.set(request, response, authcResult);
        }
        else if (authentication instanceof JwtProviderAuthenticationToken) {

            AccessTokenResult authenticationResult = (AccessTokenResult) ((JwtProviderAuthenticationToken) authentication).getAuthenticationResult();

            authenticationResultSaver.set(request, response, authenticationResult);
        }
        else {
            super.saveAccount(request, response, authentication);
        }
    }

    protected Account getAccount(Authentication authentication) {

        if (authentication instanceof ProviderAuthenticationToken) {
            return ((ProviderAuthenticationToken) authentication).getAccount();
        }
        else if (authentication instanceof AccessTokenResult) {
            return ((AccessTokenResult) authentication).getAccount();
        }
        else if (authentication instanceof JwtProviderAuthenticationToken) {
            return ((JwtProviderAuthenticationToken) authentication).getAuthenticationResult().getAccount();
        }

        return super.getAccount(authentication);
    }
}
