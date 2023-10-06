package com.ecommerce.orderservice.feign;

import com.ecommerce.sharedlibrary.exception.BadRequestException;
import com.ecommerce.sharedlibrary.exception.NotFoundException;

import feign.Response;
import feign.codec.ErrorDecoder;

public class CustomErrorDecoder implements ErrorDecoder {

	@Override
	public Exception decode(String methodKey, Response response) {
		switch (response.status()) {
		case 400:
			return new BadRequestException("Bad request received or the requested URL is unreachable.");
		case 404:
			return new NotFoundException("Requested resource not found.");
		default:
			return new Exception("Generic error");
		}
	}
}