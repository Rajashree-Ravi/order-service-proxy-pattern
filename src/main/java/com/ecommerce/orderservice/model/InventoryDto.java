package com.ecommerce.orderservice.model;

import java.math.BigDecimal;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@ApiModel(description = "Class representing a inventory in e-commerce application.")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryDto {

	@ApiModelProperty(notes = "Unique identifier of the Inventory.", example = "1")
	private Long id;

	@ApiModelProperty(notes = "Inventory Code.", example = "456455", required = true)
	@NotBlank
	private String sku;

	@ApiModelProperty(notes = "Unique identifier of the Product.", example = "5", required = true)
	@NotNull
	private Long productId;

	@ApiModelProperty(notes = "Name of the Vendor.", example = "Kuber Industries", required = true)
	@NotBlank
	private String vendor;

	@ApiModelProperty(notes = "Quantity of the product available in inventory.", example = "150", required = true)
	@NotNull
	private Integer vendorInventory;

	@ApiModelProperty(notes = "Price determined by Vendor.", example = "450.00", required = true)
	@NotNull
	private BigDecimal vendorPrice;
}
