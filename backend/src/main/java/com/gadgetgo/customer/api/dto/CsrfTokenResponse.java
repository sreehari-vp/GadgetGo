package com.gadgetgo.customer.api.dto;

public record CsrfTokenResponse(String headerName, String parameterName, String token) {
}
