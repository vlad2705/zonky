package com.rudenvla.zonky.scheduler;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class LoanScheduler {
  
  @Value("${loan.url}")
  private String url;
  
  private final int period = 300_000; // 5 minutes
  
  private String datePublished; // it has to filter new loans
  
  /**
   * Checks new Zonky's loans every 5 minutes and prints them
   */
  @Scheduled(fixedDelay = period)
  public void getLoans() {
    RestTemplate restTemplate = new RestTemplate();
    
    ResponseEntity<String> response = restTemplate.exchange(createUrl(), HttpMethod.GET, createHeader(), String.class);
    
    if (response.getStatusCode().is2xxSuccessful()) {
      printResult(response.getBody());
    }
  }
  
  /**
   * Creates URL
   *
   * @return {@link String} url
   */
  private String createUrl() {
    UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
    if (this.datePublished != null) {
      builder.queryParam("datePublished__gt", this.datePublished);
    }
    return builder.toUriString();
  }
  
  /**
   * Adds sort by datePublished desc
   *
   * @return {@link HttpEntity<String>} header
   */
  private HttpEntity<String> createHeader() {
    HttpHeaders headers = new HttpHeaders();
    headers.set("X-Order", "-datePublished");
    return new HttpEntity<>("parameters", headers);
  }
  
  /**
   * Prints last loans
   * @param result from Zonky
   */
  private void printResult(String result) {
    JSONArray jsonArray = new JSONArray(result);
    if (jsonArray.length() > 0) {
      System.out.println(result);
      setDatePublished(jsonArray.getJSONObject(0));
    }
  }
  
  /**
   * Rewrites last datePublished
   *
   * @param lastLoan from Zonky
   */
  private void setDatePublished(JSONObject lastLoan) {
    if (lastLoan.has("datePublished")) {
      this.datePublished = lastLoan.getString("datePublished").split("\\+")[0];
    }
  }
}
