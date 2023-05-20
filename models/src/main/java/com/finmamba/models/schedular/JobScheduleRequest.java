package com.finmamba.models.schedular;

import java.time.ZoneId;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.springframework.lang.NonNull;

import lombok.Data;

@Data
public class JobScheduleRequest {
	@NonNull
    private int requestId;

    @NonNull
    private int negotiationId;

    @NonNull
    private int quoteValidityInSeconds;

    @NonNull
    private long intervalInMilliseconds;

    @NotNull
    private ZoneId timeZone;

}