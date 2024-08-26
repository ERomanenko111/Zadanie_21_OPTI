package com.example.location.controller;

import com.example.location.model.Location;
import com.example.location.model.Weather;
import com.example.location.repository.LocationRepository;
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
            newLocation.setId(optionalLocation.get().getId());  // сохраняем ID существующего объекта
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
    public ResponseEntity<Weather> getWeatherForLocation(@RequestParam("name") String name) {
        Optional<Location> optionalLocation = repository.findByName(name);
        if (optionalLocation.isPresent()) {
            Location location = optionalLocation.get();

            // Логирование для отладки
            System.out.println("Fetching weather for location: " + location.getName()
                    + " with lat: " + location.getLatitude()
                    + " and lon: " + location.getLongitude());

            String url = String.format("http://localhost:8082/weather?lat=54.1838&lon=45.1749", location.getLatitude(), location.getLongitude());

            try {
                Weather weather = restTemplate.getForObject(url, Weather.class);
                if (weather == null) {
                    // Если погода не найдена
                    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
                }
                return new ResponseEntity<>(weather, HttpStatus.OK);
            } catch (Exception e) {
                e.printStackTrace(); // Логируем стек ошибки
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }

        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}