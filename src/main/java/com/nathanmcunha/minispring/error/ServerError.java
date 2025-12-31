package com.nathanmcunha.minispring.error;

public record ServerError(Exception exception,int suggestedStatusCode) {
   
}
