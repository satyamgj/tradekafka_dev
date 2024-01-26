package com.finmamba.models.ondc;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SellerCatalogue {
 Long userId;
 Long baseCcyId;
 Long quoteCcyId;
}
