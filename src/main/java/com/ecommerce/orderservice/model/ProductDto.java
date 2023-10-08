package com.ecommerce.orderservice.model;

import java.math.BigDecimal;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@ApiModel(description = "Class representing a product in e-commerce application.")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {

	@ApiModelProperty(notes = "Unique identifier of the Product.", example = "1")
	private Long id;

	@ApiModelProperty(notes = "Product Code.", example = "21455", required = true)
	@NotBlank
	private String productCode;

	@ApiModelProperty(notes = "Name of the Product.", example = "bOAT Wave 2.0", required = true)
	@NotBlank
	private String productName;

	@ApiModelProperty(notes = "Price of the Product.", example = "2449.00", required = true)
	@NotNull
	private BigDecimal price;

	@ApiModelProperty(notes = "Description of the Product.", example = "Smart Watch")
	private String description;

	@ApiModelProperty(notes = "Product Category.", example = "Wearables", required = true)
	@NotBlank
	private String category;
}
