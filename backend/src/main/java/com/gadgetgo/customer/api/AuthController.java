package com.gadgetgo.customer.api;

import com.gadgetgo.customer.api.dto.CsrfTokenResponse;
import com.gadgetgo.customer.api.dto.CustomerResponse;
import com.gadgetgo.customer.api.dto.LoginRequest;
import com.gadgetgo.customer.api.dto.RegisterRequest;
import com.gadgetgo.customer.domain.CustomerRole;
import com.gadgetgo.customer.service.CustomerAccountService;
import com.gadgetgo.customer.service.InvalidCredentialsException;
import com.gadgetgo.security.CustomerPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final CustomerAccountService customerService;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;
    private final CsrfTokenRepository csrfTokenRepository;

    public AuthController(CustomerAccountService customerService,
                          AuthenticationManager authenticationManager,
                          SecurityContextRepository securityContextRepository,
                          CsrfTokenRepository csrfTokenRepository) {
        this.customerService = customerService;
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
        this.csrfTokenRepository = csrfTokenRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<CustomerResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(customerService.register(request));
    }

    @PostMapping("/login")
    public CustomerResponse login(@Valid @RequestBody LoginRequest request,
                                  HttpServletRequest servletRequest,
                                  HttpServletResponse servletResponse) {
        return loginForRole(request, CustomerRole.CUSTOMER, servletRequest, servletResponse);
    }

    @PostMapping("/login/technician")
    public CustomerResponse technicianLogin(@Valid @RequestBody LoginRequest request,
                                            HttpServletRequest servletRequest,
                                            HttpServletResponse servletResponse) {
        return loginForRole(request, CustomerRole.TECHNICIAN, servletRequest, servletResponse);
    }

    @PostMapping("/login/supplier")
    public CustomerResponse supplierLogin(@Valid @RequestBody LoginRequest request,
                                          HttpServletRequest servletRequest,
                                          HttpServletResponse servletResponse) {
        return loginForRole(request, CustomerRole.SUPPLIER, servletRequest, servletResponse);
    }

    private CustomerResponse loginForRole(LoginRequest request,
                                          CustomerRole expectedRole,
                                          HttpServletRequest servletRequest,
                                          HttpServletResponse servletResponse) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(request.email().trim(), request.password()));
        } catch (AuthenticationException exception) {
            throw new InvalidCredentialsException();
        }

        if (!authentication.getAuthorities().stream().anyMatch(authority -> authority.getAuthority().equals("ROLE_" + expectedRole.name()))) {
            throw new InvalidCredentialsException();
        }

        // Rotate even a pre-authentication session id before storing the authenticated context.
        servletRequest.getSession(true);
        servletRequest.changeSessionId();
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, servletRequest, servletResponse);
        csrfTokenRepository.saveToken(null, servletRequest, servletResponse);
        return customerService.getProfile(((CustomerPrincipal) authentication.getPrincipal()).id());
    }

    @GetMapping("/csrf")
    public CsrfTokenResponse csrf(CsrfToken csrfToken) {
        return new CsrfTokenResponse(csrfToken.getHeaderName(), csrfToken.getParameterName(), csrfToken.getToken());
    }

    @GetMapping("/me")
    public CustomerResponse currentCustomer(@org.springframework.security.core.annotation.AuthenticationPrincipal CustomerPrincipal principal) {
        return customerService.getProfile(principal.id());
    }
}
