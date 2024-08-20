package com.example.weather.controller;

import com.example.weather.model.Root;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class WeatherController {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${appid}")
    private String appId;

    @Value("${url.weather}")
    private String urlWeather;

    @Cacheable(value = "weatherCache", key = "#lat + '_' + #lon", unless = "#result == null")
    @GetMapping("/weather")
    public Root getWeather(@RequestParam("lat") double lat, @RequestParam("lon") double lon) {
        String url = urlWeather + "?lat=" + lat + "&lon=" + lon + "&units=metric&appid=" + appId;
        return restTemplate.getForObject(url, Root.class);
    }
}
