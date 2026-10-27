package com.gadgetgo.gadget.api;

import com.gadgetgo.gadget.api.dto.GadgetRequest;
import com.gadgetgo.gadget.api.dto.GadgetResponse;
import com.gadgetgo.gadget.service.GadgetService;
import com.gadgetgo.security.CustomerPrincipal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/gadgets")
@Validated
public class GadgetController {

    private final GadgetService gadgetService;

    public GadgetController(GadgetService gadgetService) {
        this.gadgetService = gadgetService;
    }

    @GetMapping
    public List<GadgetResponse> list(@AuthenticationPrincipal CustomerPrincipal principal) {
        return gadgetService.list(principal.id());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GadgetResponse create(@AuthenticationPrincipal CustomerPrincipal principal,
                                 @Valid @RequestBody GadgetRequest request) {
        return gadgetService.create(principal.id(), request);
    }

    @GetMapping("/{id}")
    public GadgetResponse get(@AuthenticationPrincipal CustomerPrincipal principal,
                              @PathVariable @Positive Long id) {
        return gadgetService.get(principal.id(), id);
    }

    @PutMapping("/{id}")
    public GadgetResponse update(@AuthenticationPrincipal CustomerPrincipal principal,
                                 @PathVariable @Positive Long id,
                                 @Valid @RequestBody GadgetRequest request) {
        return gadgetService.update(principal.id(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal CustomerPrincipal principal,
                       @PathVariable @Positive Long id) {
        gadgetService.delete(principal.id(), id);
    }
}
