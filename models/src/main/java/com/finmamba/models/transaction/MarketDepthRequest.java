package com.finmamba.models.transaction;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MarketDepthRequest implements Serializable{

	Long buyCurrencyId;
	Long sellCurrencyId;
}
