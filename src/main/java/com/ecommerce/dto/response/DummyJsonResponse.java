package com.ecommerce.dto.response;

import com.ecommerce.dto.request.DummyJsonRequest;
import lombok.Data;
import java.util.List;

@Data
public class DummyJsonResponse {
    private List<DummyJsonRequest> products;
}