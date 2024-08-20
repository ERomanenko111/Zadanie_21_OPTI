package com.example.location.controller;

import com.example.location.model.Location;
import com.example.location.repository.LocationRepository;
import com.example.location.model.Weather;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/location")
public class LocationController {
    @Autowired
    private LocationRepository repository;

    @Autowired
    private RestTemplate restTemplate;

    @GetMapping
    public ResponseEntity<List<Location>> getAllLocations() {
        List<Location> locations = (List<Location>) repository.findAll();
        return ResponseEntity.ok(locations);
    }

    @GetMapping(params = "name")
    public ResponseEntity<Location> getLocationByName(@RequestParam String name) {
        Optional<Location> location = repository.findByName(name);
        return location.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping
    public ResponseEntity<Location> save(@RequestBody Location location) {
        return ResponseEntity.status(HttpStatus.CREATED).body(repository.save(location));
    }

    @PutMapping(params = "name")
    public ResponseEntity<Location> updateLocation(@RequestParam String name, @RequestBody Location newLocation) {
        Optional<Location> optionalLocation = repository.findByName(name);
        if (optionalLocation.isPresent()) {
            newLocation.setId(optionalLocation.get().getId());
            return ResponseEntity.ok(repository.save(newLocation));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @DeleteMapping(params = "name")
    public ResponseEntity<Void> deleteLocation(@RequestParam String name) {
        Optional<Location> optionalLocation = repository.findByName(name);
        if (optionalLocation.isPresent()) {
            repository.delete(optionalLocation.get());
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/weather")
    public ResponseEntity<Weather> getWeatherForLocation(@RequestParam String name) {
        Optional<Location> optionalLocation = repository.findByName(name);
        if (optionalLocation.isPresent()) {
            Location location = optionalLocation.get();
            // Формируем URL для получения погоды
            String url = String.format("http://localhost:8082/?lat=%s&lon=%s", location.getLat(), location.getLon());
            // Получаем данные о погоде с внешнего API
            Weather weather = restTemplate.getForObject(url, Weather.class);
            return new ResponseEntity<>(weather, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
