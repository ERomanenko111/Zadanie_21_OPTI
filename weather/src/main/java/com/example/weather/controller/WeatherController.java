package com.example.weather.controller;

import com.example.weather.model.Main;
import com.example.weather.model.Root;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class WeatherController {
    @Autowired
    private RestTemplate restTemplate;

    @Value("${appid}")
    String appId;

    @Value("${url.weather}")
    private String urlWeather;

    @Cacheable(value = "weatherCache", key = "#lat + '_' + #lon")
    @GetMapping("/weather")
    public Main getWeather(@RequestParam String lat, @RequestParam String lon) {
        String request = String.format("%s?lat=%s&lon=%s&units=metric&appid=%s", urlWeather, lat, lon, appId);
        return restTemplate.getForObject(request, Root.class).getMain();
    }

    @Scheduled(fixedDelay = 60000, initialDelay = 60000)
    @CacheEvict(value = "weatherCache", key = "#lat + '_' + #lon", cacheManager = "cacheManager", allEntries = true)
    public void evictCache() {}
}
